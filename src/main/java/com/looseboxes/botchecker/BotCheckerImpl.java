/*
 * Copyright 2017 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.looseboxes.botchecker;

import com.looseboxes.botchecker.util.GetClientIpAddress;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Chinomso Bassey Ikwuagwu on Jan 4, 2019 12:50:40 PM
 */
public class BotCheckerImpl implements BotChecker, AutoCloseable, Serializable {

    private transient static final Logger LOG = Logger.getLogger(BotCheckerImpl.class.getName());
    
    public static final String BOT_ATTRIBUTE_NAME = BotCheckerImpl.class.getName() + ".botCategory";
        
    private final BotCache botCache;
    
    private final Collection<String> trapfiles;
    
    private final BiFunction<HttpServletRequest, String, String> getIp;

    public BotCheckerImpl() {
        this(BotCache.NO_OP, Collections.EMPTY_LIST);
    }
    
    public BotCheckerImpl(BotCache botCache, String... trapfiles) {
        this(botCache, Arrays.asList(trapfiles));
    }
    
    public BotCheckerImpl(BotCache botCache, Collection<String> trapfiles) {
        this.botCache = Objects.requireNonNull(botCache);
        this.trapfiles = Objects.requireNonNull(trapfiles);
        this.getIp = new GetClientIpAddress();
    }

    @Override
    public void close() throws Exception {
        if( ! this.botCache.isClosed()) {
            this.botCache.close();
        }
    }
    
    @Override
    public boolean isFromBot(HttpServletRequest request) {
        final BotChecker.BotCategory botCategory = this.check(request);
        return botCategory != null && botCategory.isBot();
    }
    
    @Override
    public BotCategory check(HttpServletRequest request) {
        
        final HttpSession session = request.getSession();
        
        final String remoteAddr = getIp.apply(request, null);
        final String remoteHost = request.getRemoteHost();
        final String userAgent = request.getHeader("User-Agent");
        final String requestURI = request.getRequestURI();
        
        BotCategory result = (BotCategory)session.getAttribute(BOT_ATTRIBUTE_NAME);
        
        boolean fromSession = result != null;
        
        boolean fromCache = false;
        
        if(result == null) {
        
            result = this.getCached(remoteAddr, remoteHost, userAgent);
            
            if(result != null) {
                
                fromCache = true;
                
            }else{    
                
                result =  this.check(remoteAddr, remoteHost, userAgent, requestURI);
            }
        }
        
        if(fromSession || fromCache) {
            
            final BotCategory update = result.isMalicious() ? result :
                    check(remoteAddr, remoteHost, userAgent, requestURI);
            
            if(update != result) {
                
                result = update;
                
                fromSession = false;
                fromCache = false;
            }
        }
        
        if(result != null && result != BotChecker.BotCategory.NONE) {
            
            if( ! fromSession) {
                session.setAttribute(BOT_ATTRIBUTE_NAME, result);
            }
            
            if( ! fromCache) {
                this.addToCache(remoteAddr, remoteHost, userAgent, result);
            }
        }
        
        return result;
    }
    
    @Override
    public void clear(HttpServletRequest request) {
        
        final HttpSession session = request.getSession(false);
        if(session != null) {
            session.removeAttribute(BOT_ATTRIBUTE_NAME);
        }
        
        final String remoteAddr = this.getIp.apply(request, null);
        final String remoteHost = request.getRemoteHost();
        final String userAgent = request.getHeader("User-Agent");
        final String requestURI = request.getRequestURI();
        
        this.clear(remoteAddr, remoteHost, userAgent, requestURI);
    }
    
    @Override
    public void clear(String remoteAddr, String remoteHost, String userAgent, String requestURI) {
        this.removeFromCache(remoteAddr, remoteHost, userAgent);
    }
    
    public void addToCache(String remoteAddr, String remoteHost, String userAgent, BotCategory value) {
        if(this.hasChars(remoteAddr)) {
            botCache.put(remoteAddr, value);
        }
        if(this.hasChars(userAgent)) {
            botCache.put(userAgent, value);
        }
        botCache.flush();
    }
    
    public void removeFromCache(String remoteAddr, String remoteHost, String userAgent) {
        if(this.hasChars(remoteAddr)) {
            botCache.remove(remoteAddr);
        }
        if(this.hasChars(userAgent)) {
            botCache.remove(userAgent);
        }
        botCache.flush();
    }

    public BotCategory getCached(String remoteAddr, String remoteHost, String userAgent) {
        
        BotCategory result = !this.hasChars(remoteAddr) ? null : botCache.getOrDefault(remoteAddr, null);

        if(result == null) {

            result = !this.hasChars(userAgent) ? null : botCache.getOrDefault(userAgent, null);
        }
        
        return result;
    }
    
    private boolean hasChars(String s) {
        return s != null && !s.isEmpty();
    }

    @Override
    public BotCategory check(String address, String host, String userAgent, String requestURI) {

        final BotCategory result;
        
        if(this.uriRequestsTrapFile(requestURI)) {
            result = BotCategory.URI_REQUESTS_BOT_TRAPFILE;
        }else if(this.userAgentContainsBotIdentifier(userAgent)) {
            result = BotCategory.USERAGENT_CONTAINS_BOT_IDENTIFIER;
        }else if(this.uriRequestsRobotsTextFile(requestURI)) {
            result = BotCategory.URI_REQUESTS_ROBOTS_TEXTFILE;
        }else if(this.userAgentNotSpecified(userAgent) &&
                ! this.isLocalHost(address) && ! isLocalHost(host)) {
            result = BotCategory.USERAGENT_NOT_SPECIFIED;
        }else{
            result = BotCategory.NONE;
        }
        
        if(LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "BotType: {0}, address: {1}, host: {2}, User-Agent: {3}, requestURI: {4}", 
                    new Object[]{result, address, host, userAgent, requestURI});        
        }
        
        return result;
    }
    
    public boolean uriRequestsTrapFile(String requestURI) {
        boolean result = false;
        for(String trapfile : trapfiles) {
            if(requestURI.contains(trapfile)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean userAgentContainsBotIdentifier(String userAgent) {
        if(userAgent == null || userAgent.isEmpty()) {
            return false;
        }else{
            userAgent = userAgent.toLowerCase();
            return userAgent.contains("bot") || userAgent.contains("spider") || userAgent.contains("robot");
        }
    }
    
    public boolean uriRequestsRobotsTextFile(String requestURI) {
        return requestURI.contains("robots.txt");
    }

    public boolean userAgentNotSpecified(String userAgent) {
        return userAgent == null || userAgent.equals("");
    }

    public boolean isValidAddress(String value, boolean acceptLocalhost) {
        return this.isValidHost(value, acceptLocalhost);
    }
    
    public boolean isValidHost(String value, boolean acceptLocalhost) {
        if(acceptLocalhost) {
            return value != null && value.length() > 0; 
        }else{
            return value != null && value.length() > 0 && value.charAt(value.length() - 1) > 64;
        }
    }

    public boolean isLocalHost(String value) {
        return "localhost".equalsIgnoreCase(value) || "127.0.0.1".equals(value);
    }
}

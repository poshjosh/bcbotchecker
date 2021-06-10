package com.looseboxes.botchecker;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author hp
 */
public class BotFilter implements javax.servlet.Filter{
    
    private BotChecker botChecker;

    public BotFilter() { }
    
    public BotFilter(BotChecker botChecker) { 
        this.botChecker = Objects.requireNonNull(botChecker);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        if(request instanceof HttpServletRequest) {
            
            boolean isFromBot = botChecker.isFromBot((HttpServletRequest)request);
            
            if(isFromBot) {
                
                // Just return, thus interrupting the filter chain
                //
                return;
            }
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        if(botChecker instanceof AutoCloseable) {
            try{
                ((AutoCloseable)botChecker).close();
            }catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isFromMaliciousBot(HttpServletRequest request) {
    
        final BotChecker.BotCategory botCategory = check(request);
        
        return botCategory.isMalicious();
    }
    
    public boolean isFromBot(HttpServletRequest request) {
    
        final BotChecker.BotCategory botCategory = check(request);
        
        return botCategory.isBot();
    }
    
    public BotChecker.BotCategory check(HttpServletRequest request) {
    
        final BotChecker.BotCategory botCategory = botChecker.check(request);
        
        return botCategory;
    }
}

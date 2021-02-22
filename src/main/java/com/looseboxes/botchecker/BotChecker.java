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

import javax.servlet.http.HttpServletRequest;

/**
 * @author Chinomso Bassey Ikwuagwu on Jan 4, 2019 1:24:32 PM
 */
public interface BotChecker {

    enum BotCategory{
        NONE(0),
        USERAGENT_NOT_SPECIFIED(10),
        URI_REQUESTS_ROBOTS_TEXTFILE(20),
        USERAGENT_CONTAINS_BOT_IDENTIFIER(30),
        URI_REQUESTS_BOT_TRAPFILE(100);
        
        int id;
        BotCategory(int id) {
            this.id = id;
        }
        public static BotCategory getOrDefault(int id, BotCategory resultIfNone) {
            final BotCategory [] values = BotCategory.values();
            for(BotCategory value : values) {
                if(value.getId() == id) {
                    return value;
                }
            }
            return resultIfNone;
        }
        public boolean isBot() {
            return this != NONE;
        }
        public boolean isMalicious() {
            return this == URI_REQUESTS_BOT_TRAPFILE;
        }
        public int getId() {
            return this.id;
        }
    }
    
    BotChecker NO_OP = new BotChecker() {
        @Override
        public boolean isFromBot(HttpServletRequest request) { return false; }
        @Override
        public BotCategory check(HttpServletRequest request) { return BotCategory.NONE; }
        @Override
        public BotCategory check(String address, String host, String userAgent, String requestURI) { return BotCategory.NONE; }
        @Override
        public void clear(HttpServletRequest request) { }
        @Override
        public void clear(String address, String host, String userAgent, String requestURI) { }
    };
    
    boolean isFromBot(HttpServletRequest request);

    BotCategory check(HttpServletRequest request);
    
    BotCategory check(String address, String host, String userAgent, String requestURI);

    void clear(HttpServletRequest request);
    
    void clear(String remoteAddr, String remoteHost, String userAgent, String requestURI);
}

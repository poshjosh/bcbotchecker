/*
 * Copyright 2019 NUROX Ltd.
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

/**
 * @author Chinomso Bassey Ikwuagwu on Jan 4, 2019 8:09:25 PM
 */
public interface BotCache extends AutoCloseable{

    BotCache NO_OP = new BotCache() {
        @Override
        public boolean isClosed() { return false; }
        @Override
        public void clear() { }
        @Override
        public void flush() { }
        @Override
        public BotChecker.BotCategory getOrDefault(String key, BotChecker.BotCategory resultIfNone) { return resultIfNone; }
        @Override
        public boolean put(String key, BotChecker.BotCategory value) { return false; }
        @Override
        public boolean remove(String key) { return false; }
        @Override
        public void close() { }
    };

    boolean isClosed();
    
    void clear();
    
    void flush();
    
    BotChecker.BotCategory getOrDefault(String key, BotChecker.BotCategory resultIfNone);
    
    boolean put(String key, BotChecker.BotCategory value);
    
    boolean remove(String key);
}

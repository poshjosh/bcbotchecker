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

package com.looseboxes.botchecker.util;

import java.util.function.BiFunction;
import javax.servlet.http.HttpServletRequest;

/**
 * @see https://stackoverflow.com/questions/9326138/is-it-possible-to-accurately-determine-the-ip-address-of-a-client-in-java-servle
 * @see https://stackoverflow.com/questions/29910074/how-to-get-client-ip-address-in-java-httpservletrequest 
 * @see https://www.mkyong.com/java/how-to-get-client-ip-address-in-java/
 * @see https://gist.github.com/nioe/11477264
 * @author Chinomso Bassey Ikwuagwu on Jan 14, 2019 3:07:04 PM
 */
public class GetClientIpAddress implements BiFunction<HttpServletRequest, String, String> {

//    public static final String HEADER_AUTHORIZATION = "Authorization",
//    AUTHENTICATION_TYPE_BASIC("Basic"),
//    X_AUTH_TOKEN("X-AUTH-TOKEN"),
//    WWW_Authenticate("WWW-Authenticate"),
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
    public static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    public static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
    public static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";

    @Override
    public String apply(HttpServletRequest request, String resultIfNone) {
        return this.execute(request, resultIfNone);
    }
    
    public String execute(HttpServletRequest request) {
        
        return this.execute(request, request.getRemoteAddr());
    }
    
    public String execute(HttpServletRequest request, String resultIfNone) {
        
        String ip = null;
        
        int tryCount = 1;

        while (!isIpFound(ip) && tryCount <= 6) {
            switch (tryCount) {
                case 1:
                    ip = request.getHeader(X_FORWARDED_FOR);
                    break;
                case 2:
                    ip = request.getHeader(PROXY_CLIENT_IP);
                    break;
                case 3:
                    ip = request.getHeader(WL_PROXY_CLIENT_IP);
                    break;
                case 4:
                    ip = request.getHeader(HTTP_CLIENT_IP);
                    break;
                case 5:
                    ip = request.getHeader(HTTP_X_FORWARDED_FOR);
                    break;
                default:
                    ip = null;
            }

            tryCount++;
        }

        return ip == null ? resultIfNone : ip;
    }

    private boolean isIpFound(String ip) {
        return ip != null && ip.length() > 0 && !"unknown".equalsIgnoreCase(ip);
    }
}

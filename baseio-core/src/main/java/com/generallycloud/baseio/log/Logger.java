/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.baseio.log;

public interface Logger {

    void info(String message);

    void info(String message, Object param);

    void info(String message, Object param, Object param1);

    void info(String message, Object... params);

    void debug(String message);

    void debug(Throwable throwable);

    void debug(String message, Object param);

    void debug(String message, Object param, Object param1);

    void debug(String message, Object... params);

    void error(String message);

    void error(String message, Object param);

    void error(String message, Object param, Object param1);

    void error(String message, Object... params);

    void error(String message, Throwable throwable);

    Class<?> getLoggerClass();

    boolean isEnableDebug();

    void error(Throwable e);

}

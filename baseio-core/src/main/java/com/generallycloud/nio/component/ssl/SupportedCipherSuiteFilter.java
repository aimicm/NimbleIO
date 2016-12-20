/*
 * Copyright 2015 GenerallyCloud.com
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
package com.generallycloud.nio.component.ssl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLEngine;

/**
 * This class will filter all requested ciphers out that are not supported by the current {@link SSLEngine}.
 */
public final class SupportedCipherSuiteFilter implements CipherSuiteFilter {
    public static final SupportedCipherSuiteFilter INSTANCE = new SupportedCipherSuiteFilter();

    private SupportedCipherSuiteFilter() { }

    @Override
    public String[] filterCipherSuites(Iterable<String> ciphers, List<String> defaultCiphers,
            Set<String> supportedCiphers) {
        if (defaultCiphers == null) {
            throw new NullPointerException("defaultCiphers");
        }
        if (supportedCiphers == null) {
            throw new NullPointerException("supportedCiphers");
        }

        final List<String> newCiphers = new ArrayList<String>();
//        if (ciphers == null) {
//            newCiphers = InternalThreadLocalMap.get().arrayList(defaultCiphers.size());
//            ciphers = defaultCiphers;
//        } else {
//            newCiphers = InternalThreadLocalMap.get().arrayList(supportedCiphers.size());
//        }
        for (String c : ciphers) {
            if (c == null) {
                break;
            }
            if (supportedCiphers.contains(c)) {
                newCiphers.add(c);
            }
        }
        return newCiphers.toArray(new String[newCiphers.size()]);
    }

}

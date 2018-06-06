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
package com.generallycloud.baseio.balance.router;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.baseio.balance.facade.FacadeSocketSession;
import com.generallycloud.baseio.balance.reverse.ReverseSocketSession;
import com.generallycloud.baseio.protocol.Future;

public class SimpleNextRouter extends AbstractBalanceRouter {

    private int                        index      = 0;
    private ReentrantLock              lock       = new ReentrantLock();
    private List<ReverseSocketSession> routerList = new ArrayList<>();

    private ReverseSocketSession getNextRouterSession() {
        List<ReverseSocketSession> list = this.routerList;
        if (list.isEmpty()) {
            return null;
        }
        ReverseSocketSession session;
        if (index < list.size()) {
            session = list.get(index++);
        } else {
            index = 1;
            session = list.get(0);
        }
        return session;
    }

    @Override
    public void addRouterSession(ReverseSocketSession session) {
        ReentrantLock lock = this.lock;
        lock.lock();
        this.routerList.add(session);
        lock.unlock();
    }

    @Override
    public void removeRouterSession(ReverseSocketSession session) {
        ReentrantLock lock = this.lock;
        lock.lock();
        routerList.remove(session);
        lock.unlock();
    }

    @Override
    public ReverseSocketSession getRouterSession(FacadeSocketSession session, Future future) {
        ReverseSocketSession router_session = getRouterSession(session);
        if (router_session == null) {
            return getRouterSessionFresh(session);
        }
        if (router_session.isClosed()) {
            return getRouterSessionFresh(session);
        }
        return router_session;
    }

    private ReverseSocketSession getRouterSessionFresh(FacadeSocketSession session) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            ReverseSocketSession router_session = getRouterSession(session);
            if (router_session == null || router_session.isClosed()) {
                router_session = getNextRouterSession();
                if (router_session == null) {
                    return null;
                }
                session.setReverseSocketSession(router_session);
            }
            return router_session;
        } finally {
            lock.unlock();
        }
    }

}

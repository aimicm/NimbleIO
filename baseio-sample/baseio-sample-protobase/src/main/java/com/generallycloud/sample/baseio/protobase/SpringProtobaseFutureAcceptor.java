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
package com.generallycloud.sample.baseio.protobase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.FutureAcceptor;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.protobase.ProtobaseFutureAcceptor;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.NamedFuture;

/**
 * @author wangkai
 *
 */
public class SpringProtobaseFutureAcceptor extends ProtobaseFutureAcceptor {

    private ClassPathXmlApplicationContext applicationContext;

    @Override
    public void accept(SocketSession session, Future future) throws Exception {
        NamedFuture f = (NamedFuture) future;
        FutureAcceptor acceptor = (FutureAcceptor) ContextUtil.getBean(f.getFutureName());
        if (acceptor == null) {
            future.write("404", session);
            session.flush(future);
            return;
        }
        acceptor.accept(session, future);
    }

    @Override
    protected void initialize(ChannelContext context, boolean redeploy) throws Exception {
        super.initialize(context, redeploy);
        Thread.currentThread().setContextClassLoader(null); // for spring
        System.setProperty("org.apache.commons.logging.log", Sl4jLogger.class.getName());
        applicationContext = new ClassPathXmlApplicationContext("classpath:spring-core.xml");
        applicationContext.start();
    }

    @Override
    protected void destroy(ChannelContext context, boolean redeploy) {
        applicationContext.destroy();
        super.destroy(context, redeploy);
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}

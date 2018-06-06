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
package com.generallycloud.test.io.protobase;

import com.generallycloud.baseio.codec.protobase.ProtobaseCodec;
import com.generallycloud.baseio.codec.protobase.ProtobaseFutureImpl;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioEventLoopGroup;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.SessionActiveIdleEventListener;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.log.DebugUtil;
import com.generallycloud.baseio.protocol.Future;

public class TestBeat {

    public static void main(String[] args) throws Exception {

        DebugUtil.setEnableDebug(true);

        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

            @Override
            public void accept(SocketSession session, Future future) throws Exception {
                DebugUtil.debug("______________" + future);
            }
        };

        String serviceKey = "TestSimpleServlet";
        NioEventLoopGroup group = new NioEventLoopGroup();
        group.setIdleTime(10);
        ChannelContext context = new ChannelContext(new Configuration(8300));
        ChannelConnector connector = new ChannelConnector(context,group);
        context.addSessionIdleEventListener(new SessionActiveIdleEventListener());
        context.addSessionEventListener(new LoggerSocketSEListener());
        context.setProtocolCodec(new ProtobaseCodec());
        context.setIoEventHandle(eventHandleAdaptor);
        SocketSession session = connector.connect();
        String param = "tttt";
        long old = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            Future future = new ProtobaseFutureImpl(serviceKey);
            future.write(param, context);
            session.flush(future);
            ThreadUtil.sleep(300);
        }
        System.out.println("Time:" + (System.currentTimeMillis() - old));
        Thread.sleep(2000);
        CloseUtil.close(connector);
    }

}

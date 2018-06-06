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
package com.generallycloud.test.io.fixedlength;

import com.generallycloud.baseio.codec.fixedlength.FixedLengthCodec;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthFuture;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthFutureImpl;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.ssl.SSLUtil;
import com.generallycloud.baseio.component.ssl.SslContext;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.log.DebugUtil;
import com.generallycloud.baseio.protocol.Future;

public class TestFIxedLengthClient {

    public static void main(String[] args) throws Exception {

        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

            @Override
            public void accept(SocketSession session, Future future) throws Exception {
                System.out.println();
                System.out.println("____________________" + future);
                System.out.println();
            }
        };

        SslContext sslContext = SSLUtil.initClient(true);
        ChannelContext context = new ChannelContext(new Configuration("localhost", 8300));
        ChannelConnector connector = new ChannelConnector(context);
        context.setIoEventHandle(eventHandleAdaptor);
        context.addSessionEventListener(new LoggerSocketSEListener());
        //		context.addSessionEventListener(new SessionActiveSEListener());
        context.setProtocolCodec(new FixedLengthCodec());
        context.setSslContext(sslContext);
        SocketSession session = connector.connect();
        FixedLengthFuture future = new FixedLengthFutureImpl();
        future.write("hello server!", session);
        session.flush(future);
        ThreadUtil.sleep(100);
        CloseUtil.close(connector);
        DebugUtil.debug("连接已关闭。。。");
    }

}

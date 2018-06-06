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
package com.generallycloud.baseio.codec.http11;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;

/**
 * @author wangkai
 *
 */
public class ClientHttpCodec extends AbstractHttpCodec {

    private static final byte[] COOKIE    = "Cookie:".getBytes();
    private static final byte[] PROTOCOL  = " HTTP/1.1\r\n".getBytes();
    private static final byte   SEMICOLON = ';';
    private int                 bodyLimit;
    private int                 headerLimit;

    public ClientHttpCodec() {
        this(1024 * 8, 1024 * 512);
    }

    public ClientHttpCodec(int headerLimit, int bodyLimit) {
        this.headerLimit = headerLimit;
        this.bodyLimit = bodyLimit;
    }

    @Override
    public Future createPINGPacket(SocketSession session) {
        return null;
    }

    @Override
    public Future createPONGPacket(SocketSession session, ChannelFuture ping) {
        return null;
    }

    @Override
    public ChannelFuture decode(NioSocketChannel channel, ByteBuf buffer) throws IOException {
        return new ClientHttpFuture(channel, headerLimit, bodyLimit);
    }

    @Override
    public void encode(NioSocketChannel channel, ChannelFuture future) throws IOException {
        ByteBufAllocator allocator = channel.allocator();
        HttpFuture f = (HttpFuture) future;
        ByteBuf buf = allocator.allocate(256);
        buf.put(f.getMethod().getBytes());
        buf.putByte(SPACE);
        buf.put(getRequestURI(f).getBytes());
        buf.put(PROTOCOL);
        writeHeaders(f, buf);
        List<Cookie> cookieList = f.getCookieList();
        if (cookieList != null && cookieList.size() > 0) {
            buf.put(COOKIE);
            for (Cookie c : cookieList) {
                writeBuf(buf, c.getName().getBytes());
                writeBuf(buf, COLON);
                writeBuf(buf, c.getValue().getBytes());
                writeBuf(buf, SEMICOLON);
            }
            buf.position(buf.position() - 1);
        }
        buf.putByte(R);
        buf.putByte(N);
        future.setByteBuf(buf.flip());
    }

    @Override
    public String getProtocolId() {
        return "HTTP11";
    }

    private String getRequestURI(HttpFuture future) {
        Map<String, String> params = future.getRequestParams();
        if (params == null) {
            return future.getRequestURL();
        }
        String url = future.getRequestURI();
        StringBuilder u = new StringBuilder(url);
        u.append("?");
        Set<Entry<String, String>> ps = params.entrySet();
        for (Entry<String, String> p : ps) {
            u.append(p.getKey());
            u.append("=");
            u.append(p.getValue());
            u.append("&");
        }
        return u.toString();
    }

    @Override
    public void initialize(ChannelContext context) {}

}

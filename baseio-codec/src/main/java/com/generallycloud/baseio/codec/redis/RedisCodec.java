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
package com.generallycloud.baseio.codec.redis;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.codec.redis.RedisFuture.RedisCommand;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.ProtocolCodec;

/**
 * @author wangkai
 *
 */
public class RedisCodec implements ProtocolCodec {

    @Override
    public ChannelFuture decode(NioSocketChannel channel, ByteBuf buffer) throws IOException {
        return new RedisFutureImpl();
    }

    @Override
    public Future createPINGPacket(SocketSession session) {
        RedisCmdFuture f = new RedisCmdFuture();
        f.setPING();
        f.writeCommand(RedisCommand.PING.raw);
        return f;
    }

    @Override
    public Future createPONGPacket(SocketSession session, ChannelFuture ping) {
        RedisCmdFuture f = (RedisCmdFuture) ping;
        f.setPONG();
        f.writeCommand(RedisCommand.PONG.raw);
        return f;
    }

    @Override
    public void encode(NioSocketChannel channel, ChannelFuture future) throws IOException {
        RedisFuture f = (RedisFuture) future;
        int writeSize = f.getWriteSize();
        if (writeSize == 0) {
            throw new IOException("null write buffer");
        }
        ByteBuf buf = UnpooledByteBufAllocator.getHeap().wrap(f.getWriteBuffer(), 0, writeSize);
        future.setByteBuf(buf);
    }

    @Override
    public String getProtocolId() {
        return "Redis";
    }

    @Override
    public void initialize(ChannelContext context) {}

}

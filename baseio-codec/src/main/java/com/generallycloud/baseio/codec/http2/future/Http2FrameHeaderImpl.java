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
package com.generallycloud.baseio.codec.http2.future;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.MathUtil;
import com.generallycloud.baseio.component.NioEventLoop;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.AbstractChannelFuture;

public class Http2FrameHeaderImpl extends AbstractChannelFuture implements Http2FrameHeader {

    private boolean          header_complete;

    private byte             flags;

    private int              streamIdentifier;

    private SocketHttp2Frame frame;

    public Http2FrameHeaderImpl(ByteBuf buf) {
        setByteBuf(buf);
    }

    public Http2FrameHeaderImpl() {}

    private void doHeaderComplete(NioSocketChannel channel, ByteBuf buf) {
        byte b0 = buf.getByte();
        byte b1 = buf.getByte();
        byte b2 = buf.getByte();
        int length = ((b0 & 0xff) << 8 * 2) | ((b1 & 0xff) << 8 * 1) | ((b2 & 0xff) << 8 * 0);
        int type = buf.getUnsignedByte();
        this.flags = buf.getByte();
        this.streamIdentifier = MathUtil.int2int31(buf.getInt());
        this.frame = genFrame(channel, type, length);
    }

    @Override
    public boolean read(NioSocketChannel channel, ByteBuf buffer) throws IOException {
        ByteBuf buf = getByteBuf();
        if (!header_complete) {
            buf.read(buffer);
            if (buf.hasRemaining()) {
                return false;
            }
            header_complete = true;
            doHeaderComplete(channel, buf.flip());
        }
        return frame.read(channel, buffer);
    }

    @Override
    public byte getFlags() {
        return flags;
    }

    @Override
    public boolean isSilent() {
        return frame.isSilent();
    }

    @Override
    public Http2FrameType getHttp2FrameType() {
        return frame.getHttp2FrameType();
    }

    @Override
    public int getStreamIdentifier() {
        return streamIdentifier;
    }

    @Override
    public Http2Frame getFrame() {
        return frame;
    }

    private SocketHttp2Frame genFrame(NioSocketChannel channel, Http2FrameType type, int length) {
        switch (type) {
            case FRAME_TYPE_CONTINUATION:
                break;
            case FRAME_TYPE_DATA:
                break;
            case FRAME_TYPE_GOAWAY:
                break;
            case FRAME_TYPE_HEADERS:
                return new Http2HeadersFrameImpl(allocate(channel, length), this);
            case FRAME_TYPE_PING:
                break;
            case FRAME_TYPE_PRIORITY:
                break;
            case FRAME_TYPE_PUSH_PROMISE:
                break;
            case FRAME_TYPE_RST_STREAM:
                break;
            case FRAME_TYPE_SETTINGS:
                return new Http2SettingsFrameImpl(allocate(channel, length), this);
            case FRAME_TYPE_WINDOW_UPDATE:
                return new Http2WindowUpdateFrameImpl(allocate(channel, length), this);
            default:
                break;
        }
        throw new IllegalArgumentException(type.toString());
    }

    private SocketHttp2Frame genFrame(NioSocketChannel channel, int type, int length) {
        return genFrame(channel, Http2FrameType.getValue(type), length);
    }

    @Override
    public void release(NioEventLoop eventLoop) {
        super.release(eventLoop);
        frame.release(eventLoop);
    }

    @Override
    public boolean isReleased() {
        return frame.isReleased() && getByteBuf().isReleased();
    }

}

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
import com.generallycloud.baseio.component.NioSocketChannel;

public class Http2WindowUpdateFrameImpl extends AbstractHttp2Frame
        implements Http2WindowUpdateFrame {

    private int updateValue;

    public Http2WindowUpdateFrameImpl(ByteBuf buf, Http2FrameHeader header) {
        super(header);
        this.setByteBuf(buf);
    }

    private void doComplete(NioSocketChannel channel, ByteBuf buf) throws IOException {

        this.updateValue = MathUtil.int2int31(buf.getInt());
    }

    @Override
    public boolean read(NioSocketChannel channel, ByteBuf buffer) throws IOException {
        ByteBuf buf = getByteBuf();
        buf.read(buffer);
        if (buf.hasRemaining()) {
            return false;
        }
        doComplete(channel, buf.flip());
        return true;
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    @Override
    public Http2FrameType getHttp2FrameType() {
        return Http2FrameType.FRAME_TYPE_SETTINGS;
    }

    @Override
    public int getUpdateValue() {
        return updateValue;
    }

}

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
package com.generallycloud.test.io.buffer;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.PooledByteBufAllocatorGroup;
import com.generallycloud.baseio.component.NioEventLoopGroup;

public class TestBytebufAllocator {

    public static void main(String[] args) throws Exception {

        test();

    }

    static void test() throws Exception {

        NioEventLoopGroup group = new NioEventLoopGroup();
        group.setMemoryPoolCapacity(10);
        group.setMemoryPoolUnit(1);
        PooledByteBufAllocatorGroup allocator = new PooledByteBufAllocatorGroup(group);

        allocator.start();

        ByteBufAllocator allocator2 = allocator.getNext();

        ByteBuf buf = allocator2.allocate(15);

        System.out.println(buf);
    }

}

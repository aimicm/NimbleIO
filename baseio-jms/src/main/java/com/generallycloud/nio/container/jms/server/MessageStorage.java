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
package com.generallycloud.nio.container.jms.server;

import com.generallycloud.nio.component.concurrent.ListQueue;
import com.generallycloud.nio.component.concurrent.ListQueueABQ;
import com.generallycloud.nio.container.jms.Message;

public class MessageStorage {

	private ListQueue<Message>	messages	= new ListQueueABQ<Message>(1024 * 8 * 10);

	public Message poll(long timeout) {
		return messages.poll(timeout);
	}

	//FIXME offer failed
	public boolean offer(Message message) {

		return messages.offer(message);
	}
	
	public int size(){
		return messages.size();
	}

}
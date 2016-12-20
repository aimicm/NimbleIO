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
package com.generallycloud.nio.container;

import java.util.HashSet;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.SocketSEListenerAdapter;
import com.generallycloud.nio.component.SocketSession;

public class IPFilter extends SocketSEListenerAdapter{

	private HashSet<String> blackIPs;
	
	public IPFilter(HashSet<String> blackIPs) {
		this.blackIPs = blackIPs;
	}

	@Override
	public void sessionOpened(SocketSession session) {
		if (!blackIPs.contains(session.getRemoteAddr())) {
			CloseUtil.close(session);
		}
	}
	
}

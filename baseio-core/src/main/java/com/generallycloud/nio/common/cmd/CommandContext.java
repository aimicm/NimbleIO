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
package com.generallycloud.nio.common.cmd;

import java.util.HashMap;

import com.generallycloud.nio.AttributesImpl;

public class CommandContext extends AttributesImpl{

	private HashMap<String, Executable>	executors	= new HashMap<String, Executable>();
	
	public void registExecutable(String cmd, Executable executable) {
		executors.put(cmd.toUpperCase(), executable);
	}
	
	public Executable getExecutable(String cmd){
		return executors.get(cmd.toUpperCase());
	}
	
	public String getCommandKeys(){
		return executors.keySet().toString();
		
	}
}

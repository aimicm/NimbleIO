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
package com.generallycloud.nio.common;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class BeanUtil {

	private static Map<Class<?>, FieldMapping>	fieldMapping	= new HashMap<Class<?>, FieldMapping>();
	private static ReentrantLock			lock				= new ReentrantLock();
	
	public static Object map2Object(Map<String, Object> map, Class<?> clazz) {
		if (map == null || clazz == null) {
			return null;
		}

		Object object = ClassUtil.newInstance(clazz);

		FieldMapping mapping = fieldMapping.get(object.getClass());

		if (mapping == null) {

			ReentrantLock lock = BeanUtil.lock;

			lock.lock();

			mapping = fieldMapping.get(object.getClass());

			if (mapping == null) {

				mapping = new FieldMapping(object.getClass());

				fieldMapping.put(object.getClass(), mapping);
			}

			lock.unlock();
		}

		List<Field> fieldList = mapping.getFieldList();

		for (Field f : fieldList) {

			Object v = map.get(f.getName());

			if (v == null) {
				continue;
			}

			if (!f.isAccessible()) {
				f.setAccessible(true);
			}

			try {
				f.set(object, v);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		return object;
	}

}

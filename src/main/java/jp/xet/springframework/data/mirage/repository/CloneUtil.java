/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.xet.springframework.data.mirage.repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

import org.springframework.util.ClassUtils;

@Slf4j
class CloneUtil {
	
	@SuppressWarnings("unchecked")
	static <T> T cloneIfPossible(T entity) {
		if (entity == null) {
			return entity;
		}
		
		Class<?> entityClass = entity.getClass();
		if (entity instanceof String || ClassUtils.isPrimitiveOrWrapper(entityClass)) {
			return entity;
		}
		if (entity instanceof Cloneable) {
			try {
				Method method = entityClass.getDeclaredMethod("clone");
				method.setAccessible(true);
				return (T) method.invoke(entity);
			} catch (ReflectiveOperationException | ClassCastException e) {
				log.warn("Failed to clone: {}", entityClass, e);
			}
		}
		if (entity instanceof Serializable) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				new ObjectOutputStream(baos).writeObject(entity);
				return (T) new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
			} catch (IOException | ClassNotFoundException e) {
				log.warn("Failed to serialize: {}", entityClass, e);
			}
		}
		return Arrays.stream(entityClass.getDeclaredConstructors())
			.filter(ctor -> ctor.getParameterTypes().length == 1)
			.filter(ctor -> ctor.getParameterTypes()[0].equals(entityClass))
			.map(ctor -> newInstance(ctor, entity))
			.findFirst()
			.orElseGet(() -> {
				log.debug("Impossible to clone: {}", entityClass);
				return entity;
			});
	}
	
	private static <T> T newInstance(Constructor<?> ctor, T entity) {
		try {
			@SuppressWarnings("unchecked")
			T instance = (T) ctor.newInstance(entity);
			return instance;
		} catch (ReflectiveOperationException e) {
			log.warn("Failed to invoke ctor");
		}
		return null;
	}
}

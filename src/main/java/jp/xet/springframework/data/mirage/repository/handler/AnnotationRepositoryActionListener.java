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
package jp.xet.springframework.data.mirage.repository.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;

/**
 * @see BeforeCreate
 * @see BeforeUpdate
 */
@Slf4j
public class AnnotationRepositoryActionListener implements RepositoryActionListener {
	
	@Override
	public void beforeCreate(Object entity) {
		preProcess(entity, BeforeCreate.class);
	}
	
	@Override
	public void beforeUpdate(Object entity) {
		preProcess(entity, BeforeUpdate.class);
	}
	
	private void preProcess(Object entity, Class<? extends Annotation> annotation) {
		Class<?> c = entity.getClass();
		while (c != null && c != Object.class) {
			Method[] declaredMethods = c.getDeclaredMethods();
			for (Method method : declaredMethods) {
				Annotation idAnnotation = method.getAnnotation(annotation);
				if (idAnnotation != null) {
					method.setAccessible(true);
					try {
						method.invoke(entity);
					} catch (Exception e) { // NOPMD
						log.warn("failed to invoke: {}", method);
						// ignore
					}
				}
			}
			c = c.getSuperclass();
		}
	}
}

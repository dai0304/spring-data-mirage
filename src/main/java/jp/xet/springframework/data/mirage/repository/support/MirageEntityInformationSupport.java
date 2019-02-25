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
package jp.xet.springframework.data.mirage.repository.support;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.support.AbstractEntityInformation;

import com.miragesql.miragesql.SqlManager;
import com.miragesql.miragesql.naming.NameConverter;
import com.miragesql.miragesql.util.MirageUtil;

/**
 * TODO for daisuke
 *
 * @param <T> type of entity (domainClass)
 * @param <ID>
 */
public class MirageEntityInformationSupport<T, ID extends Serializable, C>
		extends AbstractEntityInformation<T, ID> implements MirageEntityInformation<T, ID, C> {
	
	/**
	 * Creates a {@link MirageEntityInformation} for the given domain class and {@link SqlManager}.
	 *
	 * @param domainClass class of entity
	 * @param nameConverter {@link NameConverter}
	 * @return created {@link MirageEntityInformation}
	 */
	public static <T> EntityInformation<T, ?> getMetadata(Class<T> domainClass, NameConverter nameConverter) {
		return new MirageEntityInformationSupport<>(domainClass, nameConverter);
	}
	
	
	private final NameConverter nameConverter;
	
	
	/**
	 * Creates a new {@link MirageEntityInformationSupport} with the given domain class.
	 *
	 * @param domainClass
	 */
	public MirageEntityInformationSupport(Class<T> domainClass, NameConverter nameConverter) {
		super(domainClass);
		this.nameConverter = nameConverter;
	}
	
	@Override
	public String getEntityName() {
		try {
			return MirageUtil.getTableName(getJavaType(), nameConverter);
		} catch (NullPointerException e) { // NOPMD
			throw new IllegalStateException("If nameConverter bean is not registered, "
					+ "entity class must have @Table annotation", e);
		}
	}
	
	@Override
	public ID getId(T entity) {
		Class<?> c = getJavaType();
		while (c != null && c != Object.class) {
			Field[] declaredFields = c.getDeclaredFields();
			for (Field field : declaredFields) {
				Id idAnnotation = field.getAnnotation(Id.class);
				if (idAnnotation != null) {
					field.setAccessible(true);
					try {
						@SuppressWarnings("unchecked")
						ID id = (ID) field.get(entity);
						return id;
					} catch (Exception e) { // NOPMD
						// NOPMD ignore
					}
				}
			}
			c = c.getSuperclass();
		}
		return null;
	}
	
	@Override
	public Class<ID> getIdType() {
		Class<?> c = getJavaType();
		while (c != null && c != Object.class) {
			Field[] declaredFields = c.getDeclaredFields();
			for (Field field : declaredFields) {
				Id idAnnotation = field.getAnnotation(Id.class);
				if (idAnnotation != null) {
					@SuppressWarnings("unchecked")
					Class<ID> idType = (Class<ID>) field.getType();
					return idType;
				}
			}
			c = c.getSuperclass();
		}
		throw new IllegalStateException("Id annotation not found in: " + getJavaType());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public C getCondition(T entity) {
		Class<?> c = getJavaType();
		while (c != null && c != Object.class) {
			Field[] declaredFields = c.getDeclaredFields();
			for (Field field : declaredFields) {
				Version versionAnnotation = field.getAnnotation(Version.class);
				if (versionAnnotation != null) {
					field.setAccessible(true);
					try {
						return (C) field.get(entity);
					} catch (Exception e) { // NOPMD
						// NOPMD ignore
					}
				}
			}
			c = c.getSuperclass();
		}
		return null;
	}
}

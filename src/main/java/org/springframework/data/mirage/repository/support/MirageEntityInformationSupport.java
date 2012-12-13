/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2012/05/16
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.springframework.data.mirage.repository.support;

import java.io.Serializable;

import jp.sf.amateras.mirage.SqlManager;
import jp.sf.amateras.mirage.annotation.Table;

import org.springframework.data.repository.core.support.AbstractEntityInformation;
import org.springframework.util.StringUtils;

/**
 * TODO for daisuke
 * 
 * @param <T> 
 * @param <ID> 
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
public class MirageEntityInformationSupport<T, ID extends Serializable> extends AbstractEntityInformation<T, ID>
		implements MirageEntityInformation<T, ID> {
	
	/**
	 * Creates a {@link MirageEntityInformation} for the given domain class and {@link SqlManager}.
	 * 
	 * @param domainClass
	 * @param sqlManager {@link SqlManager}
	 * @return
	 */
	public static <T>MirageEntityInformation<T, ?> getMetadata(Class<T> domainClass, SqlManager sqlManager) {
		return new MirageEntityInformationSupport<T, Serializable>(domainClass);
	}
	
	/**
	 * Creates a new {@link MirageEntityInformationSupport} with the given domain class.
	 * 
	 * @param domainClass
	 */
	public MirageEntityInformationSupport(Class<T> domainClass) {
		super(domainClass);
	}
	
	@Override
	public String getEntityName() {
		Class<?> domainClass = getJavaType();
		Table entity = domainClass.getAnnotation(Table.class);
		return entity != null && StringUtils.hasText(entity.name()) ? entity.name() : domainClass.getSimpleName();
	}
	
	@Override
	public ID getId(T entity) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Class<ID> getIdType() {
		// TODO Auto-generated method stub
		return null;
	}
}

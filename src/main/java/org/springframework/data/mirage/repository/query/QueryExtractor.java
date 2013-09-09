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
package org.springframework.data.mirage.repository.query;

import jp.sf.amateras.mirage.SqlResource;

/**
 * Interface to hide different implementations to extract the original Mirage query string from a {@link Query}.
 * 
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
public interface QueryExtractor {
	
	/**
	 * Returns whether the extractor is able to extract the original query string from a given {@link Query}.
	 * 
	 * @return
	 */
	boolean canExtractQuery();
	
	/**
	 * Reverse engineers the query string from the {@link Query} object. This requires provider specific API as JPA does
	 * not provide access to the underlying query string as soon as one has created a {@link Query} instance of it.
	 * 
	 * @param query
	 * @return the query string representing the query or {@literal null} if resolving is not possible.
	 */
	String extractQueryString(SqlResource query);
}

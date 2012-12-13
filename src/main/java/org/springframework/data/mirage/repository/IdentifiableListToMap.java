/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2012/02/15
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
package org.springframework.data.mirage.repository;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * TODO for daisuke
 * 
 * @param <T> 
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
public class IdentifiableListToMap<T extends Identifiable> implements Function<Collection<T>, Map<Long, T>> {
	
	@Override
	public Map<Long, T> apply(Collection<T> input) {
		if (input == null) {
			return null;
		}
		Map<Long, T> result = Maps.newHashMap();
		for (T element : input) {
			if (element != null) {
				result.put(element.getId(), element);
			}
		}
		return result;
	}
}

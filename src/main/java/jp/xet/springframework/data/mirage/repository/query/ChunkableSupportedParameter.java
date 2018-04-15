/*
 * Copyright 2011-2018 the original author or authors.
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
package jp.xet.springframework.data.mirage.repository.query;

import java.util.Arrays;
import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Parameter;

import jp.xet.sparwings.spring.data.chunk.Chunkable;

/**
 * TODO for daisuke
 * 
 * @since TODO for daisuke
 * @version $Id$
 * @author daisuke
 */
public class ChunkableSupportedParameter extends Parameter {
	
	static final List<Class<?>> TYPES = Arrays.asList(Pageable.class, Sort.class, Chunkable.class);
	
	private MethodParameter parameter;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param parameter
	 */
	public ChunkableSupportedParameter(MethodParameter parameter) {
		super(parameter);
		this.parameter = parameter;
	}
	
	@Override
	public boolean isSpecialParameter() {
		return TYPES.contains(parameter.getParameterType());
	}
	
	/**
	 * Returns whether the {@link Parameter} is a {@link Chunkable} parameter.
	 * 
	 * @return
	 */
	boolean isChunkable() {
		return Chunkable.class.isAssignableFrom(getType());
	}
}

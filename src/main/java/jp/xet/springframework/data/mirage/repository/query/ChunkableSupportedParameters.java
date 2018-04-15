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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.data.repository.query.DefaultParameters;
import org.springframework.data.repository.query.Parameters;

import jp.xet.sparwings.spring.data.chunk.Chunkable;

/**
 * TODO for daisuke
 * 
 * @since TODO for daisuke
 * @version $Id$
 * @author daisuke
 */
public class ChunkableSupportedParameters
		extends Parameters<ChunkableSupportedParameters, ChunkableSupportedParameter> {
	
	private int chunkableIndex;
	
	
	/**
	 * Creates a new {@link DefaultParameters} instance from the given {@link Method}.
	 * 
	 * @param method must not be {@literal null}.
	 */
	public ChunkableSupportedParameters(Method method) {
		super(method);
		List<Class<?>> types = Arrays.asList(method.getParameterTypes());
		chunkableIndex = types.indexOf(Chunkable.class);
	}
	
	private ChunkableSupportedParameters(List<ChunkableSupportedParameter> originals) {
		super(originals);
		
		int chunkableIndexTemp = -1;
		
		for (int i = 0; i < originals.size(); i++) {
			ChunkableSupportedParameter original = originals.get(i);
			chunkableIndexTemp = original.isChunkable() ? i : -1;
		}
		
		chunkableIndex = chunkableIndexTemp;
	}
	
	/**
	 * Returns the index of the {@link Chunkable} {@link Method} parameter if available. Will return {@literal -1} if there
	 * is no {@link Chunkable} argument in the {@link Method}'s parameter list.
	 * 
	 * @return the pageableIndex
	 */
	public int getChunkableIndex() {
		return chunkableIndex;
	}
	
	/**
	 * Returns whether the method the {@link Parameters} was created for contains a {@link Chunkable} argument.
	 * 
	 * @return
	 */
	public boolean hasChunkableParameter() {
		return chunkableIndex != -1;
	}
	
	@Override
	protected ChunkableSupportedParameters createFrom(List<ChunkableSupportedParameter> parameters) {
		return new ChunkableSupportedParameters(parameters);
	}
	
	@Override
	protected ChunkableSupportedParameter createParameter(MethodParameter parameter) {
		return new ChunkableSupportedParameter(parameter);
	}
}

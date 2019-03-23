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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.junit.Test;

/**
 * Test for {@link CloneUtil}.
 */
@Slf4j
public class CloneUtilTest {
	
	@Test
	public void testNull() {
		// setup
		Object entity = null;
		// exercise
		Object actual = CloneUtil.cloneIfPossible(entity);
		// verify
		assertThat(actual).isNull();
	}
	
	@Test
	public void testString() {
		// setup
		String entity = "aaa";
		// exercise
		String actual = CloneUtil.cloneIfPossible(entity);
		// verify
		assertThat(actual).isSameAs(entity);
	}
	
	
	@Data
	static class ExampleCloneable implements Cloneable {
		
		private final String foo;
		
		
		@Override // -@cs[NoClone]
		public ExampleCloneable clone() throws CloneNotSupportedException {
			return (ExampleCloneable) super.clone();
		}
	}
	
	
	@Test // -@cs[InnerTypeLast]
	public void testCloneable() {
		// setup
		ExampleCloneable entity = new ExampleCloneable("bbb");
		// exercise
		ExampleCloneable actual = CloneUtil.cloneIfPossible(entity);
		// verify
		assertThat(actual)
			.isNotSameAs(entity)
			.returns("bbb", ExampleCloneable::getFoo);
	}
	
	
	@Data
	@SuppressWarnings("serial")
	static class ExampleSerializable implements Serializable {
		
		private final String foo;
		
	}
	
	
	@Test // -@cs[InnerTypeLast]
	public void testSerializable() {
		// setup
		ExampleSerializable entity = new ExampleSerializable("ccc");
		// exercise
		ExampleSerializable actual = CloneUtil.cloneIfPossible(entity);
		// verify
		assertThat(actual)
			.isNotSameAs(entity)
			.returns("ccc", ExampleSerializable::getFoo);
	}
	
	
	@Data
	@RequiredArgsConstructor
	static class ExampleCopyCtor {
		
		private final String foo;
		
		
		ExampleCopyCtor(ExampleCopyCtor original) {
			this.foo = original.foo;
		}
	}
	
	
	@Test // -@cs[InnerTypeLast]
	public void testCopyCtor() {
		// setup
		ExampleCopyCtor entity = new ExampleCopyCtor("ddd");
		// exercise
		ExampleCopyCtor actual = CloneUtil.cloneIfPossible(entity);
		// verify
		assertThat(actual)
			.isNotSameAs(entity)
			.returns("ddd", ExampleCopyCtor::getFoo);
	}
	
	
	@Data
	static class ExampleImpossible {
		
		private final String foo;
		
	}
	
	
	@Test // -@cs[InnerTypeLast]
	public void testImpossible() {
		// setup
		ExampleImpossible entity = new ExampleImpossible("ccc");
		// exercise
		ExampleImpossible actual = CloneUtil.cloneIfPossible(entity);
		// verify
		assertThat(actual)
			.isSameAs(entity);
	}
}

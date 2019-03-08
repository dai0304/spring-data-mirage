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
package jp.xet.springframework.data.mirage.repository.conditional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;

import jp.xet.springframework.data.mirage.repository.MirageConfiguration;
import jp.xet.springframework.data.mirage.repository.conditional.ConditionalEntityRepositoryTest.PreProcessConfiguration;
import jp.xet.springframework.data.mirage.repository.handler.AnnotationRepositoryActionListener;
import jp.xet.springframework.data.mirage.repository.handler.RepositoryActionListener;

/**
 * Test for {@link ConditionalEntityRepository}.
 */
@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
	MirageConfiguration.class,
	PreProcessConfiguration.class
})
@Transactional
@SuppressWarnings("javadoc")
public class ConditionalEntityRepositoryTest {
	
	@Autowired
	ConditionalEntityRepository repo;
	
	
	// ConditionalUpdatableRepository
	
	@Test
	@Rollback
	public void testUpdate_WithConditional() {
		ConditionalEntity foo = repo.create(new ConditionalEntity("foo", "foo"));
		assert foo.getVersion() == 1L;
		foo.setStr("FOO");
		// exercise
		ConditionalEntity actual = repo.update(foo, foo.getVersion());
		// verify
		assertThat(actual)
			.returns("foo", ConditionalEntity::getId)
			.returns("FOO", ConditionalEntity::getStr)
			.returns(2L, ConditionalEntity::getVersion);
		assertThat(repo.findById("foo"))
			.hasValueSatisfying(entity -> assertThat(entity)
				.returns("foo", ConditionalEntity::getId)
				.returns("FOO", ConditionalEntity::getStr)
				.returns(2L, ConditionalEntity::getVersion));
	}
	
	@Test
	@Rollback
	public void testUpdate_WithConditionFailed() {
		ConditionalEntity foo = repo.create(new ConditionalEntity("foo", "foo"));
		assert foo.getVersion() == 1;
		foo.setStr("FOO");
		// exercise
		Throwable actual = catchThrowable(() -> repo.update(foo, 123L));
		// verify
		assertThat(actual).isInstanceOf(OptimisticLockingFailureException.class)
			.hasMessage(String.format("expected is %s, but actual is %s", 123, 1));
		assertThat(repo.findById("foo"))
			.hasValueSatisfying(entity -> assertThat(entity)
				.returns("foo", ConditionalEntity::getStr)
				.returns(1L, ConditionalEntity::getVersion));
	}
	
	// ConditionalDeletableRepository
	
	@Test
	@Rollback
	public void testDelete_WithConditional() {
		ConditionalEntity foo = repo.create(new ConditionalEntity("foo", "foo"));
		assert foo.getVersion() == 1;
		foo.setStr("FOO");
		// exercise
		repo.delete(foo, foo.getVersion());
		// verify
		assertThat(repo.existsById("foo")).isFalse();
	}
	
	@Test
	@Rollback
	public void testDelete_WithConditionFailed() {
		ConditionalEntity foo = repo.create(new ConditionalEntity("foo", "foo"));
		assert foo.getVersion() == 1;
		foo.setStr("FOO");
		// exercise
		Throwable actual = catchThrowable(() -> repo.delete(foo, 123L));
		// verify
		assertThat(actual).isInstanceOf(OptimisticLockingFailureException.class)
			.hasMessage(String.format("expected is %s, but actual is %s", 123, 1));
		assertThat(repo.existsById("foo")).isTrue();
	}
	
	@Test
	@Rollback
	public void testDeleteById_WithConditional() {
		ConditionalEntity foo = repo.create(new ConditionalEntity("foo", "foo"));
		assert foo.getVersion() == 1;
		foo.setStr("FOO");
		// exercise
		repo.deleteById("foo", foo.getVersion());
		// verify
		assertThat(repo.existsById("foo")).isFalse();
	}
	
	@Test
	@Rollback
	public void testDeleteById_WithConditionFailed() {
		ConditionalEntity foo = repo.create(new ConditionalEntity("foo", "foo"));
		assert foo.getVersion() == 1;
		foo.setStr("FOO");
		// exercise
		Throwable actual = catchThrowable(() -> repo.deleteById("foo", 123L));
		// verify
		assertThat(actual).isInstanceOf(OptimisticLockingFailureException.class)
			.hasMessage(String.format("expected is %s, but actual is %s", 123, 1));
		assertThat(repo.existsById("foo")).isTrue();
	}
	
	
	@TestConfiguration
	public static class PreProcessConfiguration {
		
		@Bean
		public RepositoryActionListener handler() {
			return new AnnotationRepositoryActionListener();
		}
	}
}

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
package jp.xet.springframework.data.mirage.repository.appgenerated;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;

import jp.xet.springframework.data.mirage.repository.MirageConfiguration;

/**
 * Test for {@link UserRepository}.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MirageConfiguration.class)
@Transactional
@Slf4j
@SuppressWarnings("javadoc")
public class UserRepositoryTest {
	
	@Autowired
	UserRepository repo;
	
	
	// BaseRepository
	
	@Test
	public void testGetId() {
		// setup
		User user = new User("foo", "foopass");
		// exercise
		String actual = repo.getId(user);
		// verify
		assertThat(actual).isEqualTo("foo");
	}
	
	// ReadableRepository
	
	@Test
	public void testExistsById() {
		// setup
		repo.save(new User("foo", "foopass"));
		// exercise
		boolean actual = repo.existsById("foo");
		// verify
		assertThat(actual).isTrue();
	}
	
	@Test
	public void testExistsById_Absent() {
		// exercise
		boolean actual = repo.existsById("absent");
		// verify
		assertThat(actual).isFalse();
	}
	
	@Test
	public void testFindById() {
		// setup
		repo.save(new User("foo", "foopass"));
		// exercise
		Optional<User> actual = repo.findById("foo");
		// verify
		assertThat(actual).hasValueSatisfying(u -> assertThat(u.getPassword()).isEqualTo("foopass"));
	}
	
	@Test
	public void testFindById_Absent_Empty() {
		// exercise
		Optional<User> actual = repo.findById("absent");
		// verify
		assertThat(actual).isNotPresent();
	}
	
	// ScannableRepository
	
	@Test
	public void testCount() {
		// exercise
		long actual1 = repo.count();
		// verify
		assertThat(actual1).isEqualTo(0);
		
		// setup
		repo.save(new User("foo", "foopass"));
		// exercise
		long actual2 = repo.count();
		// verify
		assertThat(actual2).isEqualTo(1);
		
		// setup
		repo.save(new User("bar", "barpass"));
		// exercise
		long actual3 = repo.count();
		// verify
		assertThat(actual3).isEqualTo(2);
	}
	
	@Test
	public void testFindAll() {
		// setup
		repo.save(new User("foo", "foopass"));
		repo.save(new User("bar", "barpass"));
		// exercise
		Iterable<User> actual = repo.findAll();
		// verify
		assertThat(actual).hasSize(2).containsExactlyInAnyOrder(
				new User("foo", "foopass"),
				new User("bar", "barpass"));
	}
	
	@Test
	public void testFindAll_Empty() {
		// exercise
		Iterable<User> actual = repo.findAll();
		// verify
		assertThat(actual).isEmpty();
	}
	
	@Test
	public void testFindAll_SortAsc() {
		// setup
		repo.save(new User("foo", "foopass"));
		repo.save(new User("bar", "barpass"));
		// exercise
		List<User> actual = repo.findAll(Sort.by(Direction.ASC, "username"));
		// verify
		assertThat(actual).hasSize(2).containsExactly(
				new User("bar", "barpass"),
				new User("foo", "foopass"));
	}
	
	@Test
	public void testFindAll_SortDesc() {
		// setup
		repo.save(new User("foo", "foopass"));
		repo.save(new User("bar", "barpass"));
		// exercise
		List<User> actual = repo.findAll(Sort.by(Direction.DESC, "username"));
		// verify
		assertThat(actual).hasSize(2).containsExactly(
				new User("foo", "foopass"),
				new User("bar", "barpass"));
	}
	
	// CreatableRepository
	
	@Test
	public void testCreate() {
		User user = new User("foo", "foopass");
		// exercise
		User actual = repo.create(user);
		// verify
		assertThat(actual).isEqualTo(user);
		assertThat(repo.findById("foo")).hasValue(user);
	}
	
	@Test
	public void testCreate_DuplicateKey_DuplicateKeyException() {
		repo.create(new User("foo", "foopass"));
		User user2 = new User("foo", "foobar");
		// exercise
		Throwable actual = catchThrowable(() -> repo.create(user2));
		// verify
		assertThat(actual).isInstanceOf(DuplicateKeyException.class);
		assertThat(repo.count()).isEqualTo(1);
	}
	
	// UpsertableRepository
	
	@Test
	public void testSave() {
		User user = new User("foo", "foopass");
		// exercise
		User actual = repo.save(user);
		// verify
		assertThat(actual).isEqualTo(user);
		assertThat(repo.findById("foo")).hasValue(user);
	}
	
	@Test
	public void testSave_DuplicateKey_OverWrited() {
		repo.create(new User("foo", "foopass"));
		User user = new User("foo", "xxxx");
		// exercise
		User actual = repo.save(user);
		// verify
		assertThat(actual).isEqualTo(user);
		assertThat(repo.findById("foo")).hasValue(user);
		assertThat(repo.count()).isEqualTo(1);
	}
	
	// BatchReadableRepository
	
	@Test
	public void testFindAll_Iterable() {
		// setup
		User foo = repo.save(new User("foo", "foopass"));
		User bar = repo.save(new User("bar", "barpass"));
		User baz = repo.save(new User("baz", "bazpass"));
		// exercise
		Iterable<User> actual = repo.findAll(Arrays.asList("foo", "baz"));
		// verify
		assertThat(actual).hasSize(2).containsExactlyInAnyOrder(foo, baz).doesNotContain(bar);
	}
	
	@Test
	public void testFindAll_IterableEmpty() {
		// setup
		repo.save(new User("foo", "foopass"));
		repo.save(new User("bar", "barpass"));
		repo.save(new User("baz", "bazpass"));
		// exercise
		Iterable<User> actual = repo.findAll(Collections.emptySet());
		// verify
		assertThat(actual).isEmpty();
	}
	
	@Test
	public void testFindAll_IterableContainsAbsent() {
		// setup
		User foo = repo.save(new User("foo", "foopass"));
		User bar = repo.save(new User("bar", "barpass"));
		User baz = repo.save(new User("baz", "bazpass"));
		// exercise
		Iterable<User> actual = repo.findAll(Arrays.asList("foo", "bar", "absent"));
		// verify
		assertThat(actual).hasSize(2).containsExactlyInAnyOrder(foo, bar).doesNotContain(baz);
	}
}

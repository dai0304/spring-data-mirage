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
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Ignore;
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
	
	private static final User FOO = new User("foo", "foopass");
	
	private static final User ANOTHER_FOO = new User("foo", "hoge");
	
	private static final User BAR = new User("bar", "barpass");
	
	private static final User BAZ = new User("baz", "bazpass");
	
	@Autowired
	UserRepository repo;
	
	// ReadableRepository
	
	
	@Test
	@Rollback
	public void testExistsById() {
		// setup
		repo.save(FOO);
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
	@Rollback
	public void testFindById() {
		// setup
		repo.save(FOO);
		repo.save(BAR);
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
		repo.save(FOO);
		// exercise
		long actual2 = repo.count();
		// verify
		assertThat(actual2).isEqualTo(1);
		
		// setup
		repo.save(BAR);
		// exercise
		long actual3 = repo.count();
		// verify
		assertThat(actual3).isEqualTo(2);
	}
	
	@Test
	public void testFindAll() {
		// setup
		repo.save(FOO);
		repo.save(BAR);
		// exercise
		Iterable<User> actual = repo.findAll();
		// verify
		assertThat(actual).hasSize(2).containsExactlyInAnyOrder(
				FOO,
				BAR);
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
		repo.save(FOO);
		repo.save(BAR);
		// exercise
		List<User> actual = repo.findAll(Sort.by(Direction.ASC, "username"));
		// verify
		assertThat(actual).hasSize(2).containsExactly(BAR, FOO);
	}
	
	@Test
	public void testFindAll_SortDesc() {
		// setup
		repo.save(FOO);
		repo.save(BAR);
		// exercise
		List<User> actual = repo.findAll(Sort.by(Direction.DESC, "username"));
		// verify
		assertThat(actual).hasSize(2).containsExactly(FOO, BAR);
	}
	
	// CreatableRepository
	
	@Test
	public void testCreate() {
		// exercise
		User actual = repo.create(FOO);
		// verify
		assertThat(actual).isEqualTo(FOO);
		assertThat(repo.findById("foo")).hasValue(FOO);
	}
	
	@Test
	public void testCreate_DuplicateKey_DuplicateKeyException() {
		repo.create(FOO);
		// exercise
		Throwable actual = catchThrowable(() -> repo.create(ANOTHER_FOO));
		// verify
		assertThat(actual).isInstanceOf(DuplicateKeyException.class);
		assertThat(repo.count()).isEqualTo(1);
	}
	
	// UpsertableRepository
	
	@Test
	public void testSave() {
		// exercise
		User actual = repo.save(FOO);
		// verify
		assertThat(actual).isEqualTo(FOO);
		assertThat(repo.findById("foo")).hasValue(FOO);
	}
	
	@Test
	public void testSave_DuplicateKey_OverWrited() {
		repo.create(FOO);
		// exercise
		User actual = repo.save(ANOTHER_FOO);
		// verify
		assertThat(actual).isEqualTo(ANOTHER_FOO);
		assertThat(repo.findById("foo")).hasValue(ANOTHER_FOO);
		assertThat(repo.count()).isEqualTo(1);
	}
	
	// UpdatableRepository
	
	@Test
	public void testUpdate() {
		assertThat(repo.count()).isEqualTo(0);
		User entity = repo.create(FOO);
		assertThat(repo.count()).isEqualTo(1);
		entity.setPassword("foopass2");
		// exercise
		User actual = repo.update(entity);
		// verify
		assertThat(actual).isEqualTo(entity);
		assertThat(repo.count()).isEqualTo(1);
		assertThat(repo.findById("foo")).hasValueSatisfying(found -> assertThat(found)
			.returns("foopass2", User::getPassword));
	}
	
	@Test
	public void testUpdate_Absent_IncorrectResultSizeDataAccessException() {
		assertThat(repo.count()).isEqualTo(0);
		// exercise
		Throwable actual = catchThrowable(() -> repo.update(FOO));
		// verify
		assertThat(actual).isInstanceOf(IncorrectResultSizeDataAccessException.class);
		assertThat(repo.count()).isEqualTo(0);
	}
	
	// DeletableRepository
	
	@Test
	public void testDelete() {
		// setup
		User entity = repo.create(FOO);
		// exercise
		repo.delete(entity);
		// verify
		assertThat(repo.count()).isEqualTo(0);
	}
	
	@Test
	public void testDelete_Absent() {
		// setup
		// exercise
		repo.delete(FOO);
		// verify
		assertThat(repo.count()).isEqualTo(0);
	}
	
	@Test
	public void testDeleteById() {
		// setup
		repo.create(FOO);
		// exercise
		repo.deleteById("foo");
		// verify
		assertThat(repo.count()).isEqualTo(0);
	}
	
	@Test
	public void testDeleteById_Absent() {
		// exercise
		repo.deleteById("foo");
		// verify
		assertThat(repo.count()).isEqualTo(0);
	}
	
	// TruncatableRepository
	
	@Test
	public void testDeleteAll() {
		// setup
		repo.create(FOO);
		// exercise
		repo.deleteAll();
		// verify
		assertThat(repo.count()).isEqualTo(0);
	}
	
	@Test
	public void testDeleteAll_Absent() {
		// exercise
		repo.deleteAll();
		// verify
		assertThat(repo.count()).isEqualTo(0);
	}
	
	// BatchReadableRepository
	
	@Test
	public void testFindAll_Iterable() {
		// setup
		User foo = repo.save(FOO);
		User bar = repo.save(BAR);
		User baz = repo.save(BAZ);
		// exercise
		Iterable<User> actual = repo.findAll(Arrays.asList("foo", "baz"));
		// verify
		assertThat(actual).hasSize(2).containsExactlyInAnyOrder(foo, baz).doesNotContain(bar);
	}
	
	@Test
	public void testFindAll_IterableEmpty() {
		// setup
		repo.save(FOO);
		repo.save(BAR);
		repo.save(BAZ);
		// exercise
		Iterable<User> actual = repo.findAll(Collections.emptySet());
		// verify
		assertThat(actual).isEmpty();
	}
	
	@Test
	public void testFindAll_IterableContainsAbsent() {
		// setup
		User foo = repo.save(FOO);
		User bar = repo.save(BAR);
		User baz = repo.save(BAZ);
		// exercise
		Iterable<User> actual = repo.findAll(Arrays.asList("foo", "bar", "absent"));
		// verify
		assertThat(actual).hasSize(2).containsExactlyInAnyOrder(foo, bar).doesNotContain(baz);
	}
	
	// BatchCreatableRepository
	
	@Test
	public void testCreateAll() {
		// setup
		List<User> users = Arrays.asList(
				FOO,
				BAR,
				BAZ);
		// exercise
		repo.createAll(users);
		// verify
		assertThat(repo.count()).isEqualTo(3);
	}
	
	@Test
	@Ignore("rollbacked?")
	public void testCreateAll_DuplicateKey_DuplicateKeyException() {
		repo.create(FOO);
		List<User> users = Arrays.asList(ANOTHER_FOO, BAR, BAZ);
		// exercise
		Throwable actual = catchThrowable(() -> repo.createAll(users));
		// verify
		assertThat(actual).isInstanceOf(DuplicateKeyException.class);
		assertThat(repo.count()).isEqualTo(1);
		assertThat(repo.findById("foo")).hasValueSatisfying(foo -> assertThat(foo.getPassword()).isEqualTo("foopass"));
	}
	
	// BatchUpsertableRepository
	
	@Test
	public void testSaveAll() {
		// setup
		List<User> users = Arrays.asList(
				FOO,
				BAR,
				BAZ);
		// exercise
		Iterable<User> actual = repo.saveAll(users);
		// verify
		assertThat(actual).hasSize(3);
		assertThat(repo.count()).isEqualTo(3);
	}
	
	@Test
	public void testSaveAll_DuplicateKey_OverWrited() {
		repo.create(FOO);
		List<User> users = Arrays.asList(ANOTHER_FOO, BAR, BAZ);
		// exercise
		Iterable<User> actual = repo.saveAll(users);
		// verify
		assertThat(actual).hasSize(3);
		assertThat(repo.count()).isEqualTo(3);
		assertThat(repo.findById("foo")).hasValueSatisfying(foo -> assertThat(foo.getPassword()).isEqualTo("hoge"));
	}
	
	// BatchDeletableRepository
	
	@Test
	public void testDeleteAll_Iterable() {
		// setup
		User foo = repo.save(FOO);
		repo.save(BAR);
		User baz = repo.save(BAZ);
		// exercise
		repo.deleteAll(Arrays.asList(foo, baz));
		// verify
		assertThat(repo.count()).isEqualTo(1);
	}
	
	@Test
	public void testDeleteAll_IterableEmpty() {
		// setup
		repo.save(FOO);
		repo.save(BAR);
		repo.save(BAZ);
		// exercise
		repo.deleteAll(Collections.emptySet());
		// verify
		assertThat(repo.count()).isEqualTo(3);
	}
	
	@Test
	public void testDeleteAll_IterableContainsAbsent() {
		// setup
		User foo = repo.save(FOO);
		User bar = repo.save(BAR);
		repo.save(BAZ);
		// exercise
		repo.deleteAll(Arrays.asList(foo, bar, ANOTHER_FOO));
		// verify
		assertThat(repo.count()).isEqualTo(1);
	}
	
	@Test
	public void testDeleteAllById_Iterable() {
		// setup
		User foo = repo.save(FOO);
		User bar = repo.save(BAR);
		User baz = repo.save(BAZ);
		// exercise
		Iterable<User> actual = repo.deleteAllById(Arrays.asList("foo", "baz"));
		// verify
		assertThat(actual).hasSize(2).containsExactlyInAnyOrder(foo, baz).doesNotContain(bar);
		assertThat(repo.count()).isEqualTo(1);
	}
	
	@Test
	public void testDeleteAllById_IterableEmpty() {
		// setup
		repo.save(FOO);
		repo.save(BAR);
		repo.save(BAZ);
		// exercise
		Iterable<User> actual = repo.deleteAllById(Collections.emptySet());
		// verify
		assertThat(actual).isEmpty();
		assertThat(repo.count()).isEqualTo(3);
	}
	
	@Test
	public void testDeleteAllById_IterableContainsAbsent() {
		// setup
		User foo = repo.save(FOO);
		User bar = repo.save(BAR);
		User baz = repo.save(BAZ);
		// exercise
		Iterable<User> actual = repo.deleteAllById(Arrays.asList("foo", "bar", "absent"));
		// verify
		assertThat(actual).hasSize(2).containsExactlyInAnyOrder(foo, bar).doesNotContain(baz);
		assertThat(repo.count()).isEqualTo(1);
	}
	
	// TODO LockableCrudRepository
}

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
package jp.xet.springframework.data.mirage.repository.autonum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.polycreo.chunkrequests.ChunkRequest;
import org.polycreo.chunkrequests.Chunkable;
import org.polycreo.chunkrequests.Direction;
import org.polycreo.chunks.Chunk;
import org.polycreo.chunks.ChunkFactory;

import jp.xet.springframework.data.mirage.repository.MirageConfiguration;

/**
 * Test for {@link AutonumEntityRepository}.
 *
 * @author daisuke
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MirageConfiguration.class)
@Transactional
@Slf4j
@SuppressWarnings("javadoc")
public class AutonumEntityRepositoryTest {
	
	@Autowired
	AutonumEntityRepository repo;
	
	ChunkFactory chunkFactory = new ChunkFactory();
	
	// ReadableRepository
	
	
	@Test
	@Rollback
	public void testExistsById() {
		// setup
		AutonumEntity foo = repo.create(new AutonumEntity("foo"));
		// exercise
		boolean actual = repo.existsById(foo.getId());
		// verify
		assertThat(actual).isTrue();
	}
	
	@Test
	public void testExistsById_Absent() {
		// exercise
		boolean actual = repo.existsById(ThreadLocalRandom.current().nextLong());
		// verify
		assertThat(actual).isFalse();
	}
	
	@Test
	@Rollback
	public void testFindById() {
		// setup
		AutonumEntity foo = repo.create(new AutonumEntity("foo"));
		AutonumEntity bar = repo.create(new AutonumEntity("bar"));
		repo.create(new AutonumEntity("foo2"));
		repo.create(new AutonumEntity("foo3"));
		repo.create(new AutonumEntity("bar2"));
		repo.create(new AutonumEntity("bar3"));
		
		// exercise
		Optional<AutonumEntity> actualFoo = repo.findById(foo.getId());
		Optional<AutonumEntity> actualBar = repo.findById(bar.getId());
		
		// verify
		assertThat(actualFoo).hasValueSatisfying(found -> assertThat(found)
			.returns(foo.getId(), AutonumEntity::getId)
			.returns("foo", AutonumEntity::getStr));
		
		assertThat(actualBar).hasValueSatisfying(found -> assertThat(found)
			.returns(bar.getId(), AutonumEntity::getId)
			.returns("bar", AutonumEntity::getStr));
	}
	
	@Test
	public void testFindById_Absent_Empty() {
		// exercise
		Optional<AutonumEntity> actual = repo.findById(ThreadLocalRandom.current().nextLong());
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
		repo.save(new AutonumEntity("foo"));
		// exercise
		long actual2 = repo.count();
		// verify
		assertThat(actual2).isEqualTo(1);
		
		// setup
		repo.save(new AutonumEntity("bar"));
		// exercise
		long actual3 = repo.count();
		// verify
		assertThat(actual3).isEqualTo(2);
	}
	
	@Test
	public void testFindAll() {
		// setup
		repo.save(new AutonumEntity("foo"));
		repo.save(new AutonumEntity("bar"));
		// exercise
		Iterable<AutonumEntity> actual = repo.findAll();
		// verify
		assertThat(actual).hasSize(2).extracting("str").containsExactlyInAnyOrder("foo", "bar");
	}
	
	@Test
	public void testFindAll_Empty() {
		// exercise
		Iterable<AutonumEntity> actual = repo.findAll();
		// verify
		assertThat(actual).isEmpty();
	}
	
	@Test
	public void testFindAll_SortAsc() {
		// setup
		repo.save(new AutonumEntity("foo"));
		repo.save(new AutonumEntity("bar"));
		// exercise
		List<AutonumEntity> actual = repo.findAll(Sort.by(Sort.Direction.ASC, "id"));
		// verify
		assertThat(actual).hasSize(2).extracting("str").containsExactly("foo", "bar");
	}
	
	@Test
	public void testFindAll_SortDesc() {
		// setup
		repo.save(new AutonumEntity("foo"));
		repo.save(new AutonumEntity("bar"));
		// exercise
		List<AutonumEntity> actual = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
		// verify
		assertThat(actual).hasSize(2).extracting("str").containsExactly("bar", "foo");
	}
	
	// CreatableRepository
	
	@Test
	public void testCreate() {
		AutonumEntity foo = new AutonumEntity("foo");
		long idBeforeCreate = foo.getId();
		// exercise
		AutonumEntity actual = repo.create(foo);
		// verify
		assertThat(foo.getId()).isEqualTo(idBeforeCreate); // not changed
		assertThat(actual.getId()).isNotEqualTo(foo.getId()); // auto numbered
		assertThat(actual.getStr()).isEqualTo(foo.getStr());
		assertThat(repo.findById(actual.getId())).hasValue(actual);
	}
	
	@Test
	@Ignore("Does CreatableRepository not support GenerationType.IDENTITY?")
	public void testCreate_DuplicateKey_DuplicateKeyException() {
		repo.create(new AutonumEntity("foo"));
		AutonumEntity foo2 = new AutonumEntity("foo");
		// exercise
		Throwable actual = catchThrowable(() -> repo.create(foo2));
		// verify
		assertThat(actual).isInstanceOf(DuplicateKeyException.class);
		assertThat(repo.count()).isEqualTo(1);
	}
	
	// UpsertableRepository
	
	@Test
	public void testSave() {
		long id = Math.abs(ThreadLocalRandom.current().nextLong());
		AutonumEntity foo = new AutonumEntity("foo")
			.setId(id);
		// exercise
		AutonumEntity actual = repo.save(foo);
		// verify
		assertThat(foo.getId()).isEqualTo(id); // not changed
		assertThat(actual.getId()).isNotEqualTo(foo.getId()); // auto numbered
		assertThat(actual.getStr()).isEqualTo(foo.getStr());
		assertThat(repo.findById(actual.getId())).hasValue(actual);
	}
	
	@Test
	public void testSave_DuplicateKey_OverWrited() {
		AutonumEntity created = repo.create(new AutonumEntity("foo"));
		AutonumEntity toSave = new AutonumEntity("bar").setId(created.getId());
		// exercise
		AutonumEntity actual = repo.save(toSave);
		// verify
		assertThat(actual).isEqualTo(toSave);
		assertThat(actual.getId()).isEqualTo(created.getId());
		assertThat(repo.findById(created.getId())).hasValue(toSave);
		assertThat(repo.count()).isEqualTo(1);
	}
	
	// UpdatableRepository
	
	@Test
	public void testUpdate() {
		assertThat(repo.count()).isEqualTo(0);
		AutonumEntity entity = repo.create(new AutonumEntity("foo"));
		assertThat(repo.count()).isEqualTo(1);
		entity.setStr("bar");
		// exercise
		AutonumEntity actual = repo.update(entity);
		// verify
		assertThat(actual).isEqualTo(entity);
		assertThat(repo.count()).isEqualTo(1);
		assertThat(repo.findById(entity.getId())).hasValueSatisfying(found -> assertThat(found)
			.returns("bar", AutonumEntity::getStr));
	}
	
	@Test
	public void testUpdate_Absent_IncorrectResultSizeDataAccessException() {
		assertThat(repo.count()).isEqualTo(0);
		AutonumEntity newImage = new AutonumEntity("foo");
		// exercise
		Throwable actual = catchThrowable(() -> repo.update(newImage));
		// verify
		assertThat(actual).isInstanceOf(IncorrectResultSizeDataAccessException.class);
		assertThat(repo.count()).isEqualTo(0);
	}
	
	// BatchReadableRepository
	
	@Test
	public void testFindAll_Iterable() {
		// setup
		AutonumEntity foo = repo.save(new AutonumEntity("foo"));
		AutonumEntity bar = repo.save(new AutonumEntity("bar"));
		AutonumEntity baz = repo.save(new AutonumEntity("baz"));
		// exercise
		Iterable<AutonumEntity> actual = repo.findAll(Arrays.asList(foo.getId(), baz.getId()));
		// verify
		assertThat(actual).hasSize(2).containsExactlyInAnyOrder(foo, baz).doesNotContain(bar);
	}
	
	@Test
	public void testFindAll_IterableEmpty() {
		// setup
		repo.save(new AutonumEntity("foo"));
		repo.save(new AutonumEntity("bar"));
		repo.save(new AutonumEntity("baz"));
		// exercise
		Iterable<AutonumEntity> actual = repo.findAll(Collections.emptySet());
		// verify
		assertThat(actual).isEmpty();
	}
	
	@Test
	public void testFindAll_IterableContainsAbsent() {
		// setup
		long id = ThreadLocalRandom.current().nextLong();
		AutonumEntity foo = repo.save(new AutonumEntity("foo"));
		AutonumEntity bar = repo.save(new AutonumEntity("bar"));
		AutonumEntity baz = repo.save(new AutonumEntity("baz"));
		// exercise
		Iterable<AutonumEntity> actual = repo.findAll(Arrays.asList(foo.getId(), bar.getId(), id));
		// verify
		assertThat(actual).hasSize(2).containsExactlyInAnyOrder(foo, bar).doesNotContain(baz);
	}
	
	// ChunkableRepository
	
	@Test
	@Rollback
	public void testFindAll_ChunkRequestAsc() {
		// setup
		assertThat(repo.count()).isEqualTo(0);
		repo.create(new AutonumEntity("foo"));
		repo.create(new AutonumEntity("bar"));
		repo.create(new AutonumEntity("baz"));
		repo.create(new AutonumEntity("qux"));
		repo.create(new AutonumEntity("quux"));
		repo.create(new AutonumEntity("courge"));
		repo.create(new AutonumEntity("grault"));
		repo.create(new AutonumEntity("garply"));
		
		Chunkable chunkable = new ChunkRequest(2);
		// exercise
		List<AutonumEntity> list = repo.findAll(chunkable);
		// verify
		assertThat(list).hasSize(2)
			.extracting("str").containsExactly("foo", "bar");
		Chunk<AutonumEntity> chunk = chunkFactory.createChunk(list, chunkable);
		
		// exercise
		list = repo.findAll(chunk.nextChunkable());
		// verify
		assertThat(list).hasSize(2)
			.extracting("str").containsExactly("baz", "qux");
	}
	
	@Test
	@Rollback
	public void testFindAll_ChunkRequestDesc() {
		// setup
		assertThat(repo.count()).isEqualTo(0);
		repo.create(new AutonumEntity("foo"));
		repo.create(new AutonumEntity("bar"));
		repo.create(new AutonumEntity("baz"));
		repo.create(new AutonumEntity("qux"));
		repo.create(new AutonumEntity("quux"));
		repo.create(new AutonumEntity("courge"));
		repo.create(new AutonumEntity("grault"));
		repo.create(new AutonumEntity("garply"));
		
		Chunkable chunkable = new ChunkRequest(2, Direction.DESC);
		// exercise
		List<AutonumEntity> list = repo.findAll(chunkable);
		// verify
		assertThat(list).hasSize(2)
			.extracting("str").containsExactly("garply", "grault");
		Chunk<AutonumEntity> chunk = chunkFactory.createChunk(list, chunkable);
		
		// exercise
		list = repo.findAll(chunk.nextChunkable());
		assertThat(list).hasSize(2)
			.extracting("str").containsExactly("courge", "quux");
	}
	
	// PageableRepository
	
	@Test
	@Rollback
	public void testFindAll_PageRequest() {
		// setup
		assertThat(repo.count()).isEqualTo(0);
		repo.create(new AutonumEntity("foo"));
		repo.create(new AutonumEntity("bar"));
		repo.create(new AutonumEntity("baz"));
		repo.create(new AutonumEntity("qux"));
		repo.create(new AutonumEntity("quux"));
		repo.create(new AutonumEntity("courge"));
		repo.create(new AutonumEntity("grault"));
		repo.create(new AutonumEntity("garply"));
		
		Pageable pageable = PageRequest.of(0/*zero based*/, 2);
		// exercise
		Page<AutonumEntity> page = repo.findAll(pageable);
		// verify
		assertThat(page.getNumber()).isEqualTo(0);
		assertThat(page.getSize()).isEqualTo(2);
		assertThat(page.getNumberOfElements()).isEqualTo(2);
		assertThat(page.hasContent()).isTrue();
		assertThat(page.isFirst()).isTrue();
		assertThat(page.isLast()).isFalse();
		assertThat(page.hasNext()).isTrue();
		assertThat(page.hasPrevious()).isFalse();
		assertThat(page.getTotalPages()).isEqualTo(4);
		assertThat(page.getTotalElements()).isEqualTo(8);
		assertThat(page.getContent()).hasSize(2)
			.extracting("str").containsExactly("foo", "bar");
		
		// setup
		pageable = page.nextPageable();
		// exercise
		page = repo.findAll(pageable);
		// verify
		assertThat(page.getNumber()).isEqualTo(1);
		assertThat(page.getSize()).isEqualTo(2);
		assertThat(page.getNumberOfElements()).isEqualTo(2);
		assertThat(page.hasContent()).isTrue();
		assertThat(page.isFirst()).isFalse();
		assertThat(page.isLast()).isFalse();
		assertThat(page.hasNext()).isTrue();
		assertThat(page.hasPrevious()).isTrue();
		assertThat(page.getTotalPages()).isEqualTo(4);
		assertThat(page.getTotalElements()).isEqualTo(8);
		assertThat(page.getContent()).hasSize(2)
			.extracting("str").containsExactly("baz", "qux");
	}
	
	// Custom
	
	@Test
	@Rollback
	public void testFindByStr() {
		// setup
		assertThat(repo.count()).isEqualTo(0);
		repo.create(new AutonumEntity("foo"));
		repo.create(new AutonumEntity("foo"));
		repo.create(new AutonumEntity("bar"));
		repo.create(new AutonumEntity("baz"));
		
		List<AutonumEntity> foundFoos = repo.findByStr("foo");
		assertThat(foundFoos).hasSize(2);
		
		List<AutonumEntity> foundStartsWithFoos = repo.findByStrStartsWith("ba");
		assertThat(foundStartsWithFoos).hasSize(2);
		
		List<AutonumEntity> foundQux = repo.findByStr("qux");
		assertThat(foundQux).isEmpty();
	}
	
	@Test
	@Rollback
	public void testFindXxx() {
		repo.create(new AutonumEntity("hoge"));
		repo.create(new AutonumEntity("fuga"));
		
		List<AutonumEntity> foundXxx = repo.findXxx();
		assertThat(foundXxx).hasSize(1);
	}
}

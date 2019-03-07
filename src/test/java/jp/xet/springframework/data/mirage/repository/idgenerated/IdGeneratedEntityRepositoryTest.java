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
package jp.xet.springframework.data.mirage.repository.idgenerated;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ws2ten1.chunks.Chunk;
import org.ws2ten1.chunks.ChunkRequest;
import org.ws2ten1.chunks.Chunkable;

import com.google.common.collect.Iterables;

import jp.xet.springframework.data.mirage.repository.TestConfiguration;

/**
 * Test for {@link IdGeneratedEntityRepository}.
 *
 * @author daisuke
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@Transactional
@Slf4j
@SuppressWarnings("javadoc")
public class IdGeneratedEntityRepositoryTest {
	
	@Autowired
	IdGeneratedEntityRepository repo;
	
	
	@Test
	@Rollback
	public void test_create_and_findOne() {
		assertThat(repo, is(notNullValue()));
		assertThat(repo.count(), is(0L));
		IdGeneratedEntity foo = repo.create(new IdGeneratedEntity("foo"));
		assertThat(repo.count(), is(1L));
		IdGeneratedEntity bar = repo.create(new IdGeneratedEntity("bar"));
		assertThat(repo.count(), is(2L));
		repo.save(new IdGeneratedEntity("foo"));
		repo.save(new IdGeneratedEntity("foo2"));
		repo.save(new IdGeneratedEntity("foo3"));
		repo.save(new IdGeneratedEntity("bar2"));
		repo.save(new IdGeneratedEntity("bar3"));
		
		IdGeneratedEntity foundFoo = repo.findById(foo.getId()).orElse(null);
		assertThat(foundFoo.getId(), is(foo.getId()));
		assertThat(foundFoo.getStr(), is("foo"));
		
		IdGeneratedEntity foundBar = repo.findById(bar.getId()).orElse(null);
		assertThat(foundBar.getId(), is(bar.getId()));
		assertThat(foundBar.getStr(), is("bar"));
		
		Iterable<IdGeneratedEntity> all = repo.findAll();
		assertThat(Iterables.size(all), is(7));
	}
	
	@Ignore
	@Test(expected = DuplicateKeyException.class)
	public void test_fail_to_create() {
		assertThat(repo, is(notNullValue()));
		assertThat(repo.count(), is(0L));
		IdGeneratedEntity foo = repo.create(new IdGeneratedEntity("foo"));
		assertThat(repo.count(), is(1L));
		foo.setStr("bar");
		repo.create(foo);
	}
	
	@Test(expected = IncorrectResultSizeDataAccessException.class)
	public void test_fail_to_update() {
		assertThat(repo, is(notNullValue()));
		assertThat(repo.count(), is(0L));
		repo.update(new IdGeneratedEntity("foo"));
	}
	
	@Test
	public void test_save_and_findOne() {
		assertThat(repo, is(notNullValue()));
		assertThat(repo.count(), is(0L));
		IdGeneratedEntity foo = repo.save(new IdGeneratedEntity("foo"));
		assertThat(repo.count(), is(1L));
		IdGeneratedEntity bar = repo.save(new IdGeneratedEntity("bar"));
		assertThat(repo.count(), is(2L));
		repo.save(new IdGeneratedEntity("foo"));
		repo.save(new IdGeneratedEntity("foo2"));
		repo.save(new IdGeneratedEntity("foo3"));
		repo.save(new IdGeneratedEntity("bar2"));
		repo.save(new IdGeneratedEntity("bar3"));
		
		IdGeneratedEntity foundFoo = repo.findById(foo.getId()).orElse(null);
		assertThat(foundFoo.getId(), is(foo.getId()));
		assertThat(foundFoo.getStr(), is("foo"));
		
		IdGeneratedEntity foundBar = repo.findById(bar.getId()).orElse(null);
		assertThat(foundBar.getId(), is(bar.getId()));
		assertThat(foundBar.getStr(), is("bar"));
		
		Iterable<IdGeneratedEntity> all = repo.findAll();
		assertThat(Iterables.size(all), is(7));
	}
	
	@Test
	@Rollback
	public void test_update_and_findOne() {
		assertThat(repo, is(notNullValue()));
		assertThat(repo.count(), is(0L));
		IdGeneratedEntity foo = repo.create(new IdGeneratedEntity("foo"));
		assertThat(repo.count(), is(1L));
		foo.setStr("bar");
		repo.update(foo);
		assertThat(repo.count(), is(1L));
		
		IdGeneratedEntity foundFoo = repo.findById(foo.getId()).orElse(null);
		assertThat(foundFoo.getId(), is(foo.getId()));
		assertThat(foundFoo.getStr(), is("bar"));
	}
	
	@Test
	@Rollback
	public void test_create_and_findChunk() {
		assertThat(repo.count(), is(0L));
		IdGeneratedEntity foo = repo.create(new IdGeneratedEntity("foo"));
		assertThat(repo.count(), is(1L));
		Chunk<IdGeneratedEntity> chunk = repo.findChunk();
		assertThat(chunk.getContent().size(), is(1));
		
		IdGeneratedEntity foundFoo = chunk.iterator().next();
		assertThat(foundFoo.getId(), is(foo.getId()));
		assertThat(foundFoo.getStr(), is("foo"));
	}
	
	@Test
	@Rollback
	public void testChunking_8items_ASC() {
		assertThat(repo.count(), is(0L));
		repo.save(new IdGeneratedEntity("foo"));
		repo.save(new IdGeneratedEntity("bar"));
		repo.save(new IdGeneratedEntity("baz"));
		repo.save(new IdGeneratedEntity("qux"));
		repo.save(new IdGeneratedEntity("quux"));
		repo.save(new IdGeneratedEntity("courge"));
		repo.save(new IdGeneratedEntity("grault"));
		repo.save(new IdGeneratedEntity("garply"));
		
		List<IdGeneratedEntity> list = new ArrayList<IdGeneratedEntity>();
		
		Chunkable req = new ChunkRequest(2);
		Chunk<IdGeneratedEntity> chunk = repo.findAll(req);
		assertThat(chunk, is(notNullValue()));
		log.info("{}", chunk.getContent());
		assertThat(chunk.getContent(), hasSize(2));
		assertThat(chunk.getContent().toString(), is("[IdGeneratedEntity(str=foo), IdGeneratedEntity(str=bar)]"));
		list.addAll(chunk.getContent());
		
		do {
			req = chunk.nextChunkable();
			chunk = repo.findAll(req);
			assertThat(chunk, is(notNullValue()));
			log.info("{}", chunk.getContent());
			assertThat(chunk.getContent(), hasSize(lessThanOrEqualTo(2)));
			list.addAll(chunk.getContent());
		} while (chunk.hasNext());
		assertThat(list, hasSize(8));
	}
	
	@Test
	@Rollback
	public void testChunking_ASC() {
		assertThat(repo.count(), is(0L));
		repo.save(new IdGeneratedEntity("foo"));
		repo.save(new IdGeneratedEntity("bar"));
		repo.save(new IdGeneratedEntity("baz"));
		repo.save(new IdGeneratedEntity("qux"));
		repo.save(new IdGeneratedEntity("quux"));
		repo.save(new IdGeneratedEntity("courge"));
		repo.save(new IdGeneratedEntity("grault"));
		
		List<IdGeneratedEntity> list = new ArrayList<IdGeneratedEntity>();
		
		log.info("==== 1st chunk");
		Chunkable req = new ChunkRequest(2);
		Chunk<IdGeneratedEntity> chunk = repo.findAll(req);
		assertThat(chunk, is(notNullValue()));
		log.info("{}", chunk.getContent());
		assertThat(chunk.getContent(), hasSize(2));
		assertThat(chunk.getContent().toString(), is("[IdGeneratedEntity(str=foo), IdGeneratedEntity(str=bar)]"));
		list.addAll(chunk.getContent());
		
		log.info("==== 2nd chunk");
		req = chunk.nextChunkable();
		chunk = repo.findAll(req);
		assertThat(chunk, is(notNullValue()));
		log.info("{}", chunk.getContent());
		assertThat(chunk.getContent(), hasSize(2));
		assertThat(chunk.getContent().toString(), is("[IdGeneratedEntity(str=baz), IdGeneratedEntity(str=qux)]"));
		list.addAll(chunk.getContent());
		
		do {
			log.info("==== following chunks");
			req = chunk.nextChunkable();
			chunk = repo.findAll(req);
			assertThat(chunk, is(notNullValue()));
			log.info("{}", chunk.getContent());
			assertThat(chunk.getContent(), hasSize(lessThanOrEqualTo(2)));
			list.addAll(chunk.getContent());
		} while (chunk.hasNext());
		assertThat(chunk.getContent(), hasSize(1));
		assertThat(chunk.getContent().toString(), is("[IdGeneratedEntity(str=grault)]"));
		assertThat(list, hasSize(7));
		
		log.info("==== previous chunk");
		req = chunk.prevChunkable();
		chunk = repo.findAll(req);
		assertThat(chunk, is(notNullValue()));
		log.info("{}", chunk.getContent());
		assertThat(chunk.getContent(), hasSize(2));
		assertThat(chunk.getContent().toString(), is("[IdGeneratedEntity(str=quux), IdGeneratedEntity(str=courge)]"));
	}
	
	@Test
	@Rollback
	public void testChunking_DESC() {
		assertThat(repo.count(), is(0L));
		repo.save(new IdGeneratedEntity("foo"));
		repo.save(new IdGeneratedEntity("bar"));
		repo.save(new IdGeneratedEntity("baz"));
		repo.save(new IdGeneratedEntity("qux"));
		repo.save(new IdGeneratedEntity("quux"));
		repo.save(new IdGeneratedEntity("courge"));
		repo.save(new IdGeneratedEntity("grault"));
		
		List<IdGeneratedEntity> list = new ArrayList<IdGeneratedEntity>();
		
		log.info("==== 1st chunk");
		Chunkable req = new ChunkRequest(2, Direction.DESC);
		Chunk<IdGeneratedEntity> chunk = repo.findAll(req);
		assertThat(chunk, is(notNullValue()));
		log.info("{}", chunk.getContent());
		assertThat(chunk.getContent(), hasSize(2));
		assertThat(chunk.getContent().toString(), is("[IdGeneratedEntity(str=grault), IdGeneratedEntity(str=courge)]"));
		list.addAll(chunk.getContent());
		
		log.info("==== 2nd chunk");
		req = chunk.nextChunkable();
		chunk = repo.findAll(req);
		assertThat(chunk, is(notNullValue()));
		log.info("{}", chunk.getContent());
		assertThat(chunk.getContent(), hasSize(2));
		assertThat(chunk.getContent().toString(), is("[IdGeneratedEntity(str=quux), IdGeneratedEntity(str=qux)]"));
		list.addAll(chunk.getContent());
		
		do {
			log.info("==== following chunks");
			req = chunk.nextChunkable();
			chunk = repo.findAll(req);
			assertThat(chunk, is(notNullValue()));
			log.info("{}", chunk.getContent());
			assertThat(chunk.getContent(), hasSize(lessThanOrEqualTo(2)));
			list.addAll(chunk.getContent());
		} while (chunk.hasNext());
		assertThat(chunk.getContent(), hasSize(1));
		assertThat(chunk.getContent().toString(), is("[IdGeneratedEntity(str=foo)]"));
		assertThat(list, hasSize(7));
		
		log.info("==== previous chunk");
		req = chunk.prevChunkable();
		chunk = repo.findAll(req);
		assertThat(chunk, is(notNullValue()));
		log.info("{}", chunk.getContent());
		assertThat(chunk.getContent(), hasSize(2));
		assertThat(chunk.getContent().toString(), is("[IdGeneratedEntity(str=baz), IdGeneratedEntity(str=bar)]"));
	}
	
	@Test
	@Rollback
	public void testPaging() {
		repo.save(new IdGeneratedEntity("foo"));
		repo.save(new IdGeneratedEntity("bar"));
		repo.save(new IdGeneratedEntity("foo"));
		repo.save(new IdGeneratedEntity("foo2"));
		repo.save(new IdGeneratedEntity("foo3"));
		repo.save(new IdGeneratedEntity("bar2"));
		repo.save(new IdGeneratedEntity("bar3"));
		
		Page<IdGeneratedEntity> page1 = repo.findAll(PageRequest.of(1/*zero based*/, 2, Direction.ASC, "str"));
		assertThat(page1.getNumber(), is(1));
		assertThat(page1.getNumberOfElements(), is(2));
		assertThat(page1.getTotalElements(), is(7L));
		assertThat(page1.getContent().size(), is(2));
		assertThat(page1.getContent().get(0).getStr(), is("bar3"));
		assertThat(page1.getContent().get(1).getStr(), is("foo"));
		
		Page<IdGeneratedEntity> page2 = repo.findAll(PageRequest.of(2/*zero based*/, 2, Direction.ASC, "str"));
		assertThat(page2.getNumber(), is(2));
		assertThat(page2.getNumberOfElements(), is(2));
		assertThat(page2.getTotalElements(), is(7L));
		assertThat(page2.getContent().size(), is(2));
		assertThat(page2.getContent().get(0).getStr(), is("foo"));
		assertThat(page2.getContent().get(1).getStr(), is("foo2"));
		
		List<IdGeneratedEntity> foundFoos = repo.findByStr("foo");
		assertThat(foundFoos.size(), is(2));
		
		List<IdGeneratedEntity> foundStartsWithFoos = repo.findByStrStartsWith("foo");
		assertThat(foundStartsWithFoos.size(), is(4));
		
		List<IdGeneratedEntity> foundBars = repo.findByStr("bar");
		assertThat(foundBars.size(), is(1));
		
		List<IdGeneratedEntity> foundStartsWithBars = repo.findByStrStartsWith("bar");
		assertThat(foundStartsWithBars.size(), is(3));
		
		List<IdGeneratedEntity> foundQux = repo.findByStr("qux");
		assertThat(foundQux.size(), is(0));
	}
	
	@Test
	@Rollback
	public void testFindXxx() {
		repo.save(new IdGeneratedEntity("hoge"));
		repo.save(new IdGeneratedEntity("fuga"));
		
		List<IdGeneratedEntity> foundXxx = repo.findXxx();
		assertThat(foundXxx.size(), is(1));
	}
}

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
package jp.xet.springframework.data.mirage.repository.example;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Iterables;

import jp.xet.sparwings.spring.data.chunk.Chunk;
import jp.xet.sparwings.spring.data.chunk.ChunkRequest;
import jp.xet.sparwings.spring.data.chunk.Chunkable;

import jp.xet.springframework.data.mirage.repository.TestConfiguration;

/**
 * Test for {@link EntityRepository}.
 * 
 * @author daisuke
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@Transactional
@SuppressWarnings("javadoc")
public class EntityRepositoryTest {
	
	private static Logger log = LoggerFactory.getLogger(EntityRepositoryTest.class);
	
	@Autowired
	EntityRepository repo;
	
	
	@Test
	@Rollback
	public void test_create_and_findOne() {
		assertThat(repo, is(notNullValue()));
		assertThat(repo.count(), is(0L));
		Entity foo = repo.create(new Entity("foo"));
		assertThat(repo.count(), is(1L));
		Entity bar = repo.create(new Entity("bar"));
		assertThat(repo.count(), is(2L));
		repo.save(new Entity("foo"));
		repo.save(new Entity("foo2"));
		repo.save(new Entity("foo3"));
		repo.save(new Entity("bar2"));
		repo.save(new Entity("bar3"));
		
		Entity foundFoo = repo.findOne(foo.getId());
		assertThat(foundFoo.getId(), is(foo.getId()));
		assertThat(foundFoo.getStr(), is("foo"));
		
		Entity foundBar = repo.findOne(bar.getId());
		assertThat(foundBar.getId(), is(bar.getId()));
		assertThat(foundBar.getStr(), is("bar"));
		
		Iterable<Entity> all = repo.findAll();
		assertThat(Iterables.size(all), is(7));
	}
	
	@Ignore
	@Test(expected = DuplicateKeyException.class)
	public void test_fail_to_create() {
		assertThat(repo, is(notNullValue()));
		assertThat(repo.count(), is(0L));
		Entity foo = repo.create(new Entity("foo"));
		assertThat(repo.count(), is(1L));
		foo.setStr("bar");
		repo.create(foo);
	}
	
	@Test(expected = IncorrectResultSizeDataAccessException.class)
	public void test_fail_to_update() {
		assertThat(repo, is(notNullValue()));
		assertThat(repo.count(), is(0L));
		repo.update(new Entity("foo"));
	}
	
	@Test
	public void test_save_and_findOne() {
		assertThat(repo, is(notNullValue()));
		assertThat(repo.count(), is(0L));
		Entity foo = repo.save(new Entity("foo"));
		assertThat(repo.count(), is(1L));
		Entity bar = repo.save(new Entity("bar"));
		assertThat(repo.count(), is(2L));
		repo.save(new Entity("foo"));
		repo.save(new Entity("foo2"));
		repo.save(new Entity("foo3"));
		repo.save(new Entity("bar2"));
		repo.save(new Entity("bar3"));
		
		Entity foundFoo = repo.findOne(foo.getId());
		assertThat(foundFoo.getId(), is(foo.getId()));
		assertThat(foundFoo.getStr(), is("foo"));
		
		Entity foundBar = repo.findOne(bar.getId());
		assertThat(foundBar.getId(), is(bar.getId()));
		assertThat(foundBar.getStr(), is("bar"));
		
		Iterable<Entity> all = repo.findAll();
		assertThat(Iterables.size(all), is(7));
	}
	
	@Test
	@Rollback
	public void test_update_and_findOne() {
		assertThat(repo, is(notNullValue()));
		assertThat(repo.count(), is(0L));
		Entity foo = repo.create(new Entity("foo"));
		assertThat(repo.count(), is(1L));
		Entity bar = foo;
		bar.setStr("bar");
		bar = repo.update(bar);
		assertThat(repo.count(), is(1L));
		
		Entity foundFoo = repo.findOne(foo.getId());
		assertThat(foundFoo.getId(), is(foo.getId()));
		assertThat(foundFoo.getStr(), is("bar"));
	}
	
	@Test
	@Rollback
	public void test_create_and_findChunk() {
		assertThat(repo.count(), is(0L));
		Entity foo = repo.create(new Entity("foo"));
		Entity bar = repo.create(new Entity("bar"));
		Entity baz = repo.create(new Entity("baz"));
		assertThat(repo.count(), is(3L));
		Chunk<Entity> chunk1 = repo.findChunk(new ChunkRequest(1));
		assertThat(chunk1.getContent().size(), is(1));
		
		Entity foundBar = chunk1.iterator().next();
		assertThat(foundBar.getId(), is(bar.getId()));
		assertThat(foundBar.getStr(), is("bar"));
		
		Chunk<Entity> chunk2 = repo.findChunk(chunk1.nextChunkable());
		assertThat(chunk2.getContent().size(), is(1));
		
		Entity foundBaz = chunk2.iterator().next();
		assertThat(foundBaz.getId(), is(baz.getId()));
		assertThat(foundBaz.getStr(), is("baz"));
		
		Chunk<Entity> chunk3 = repo.findChunk(chunk2.nextChunkable());
		assertThat(chunk3.getContent().isEmpty(), is(true));
		assertThat(chunk3.getPaginationToken(), is(nullValue()));
	}
	
	@Test
	@Rollback
	public void testChunking_8items_ASC() {
		assertThat(repo.count(), is(0L));
		repo.save(new Entity("foo"));
		repo.save(new Entity("bar"));
		repo.save(new Entity("baz"));
		repo.save(new Entity("qux"));
		repo.save(new Entity("quux"));
		repo.save(new Entity("courge"));
		repo.save(new Entity("grault"));
		repo.save(new Entity("garply"));
		
		List<Entity> list = new ArrayList<Entity>();
		
		Chunkable req = new ChunkRequest(2);
		Chunk<Entity> chunk = repo.findAll(req);
		assertThat(chunk, is(notNullValue()));
		log.info("{}", chunk.getContent());
		assertThat(chunk.getContent(), hasSize(2));
		assertThat(chunk.getContent().toString(), is("[Entity[foo], Entity[bar]]"));
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
		repo.save(new Entity("foo"));
		repo.save(new Entity("bar"));
		repo.save(new Entity("baz"));
		repo.save(new Entity("qux"));
		repo.save(new Entity("quux"));
		repo.save(new Entity("courge"));
		repo.save(new Entity("grault"));
		
		List<Entity> list = new ArrayList<Entity>();
		
		log.info("==== 1st chunk");
		Chunkable req = new ChunkRequest(2);
		Chunk<Entity> chunk = repo.findAll(req);
		assertThat(chunk, is(notNullValue()));
		log.info("{}", chunk.getContent());
		assertThat(chunk.getContent(), hasSize(2));
		assertThat(chunk.getContent().toString(), is("[Entity[foo], Entity[bar]]"));
		list.addAll(chunk.getContent());
		
		log.info("==== 2nd chunk");
		req = chunk.nextChunkable();
		chunk = repo.findAll(req);
		assertThat(chunk, is(notNullValue()));
		log.info("{}", chunk.getContent());
		assertThat(chunk.getContent(), hasSize(2));
		assertThat(chunk.getContent().toString(), is("[Entity[baz], Entity[qux]]"));
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
		assertThat(chunk.getContent().toString(), is("[Entity[grault]]"));
		assertThat(list, hasSize(7));
		
		log.info("==== previous chunk");
		req = chunk.prevChunkable();
		chunk = repo.findAll(req);
		assertThat(chunk, is(notNullValue()));
		log.info("{}", chunk.getContent());
		assertThat(chunk.getContent(), hasSize(2));
		assertThat(chunk.getContent().toString(), is("[Entity[quux], Entity[courge]]"));
	}
	
	@Test
	@Rollback
	public void testChunking_DESC() {
		assertThat(repo.count(), is(0L));
		repo.save(new Entity("foo"));
		repo.save(new Entity("bar"));
		repo.save(new Entity("baz"));
		repo.save(new Entity("qux"));
		repo.save(new Entity("quux"));
		repo.save(new Entity("courge"));
		repo.save(new Entity("grault"));
		
		List<Entity> list = new ArrayList<Entity>();
		
		log.info("==== 1st chunk");
		Chunkable req = new ChunkRequest(2, Direction.DESC);
		Chunk<Entity> chunk = repo.findAll(req);
		assertThat(chunk, is(notNullValue()));
		log.info("{}", chunk.getContent());
		assertThat(chunk.getContent(), hasSize(2));
		assertThat(chunk.getContent().toString(), is("[Entity[grault], Entity[courge]]"));
		list.addAll(chunk.getContent());
		
		log.info("==== 2nd chunk");
		req = chunk.nextChunkable();
		chunk = repo.findAll(req);
		assertThat(chunk, is(notNullValue()));
		log.info("{}", chunk.getContent());
		assertThat(chunk.getContent(), hasSize(2));
		assertThat(chunk.getContent().toString(), is("[Entity[quux], Entity[qux]]"));
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
		assertThat(chunk.getContent().toString(), is("[Entity[foo]]"));
		assertThat(list, hasSize(7));
		
		log.info("==== previous chunk");
		req = chunk.prevChunkable();
		chunk = repo.findAll(req);
		assertThat(chunk, is(notNullValue()));
		log.info("{}", chunk.getContent());
		assertThat(chunk.getContent(), hasSize(2));
		assertThat(chunk.getContent().toString(), is("[Entity[baz], Entity[bar]]"));
	}
	
	@Test
	@Rollback
	public void testPaging() {
		repo.save(new Entity("foo"));
		repo.save(new Entity("bar"));
		repo.save(new Entity("foo"));
		repo.save(new Entity("foo2"));
		repo.save(new Entity("foo3"));
		repo.save(new Entity("bar2"));
		repo.save(new Entity("bar3"));
		
		Page<Entity> page1 = repo.findAll(new PageRequest(1/*zero based*/, 2, Direction.ASC, "str"));
		assertThat(page1.getNumber(), is(1));
		assertThat(page1.getNumberOfElements(), is(2));
		assertThat(page1.getTotalElements(), is(7L));
		assertThat(page1.getContent().size(), is(2));
		assertThat(page1.getContent().get(0).getStr(), is("bar3"));
		assertThat(page1.getContent().get(1).getStr(), is("foo"));
		
		Page<Entity> page2 = repo.findAll(new PageRequest(2/*zero based*/, 2, Direction.ASC, "str"));
		assertThat(page2.getNumber(), is(2));
		assertThat(page2.getNumberOfElements(), is(2));
		assertThat(page2.getTotalElements(), is(7L));
		assertThat(page2.getContent().size(), is(2));
		assertThat(page2.getContent().get(0).getStr(), is("foo"));
		assertThat(page2.getContent().get(1).getStr(), is("foo2"));
		
		List<Entity> foundFoos = repo.findByStr("foo");
		assertThat(foundFoos.size(), is(2));
		
		List<Entity> foundStartsWithFoos = repo.findByStrStartsWith("foo");
		assertThat(foundStartsWithFoos.size(), is(4));
		
		List<Entity> foundBars = repo.findByStr("bar");
		assertThat(foundBars.size(), is(1));
		
		List<Entity> foundStartsWithBars = repo.findByStrStartsWith("bar");
		assertThat(foundStartsWithBars.size(), is(3));
		
		List<Entity> foundQux = repo.findByStr("qux");
		assertThat(foundQux.size(), is(0));
	}
	
	@Test
	@Rollback
	public void testFindXxx() {
		repo.save(new Entity("hoge"));
		repo.save(new Entity("fuga"));
		
		List<Entity> foundXxx = repo.findXxx();
		assertThat(foundXxx.size(), is(1));
	}
}

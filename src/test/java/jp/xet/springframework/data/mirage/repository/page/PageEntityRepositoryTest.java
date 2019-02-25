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
package jp.xet.springframework.data.mirage.repository.page;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Ordering;

import jp.xet.springframework.data.mirage.repository.MirageConfiguration;

/**
 * Test for {@link PageEntityRepository}.
 */
@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MirageConfiguration.class)
@Transactional
@SuppressWarnings("javadoc")
public class PageEntityRepositoryTest {
	
	@Autowired
	PageEntityRepository repo;
	
	
	// PageableRepository
	
	@Test
	@Rollback
	public void testPagingAsc() {
		// setup
		for (int i = 0; i < 10; i++) {
			repo.save(new PageEntity(String.format("%03d", i), i));
		}
		List<PageEntity> resultAsc = new ArrayList<>(10);
		
		Pageable requestAsc = PageRequest.of(0, 8);
		do {
			Page<PageEntity> page = repo.findAll(requestAsc);
			resultAsc.addAll(page.getContent());
			requestAsc = page.hasNext() ? page.nextPageable() : null; // NOPMD
		} while (requestAsc != null);
		
		resultAsc.forEach(e -> log.info("{}", e));
		assertThat(resultAsc).hasSize(10);
		assertThat(Ordering.natural().isStrictlyOrdered(resultAsc)).isTrue();
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(resultAsc)).isFalse();
	}
	
	@Test
	@Rollback
	public void testPagingAsc_Under20() {
		for (int i = 0; i < 20; i++) {
			repo.save(new PageEntity(String.format("%03d", i), i));
		}
		
		Pageable requestAsc = PageRequest.of(0, 8);
		Page<PageEntity> page1 = repo.findAll(requestAsc);
		assertThat(page1.hasContent()).isTrue();
		assertThat(page1.isFirst()).isTrue();
		assertThat(page1.isLast()).isFalse();
		assertThat(page1.hasNext()).isTrue();
		assertThat(page1.nextPageable()).isNotNull();
		assertThat(page1.hasPrevious()).isFalse();
		assertThat(page1.previousPageable()).isNotNull().isEqualTo(Pageable.unpaged());
		assertThat(page1.getContent()).hasSize(8);
		assertThat(Ordering.natural().isStrictlyOrdered(page1.getContent())).isTrue();
		assertThat(page1.getContent().get(0)).isEqualTo(new PageEntity("000", 0));
		assertThat(page1.getContent().get(7)).isEqualTo(new PageEntity("007", 7));
		
		Page<PageEntity> page2 = repo.findAll(page1.nextPageable());
		assertThat(page2.hasContent()).isTrue();
		assertThat(page2.isFirst()).isFalse();
		assertThat(page2.isLast()).isFalse();
		assertThat(page2.hasNext()).isTrue();
		assertThat(page2.nextPageable()).isNotNull();
		assertThat(page2.hasPrevious()).isTrue();
		assertThat(page2.previousPageable()).isNotNull();
		assertThat(page2.getContent()).hasSize(8);
		assertThat(Ordering.natural().isStrictlyOrdered(page2.getContent())).isTrue();
		assertThat(page2.getContent().get(0)).isEqualTo(new PageEntity("008", 8));
		assertThat(page2.getContent().get(7)).isEqualTo(new PageEntity("015", 15));
		
		Page<PageEntity> page3 = repo.findAll(page2.nextPageable());
		assertThat(page3.hasContent()).isTrue();
		assertThat(page3.isFirst()).isFalse();
		assertThat(page3.isLast()).isTrue();
		assertThat(page3.hasNext()).isFalse();
		assertThat(page3.nextPageable()).isNotNull().isEqualTo(Pageable.unpaged());
		assertThat(page3.hasPrevious()).isTrue();
		assertThat(page3.previousPageable()).isNotNull();
		assertThat(page3.getContent()).hasSize(4);
		assertThat(Ordering.natural().isStrictlyOrdered(page3.getContent())).isTrue();
		assertThat(page3.getContent().get(0)).isEqualTo(new PageEntity("016", 16));
		assertThat(page3.getContent().get(3)).isEqualTo(new PageEntity("019", 19));
		
		Page<PageEntity> page2again = repo.findAll(page3.previousPageable());
		assertThat(page2again.hasContent()).isTrue();
		assertThat(page2again.isFirst()).isFalse();
		assertThat(page2again.isLast()).isFalse();
		assertThat(page2again.hasNext()).isTrue();
		assertThat(page2again.nextPageable()).isNotNull();
		assertThat(page2again.hasPrevious()).isTrue();
		assertThat(page2again.previousPageable()).isNotNull();
		assertThat(page2again.getContent()).hasSize(8);
		assertThat(Ordering.natural().isStrictlyOrdered(page2again.getContent())).isTrue();
		assertThat(page2again.getContent().get(0)).isEqualTo(new PageEntity("008", 8));
		assertThat(page2again.getContent().get(7)).isEqualTo(new PageEntity("015", 15));
		
		Page<PageEntity> page1again = repo.findAll(page2again.previousPageable());
		assertThat(page1again.hasContent()).isTrue();
		assertThat(page1again.isFirst()).isTrue();
		assertThat(page1again.isLast()).isFalse();
		assertThat(page1again.hasNext()).isTrue();
		assertThat(page1again.nextPageable()).isNotNull();
		assertThat(page1again.hasPrevious()).isFalse();
		assertThat(page1again.previousPageable()).isNotNull();
		assertThat(page1again.getContent()).hasSize(8);
		assertThat(Ordering.natural().isStrictlyOrdered(page1again.getContent())).isTrue();
		assertThat(page1again.getContent().get(0)).isEqualTo(new PageEntity("000", 0));
		assertThat(page1again.getContent().get(7)).isEqualTo(new PageEntity("007", 7));
	}
	
	@Test
	@Rollback
	public void testPagingAsc_Exact20() {
		for (int i = 0; i < 20; i++) {
			repo.save(new PageEntity(String.format("%03d", i), i));
		}
		
		Pageable requestAsc = PageRequest.of(0, 20, Direction.ASC, "id");
		Page<PageEntity> page1 = repo.findAll(requestAsc);
		assertThat(page1.hasContent()).isTrue();
		assertThat(page1.isFirst()).isTrue();
		assertThat(page1.isLast()).isTrue();
		assertThat(page1.hasNext()).isFalse();
		assertThat(page1.nextPageable()).isNotNull().isEqualTo(Pageable.unpaged());
		assertThat(page1.hasPrevious()).isFalse();
		assertThat(page1.previousPageable()).isNotNull().isEqualTo(Pageable.unpaged());
		assertThat(page1.getContent()).hasSize(20);
		assertThat(Ordering.natural().isStrictlyOrdered(page1.getContent())).isTrue();
		assertThat(page1.getContent().get(0)).isEqualTo(new PageEntity("000", 0));
		assertThat(page1.getContent().get(19)).isEqualTo(new PageEntity("019", 19));
	}
	
	@Test
	@Rollback
	public void testPagingAsc_Over20() {
		for (int i = 0; i < 20; i++) {
			repo.save(new PageEntity(String.format("%03d", i), i));
		}
		
		Pageable requestAsc = PageRequest.of(0, 30);
		Page<PageEntity> page = repo.findAll(requestAsc);
		assertThat(page.hasContent()).isTrue();
		assertThat(page.isFirst()).isTrue();
		assertThat(page.isLast()).isTrue();
		assertThat(page.hasNext()).isFalse();
		assertThat(page.nextPageable()).isNotNull().isEqualTo(Pageable.unpaged());
		assertThat(page.hasPrevious()).isFalse();
		assertThat(page.previousPageable()).isNotNull().isEqualTo(Pageable.unpaged());
		assertThat(page.getContent()).hasSize(20);
		assertThat(Ordering.natural().isStrictlyOrdered(page.getContent())).isTrue();
		assertThat(page.getContent().get(0)).isEqualTo(new PageEntity("000", 0));
		assertThat(page.getContent().get(19)).isEqualTo(new PageEntity("019", 19));
	}
	
	@Test
	@Rollback
	public void testPagingDesc() {
		for (int i = 0; i < 100; i++) {
			repo.save(new PageEntity(String.format("%03d", i), i));
		}
		
		List<PageEntity> resultDesc = new ArrayList<>(100);
		
		Pageable requestDesc = PageRequest.of(0, 8, Direction.DESC, "id");
		do {
			Page<PageEntity> page = repo.findAll(requestDesc);
			resultDesc.addAll(page.getContent());
			requestDesc = page.hasNext() ? page.nextPageable() : null; // NOPMD
		} while (requestDesc != null);
		
		resultDesc.forEach(e -> log.info("{}", e));
		assertThat(resultDesc).hasSize(100);
		assertThat(Ordering.natural().isStrictlyOrdered(resultDesc)).isFalse();
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(resultDesc)).isTrue();
	}
	
	@Test
	@Rollback
	public void testPagingDesc_Under20() {
		for (int i = 0; i < 20; i++) {
			repo.save(new PageEntity(String.format("%03d", i), i));
		}
		
		Pageable requestAsc = PageRequest.of(0, 8, Direction.DESC, "id");
		Page<PageEntity> page1 = repo.findAll(requestAsc);
		assertThat(page1.hasContent()).isTrue();
		assertThat(page1.isFirst()).isTrue();
		assertThat(page1.isLast()).isFalse();
		assertThat(page1.hasNext()).isTrue();
		assertThat(page1.nextPageable()).isNotNull();
		assertThat(page1.hasPrevious()).isFalse();
		assertThat(page1.previousPageable()).isNotNull().isEqualTo(Pageable.unpaged());
		assertThat(page1.getContent()).hasSize(8);
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(page1.getContent())).isTrue();
		assertThat(page1.getContent().get(0)).isEqualTo(new PageEntity("019", 19));
		assertThat(page1.getContent().get(7)).isEqualTo(new PageEntity("012", 12));
		
		Page<PageEntity> page2 = repo.findAll(page1.nextPageable());
		assertThat(page2.hasContent()).isTrue();
		assertThat(page2.isFirst()).isFalse();
		assertThat(page2.isLast()).isFalse();
		assertThat(page2.hasNext()).isTrue();
		assertThat(page2.nextPageable()).isNotNull();
		assertThat(page2.hasPrevious()).isTrue();
		assertThat(page2.previousPageable()).isNotNull();
		assertThat(page2.getContent()).hasSize(8);
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(page2.getContent())).isTrue();
		assertThat(page2.getContent().get(0)).isEqualTo(new PageEntity("011", 11));
		assertThat(page2.getContent().get(7)).isEqualTo(new PageEntity("004", 4));
		
		Page<PageEntity> page3 = repo.findAll(page2.nextPageable());
		assertThat(page3.hasContent()).isTrue();
		assertThat(page3.isFirst()).isFalse();
		assertThat(page3.isLast()).isTrue();
		assertThat(page3.hasNext()).isFalse();
		assertThat(page3.nextPageable()).isNotNull().isEqualTo(Pageable.unpaged());
		assertThat(page3.hasPrevious()).isTrue();
		assertThat(page3.previousPageable()).isNotNull();
		assertThat(page3.getContent()).hasSize(4);
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(page3.getContent())).isTrue();
		assertThat(page3.getContent().get(0)).isEqualTo(new PageEntity("003", 3));
		assertThat(page3.getContent().get(3)).isEqualTo(new PageEntity("000", 0));
		
		Page<PageEntity> page2again = repo.findAll(page3.previousPageable());
		assertThat(page2again.hasContent()).isTrue();
		assertThat(page2again.isFirst()).isFalse();
		assertThat(page2again.isLast()).isFalse();
		assertThat(page2again.hasNext()).isTrue();
		assertThat(page2again.nextPageable()).isNotNull();
		assertThat(page2again.hasPrevious()).isTrue();
		assertThat(page2again.previousPageable()).isNotNull();
		assertThat(page2again.getContent()).hasSize(8);
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(page2again.getContent())).isTrue();
		assertThat(page2again.getContent().get(0)).isEqualTo(new PageEntity("011", 11));
		assertThat(page2again.getContent().get(7)).isEqualTo(new PageEntity("004", 4));
		
		Page<PageEntity> page1again = repo.findAll(page2again.previousPageable());
		assertThat(page1again.hasContent()).isTrue();
		assertThat(page1again.isFirst()).isTrue();
		assertThat(page1again.isLast()).isFalse();
		assertThat(page1again.hasNext()).isTrue();
		assertThat(page1again.nextPageable()).isNotNull();
		assertThat(page1again.hasPrevious()).isFalse();
		assertThat(page1again.previousPageable()).isNotNull();
		assertThat(page1again.getContent()).hasSize(8);
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(page1again.getContent())).isTrue();
		assertThat(page1again.getContent().get(0)).isEqualTo(new PageEntity("019", 19));
		assertThat(page1again.getContent().get(7)).isEqualTo(new PageEntity("012", 12));
	}
	
	@Test
	@Rollback
	public void testPagingDesc_Exact20() {
		for (int i = 0; i < 20; i++) {
			repo.save(new PageEntity(String.format("%03d", i), i));
		}
		
		Pageable requestAsc = PageRequest.of(0, 20, Direction.DESC, "id");
		Page<PageEntity> page1 = repo.findAll(requestAsc);
		assertThat(page1.hasContent()).isTrue();
		assertThat(page1.isFirst()).isTrue();
		assertThat(page1.isLast()).isTrue();
		assertThat(page1.hasNext()).isFalse();
		assertThat(page1.nextPageable()).isNotNull().isEqualTo(Pageable.unpaged());
		assertThat(page1.hasPrevious()).isFalse();
		assertThat(page1.previousPageable()).isNotNull().isEqualTo(Pageable.unpaged());
		assertThat(page1.getContent()).hasSize(20);
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(page1.getContent())).isTrue();
		assertThat(page1.getContent().get(0)).isEqualTo(new PageEntity("019", 19));
		assertThat(page1.getContent().get(19)).isEqualTo(new PageEntity("000", 0));
	}
	
	@Test
	@Rollback
	public void testPagingDesc_Over20() {
		for (int i = 0; i < 20; i++) {
			repo.save(new PageEntity(String.format("%03d", i), i));
		}
		
		Pageable requestAsc = PageRequest.of(0, 30, Direction.DESC, "id");
		Page<PageEntity> page = repo.findAll(requestAsc);
		assertThat(page.hasContent()).isTrue();
		assertThat(page.isFirst()).isTrue();
		assertThat(page.isLast()).isTrue();
		assertThat(page.hasNext()).isFalse();
		assertThat(page.nextPageable()).isNotNull().isEqualTo(Pageable.unpaged());
		assertThat(page.hasPrevious()).isFalse();
		assertThat(page.previousPageable()).isNotNull().isEqualTo(Pageable.unpaged());
		assertThat(page.getContent()).hasSize(20);
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(page.getContent())).isTrue();
		assertThat(page.getContent().get(0)).isEqualTo(new PageEntity("019", 19));
		assertThat(page.getContent().get(19)).isEqualTo(new PageEntity("000", 0));
	}
}

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
package jp.xet.springframework.data.mirage.repository;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.miragesql.miragesql.SqlManager;
import com.miragesql.miragesql.SqlManagerImpl;
import com.miragesql.miragesql.bean.BeanDescFactory;
import com.miragesql.miragesql.bean.FieldPropertyExtractor;
import com.miragesql.miragesql.dialect.MySQLDialect;
import com.miragesql.miragesql.integration.spring.SpringConnectionProvider;
import com.miragesql.miragesql.naming.RailsLikeNameConverter;
import com.miragesql.miragesql.provider.ConnectionProvider;

import jp.xet.springframework.data.mirage.repository.config.EnableMirageRepositories;
import jp.xet.springframework.data.mirage.repository.support.MiragePersistenceExceptionTranslator;

/**
 * Test Configuration
 */
@Configuration
@EnableTransactionManagement
@EnableMirageRepositories
public class TestConfiguration {
	
	@Bean
	public SqlManager sqlManager() {
		SqlManagerImpl sqlManagerImpl = new SqlManagerImpl();
		sqlManagerImpl.setConnectionProvider(connectionProvider());
		sqlManagerImpl.setDialect(new MySQLDialect());
		sqlManagerImpl.setBeanDescFactory(beanDescFactory());
		sqlManagerImpl.setNameConverter(new RailsLikeNameConverter());
		return sqlManagerImpl;
	}
	
	@Bean
	public ConnectionProvider connectionProvider() {
		SpringConnectionProvider springConnectionProvider = new SpringConnectionProvider();
		springConnectionProvider.setDataSource(dataSource());
		return springConnectionProvider;
	}
	
	@Bean
	public BeanDescFactory beanDescFactory() {
		BeanDescFactory beanDescFactory = new BeanDescFactory();
		beanDescFactory.setPropertyExtractor(new FieldPropertyExtractor());
		return beanDescFactory;
	}
	
	@Bean
	public MiragePersistenceExceptionTranslator persistenceExceptionTranslator() {
		return new MiragePersistenceExceptionTranslator();
	}
	
	@Bean
	public DataSource dataSource() {
		return new EmbeddedDatabaseBuilder()
			.setType(EmbeddedDatabaseType.H2)
			.addScripts("classpath:create.sql")
			.setName("test")
			.build();
	}
	
	@Bean
	public PlatformTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}
}

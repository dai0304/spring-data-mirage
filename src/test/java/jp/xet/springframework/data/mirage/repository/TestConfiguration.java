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

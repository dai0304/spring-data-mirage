Spring Data Mirage SQL
======================

The primary goal of the [Spring Data](http://www.springsource.org/spring-data) project is to make it easier to build
Spring-powered applications that use data access technologies. This module deals with enhanced support for
[Mirage SQL](https://github.com/takezoe/mirage) based data access layers.

## Features ##
This project defines a `JdbcRepository` base interface  :

```java
public interface JdbcRepository<E, ID extends Serializable> extends PagingAndSortingRepository<E, ID> {
  T findOne(ID id);
  List<T> findAll();
  boolean exists(ID id);
  long count();
  <S extends E>S save(S entity);
  // ...
}
```


## Quick Start ##

Add the repository definition to your `pom.xml` :

```xml
<repositories>
  <repository>
    <id>xet.jp-release</id>
    <name>xet.jp-release</name>
    <url>http://maven.xet.jp/release</url>
  </repository>
</repositories>
```

Add the jar to your maven project :

```xml
<dependency>
  <groupId>org.springframework.data</groupId>
  <artifactId>spring-data-mirage</artifactId>
  <version>0.1.2.RELEASE</version>
</dependency>
```

Configure your infrastructure :

```xml
<bean id="dataSource" ...>
  <!-- ... -->
</bean>

<bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
  <property name="dataSource" ref="dataSource" />
</bean>

<tx:annotation-driven transaction-manager="transactionManager" proxy-target-class="false" />

<bean id="connectionProvider" class="jp.sf.amateras.mirage.integration.spring.SpringConnectionProvider">
  <property name="transactionManager" ref="transactionManager" />
</bean>

<bean id="dialect" class="jp.sf.amateras.mirage.dialect.MySQLDialect" />
<bean id="railsLikeNameConverter" class="jp.sf.amateras.mirage.naming.RailsLikeNameConverter" />

<bean id="sqlManager" class="jp.sf.amateras.mirage.SqlManagerImpl" depends-on="fieldPropertyExtractorInitializer">
  <property name="connectionProvider" ref="connectionProvider" />
  <property name="dialect" ref="dialect" />
  <property name="nameConverter" ref="railsLikeNameConverter" />
  <!-- ... -->
</bean>

<mirage:repositories base-package="com.example.product.repository" sql-manager-ref="sqlManager" />
```

Create an entity:

```java
@Table(name = "users")
public class User {

  @Id
  @PrimaryKey(generationType = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long id;

  @Column(name = "first_name")
  private String firstname;

  @Column(name = "last_name")
  private String lastname;

  // Getters and setters
}
```

Create a repository interface in `com.example.product.repository`:

```java
public interface AppUserRepository extends JdbcRepository<AppUser, Long> {

  List<AppUser> findByFirstname(@Param("first_name") String firstName);

  // another query methods...
}
```

Write SQL file `AppUserRepository.sql` and place on the same directory with `AppUserRepository.class` :

```sql
SELECT *
FROM users

/*BEGIN*/
WHERE
	/*IF id != null*/
	user_id = /*id*/1
	/*END*/

	/*IF first_name != null*/
	AND first_name = /*first_name*/'miyamoto'
	/*END*/
/*END*/

/*IF orders != null*/
ORDER BY /*$orders*/user_id
/*END*/

/*BEGIN*/
LIMIT
	/*IF offset != null*/
	/*offset*/0,
	/*END*/

	/*IF size != null*/
	/*size*/10
	/*END*/
/*END*/
```

Write a test client

```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/applicationContext.xml")
public class AppUserRepositoryTest {

  @Autowired
  AppUserRepository repos;

  @Test
  @TransactionConfiguration
  @Transactional
  public void sampleTestCase() {
    AppUser user = repos.findOne(1L);
    assertThat("user", user, is(notNullValue()));
    List<AppUser> users = customerRepository.findAll();
    assertThat("users", users, is(notNullValue()));
    assertThat("usersSize", users.size() > 0, is(true));
  }
}
```

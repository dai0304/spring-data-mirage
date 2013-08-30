Spring Data Mirage SQL
======================

The primary goal of the [Spring Data](http://www.springsource.org/spring-data) project is to make it easier to build
Spring-powered applications that use data access technologies. This module deals with enhanced support for
[Mirage SQL](https://github.com/takezoe/mirage) based data access layers.

## Features ##
This project defines a `MirageRepository` base interface  :

```java
public interface MirageRepository<E, ID extends Serializable> extends PagingAndSortingRepository<E, ID> {
  T findOne(ID id);
  List<T> findAll();
  boolean exists(ID id);
  long count();
  <S extends E>S save(S entity);
  // ...
}
```


## Quick Start ##

### dependency

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

### Spring beans configurations

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

### Entity classes

Create an mirage entity:

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

### Repository interfaces

Create a repository interface in `com.example.product.repository`:

```java
public interface AppUserRepository extends MirageRepository<AppUser, Long> {

  List<AppUser> findByFirstname(@Param("first_name") String firstName);

  List<AppUser> findByComplexCondition(@Param("complex_param1") String cp1, @Param("complex_param2") int cp2);

  // another query methods...
}
```

### SQL files

Write SQL file `AppUserRepository.sql` (that's called 'base-select-SQL') and place on the same directory
with `AppUserRepository.class` :

```sql
SELECT *
FROM users

/*BEGIN*/
WHERE
	/*IF id != null*/
	user_id = /*id*/1
	/*END*/

	/*IF ids != null*/
	AND user_id IN /*ids*/(1, 2, 3)
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

This base-select-SQL must support "id", "ids", "orders", "offset" and "size" parameters.  These parameters are used
by `findOne()`, `findAll(Iterable<ID>)`, `findAll(Pageable)` and the like.

And you can place another 2-way-sql for specific query method (that's called 'method-specific-2-way-sql')
like this: `AppUserRepository_findByComplexCondition.sql`

```
SELECT U.*
FROM users U
	JOIN blahblah B ON U.username = B.username
WHERE
	B.complex_param1 LIKE /*complex_param1*/'%foobar%'
	OR
	B.complex_param2 > /*complex_param2*/10
```

If the all additional methods are supported by method-specific-2-way-sql or you don't declare additional query methods,
you don't need to create base-select-SQL file.

### Clients

Write a test client :

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
    AppUser user = repos.findOne(1L); // SELECT * FROM users WHERE user_id = 1
    assertThat("user", user, is(notNullValue()));
    List<AppUser> users = customerRepository.findAll(); // SELECT * FROM users
    assertThat("users", users, is(notNullValue()));
    assertThat("usersSize", users.size() > 0, is(true));
    
    List<AppUser> complex = customerRepository.findByComplexCondition("xy%z", 2);
    // SELECT U.* FROM users U JOIN blahblah B ON U.username = B.username
    // WHERE B.complex_param1 LIKE 'xy%z' OR B.complex_param2 > 2
  }
}
```

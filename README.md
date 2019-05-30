# Spring Data Mirage SQL

[![Download](https://api.bintray.com/packages/dai0304/maven/spring-data-mirage/images/download.svg)](https://bintray.com/dai0304/maven/spring-data-mirage/_latestVersion)

The primary goal of the [Spring Data](http://www.springsource.org/spring-data) project is to make it easier to build
Spring-powered applications that use data access technologies. This module deals with enhanced support for
[Mirage SQL](https://github.com/mirage-sql/mirage) based data access layers.


## Features

This project defines a `MirageRepository` base interface:

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


## Requirements

* Java 8+
* Spring Data Commons 2.1.x
* Mirage 2.1.x


## Quick Start

### dependency

Add the repository definition to your `pom.xml`:

```xml
<repositories>
  <repository>
    <id>bintray-dai0304-maven</id>
    <name>bintray-dai0304-maven</name>
    <url>https://dl.bintray.com/dai0304/maven</url>
  </repository>
</repositories>
```

Add the jar to your maven project :

```xml
<dependency>
  <groupId>org.springframework.data</groupId>
  <artifactId>spring-data-mirage</artifactId>
  <version>x.x.x.RELEASE</version>
</dependency>
```

### Spring beans configurations

Configure your infrastructure:

```xml
<bean id="dataSource" ...>
  <!-- ... -->
</bean>

<bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
  <property name="dataSource" ref="dataSource" />
</bean>

<tx:annotation-driven transaction-manager="transactionManager" proxy-target-class="false" />

<bean id="connectionProvider" class="com.miragesql.miragesql.integration.spring.SpringConnectionProvider">
  <property name="transactionManager" ref="transactionManager" />
</bean>

<bean id="dialect" class="com.miragesql.miragesql.dialect.MySQLDialect" />
<bean id="railsLikeNameConverter" class="com.miragesql.miragesql.naming.RailsLikeNameConverter" />

<bean id="sqlManager" class="com.miragesql.miragesql.SqlManagerImpl" depends-on="fieldPropertyExtractorInitializer">
  <property name="connectionProvider" ref="connectionProvider" />
  <property name="dialect" ref="dialect" />
  <property name="nameConverter" ref="railsLikeNameConverter" />
  <property name="beanDescFactory">
    <bean class="com.miragesql.miragesql.bean.BeanDescFactory">
      <property name="propertyExtractor">
        <bean class="com.miragesql.miragesql.bean.FieldPropertyExtractor" />
      </property>
    </bean>
  </property>
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
public interface UserRepository extends MirageRepository<User, Long> {

  List<User> findByFirstname(@Param("first_name") String firstName);

  List<User> findByComplexCondition(
    @Param("complex_param1") String cp1, @Param("complex_param2") int cp2);

  // another query methods...
}
```

### SQL files

Write SQL file `UserRepository.sql` (that's called 'base-select-SQL') and place on the same directory
with `UserRepository.class`:

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
like this: `UserRepository_findByComplexCondition.sql`

```sql
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
@ContextConfiguration(...)
public class UserRepositoryTest {

  @Autowired
  UserRepository repo;

  @Test
  @TransactionConfiguration
  @Transactional
  public void sampleTestCase() {
    User user = repo.findOne(1L); // SELECT * FROM users WHERE user_id = 1
    assertThat("user", user, is(notNullValue()));
    List<User> users = repo.findAll(); // SELECT * FROM users
    assertThat("users", users, is(notNullValue()));
    assertThat("usersSize", users.size() > 0, is(true));
    
    List<User> complex = repo.findByComplexCondition("xy%z", 2);
    // SELECT U.* FROM users U JOIN blahblah B ON U.username = B.username
    // WHERE B.complex_param1 LIKE 'xy%z' OR B.complex_param2 > 2
  }
}
```


## Miscellaneous things

### Modifying query

You must mark modifying (insert, update and delete) query methods by `@Modifying` annotation:

`FooBarRepository.java`
```java
public interface FooBarRepository  extends MirageRepository<FooBar, Long> {

	@Modifying
	void updateFooBar(@Param("foo") String foo, @Param("bar") String bar);

	// ...
}
```

`FooBarRepository_updateFooBar.sql`
```
UPDATE ...
```

### Static parameters

If you want to pass parameters to 2-way-sql statically, you can use `@StaticParam` annotation like this:

```java
@StaticParam(key = "id", value = "foo")
User findFoo();
```

You can use multiple `@StaticParam` annotations by using `@StaticParams` annotation like this:

```java
@StaticParams({
	@StaticParam(key = "foo", value = "foovalue"),
	@StaticParam(key = "bar", value = "barvalue")
})
List<User> findXxx();
```


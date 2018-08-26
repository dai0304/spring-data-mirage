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
package jp.xet.springframework.data.mirage.repository.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.transaction.PlatformTransactionManager;

import com.miragesql.miragesql.SqlManager;

import jp.xet.springframework.data.mirage.repository.support.MirageRepositoryFactoryBean;

/**
 * Annotation to enable Mirage repositories. Will scan the package of the annotated configuration class for Spring Data
 * repositories by default.
 * 
 * @since 0.2.0
 * @version $Id$
 * @author daisuke
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(MirageRepositoriesRegistrar.class)
public @interface EnableMirageRepositories {
	
	/**
	 * Type-safe alternative to {@link #basePackages()} for specifying the packages to scan for annotated components. The
	 * package of each class specified will be scanned. Consider creating a special no-op marker class or interface in
	 * each package that serves no purpose other than being referenced by this attribute.
	 */
	Class<?>[] basePackageClasses() default {};
	
	/**
	 * Base packages to scan for annotated components. {@link #value()} is an alias for (and mutually exclusive with) this
	 * attribute. Use {@link #basePackageClasses()} for a type-safe alternative to String-based package names.
	 */
	String[] basePackages() default {};
	
	/**
	 * Specifies which types are not eligible for component scanning.
	 */
	Filter[] excludeFilters() default {};
	
	/**
	 * Specifies which types are eligible for component scanning. Further narrows the set of candidate components from
	 * everything in {@link #basePackages()} to everything in the base packages that matches the given filter or filters.
	 */
	Filter[] includeFilters() default {};
	
	/**
	 * Configures the location of where to find the Spring Data named queries properties file. Will default to
	 * {@code META-INFO/mirage-named-queries.properties}.
	 */
	String namedQueriesLocation() default "";
	
	/**
	 * Returns the key of the {@link QueryLookupStrategy} to be used for lookup queries for query methods. Defaults to
	 * {@link Key#CREATE_IF_NOT_FOUND}.
	 */
	Key queryLookupStrategy() default Key.CREATE_IF_NOT_FOUND;
	
	/**
	 * Returns the {@link FactoryBean} class to be used for each repository instance. Defaults to
	 * {@link MirageRepositoryFactoryBean}.
	 */
	Class<?> repositoryFactoryBeanClass() default MirageRepositoryFactoryBean.class;
	
	/**
	 * Returns the postfix to be used when looking up custom repository implementations. Defaults to {@literal Impl}. So
	 * for a repository named {@code PersonRepository} the corresponding implementation class will be looked up scanning
	 * for {@code PersonRepositoryImpl}.
	 */
	String repositoryImplementationPostfix() default "Impl";
	
	/**
	 * Configures the name of the {@link SqlManager} bean definition to be used to create repositories
	 * discovered through this annotation. Defaults to {@code sqlManager}.
	 */
	String sqlManagerRef() default "sqlManager";
	
	/**
	 * Configures the name of the {@link PlatformTransactionManager} bean definition to be used to create repositories
	 * discovered through this annotation. Defaults to {@code transactionManager}.
	 */
	String transactionManagerRef() default "transactionManager";
	
	/**
	 * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation declarations e.g.:
	 * {@code @EnableMirageRepositories("org.my.pkg")} instead of {@code @EnableMirageRepositories(basePackages="org.my.pkg")}.
	 */
	String[] value() default {};
}

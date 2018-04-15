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

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.data.repository.config.XmlRepositoryConfigurationSource;
import org.springframework.util.StringUtils;

import org.w3c.dom.Element;

import jp.xet.springframework.data.mirage.repository.support.MirageRepositoryFactoryBean;

/**
 * Mirage specific configuration extension parsing custom attributes from the XML namespace and
 * {@link EnableMirageRepositories} annotation.
 * 
 * @author Oliver Gierke
 * @author Eberhard Wolff
 * @author Gil Markham
 */
public class MirageRepositoryConfigExtension extends RepositoryConfigurationExtensionSupport {
	
	private static final String DEFAULT_SQL_MANAGER_BEAN_NAME = "sqlManager";
	
	private static final String DEFAULT_TRANSACTION_MANAGER_BEAN_NAME = "transactionManager";
	
	private static final Class<?> PET_POST_PROCESSOR = PersistenceExceptionTranslationPostProcessor.class;
	
	
	@Override
	public String getRepositoryFactoryBeanClassName() {
		return MirageRepositoryFactoryBean.class.getName();
	}
	
	@Override
	public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {
		AnnotationAttributes attributes = config.getAttributes();
		postProcess(builder, attributes.getString("sqlManagerRef"), attributes.getString("transactionManagerRef"),
				config.getSource());
	}
	
	@Override
	public void postProcess(BeanDefinitionBuilder builder, XmlRepositoryConfigurationSource config) {
		Element element = config.getElement();
		postProcess(builder, element.getAttribute("sql-manager-ref"), element.getAttribute("transaction-manager-ref"),
				config.getSource());
	}
	
	@Override
	public void registerBeansForRoot(BeanDefinitionRegistry registry,
			RepositoryConfigurationSource configurationSource) {
		super.registerBeansForRoot(registry, configurationSource);
		
		if (!hasBean(PET_POST_PROCESSOR, registry)) {
			AbstractBeanDefinition definition =
					BeanDefinitionBuilder.rootBeanDefinition(PET_POST_PROCESSOR).getBeanDefinition();
			registerWithSourceAndGeneratedBeanName(registry, definition, configurationSource.getSource());
		}
	}
	
	@Override
	protected String getModulePrefix() {
		return "mirage";
	}
	
	private void postProcess(BeanDefinitionBuilder builder, String sqlManagerRef, String transactionManagerRef,
			Object source) {
		if (StringUtils.hasText(sqlManagerRef)) {
			builder.addPropertyReference("sqlManager", sqlManagerRef);
		} else {
			builder.addPropertyReference("sqlManager", DEFAULT_SQL_MANAGER_BEAN_NAME);
		}
		
		if (StringUtils.hasText(transactionManagerRef)) {
			builder.addPropertyValue("transactionManager", transactionManagerRef);
		} else {
			builder.addPropertyValue("transactionManager", DEFAULT_TRANSACTION_MANAGER_BEAN_NAME);
		}
	}
}

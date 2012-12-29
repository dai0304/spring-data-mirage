/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2012/12/09
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.springframework.data.mirage.repository.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mirage.repository.config.SimpleMirageRepositoryConfiguration.MirageRepositoryConfiguration;
import org.springframework.data.repository.config.AbstractRepositoryConfigDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser to create bean definitions for repositories namespace. Registers bean definitions for repositories as well as
 * {@code PersistenceExceptionTranslationPostProcessor} to apply exception translation.
 * <p>
 * The definition parser allows two ways of configuration. Either it looks up the manually defined repository instances
 * or scans the defined domain package for candidates for repositories.
 * 
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
class MirageRepositoryConfigDefinitionParser extends
		AbstractRepositoryConfigDefinitionParser<SimpleMirageRepositoryConfiguration, MirageRepositoryConfiguration> {
	
	private static final Class<?> PET_POST_PROCESSOR = PersistenceExceptionTranslationPostProcessor.class;
	
	
	@Override
	protected SimpleMirageRepositoryConfiguration getGlobalRepositoryConfigInformation(Element element) {
		return new SimpleMirageRepositoryConfiguration(element);
	}
	
	@Override
	protected void postProcessBeanDefinition(MirageRepositoryConfiguration ctx, BeanDefinitionBuilder builder,
			BeanDefinitionRegistry registry, Object beanSource) {
		String sqlManagerRef = ctx.getSqlManagerRef();
		if (StringUtils.hasText(sqlManagerRef)) {
			BeanDefinition sqlManagerBeanDefinition = registry.getBeanDefinition(sqlManagerRef);
			builder.addPropertyValue("sqlManager", sqlManagerBeanDefinition);
		}
	}
	
	@Override
	protected void registerBeansForRoot(BeanDefinitionRegistry registry, Object source) {
		super.registerBeansForRoot(registry, source);
		
		if (!hasBean(PET_POST_PROCESSOR, registry)) {
			AbstractBeanDefinition definition =
					BeanDefinitionBuilder.rootBeanDefinition(PET_POST_PROCESSOR).getBeanDefinition();
			
			registerWithSourceAndGeneratedBeanName(registry, definition, source);
		}
	}
}

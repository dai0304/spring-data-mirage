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
package jp.xet.springframework.data.mirage.domain.support;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.data.domain.AuditorAware;

/**
 * TODO daisuke
 * 
 * @since 0.2.0
 * @version $Id$
 * @author daisuke
 */
public class AuditingBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
	
	static final String BEAN_CONFIGURER_ASPECT_BEAN_NAME =
			"org.springframework.context.config.internalBeanConfigurerAspect";
	
	
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		
		if (!isSpringConfigured(beanFactory)) {
			return;
		}
		
		for (String beanName : BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, AuditorAware.class,
				true, false)) {
			BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
			definition.setLazyInit(true);
		}
	}
	
	/**
	 * Returns whether we have a bean factory for which {@code &lt;context:spring-configured&gt;} was activated.
	 * 
	 * @param factory {@link BeanFactory}
	 * @return {@code true} if {@code context:spring-configured} was activated
	 */
	private boolean isSpringConfigured(BeanFactory factory) {
		
		try {
			factory.getBean(BEAN_CONFIGURER_ASPECT_BEAN_NAME);
			return true;
		} catch (NoSuchBeanDefinitionException e) {
			return false;
		}
	}
}

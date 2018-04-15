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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.ClassUtils;

import org.w3c.dom.Element;

/**
 * TODO for daisuke
 * 
 * @since 0.2.0
 * @version $Id$
 * @author daisuke
 */
public class AuditingBeanDefinitionParser implements BeanDefinitionParser {
	
	private final SpringConfiguredBeanDefinitionParser springConfiguredParser =
			new SpringConfiguredBeanDefinitionParser();
	
	
	@Override
	public BeanDefinition parse(Element element, ParserContext parser) {
		springConfiguredParser.parse(element, parser);
		
		return null;
	}
	
	
	/**
	 * Copied code of SpringConfiguredBeanDefinitionParser until this class gets public.
	 * 
	 * @see <a href="http://jira.springframework.org/browse/SPR-7340">Make SpringConfiguredBeanDefinitionParser public</a>
	 * @author Juergen Hoeller
	 */
	private static class SpringConfiguredBeanDefinitionParser implements BeanDefinitionParser {
		
		/**
		 * The bean name of the internally managed bean configurer aspect.
		 */
		private static final String BEAN_CONFIGURER_ASPECT_BEAN_NAME =
				"org.springframework.context.config.internalBeanConfigurerAspect";
		
		private static final String BEAN_CONFIGURER_ASPECT_CLASS_NAME =
				"org.springframework.beans.factory.aspectj.AnnotationBeanConfigurerAspect";
		
		
		@Override
		public BeanDefinition parse(Element element, ParserContext parserContext) {
			
			if (!parserContext.getRegistry().containsBeanDefinition(BEAN_CONFIGURER_ASPECT_BEAN_NAME)) {
				if (!ClassUtils.isPresent(BEAN_CONFIGURER_ASPECT_CLASS_NAME, getClass().getClassLoader())) {
					parserContext.getReaderContext().error(
							"Could not configure Spring Data Mirage auditing-feature because"
									+ " spring-aspects.jar is not on the classpath!\n"
									+ "If you want to use auditing please add spring-aspects.jar to the classpath.",
							element);
				}
				
				RootBeanDefinition def = new RootBeanDefinition();
				def.setBeanClassName(BEAN_CONFIGURER_ASPECT_CLASS_NAME);
				def.setFactoryMethodName("aspectOf");
				
				def.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				def.setSource(parserContext.extractSource(element));
				parserContext.registerBeanComponent(new BeanComponentDefinition(def, BEAN_CONFIGURER_ASPECT_BEAN_NAME));
			}
			return null;
		}
	}
}

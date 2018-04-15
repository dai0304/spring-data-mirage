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

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.data.repository.config.RepositoryBeanDefinitionParser;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

/**
 * Simple namespace handler for {@literal repositories} namespace.
 * 
 * @since 0.1
 * @version $Id$
 * @author daisuke
 */
public class MirageRepositoryNameSpaceHandler extends NamespaceHandlerSupport {
	
	@Override
	public void init() {
		RepositoryConfigurationExtension extension = new MirageRepositoryConfigExtension();
		RepositoryBeanDefinitionParser repositoryBeanDefinitionParser = new RepositoryBeanDefinitionParser(extension);
		
		registerBeanDefinitionParser("repositories", repositoryBeanDefinitionParser);
		registerBeanDefinitionParser("auditing", new AuditingBeanDefinitionParser());
	}
}

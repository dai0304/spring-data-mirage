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

import jp.sf.amateras.mirage.SqlManager;

import org.springframework.data.mirage.repository.config.SimpleMirageRepositoryConfiguration.MirageRepositoryConfiguration;
import org.springframework.data.repository.config.AutomaticRepositoryConfigInformation;
import org.springframework.data.repository.config.ManualRepositoryConfigInformation;
import org.springframework.data.repository.config.RepositoryConfig;
import org.springframework.data.repository.config.SingleRepositoryConfigInformation;
import org.w3c.dom.Element;

/**
 * TODO for daisuke
 * 
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
public class SimpleMirageRepositoryConfiguration extends
		RepositoryConfig<MirageRepositoryConfiguration, SimpleMirageRepositoryConfiguration> {
	
	private static final String FACTORY_CLASS =
			"org.springframework.data.mirage.repository.support.MirageRepositoryFactoryBean";
	
	private static final String SQL_MANAGER_REF = "sql-manager-ref";
	
	
	/**
	 * @param repositoriesElement
	 */
	public SimpleMirageRepositoryConfiguration(Element repositoriesElement) {
		super(repositoriesElement, FACTORY_CLASS);
	}
	
	@Override
	public MirageRepositoryConfiguration createSingleRepositoryConfigInformationFor(Element element) {
		return new ManualMirageRepositoryConfigInformation(element, this);
	}
	
	@Override
	public MirageRepositoryConfiguration getAutoconfigRepositoryInformation(String interfaceName) {
		
		return new AutomaticMirageRepositoryConfigInformation(interfaceName, this);
	}
	
	@Override
	public String getNamedQueriesLocation() {
		return "classpath*:META-INF/mirage-named-queries.properties";
	}
	
	/**
	* Returns the name of the {@link SqlManager} bean.
	* 
	* @return the name of the {@link SqlManager} bean.
	*/
	public String getSqlManagerRef() {
		return getSource().getAttribute(SQL_MANAGER_REF);
	}
	
	
	interface MirageRepositoryConfiguration extends
			SingleRepositoryConfigInformation<SimpleMirageRepositoryConfiguration> {
		
		String getSqlManagerRef();
	}
	
	private static class AutomaticMirageRepositoryConfigInformation extends
			AutomaticRepositoryConfigInformation<SimpleMirageRepositoryConfiguration> implements
			MirageRepositoryConfiguration {
		
		public AutomaticMirageRepositoryConfigInformation(String interfaceName,
				SimpleMirageRepositoryConfiguration parent) {
			super(interfaceName, parent);
		}
		
		/**
		 * Returns the {@link SqlManager} reference to be used for all the repository instances configured.
		 * 
		 * @return {@link SqlManager} reference
		 */
		@Override
		public String getSqlManagerRef() {
			return getParent().getSqlManagerRef();
		}
	}
	
	private static class ManualMirageRepositoryConfigInformation extends
			ManualRepositoryConfigInformation<SimpleMirageRepositoryConfiguration> implements
			MirageRepositoryConfiguration {
		
		public ManualMirageRepositoryConfigInformation(Element element, SimpleMirageRepositoryConfiguration parent) {
			super(element, parent);
		}
		
		@Override
		public String getSqlManagerRef() {
			return getAttribute(SQL_MANAGER_REF);
		}
	}
}

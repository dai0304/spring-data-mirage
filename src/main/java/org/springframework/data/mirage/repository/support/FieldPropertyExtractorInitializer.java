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
package org.springframework.data.mirage.repository.support;

import jp.sf.amateras.mirage.bean.BeanDescFactory;
import jp.sf.amateras.mirage.bean.FieldPropertyExtractor;

/**
 * {@link BeanDescFactory}に{@link FieldPropertyExtractor}を設定するタイミングが掴めなかったので強引に…orz
 * 
 * @since 0.1
 * @version $Id: FieldPropertyExtractorInitializer.java 160 2011-10-21 09:49:56Z daisuke $
 * @author daisuke
 * @deprecated use {@link BeanDescFactory#setPropertyExtractor(jp.sf.amateras.mirage.bean.PropertyExtractor)} instead.
 */
@Deprecated
public class FieldPropertyExtractorInitializer {
	
//	static {
//		BeanDescFactory.setPropertyExtractor(new FieldPropertyExtractor());
//	}
}

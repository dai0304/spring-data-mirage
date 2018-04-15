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
package jp.xet.springframework.data.mirage.repository.support;

import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * {@link java.util.logging.Logger}によるログを SLF4J で扱うように設定するクラス。
 * 
 * <p>コンストラクタ内で、{@link SLF4JBridgeHandler} のインストールを行う。DIコンテナ等によるインスタンス化を行うことを想定している。</p>
 * 
 * @since 0.1
 * @version $Id: FieldPropertyExtractorInitializer.java 160 2011-10-21 09:49:56Z daisuke $
 * @author daisuke
 * @see <a href="http://blog.cn-consult.dk/2009/03/bridging-javautillogging-to-slf4j.html"
 * 		>Bridging java.util.logging to SLF4J</a>
 */
public class EnableSLF4JBridgeHandler {
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @since 0.1
	 */
	public EnableSLF4JBridgeHandler() {
		init();
	}
	
	/**
	 * {@link java.util.logging.Logger}によるログを SLF4J で扱うように設定する。
	 * 
	 * @since 0.1
	 */
	public void init() {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}
}

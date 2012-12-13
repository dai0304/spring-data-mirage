/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2011/10/21
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
package org.springframework.data.mirage.repository.support;

import java.util.logging.Handler;
import java.util.logging.LogManager;

import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * {@link java.util.logging.Logger}によるログを SLF4J で扱うように設定するクラス。
 * 
 * <p>コンストラクタ内で、{@link SLF4JBridgeHandler} のインストールを行う。DIコンテナ等によるインスタンス化を行うことを想定している。</p>
 * 
 * @since 1.0
 * @version $Id: FieldPropertyExtractorInitializer.java 160 2011-10-21 09:49:56Z daisuke $
 * @author daisuke
 * @see <a href="http://blog.cn-consult.dk/2009/03/bridging-javautillogging-to-slf4j.html"
 * 		>Bridging java.util.logging to SLF4J</a>
 */
public class EnableSLF4JBridgeHandler {
	
	/**
	 * インスタンスを生成する。
	 */
	public EnableSLF4JBridgeHandler() {
		init();
	}
	
	/**
	 * {@link java.util.logging.Logger}によるログを SLF4J で扱うように設定する。
	 * 
	 * @since 1.0
	 */
	public void init() {
		java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
		Handler[] handlers = rootLogger.getHandlers();
		for (Handler handler : handlers) {
			rootLogger.removeHandler(handler);
		}
		SLF4JBridgeHandler.install();
	}
}

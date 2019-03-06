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
package jp.xet.springframework.data.mirage.repository.preprocess;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.Id;

import com.miragesql.miragesql.annotation.Column;
import com.miragesql.miragesql.annotation.PrimaryKey;
import com.miragesql.miragesql.annotation.PrimaryKey.GenerationType;
import com.miragesql.miragesql.annotation.Table;

import jp.xet.springframework.data.mirage.repository.handler.BeforeCreate;
import jp.xet.springframework.data.mirage.repository.handler.BeforeUpdate;

/**
 * Sample entity class.
 */
@Table(name = "sample_preprocess")
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@SuppressWarnings("serial")
public class PreProcessEntity {
	
	@Id
	@Column(name = "id")
	@PrimaryKey(generationType = GenerationType.APPLICATION)
	@Setter(AccessLevel.PACKAGE)
	private String id;
	
	@Column(name = "str")
	private String str;
	
	@Column(name = "last_updated")
	private long lastUpdated;
	
	
	@BeforeCreate
	@BeforeUpdate
	public void preProcess() {
		lastUpdated = Instant.now().toEpochMilli();
	}
}

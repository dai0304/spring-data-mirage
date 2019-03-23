/*
 * Copyright 2019 the original author or authors.
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
package jp.xet.springframework.data.mirage.repository.autonum;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.springframework.data.annotation.Id;

import com.miragesql.miragesql.annotation.Column;
import com.miragesql.miragesql.annotation.PrimaryKey;
import com.miragesql.miragesql.annotation.PrimaryKey.GenerationType;
import com.miragesql.miragesql.annotation.Table;

/**
 * Sample entity class.
 *
 * @author daisuke
 */
@Table(name = "autonum_string")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@SuppressWarnings("serial")
public class AutonumEntity implements Serializable {
	
	@Id
	@Column(name = "id")
	@PrimaryKey(generationType = GenerationType.IDENTITY)
	@Setter(AccessLevel.PACKAGE)
	private long id;
	
	@Column(name = "str")
	private String str;
	
	
	/**
	 * インスタンスを生成する。
	 *
	 * @param str string
	 */
	public AutonumEntity(String str) {
		this.str = str;
	}
}

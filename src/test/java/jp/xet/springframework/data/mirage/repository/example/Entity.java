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
package jp.xet.springframework.data.mirage.repository.example;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import com.miragesql.miragesql.annotation.Column;
import com.miragesql.miragesql.annotation.PrimaryKey;
import com.miragesql.miragesql.annotation.PrimaryKey.GenerationType;
import com.miragesql.miragesql.annotation.Table;

import jp.xet.springframework.data.mirage.repository.Identifiable;

/**
 * Sample entity class.
 * 
 * @author daisuke
 */
@Table(name = "samples")
@SuppressWarnings("serial")
public class Entity implements Identifiable, Serializable {
	
	@Id
	@Column(name = "id")
	@PrimaryKey(generationType = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "str")
	private String str;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param str string
	 */
	public Entity(String str) {
		this.str = str;
	}
	
	Entity() {
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Entity other = (Entity) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}
	
	@Override
	public long getId() {
		return id;
	}
	
	@SuppressWarnings("javadoc")
	public String getStr() {
		return str;
	}
	
	@Override
	public int hashCode() {
		return (int) (id ^ (id >>> 32)); // CHECKSTYLE IGNORE THIS LINE
	}
	
	@SuppressWarnings("javadoc")
	public void setStr(String str) {
		this.str = str;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Entity[").append(str).append("]");
		return builder.toString();
	}
	
	void setId(long id) {
		this.id = id;
	}
}

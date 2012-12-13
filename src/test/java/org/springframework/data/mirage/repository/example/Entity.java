/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2011/10/22
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
package org.springframework.data.mirage.repository.example;

import java.io.Serializable;

import jp.sf.amateras.mirage.annotation.Column;
import jp.sf.amateras.mirage.annotation.PrimaryKey;
import jp.sf.amateras.mirage.annotation.PrimaryKey.GenerationType;
import jp.sf.amateras.mirage.annotation.Table;

import org.springframework.data.annotation.Id;
import org.springframework.data.mirage.repository.Identifiable;

/**
 * TODO を表すエンティティクラス。
 * 
 * @since 1.0
 * @version $Id$
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
	
	void setId(long id) {
		this.id = id;
	}
}

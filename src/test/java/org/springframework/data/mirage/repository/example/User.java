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

import jp.sf.amateras.mirage.annotation.Column;
import jp.sf.amateras.mirage.annotation.Table;
import jp.sf.amateras.mirage.annotation.Transient;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;

/**
 * ユーザを表すエンティティクラス。
 * 
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
@Table(name = "users")
@SuppressWarnings("serial")
public class User implements Persistable<String> {
	
	@Id
	@Column(name = "username")
	private String username;
	
	@Column(name = "password")
	private String password;
	
	@Transient
	private boolean persisted;
	
	
	/**
	* インスタンスを生成する。
	* 
	* @param username 
	* @param password 
	*/
	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	User() {
	}
	
	@Override
	public String getId() {
		return null;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getUsername() {
		return username;
	}
	
	@Override
	public boolean isNew() {
		return persisted;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setPersisted(boolean persisted) {
		this.persisted = persisted;
	}
	
	void setUsername(String username) {
		this.username = username;
	}
}

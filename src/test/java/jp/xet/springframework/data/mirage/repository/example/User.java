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

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;

import com.miragesql.miragesql.annotation.Column;
import com.miragesql.miragesql.annotation.PrimaryKey;
import com.miragesql.miragesql.annotation.PrimaryKey.GenerationType;
import com.miragesql.miragesql.annotation.Table;
import com.miragesql.miragesql.annotation.Transient;

/**
 * User entity.
 * 
 * @author daisuke
 */
@Table(name = "users")
@SuppressWarnings("serial")
public class User implements Persistable<String> {
	
	@Id
	@PrimaryKey(generationType = GenerationType.APPLICATION)
	@Column(name = "username")
	private String username;
	
	@Column(name = "password")
	private String password;
	
	@Transient
	private boolean persisted;
	
	
	/**
	 * Create user.
	 * 
	 * @param username username
	 * @param password password
	 */
	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	User() {
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
		User other = (User) obj;
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!username.equals(other.username)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String getId() {
		return null;
	}
	
	/**
	 * Return the password.
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * Return the username.
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}
	
	@Override
	public boolean isNew() {
		return persisted;
	}
	
	/**
	 * Set the password.
	 * 
	 * @param password password
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setPersisted(boolean persisted) {
		this.persisted = persisted;
	}
	
	@Override
	public String toString() {
		return "User [username=" + username + ", password=" + password + ", persisted=" + persisted + "]";
	}
	
	void setUsername(String username) {
		this.username = username;
	}
}

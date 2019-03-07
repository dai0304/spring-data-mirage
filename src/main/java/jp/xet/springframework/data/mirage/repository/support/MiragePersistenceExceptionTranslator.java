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
package jp.xet.springframework.data.mirage.repository.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import com.miragesql.miragesql.exception.SQLRuntimeException;

/**
 * TODO for daisuke
 */
public class MiragePersistenceExceptionTranslator implements PersistenceExceptionTranslator {
	
	private SQLErrorCodeSQLExceptionTranslator sqlExceptionTranslator = new SQLErrorCodeSQLExceptionTranslator();
	
	
	@Autowired(required = false)
	public SQLErrorCodeSQLExceptionTranslator getSqlExceptionTranslator() {
		return sqlExceptionTranslator;
	}
	
	public void setSqlExceptionTranslator(
			SQLErrorCodeSQLExceptionTranslator sqlExceptionTranslator) {
		this.sqlExceptionTranslator = sqlExceptionTranslator;
	}
	
	@Override
	public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
		if (ex instanceof SQLRuntimeException && sqlExceptionTranslator != null) {
			SQLRuntimeException sqlRuntimeException = (SQLRuntimeException) ex;
			return sqlExceptionTranslator.translate("unknown", null, sqlRuntimeException.getCause());
		}
		
		if (ex.getClass().getPackage().getName().startsWith("com.miragesql.miragesql.exception")) {
			return new MirageDataAccessException(ex);
		}
		return null;
	}
	
	
	@SuppressWarnings("serial")
	private static final class MirageDataAccessException extends DataAccessException {
		
		private MirageDataAccessException(Throwable cause) {
			super("unknown", cause);
		}
	}
}

/*
 * Created on 16-Feb-2005
 */
package com.mmakowski.medley.data;

import com.mmakowski.medley.resources.Errors;

/**
 * A factory object for JDBCConnectors. Translates database type
 * names to JDBCConnector classes.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.2 $ $Date: 2005/02/19 10:30:31 $
 */
class JDBCConnectorFactory {

	// database types
	static final String HSQLDB = "HSQLDB";
	static final String FIREBIRD = "Firebird";
	
	/**
	 * @param type database type string
	 * @return JDBCConnector for given database type
	 * @throws DataSourceException
	 */
	JDBCConnector createConnector(String type) throws DataSourceException {
		if (type.equals(FIREBIRD)) {
			return new FirebirdConnector();
		} else if (type.equals(HSQLDB)) {
			return new HSQLDBConnector();
		} else {
			throw new DataSourceException(Errors.UNSUPPORTED_DATABASE_TYPE, new Object[] {type});
		}
	}
}

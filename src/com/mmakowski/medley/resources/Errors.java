/*
 * Created on 2004-04-07
 */
package com.mmakowski.medley.resources;

import java.text.DecimalFormat;

/**
 * A class containing error codes for the whole application.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.29 $  $Date: 2005/08/19 18:04:30 $
 */
public final class Errors {
	/** the format for error codes used in resource bundles */
	private static final String CODE_FORMAT = "00000"; 
	
	/**
	 * @param errorCode the error code to be formatted;
	 * @return error code formatted for use with resource bundles etc.
	 */
	public static final String formatCode(int errorCode) {
		DecimalFormat df = new DecimalFormat(CODE_FORMAT);
		return df.format(errorCode);
	}
	
	// the error codes
	public static final int NO_SUCH_ERROR = 99999;
	
	public static final int DATA_SOURCE_CLASS_NOT_FOUND = 1001;
	public static final int DATA_SOURCE_CLASS_INCOMPATIBLE = 1002;
	public static final int DATA_OBJECT_BEHAVIOUR_ALREADY_REGISTERED = 1003;
	public static final int DATA_OBJECT_BEHAVIOUR_NOT_FOUND = 1004;
	public static final int DATA_OBJECT_CLASS_NOT_FOUND = 1005;
	public static final int DATA_SOURCE_IS_NULL = 1006;
	public static final int DATA_OBJECT_INCOMPATIBLE_WITH_SOURCE = 1007;
	public static final int DATA_SOURCE_CANT_PREPARE_STATEMENT = 1008;
	public static final int DATA_SOURCE_CANT_EXECUTE_QUERY = 1009;
	public static final int GENERAL_SQL_ERROR = 1010;
	public static final int NO_DATA_FOR_ID = 1011;
	public static final int NO_DATA_OBJECT_CLASS_FOR_BEHAVIOUR = 1012;
	public static final int DATA_OBJECT_CLASS_INCOMPATIBLE_WITH_BEHAVIOUR = 1013;
	public static final int CANT_CREATE_DATA_OBJECT = 1014;
	public static final int CANT_LOAD_JDBC_DRIVER = 1015;
	public static final int DATA_OBJECT_CLASS_CANT_BE_REGISTERED = 1016;
	public static final int DATA_OBJECT_INSERT_NOT_SUCCESSFUL = 1017;
	public static final int DATA_OBJECT_CANT_INVOKE_METHOD = 1018;
	public static final int DATA_SOURCE_CANT_UNZIP_FILE = 1019;
	public static final int DATA_SOURCE_CANT_REMOVE_TMP_FILES = 1020;
	public static final int DATA_SOURCE_CANT_ZIP_FILES = 1021;
	public static final int DATA_OBJECT_NO_INITIAL_VALUE = 1022;
	public static final int DATA_SOURCE_ERROR_WHILE_CLOSING_CONNECTION = 1023;
	public static final int DATA_SOURCE_CANT_RENAME_FILE = 1024;
	public static final int DATA_SOURCE_CANT_DELETE_FILE = 1025;
	public static final int DATA_SOURCE_CANT_OPEN_MANIFEST = 1026;
	public static final int DATA_SOURCE_MANIFEST_ENTRY_MISSING = 1027;
	public static final int UNSUPPORTED_IMAGE_FORMAT_CONSTANT = 1028;
	public static final int INCORRECT_IMAGE_FILE_NAME = 1029;
	public static final int CANT_DELETE_FILE = 1030;
	public static final int UNSUPPORTED_ITEM_TYPE_VALUE = 1031;
	public static final int UNSUPPORTED_ITEM_TYPE_STRING = 1032;
	public static final int UNSUPPORTED_TAG_TYPE_VALUE = 1033;
	public static final int UNSUPPORTED_TAG_TYPE_STRING = 1034;
	public static final int TAG_GROUP_CANT_BE_A_SUBGROUP = 1035;
	public static final int CANT_ADD_CHILD_TO_ELEMENT = 1036;
	public static final int CANT_CREATE_FILE_FOR_EXPORT = 1037;
	public static final int GENERAL_IO_ERROR = 1038;
	public static final int UNSUPPORTED_DATA_TYPE_VALUE = 1039;
	public static final int CANT_EXPORT_THIS_DB_VERSION = 1040;
	public static final int CANT_OPEN_FILE_FOR_IMPORT = 1041;
	public static final int INCORRECT_FORMAT_OF_DATA_FILE = 1042;
	public static final int CANT_COPY_FILE = 1043;
	public static final int UNSUPPORTED_RATING_TYPE_VALUE = 1044;
	public static final int UNSUPPORTED_RATING_TYPE_STRING = 1045;
	public static final int RATING_GROUP_CANT_BE_A_SUBGROUP = 1046;
	public static final int CANT_PARSE_SCORE = 1047;
	public static final int SCORE_OUT_OF_RANGE = 1048;
	public static final int DATA_OBJECT_DISPOSED = 1049;
	public static final int UNSUPPORTED_ARTIST_TYPE_VALUE = 1050;
	public static final int UNSUPPORTED_ARTIST_TYPE_STRING = 1051;
	public static final int UNSUPPORTED_DATABASE_TYPE = 1052;
	public static final int CONNECTOR_DOES_NOT_SUPPORT_SERVERS = 1053;
	public static final int CONNECTOR_DOES_NOT_SUPPORT_FILES = 1054;
	public static final int CONNECTOR_CLOSED = 1055;
	public static final int OPERATION_SUPPORTED_IN_FILE_MODE_ONLY = 1056;
	public static final int DATA_FILE_VERSION_TOO_NEW = 1057;
	public static final int DB_TYPE_NOT_SUPPORTED_ON_THIS_PLATFORM = 1058;
	public static final int INCORRECT_LISTENER_TYPE = 1059;
	public static final int UNSUPPORTED_ARTIST_ROLE_TYPE = 1060;
    public static final int SOURCE_FILE_NOT_FOUND = 1061;
    public static final int SOURCE_IS_NOT_A_FILE = 1062;
    public static final int CANT_READ_SOURCE_FILE = 1063;
    public static final int UNSUPPORTED_RATABLE_TYPE = 1064;
    public static final int UNSUPPORTED_AUDITION_TYPE = 1065;
    
	public static final int NO_STRING_FOR_TAG = 2001;
	public static final int FILE_NOT_FOUND = 2002;
	public static final int CANT_READ_TEXT_FILE = 2003;
	public static final int CANT_CREATE_DIR = 2004;
	
	public static final int ERROR_INITIALIZING_WIDGETS = 3001;
	public static final int ERROR_INITIALIZING_PREFS_WINDOW = 3002;
    
    public static final int GENERAL_INTERNAL_ERROR = 99001;
    
    // a protected constructor to make this class non-instantiable
    // but allow inheritance
    protected Errors() {}
    
} 

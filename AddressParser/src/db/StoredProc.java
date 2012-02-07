package db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import oracle.jdbc.driver.OracleTypes;

/* Revision 6j */
public class StoredProc {
	private Connection conn;
	private String dbProductName;
	private HashMap<Object, Object> resultSetMap = new HashMap<Object, Object>();
	private String userName;

	// private DCILogger logger;

	public StoredProc(Connection conn, String userName) {
		this.conn = conn;
		// logger = DCILogger.instance();
		if (userName != null) {
			this.userName = new String(userName);
		} else {
			this.userName = new String("SYSTEM");
		}

		// this is ugly -- should be handled with either
		// a derived class or an interface... fix later!!!
		dbProductName = new String("Oracle");
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public ResultSet exec(SPArgs args) {
		// build jdbc-style call to
		// stored procedure in format:
		// "{ call ep_get_foo(?,?) }"
		//
		// if db is Oracle, the first parameter
		// will always be a result set rather
		// than returning the result set in the
		// exec() statement like any rational
		// database should. So there.
		if (dbProductName.equals("Oracle")) {
			return execOracle(args);
		} else {
			return execDefault(args);
		}
	}

	public ResultSet execnolog(SPArgs args) {
		if (dbProductName.equals("Oracle")) {
			return execOracleNolog(args);
		} else {
			return execDefault(args);
		}
	}

	public void close(ResultSet rs) {
		Statement statement = (Statement) resultSetMap.get(rs);
		if (statement != null) {
			try {
				rs.close();
				statement.close();
				// logger.log("db", "info", "close(): Statement " + statement +
				// " closed");
			} catch (Exception e) {
				// logger.log("db", "info",
				// "close(): Unable to close statement "+ statement);
			}
			resultSetMap.remove(rs);
		} else {
			// logger.log("db", "info",
			// "close(): Null statement - can't close");
		}
	}

	private ResultSet execOracle(SPArgs args) {
		// determine number of args
		int numArgs = 0;
		for (int i = 1; i < args.size(); i++) {
			String argVal = "arg" + i;
			Object value = args.get(argVal);

			if (value != null) {
				numArgs++;
			}
		}
		numArgs += 2; // always 2 default arguments (CURSOR and UserName)

		String sp = (String) args.get("sp");
		StringBuffer spString = new StringBuffer("{ call " + sp);
		StringBuffer logString = new StringBuffer(sp + "(");

		if (numArgs > 0) {
			for (int i = 1; i <= numArgs; i++) {
				if (i == 1) {
					spString.append("(?");
				} else {
					spString.append(",?");
				}
			}
			spString.append(")");
		}
		spString.append("}");

		CallableStatement cs = null;
		ResultSet rs = null;
		try {
			cs = conn.prepareCall(spString.toString());
			// logger.log("db", "info", "exec(): Statement " + cs +
			// " prepared");
			// set stored procedure arguments
			cs.registerOutParameter(1, OracleTypes.CURSOR);

			if (userName != null) {
				cs.setString(2, userName);
			} else {
				cs.setString(2, "SYSTEM");
			}

			logString.append(":CURSOR, '" + userName);
			for (int i = 3; i <= numArgs; i++) {
				String argVal = "arg" + (i - 2);
				Object value = args.get(argVal);

				if (value == null || value.toString().equals("NULLVarchar")) {
					logString.append("', 'NULLVarchar");
					cs.setNull(i, OracleTypes.VARCHAR);
				} else if (value.toString().equals("NULLInteger")) {
					logString.append("', 'NULLInteger");
					cs.setNull(i, OracleTypes.INTEGER);
				} else if (value.toString().equals("NULLDate")) {
					logString.append("', 'NULLDate");
					cs.setNull(i, OracleTypes.DATE);
				} else {
					logString.append("', '" + value);
					if (value instanceof java.util.Date) {
						cs.setTimestamp(i, new java.sql.Timestamp(((java.util.Date) value).getTime()));
					} else {
						cs.setString(i, value.toString());
					}
				}
			}
			logString.append("')");

			// logger.log("db", "info", logString.toString());

			cs.execute();
			rs = (ResultSet) cs.getObject(1);
			if (rs != null) {
				resultSetMap.put(rs, cs);
			}
		} catch (SQLException e) {
			// DBUtil.logSQLException(e, logger, "execOracle");
		} catch (Exception e) {
			// logger.log("db", "warn", "execOracle: " + e.getMessage());
		} finally {
			if (rs == null) {
				try {
					cs.close();
					// logger.log("db", "info", "exec(): Statement " + cs +
					// " closed");
				} catch (Exception e) {
					// logger.log("db", "warn", "execOracle: " +
					// e.getMessage());
				}
			}
		}

		return rs;
	}

	private ResultSet execOracleNolog(SPArgs args) {
		// determine number of args
		int numArgs = 0;
		for (int i = 1; i < args.size(); i++) {
			String argVal = "arg" + i;
			Object value = args.get(argVal);

			if (value != null) {
				numArgs++;
			}
		}
		numArgs += 2; // always 2 default arguments (CURSOR and UserName)

		String sp = (String) args.get("sp");
		StringBuffer spString = new StringBuffer("{ call " + sp);
		StringBuffer logString = new StringBuffer(sp + "(");

		if (numArgs > 0) {
			for (int i = 1; i <= numArgs; i++) {
				if (i == 1) {
					spString.append("(?");
				} else {
					spString.append(",?");
				}
			}
			spString.append(")");
		}
		spString.append("}");

		CallableStatement cs = null;
		ResultSet rs = null;
		try {
			cs = conn.prepareCall(spString.toString());
			// logger.log("db", "info", "exec(): Statement " + cs +
			// " prepared");
			// set stored procedure arguments
			cs.registerOutParameter(1, OracleTypes.CURSOR);

			if (userName != null) {
				cs.setString(2, userName);
			} else {
				cs.setString(2, "SYSTEM");
			}

			logString.append(":CURSOR, '" + userName);
			for (int i = 3; i <= numArgs; i++) {
				String argVal = "arg" + (i - 2);
				Object value = args.get(argVal);

				if (value == null || value.toString().equals("NULLVarchar")) {
					logString.append("', 'NULLVarchar");
					cs.setNull(i, OracleTypes.VARCHAR);
				} else if (value.toString().equals("NULLInteger")) {
					logString.append("', 'NULLInteger");
					cs.setNull(i, OracleTypes.INTEGER);
				} else if (value.toString().equals("NULLDate")) {
					logString.append("', 'NULLDate");
					cs.setNull(i, OracleTypes.DATE);
				} else {
					logString.append("', '" + value);
					if (value instanceof java.util.Date) {
						cs.setTimestamp(i, new java.sql.Timestamp(((java.util.Date) value).getTime()));
					} else {
						cs.setString(i, value.toString());
					}
				}
			}
			logString.append("')");

			cs.execute();
			rs = (ResultSet) cs.getObject(1);
			if (rs != null) {
				resultSetMap.put(rs, cs);
			}
		} catch (SQLException e) {
			// DBUtil.logSQLException(e, logger, "execOracle");
		} catch (Exception e) {
			// logger.log("db", "warn", "execOracle: " + e.getMessage());
		} finally {
			if (rs == null) {
				try {
					cs.close();
					// logger.log("db", "info", "exec(): Statement " + cs +
					// " closed");
				} catch (Exception e) {
					// logger.log("db", "warn", "execOracle: " +
					// e.getMessage());
				}
			}
		}

		return rs;
	}

	private ResultSet execDefault(SPArgs args) {
		// determine number of args
		int numArgs = 0;

		for (int i = 1; i < args.size(); i++) {
			String argVal = "arg" + i;
			String value = (String) args.get(argVal);

			if (value != null) {
				numArgs++;
			}
		}

		String sp = (String) args.get("sp");
		String spString = "{ call " + sp;
		if (numArgs > 0) {
			for (int i = 1; i <= numArgs; i++) {
				if (i == 1) {
					spString += "(?";
				} else {
					spString += ",?";
				}
			}
			spString += ")";
		}
		spString += "}";

		CallableStatement cs = null;
		try {
			cs = conn.prepareCall(spString);
			// set stored procedure arguments
			for (int i = 1; i <= numArgs; i++) {
				String argVal = "arg" + i;
				String value = (String) args.get(argVal);
				cs.setString(i, value);
			}
			cs.execute();

			ResultSet rs = cs.getResultSet();
			return rs;
		} catch (Exception e) {
			// logger.log("db", "warn", "execDefault: " + e.getMessage());
			return null;
		}
	}
}

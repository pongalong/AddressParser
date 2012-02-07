package db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionInfo {
	private static final String DBConfigFile = "dbconfig.properties";

	private Connection conn;

	private String dbType;
	private String dbDriver;
	private String dbServer;

	// [start] set and get methods

	public String getDbType() {
		return dbType;
	}

	public String getDbDriver() {
		return dbDriver;
	}

	public String getDbServer() {
		return dbServer;
	}

	public String getDbLogin() {
		return dbLogin;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public void setDbDriver(String dbDriver) {
		this.dbDriver = dbDriver;
	}

	public void setDbServer(String dbServer) {
		this.dbServer = dbServer;
	}

	public void setDbLogin(String dbLogin) {
		this.dbLogin = dbLogin;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	// [end] set and get methods

	private String dbLogin;
	private String dbPassword;
	private int maxConnections = 50;

	// [start] constructors

	public ConnectionInfo() throws IOException {
		this(DBConfigFile);
		try {
			Class.forName(dbDriver).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ConnectionInfo(String dbType, String dbDriver, String dbServer, String dbLogin, String dbPassword,
		int maxConnections) {
		this.dbType = dbType;
		this.dbDriver = dbDriver;
		this.dbServer = dbServer;
		this.dbLogin = dbLogin;
		this.dbPassword = dbPassword;
		this.maxConnections = maxConnections;
	}

	public ConnectionInfo(String configFile) throws IOException {
		if (configFile != null) {
			// Properties p = new Properties();
			// try {
			// p.load(new FileInputStream(configFile));
			// } catch (Exception e) {
			// throw new IOException("Cannot find connection config file "
			// + configFile);
			// }
			// this.dbType = (String) p.get("dbType");
			// this.dbDriver = (String) p.get("dbDriver");
			// this.dbServer = (String) p.get("dbServer");
			// this.dbLogin = (String) p.get("dbLogin");
			// this.dbPassword = (String) p.get("dbPassword");
			// Object maxConnObj = p.get("maxConnections");
			// if (maxConnObj != null) {
			// this.maxConnections = Integer.parseInt((String) maxConnObj);
			// }
			init(configFile);
		}
	}

	// [end] constructors

	// [start] public methods

	public synchronized Connection getConnection() {
		conn = null;
		try {
			conn = DriverManager.getConnection(getDbServer(), getDbLogin(), getDbPassword());
		} catch (Exception e) {
			System.out.println("Error obtaining database connection!! " + e.getMessage());
		}
		return conn;
	}

	public synchronized void releaseConnection() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException sql_ex) {
				System.out.println("SQL Exception thrown when closing connection..." + sql_ex.getMessage());
			} catch (Exception ex) {
				System.out.println("General Exception thrown when closing connection..." + ex.getMessage());

			} finally {
				conn = null;
			}
		}
	}

	// [end] public methods

	// [start] private methods

	private void init(String iConfigFile) {
		Properties props = new Properties();
		ClassLoader cl = ConnectionInfo.class.getClassLoader();
		InputStream in = cl.getResourceAsStream("Resources/" + iConfigFile);
		try {
			props.load(in);
			// dbHost
			// dbPort
			// dbUserName
			// dbPwd
			// dbSid

			dbType = props.getProperty("dbType");
			dbDriver = props.getProperty("dbDriver");
			dbServer = props.getProperty("dbServer");
			dbLogin = props.getProperty("dbLogin");
			dbPassword = props.getProperty("dbPassword");

			Class.forName(dbDriver).newInstance();
		} catch (Exception e) {
			System.out.println("Error loading DB Properties!!");

			dbType = "Oracle";
			dbDriver = "oracle.jdbc.driver.OracleDriver";
			dbServer = "jdbc:oracle:thin:@USCAELMUX04:1521:OMSPROD";
			dbLogin = "jserv";
			dbPassword = "artgoobens";
			try {
				Class.forName(dbDriver).newInstance();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	// [end] private methods

	// [start] get methods

	public String getDBType() {
		return dbType;
	}

	public String getDBDriver() {
		return dbDriver;
	}

	public String getDBServer() {
		return dbServer;
	}

	public String getDBLogin() {
		return dbLogin;
	}

	public String getDBPassword() {
		return dbPassword;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	// [end] get methods

	public static void main(String[] args) {
		System.out.println("Testing ATT Project ConnectionInfo class....");
		ConnectionInfo ci;
		try {
			ci = new ConnectionInfo();
			System.out.println("ConnectionInfo initialized...");
			System.out.println("**** ConnectionInfo.DBType            :: " + ci.getDbType());
			System.out.println("**** ConnectionInfo.DBServer          :: " + ci.getDbServer());
			System.out.println("**** ConnectionInfo.DBDriver          :: " + ci.getDbDriver());
			System.out.println("**** ConnectionInfo.DBLogin           :: " + ci.getDbLogin());
			System.out.println("**** ConnectionInfo.DBPassword        :: " + ci.getDbPassword());
			Connection conn = ci.getConnection();
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					System.out.println("General Exception thrown!! e.getMessage() = " + e.getMessage());
				}
			} else {
				conn = null;
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Done Testing ATT Project ConnectionInfo Class.");
	}

}

package com.search.google;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class JdbcUtils {
	
	//连接数�?�库的�?�数
	private static String url = null;
	private static String user = null;
	private static String driver = null;
	private static String password = null;
	
	private JdbcUtils () {

	}

	private static JdbcUtils instance = null;

	public static JdbcUtils getInstance() {
		if (instance == null) {
			synchronized (JdbcUtils.class) {
				if (instance == null) {
					instance = new JdbcUtils();
				}

			}
		}

		return instance;
	}
	
	//�?置文件
	private static Properties prop = new Properties();
	
	//注册驱动
	static {
		try {
			prop.load(new FileInputStream("config/database.properties"));
			url = prop.getProperty("db.host");
			user = prop.getProperty("db.user");
			driver = prop.getProperty("db.driver");
			password = prop.getProperty("db.pass");
			
			Class.forName(driver);
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//该方法获得连接
	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url, user, password);
	}
	
	//释放资�?
	public void free(Connection conn, Statement st, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				 
				e.printStackTrace();
			} finally {
				if (st != null) {
					try {
						st.close();
					} catch (SQLException e) {
						 
						e.printStackTrace();
					} finally {
						if (conn != null) {
							try {
								conn.close();
							} catch (SQLException e) {
								 
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}
	 public  void freeAll(Object... objs) { 
         for (Object obj : objs) { 
                 if (obj instanceof Connection) close((Connection) obj); 
                 if (obj instanceof Statement) close((Statement) obj); 
                 if (obj instanceof ResultSet) close((ResultSet) obj); 
         } 
    } 
	 public static void close(Connection conn) { 
         if (conn != null) 
                 try { 
                         conn.close(); 
                 } catch (SQLException e) { 
                         e.printStackTrace(); 
                 } 
	 } 
	
	 public static void close(ResultSet rs) { 
	         if (rs != null) 
	                 try { 
	                         rs.close(); 
	                 } catch (SQLException e) { 
	                         e.printStackTrace(); 
	                 } 
	 } 
	
	 public static void close(Statement stmt) { 
	         if (stmt != null) 
	                 try { 
	                         stmt.close(); 
	                 } catch (SQLException e) { 
	                         e.printStackTrace(); 
	                 } 
	 } 
}
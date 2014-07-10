package com.search.google;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class SqlHelper {
	
	//瀹氫箟闇��鐨勫?橀�
	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;
	
	JdbcUtils jdbcUtils = JdbcUtils.getInstance();
	
	//璇ユ柟娉曟墽琛屼竴涓猽pdate/delete/insert璇?�
	//sql璇?ユ槸甯﹂棶�风殑鏍煎�锛屽锛歶pdate table_name set column_name = ? where ...
	//parameters = {"...", "..."...}锛�
	public void executeUpdate(String sql, String[] parameters) {
			try {
				conn = jdbcUtils.getConnection();
				ps = conn.prepareStatement(sql);
				//缁欙紵璧嬪�
				if (parameters != null) {
					for (int i=0; i<parameters.length; i++) {
						ps.setString(i+1, parameters[i]);
					}
				}
				//鎵ц璇?�
				ps.executeUpdate();
				
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} finally {
				//鍏抽棴璧勬�
				jdbcUtils.free(conn, ps, rs);
			}
	}
	
	//�互鎵ц澶氫釜update銆?delete銆?insert璇?ワ紙鑰冭檻浜嬪姟锛�
	public void executeUpdate(String[] sqls, String[][] parameters) {
		try {
			//寰楀埌杩炴帴
			conn = jdbcUtils.getConnection();
			//澶氫釜sql璇?ワ紝鑰冭檻浜嬪姟
			conn.setAutoCommit(false);
			
			for (int i=0; i<sqls.length; i++) {
				ps = conn.prepareStatement(sqls[i]);
				System.out.println(sqls[i]);
				if (parameters!=null && parameters[i] != null) {
					
					for (int j=0; j<parameters[i].length; j++) {
						ps.setString(j+1, parameters[i][j]);
					}
					
				}
				ps.executeUpdate();
			}
			
			conn.commit();
		} catch (SQLException e) {
			//鍥炴粴
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} finally {
			jdbcUtils.free(conn, ps, rs);
		}
	}
	
		//缁熶竴鐨剆elect璇?ワ紝涓轰簡鑳藉璁块棶缁撴灉闆嗭紝灏嗙粨鏋滈泦鏀惧叆ArrayList锛岃繖鏍峰?互鐩存帴鍏抽棴璧勬�
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<Map<String,Object>> executeQuery(String sql, String[] parameters) {
		ArrayList<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
		
		try {
			conn = jdbcUtils.getConnection();
			ps = conn.prepareStatement(sql);
			
			if (parameters != null) {
				for (int i=0; i<parameters.length; i++) {
					ps.setString(i+1, parameters[i]);
				}
			}
			
			rs = ps.executeQuery();
			
			ResultSetMetaData rsmd = rs.getMetaData();
			int column = rsmd.getColumnCount();
			Map<String,Object> resultColumnMap = null;
			
			while (rs.next()) {
			//	Object[] objects = new Object[column];
				resultColumnMap = new HashMap<String,Object>();
				for (int i=1; i<=column; i++) {
			//		objects[i-1] = rs.getObject(i);
					resultColumnMap.put(rsmd.getColumnName(i), rs.getObject(i));
				}
				
				results.add(resultColumnMap);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} finally {
			jdbcUtils.free(conn, ps, rs);
		}
		return results; 
	}
	
}
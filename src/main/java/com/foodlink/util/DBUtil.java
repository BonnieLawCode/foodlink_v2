package com.foodlink.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
	private static final String URL = "jdbc:mysql://localhost:3306/foodlink_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Tokyo";
	private static final String USER = "root";
	private static final String PASS = "";

	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("MySQL Driver not found", e);
		}
	}

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASS);
	}
}

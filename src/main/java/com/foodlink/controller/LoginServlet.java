package com.foodlink.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.foodlink.util.DBUtil;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LoginServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		request.setCharacterEncoding("UTF8");

		request.getRequestDispatcher("/WEB-INF/views/common/login.jsp").forward(request, response);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub

		request.setCharacterEncoding("UTF-8");
		String email = request.getParameter("email");
		String password = request.getParameter("password");

		String sql = "SELECT id, name, role, password_hash FROM users WHERE email = ?";

		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, email);
			System.out.println("database is running");
			System.out.println(ps.toString());
			//验证账号是否存在
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					request.setAttribute("error", "メールアドレスまたはパスワードが違います。");
					doGet(request, response);
					return;
				}
				//账号存在，则获取数据库中存储的密码
				String dbPass = rs.getString("password_hash");

				// MVP：先用明文对比跑通（后面我带你换 BCrypt）
				if (dbPass == null || !dbPass.equals(password)) {
					request.setAttribute("error", "メールアドレスまたはパスワードが違います。");
					doGet(request, response);
					return;
				}
				//账号密码验证成功，创建session
				HttpSession session = request.getSession(true);
				int userId = rs.getInt("id");
				session.setAttribute("userId", userId);
				String userName = rs.getString("name");
				String role = rs.getString("role");
				session.setAttribute("role", role);
				
				// JP：商家なら会社名も保存（sidebar表示用）
				// CN：如果是商家，把店名/公司名也存进去（用于 sidebar 显示）
				if ("PROVIDER".equals(role)) {
					session.setAttribute("providerName", userName); // 会社名
				}
				if ("RECEIVER".equals(role)) {
					session.setAttribute("userName", userName); // user name
					
				}
				//转发到下一个HomeServlet继续处理

				response.sendRedirect(request.getContextPath() + "/home");
			}

		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
}

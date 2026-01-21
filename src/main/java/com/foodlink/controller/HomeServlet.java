package com.foodlink.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.List;

import com.foodlink.dao.FoodDao;
import com.foodlink.model.Food;

/**
 * Servlet implementation class HomeServlet
 */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public HomeServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF8");

		// JP/CN: 判断是否企业用户，企业登录后直接进仪表盘；其他情况展示公开商品
		HttpSession session = request.getSession(false);
		if (session != null) {
			String role = (String) session.getAttribute("role");
			if ("PROVIDER".equals(role)) {
				// JP：企業ユーザーはダッシュボードへ / CN：商家进入仪表盘
				response.sendRedirect(request.getContextPath() + "/provider/dashboard");
				return;
			}
		}

		// JP/CN: 个人用户或未登录时，都展示同一套公开商品（OPEN & 未过期 & 有库存）
		String keyword = request.getParameter("keyword");
		String category = request.getParameter("category");
		String area = request.getParameter("area");
		String sort = request.getParameter("sort"); // "new" or "price"

		FoodDao dao = new FoodDao();
		List<Food> foodList = dao.findOpenFoodsForReceiver(keyword, category, area, sort);

		request.setAttribute("foodList", foodList);
		request.setAttribute("keyword", keyword);
		request.setAttribute("category", category);
		request.setAttribute("area", area);
		request.setAttribute("sort", sort);

		// 无论登录与否都 forward 到同一首页
		request.getRequestDispatcher("/WEB-INF/views/receiver/index.jsp")
				.forward(request, response);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

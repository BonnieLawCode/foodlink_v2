package com.foodlink.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		request.setCharacterEncoding("UTF8");

		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		//从session取出role进行身份判断，登陆成功跳转到相应的页面    
		String role = (String) session.getAttribute("role");

		if ("PROVIDER".equals(role)) {
			System.out.println("企業ユーザー登録");

			request.getRequestDispatcher("/WEB-INF/views/provider/dashboard.jsp")
					.forward(request, response);
		} else {
			System.out.println("個人ユーザー登録");
			request.getRequestDispatcher("/WEB-INF/views/receiver/index.jsp")
					.forward(request, response);
		}
		/********************
		JP：未ログイン時の遷移先（今は“強制ログイン方針”のため基本的に使わない）
		現在のFoodLinkは LoginServlet を入口にしており、ユーザーはログインしないと
		 receiver/index.jsp や provider/dashboard.jsp に到達できない設計。
		 そのため通常運用ではここに来ない（保険・将来拡張用の分岐）。
		 将来的に「未ログインでも閲覧できるトップページ（商品検索・サービス説明）」を
		     用意する場合は、/home 未ログインアクセスを top.jsp に forward して利用する。
		
		 CN：未登录时的跳转目的地（目前采用“强制登录策略”，基本不会走到这里）
		 现在 FoodLink 的入口是 LoginServlet，用户不登录就无法进入
		 receiver/index.jsp 或 provider/dashboard.jsp。
		 因此正常使用时这里不会触发（属于保险/未来扩展预留）。
		 如果将来要实现“未登录也能浏览的公共首页（商品检索/服务介绍）”，
		 就可以把 /home 的未登录访问 forward 到 top.jsp 来启用该页面。
		 *****************************/
		// request.getRequestDispatcher("/WEB-INF/views/common/top.jsp")
		//		        .forward(request, response);

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

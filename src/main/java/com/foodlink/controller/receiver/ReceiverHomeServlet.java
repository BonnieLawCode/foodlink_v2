package com.foodlink.controller.receiver;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.foodlink.dao.FoodDao;
import com.foodlink.model.Food;

/**
 * Servlet implementation class ReceiverHomeServlet
 */
/**
 * JP：受取者ホーム（商品一覧）を表示するServlet
 * CN：受取者主页（商品列表）显示用 Servlet
 */
@WebServlet("/receiver/home")
public class ReceiverHomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ReceiverHomeServlet() {
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

		/**JP：検索パラメータ（今はUIだけ先に用意。SQLは最小でOK）**/
		String keyword = request.getParameter("keyword");
		String category = request.getParameter("category");
		String area = request.getParameter("area");
		String sort = request.getParameter("sort"); // "new" or "price"
		/**「OPEN の商品一覧」をDBから取得**/
		FoodDao dao = new FoodDao();
		List<Food> foodList = dao.findOpenFoodsForReceiver(keyword, category, area, sort);

		request.setAttribute("foodList", foodList);

		request.setAttribute("keyword", keyword);
		request.setAttribute("category", category);
		request.setAttribute("area", area);
		request.setAttribute("sort", sort);

		request.getRequestDispatcher("/WEB-INF/views/receiver/index.jsp").forward(request, response);

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

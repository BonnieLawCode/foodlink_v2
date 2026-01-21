package com.foodlink.controller.receiver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.foodlink.dao.FoodDao;
import com.foodlink.dao.FoodDao.FoodDetailView;

/**
 * Servlet implementation class FoodDetailServlet
 */
/**
 * 【日本語】受取者：商品詳細を表示する
 *
 * URL: /receiver/foods/detail?id=123
 */
@WebServlet("/receiver/foods/detail")
public class FoodDetailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final FoodDao foodDao = new FoodDao();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FoodDetailServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		
		// 強制ログインチェック（session）
		HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userName") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        //パラメータチェック(参数校验)
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/receiver/index");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/receiver/index");
            return;
        }
        
     // DBから1件取得
        FoodDetailView food = foodDao.findDetailWithCompany(id);

        if (food == null) {
            request.setAttribute("message", "商品が見つかりませんでした。");
            request.getRequestDispatcher("/WEB-INF/views/common/notfound.jsp").forward(request, response);
            return;
        }

        request.setAttribute("food", food);
        request.getRequestDispatcher("/WEB-INF/views/receiver/foods/detail.jsp").forward(request, response);       
        
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

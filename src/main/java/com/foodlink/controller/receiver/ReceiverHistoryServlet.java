package com.foodlink.controller.receiver;

import java.io.IOException;
import java.util.List;

import com.foodlink.dao.ReservationDao;
import com.foodlink.dao.ReservationDao.ReservationView;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class ReceiverHistoryServlet
 */

@WebServlet("/receiver/history")
public class ReceiverHistoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ReceiverHistoryServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// response.getWriter().append("Served at: ").append(request.getContextPath());

		// JP：未ログインならログインへ / CN：未登录则跳转登录
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userName") == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		// 查询该登录用户的预约并传递给 JSP
		Integer receiverId = (Integer) session.getAttribute("receiverId");
		if (receiverId == null) {
			receiverId = (Integer) session.getAttribute("userId");
		}

		if (receiverId != null) {
			ReservationDao dao = new ReservationDao();
			List<ReservationView> reservations = dao.findByReceiverId(receiverId);
			request.setAttribute("reservations", reservations);
		}

		request.getRequestDispatcher("/WEB-INF/views/receiver/history.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

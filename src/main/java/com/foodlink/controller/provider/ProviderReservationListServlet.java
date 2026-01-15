package com.foodlink.controller.provider;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.foodlink.dao.ReservationDao;
import com.foodlink.dao.ReservationDao.ProviderReservationRow;

/**
 * Servlet implementation class ProviderReservationListServlet
 */
@WebServlet("/provider/reservations")
public class ProviderReservationListServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ProviderReservationListServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute("activeMenu", "reservations");

		// JP：ログイン・権限チェック / CN：登录与权限检查
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		Integer providerId = (Integer) session.getAttribute("userId");
		String role = (String) session.getAttribute("role");
		if (providerId == null || role == null || !"PROVIDER".equals(role)) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		// JP：簡易フィルタ / CN：简单过滤
		String status = trim(request.getParameter("status"));
		String pickupDate = trim(request.getParameter("pickupDate"));

		ReservationDao dao = new ReservationDao();
		List<ProviderReservationRow> reservations = dao.findProviderReservations(providerId, status, pickupDate);
		request.setAttribute("reservations", reservations);
		request.setAttribute("status", status == null ? "ALL" : status);
		request.setAttribute("pickupDate", pickupDate);

		request.getRequestDispatcher("/WEB-INF/views/provider/reservation_list.jsp")
				.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private String trim(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}

package com.foodlink.controller.provider;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.foodlink.dao.ReservationDao;
import com.foodlink.dao.ReservationDao.ProviderReservationStatusInfo;

/**
 * JP：受取済みに更新（商家用）
 * CN：商家端确认受取
 */
@WebServlet("/provider/reservations/pickedUp")
public class ProviderReservationPickedUpServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final ReservationDao reservationDao = new ReservationDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

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

        String sRid = request.getParameter("reservationId");
        int reservationId = parseInt(sRid, -1);
        if (reservationId <= 0) {
            response.sendRedirect(request.getContextPath() + "/provider/reservations?error=system");
            return;
        }

        try {
            // JP：所有権と状態を事前チェック / CN：先检查所有权与状态
            ProviderReservationStatusInfo info = reservationDao.findProviderReservationStatus(reservationId, providerId);
            if (info == null) {
                response.sendRedirect(request.getContextPath() + "/provider/reservations?error=not_found");
                return;
            }
            if (!"RESERVED".equalsIgnoreCase(info.status)) {
                response.sendRedirect(request.getContextPath() + "/provider/reservations?error=status_changed");
                return;
            }

            boolean ok = reservationDao.markReservationPickedUp(reservationId, providerId);
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/provider/reservations?msg=picked_up_success");
            } else {
                response.sendRedirect(request.getContextPath() + "/provider/reservations?error=status_changed");
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/provider/reservations?error=system");
        }
    }

    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }
}

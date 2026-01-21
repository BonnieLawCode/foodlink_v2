package com.foodlink.controller.receiver;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.foodlink.dao.ReservationDao;
import com.foodlink.dao.ReservationDao.ReservationView;

/**
 * JP：予約詳細（受取者用）
 * CN：预约详情（用户端）
 */
@WebServlet("/receiver/reservation/detail")
public class ReservationDetailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final ReservationDao reservationDao = new ReservationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // JP：ログイン・権限チェック / CN：登录与权限检查
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Integer receiverId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (receiverId == null || role == null || !"RECEIVER".equals(role)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String code = trim(req.getParameter("code"));
        String idParam = trim(req.getParameter("id"));

        ReservationView v = null;
        if (code != null) {
            v = reservationDao.findReservationViewByCodeAndReceiverId(code, receiverId);
        } else {
            int rid = parseInt(idParam, -1);
            if (rid > 0) {
                v = reservationDao.findReservationViewByIdAndReceiverId(rid, receiverId);
            }
        }

        if (v == null) {
            resp.sendRedirect(req.getContextPath() + "/receiver/history?error=not_found");
            return;
        }

        // JP：表示用日時 / CN：显示用日期时间
        String pickupDisplay = "（未指定）";
        if (v.pickupTime != null) {
            var ldt = v.pickupTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            pickupDisplay = ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }

        req.setAttribute("rv", v);
        req.setAttribute("pickupDisplay", pickupDisplay);
        req.getRequestDispatcher("/WEB-INF/views/receiver/foods/reserve_complete.jsp")
           .forward(req, resp);
    }

    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    private String trim(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

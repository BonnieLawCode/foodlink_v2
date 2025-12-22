package com.foodlink.controller.receiver;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.foodlink.dao.ReservationDao;
import com.foodlink.dao.ReservationDao.ReservationView;

/**
 * JP：予約完了画面
 * CN：预约完成页
 */
@WebServlet("/receiver/reserve/complete")
public class ReserveCompleteServlet extends HttpServlet {

    private final ReservationDao reservationDao = new ReservationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        int rid = parseInt(req.getParameter("rid"), -1);
        if (rid <= 0) {
            resp.sendRedirect(req.getContextPath() + "/receiver/home");
            return;
        }

        ReservationView v = reservationDao.findReservationViewById(rid);
        if (v == null) {
            resp.sendRedirect(req.getContextPath() + "/receiver/home");
            return;
        }

        // pickup display：本日 hh:mm / yyyy-mm-dd hh:mm
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
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}

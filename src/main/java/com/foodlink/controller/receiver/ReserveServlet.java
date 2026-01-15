package com.foodlink.controller.receiver;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.foodlink.dao.FoodDao;
import com.foodlink.dao.ReservationDao;
import com.foodlink.model.Food;

/**
 * JP：予約確認＆予約確定（GET=確認画面 / POST=確定）
 * CN：预约确认&提交（GET显示确认页 / POST提交入库）
 */
@WebServlet("/receiver/reserve")
public class ReserveServlet extends HttpServlet {

    private final FoodDao foodDao = new FoodDao();
    private final ReservationDao reservationDao = new ReservationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // 1) 登录检查（你项目里 session key 可能不同，按实际改）
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // JP：受取者IDの取り方はあなたの実装に合わせて修正
        // CN：receiverId 的 session key 按你项目实际修改
        Integer userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null || role == null || !"RECEIVER".equals(role)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int receiverId = userId;
        System.out.println("receiverId" + receiverId);

        // 2) 读取 food
        int foodId = parseInt(req.getParameter("id"), -1);
        if (foodId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/receiver/home");
            return;
        }

        Food f = foodDao.findById(foodId);
        if (f == null) {
            resp.sendRedirect(req.getContextPath() + "/receiver/home");
            return;
        }

        // 3) 预处理“本日 14:15～22:20”显示
        // JP：pickupStart/pickupEnd が同日かつ今日なら「本日」表示
        // CN：如果同一天且是今天 → 显示“本日”
        String pickupLabel = buildPickupLabel(f.getPickupStart(), f.getPickupEnd());

        req.setAttribute("food", f);
        req.setAttribute("pickupLabel", pickupLabel);

        // JSP：/WEB-INF/views/receiver/foods/reserve.jsp
        req.getRequestDispatcher("/WEB-INF/views/receiver/foods/reserve.jsp")
                .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Integer receiverId = (Integer) session.getAttribute("receiverId");
        System.out.println(receiverId);
        if (receiverId == null)
            receiverId = (Integer) session.getAttribute("userId");
        if (receiverId == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 记录收到的 POST 参数以便调试
        String pFoodId = req.getParameter("foodId");
        String pId = req.getParameter("id");
        String pQty = req.getParameter("qty");
        String pPickupTime = req.getParameter("pickupTime");
        String pTotal = req.getParameter("totalPrice");
        System.out.println("ReserveServlet POST params: foodId=" + pFoodId + ", id=" + pId + ", qty=" + pQty
                + ", pickupTime=" + pPickupTime + ", totalPrice=" + pTotal);

        int foodId = parseInt(pFoodId, -1);
        // 如果前端传的是 id 而不是 foodId，回退处理
        if (foodId <= 0) {
            foodId = parseInt(pId, -1);
        }

        int qty = parseInt(pQty, 1);

        if (foodId <= 0 || qty <= 0) {
            resp.sendRedirect(req.getContextPath() + "/receiver/home");
            return;
        }

        Food f = foodDao.findById(foodId);
        if (f == null) {
            resp.sendRedirect(req.getContextPath() + "/receiver/home");
            return;
        }

        // 单价用特价（price_offer）
        int unitPrice = f.getPriceOffer();

        // pickupTime：从前端接收 datetime-local（格式: yyyy-MM-dd'T'HH:mm）
        Timestamp pickupTime = null;
        String pickupTimeStr = req.getParameter("pickupTime");
        if (pickupTimeStr != null && !pickupTimeStr.isEmpty()) {
            try {
                // 前端为 datetime-local，形如 2025-12-22T15:30
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                LocalDateTime ldt = LocalDateTime.parse(pickupTimeStr, dtf);
                pickupTime = Timestamp.valueOf(ldt);
            } catch (Exception ex) {
                // 解析失败时继续使用 null（或根据需求返回错误）
                System.err.println("pickupTime parse error: " + pickupTimeStr + " -> " + ex.getMessage());
                pickupTime = null;
            }
        }

        // 创建预约（内部会扣库存 + insert + 生成 reservation_code）
        int reservationId = reservationDao.createReservation(foodId, receiverId, qty, unitPrice, pickupTime);

        // redirect 到完成页
        resp.sendRedirect(req.getContextPath() + "/receiver/reserve/complete?rid=" + reservationId);
    }

    // =========================
    // helpers
    // =========================
    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    private String buildPickupLabel(Timestamp start, Timestamp end) {

        if (start == null || end == null)
            return "（時間未設定）";

        LocalDateTime s = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime e = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        String timePart = s.format(timeFmt) + "～" + e.format(timeFmt);

        LocalDate today = LocalDate.now();
        if (s.toLocalDate().equals(today) && e.toLocalDate().equals(today)) {
            return "本日 " + timePart;
        }

        // 非当天：显示日期 + 时间
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return s.format(dateFmt) + " " + timePart;
    }
}

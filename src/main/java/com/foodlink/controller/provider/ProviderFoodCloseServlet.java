package com.foodlink.controller.provider;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.foodlink.dao.FoodDao;

/**
 * JP：在庫管理：販売終了（OPEN -> CLOSED）
 * CN：库存管理：销售结束（OPEN -> CLOSED）
 */
@WebServlet("/provider/foods/close")
public class ProviderFoodCloseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final FoodDao foodDao = new FoodDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        String role = (String) session.getAttribute("role");
        if (role == null || !"PROVIDER".equals(role.trim())) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        int providerId = (int) session.getAttribute("userId");

        int foodId = parseInt(request.getParameter("foodId"), -1);
        if (foodId <= 0) {
            response.sendRedirect(request.getContextPath() + "/provider/foods?error=system");
            return;
        }

        try {
            boolean ok = foodDao.closeFood(foodId, providerId);
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/provider/foods?msg=close_success");
            } else {
                response.sendRedirect(request.getContextPath() + "/provider/foods?error=not_found");
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/provider/foods?error=system");
        }
    }

    private int parseInt(String v, int def) {
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return def;
        }
    }
}

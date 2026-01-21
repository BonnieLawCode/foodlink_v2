package com.foodlink.controller.provider;

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
 * Servlet implementation class ProviderFoodListServlet
 */
/**
 * 商家：在庫管理（商品一覧）
 * URL: GET /provider/foods
 */
@WebServlet("/provider/foods")
public class ProviderFoodListServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// DAO：负责与数据库交互（JDBC）
    private final FoodDao foodDao = new FoodDao();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ProviderFoodListServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// ==========================
        // 1) 登录检查（ログインチェック / 登录校验）
        // ==========================
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            // 未登录 → 回到登录页
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // ==========================
        // 2) 角色检查（権限チェック / 权限校验）
        // ==========================
        String role = (String) session.getAttribute("role");
        if (role == null || !"PROVIDER".equals(role.trim())) {
            // 不是企业账号 → 回到 home
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        // 当前企业用户ID（对应 food.provider_id）
        int providerId = (int) session.getAttribute("userId");

        // ==========================
        // 3) 读取筛选参数（Filter Bar）/ 读取筛选参数
        // ==========================
        String view = trim(request.getParameter("view"));
        String status = trim(request.getParameter("status"));
        String q = trim(request.getParameter("q"));
        String daysParam = trim(request.getParameter("days"));
        String pageParam = trim(request.getParameter("page"));
        String sizeParam = trim(request.getParameter("size"));

        if (view == null) {
            view = "recent";
        }
        if (status == null) {
            status = "ALL";
        }
        int days = parsePositiveInt(daysParam, 7);
        int page = parsePositiveInt(pageParam, 1);
        int size = parsePositiveInt(sizeParam, 5);
        if (size > 100) {
            size = 100;
        }

        // 防御性修正：修复过期/库存为0但仍OPEN的脏数据
        foodDao.fixStatusesForProvider(providerId);

        // ==========================
        // 4) 查询该企业的商品列表
        // ==========================
        List<Food> foods = foodDao.findByProviderIdWithFiltersPaged(providerId, view, status, days, q, page, size);
        int totalCount = foodDao.countByProviderIdWithFilters(providerId, view, status, days, q);
        int totalPages = (int) Math.ceil(totalCount / (double) size);

        // JSP 使用的数据
        request.setAttribute("foods", foods);
        request.setAttribute("msg", request.getParameter("msg"));
        request.setAttribute("error", request.getParameter("error"));
        request.setAttribute("view", view);
        request.setAttribute("status", status);
        request.setAttribute("days", days);
        request.setAttribute("q", q == null ? "" : q);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", size);
        request.setAttribute("totalPages", totalPages);

        // ==========================
        // 5) forward 到 JSP（WEB-INF 内 JSP 不能直访问）
        // ==========================
        request.getRequestDispatcher("/WEB-INF/views/provider/food_list.jsp")
               .forward(request, response);
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private int parsePositiveInt(String value, int def) {
        try {
            int v = Integer.parseInt(value);
            return v > 0 ? v : def;
        } catch (Exception e) {
            return def;
        }
    }
	


}

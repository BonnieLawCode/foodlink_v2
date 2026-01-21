package com.foodlink.controller.provider;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.foodlink.dao.ProductDao;
import com.foodlink.dao.ProductDao.ProductRow;

/**
 * 商家：商品管理（products 一覧）
 * URL: GET /provider/products
 */
@WebServlet("/provider/products")
public class ProviderProductsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final ProductDao productDao = new ProductDao();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute("activeMenu", "products");

		// ログイン・権限チェック
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		String role = (String) session.getAttribute("role");
		if (!"PROVIDER".equals(role)) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}
		int providerId = (int) session.getAttribute("userId");

		// パラメータ
		String q = trim(request.getParameter("q"));
		String pageParam = trim(request.getParameter("page"));
		String sizeParam = trim(request.getParameter("size"));
		String focusProductId = trim(request.getParameter("focusProductId"));

		int page = parsePositiveInt(pageParam, 1);
		int size = parsePositiveInt(sizeParam, 10);
		if (size > 100)
			size = 100;

		List<ProductRow> products = productDao.findByProvider(providerId, q, page, size);
		int totalCount = productDao.countByProvider(providerId, q);
		int totalPages = (int) Math.ceil(totalCount / (double) size);

		request.setAttribute("products", products);
		request.setAttribute("q", q == null ? "" : q);
		request.setAttribute("currentPage", page);
		request.setAttribute("pageSize", size);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("totalCount", totalCount);
		request.setAttribute("focusProductId", focusProductId);

		request.getRequestDispatcher("/WEB-INF/views/provider/products.jsp").forward(request, response);
	}

	private String trim(String s) {
		if (s == null)
			return null;
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}

	private int parsePositiveInt(String v, int def) {
		try {
			int x = Integer.parseInt(v);
			return x > 0 ? x : def;
		} catch (Exception e) {
			return def;
		}
	}
}

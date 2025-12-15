package com.foodlink.controller.provider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import com.foodlink.dao.FoodDao;

/**
 * Servlet implementation class ProviderFoodCreateServlet
 */

@WebServlet({ "/provider/foods/new", "/provider/foods/create" })
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5L * 1024 * 1024, maxRequestSize = 10L * 1024 * 1024)
public class ProviderFoodCreateServlet extends HttpServlet {

    private final FoodDao foodDao = new FoodDao();

    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ProviderFoodCreateServlet() {
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

        request.getRequestDispatcher("/WEB-INF/views/provider/food_new.jsp").forward(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        request.setCharacterEncoding("UTF-8");
        // セッション / Session
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

        // 入力値
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String category = request.getParameter("category");

        int quantity = Integer.parseInt(request.getParameter("quantity"));
        String unit = request.getParameter("unit");

        String pn = request.getParameter("price_normal");
        Integer priceNormal = (pn == null || pn.isBlank()) ? null : Integer.valueOf(pn);

        int priceOffer = Integer.parseInt(request.getParameter("price_offer"));
        String currency = "JPY"; // 你的表默认就是 JPY

        String pickupLocation = request.getParameter("pickup_location");

        Timestamp pickupStart = parseDatetimeLocal(request.getParameter("pickup_start"));
        Timestamp pickupEnd = parseDatetimeLocal(request.getParameter("pickup_end"));
        Date expiryDate = parseDate(request.getParameter("expiry_date"));

        // 图片上传保存
        String imagePath = saveImageIfPresent(request);

        // status 用默认 OPEN，created_at 用默认 CURRENT_TIMESTAMP（不写入也行）
        foodDao.insertFood(providerId, name, description, category,
                quantity, unit, priceNormal, priceOffer, currency,
                expiryDate, pickupLocation, pickupStart, pickupEnd,
                imagePath);

        // 登録後は在庫一覧へ戻す（確認しやすい）
        // 注册成功后回到库存列表（最方便确认）
        response.sendRedirect(request.getContextPath() + "/provider/foods");

    }

    private static Timestamp parseDatetimeLocal(String v) {
        if (v == null || v.isBlank())
            return null;
        // v 形如 "2025-12-12T19:30"
        LocalDateTime ldt = LocalDateTime.parse(v);
        return Timestamp.valueOf(ldt);
    }

    private static Date parseDate(String v) {
        if (v == null || v.isBlank())
            return null;
        LocalDate d = LocalDate.parse(v);
        return Date.valueOf(d);
    }

    private String saveImageIfPresent(HttpServletRequest request) throws IOException, ServletException {
        Part imagePart = request.getPart("image");
        if (imagePart == null || imagePart.getSize() == 0)
            return null;

        String submitted = Paths.get(imagePart.getSubmittedFileName()).getFileName().toString();
        String ext = "";
        int dot = submitted.lastIndexOf('.');
        if (dot >= 0)
            ext = submitted.substring(dot).toLowerCase();

        if (!(ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") || ext.equals(".webp"))) {
            // 不符合时：直接不保存，也可以改为抛错返回页面
            return null;
        }

        String uploadDirReal = getServletContext().getRealPath("/uploads/products");
        File dir = new File(uploadDirReal);
        if (!dir.exists())
            dir.mkdirs();

        String safeName = UUID.randomUUID().toString().replace("-", "") + ext;
        File saveFile = new File(dir, safeName);

        imagePart.write(saveFile.getAbsolutePath());

        // 存进 DB 的相对路径（以后页面直接 <img src="${ctx}${image_path}">）
        return "/uploads/products/" + safeName;

    }

}

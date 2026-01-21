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
import com.foodlink.model.Food;

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

        // JP：コピー時はログイン・権限チェック / CN：复制时做登录与权限检查
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

        String mode = request.getParameter("mode");
        String productParam = request.getParameter("productId");
        // 如果显式带了 productId 但 mode 未指定，则视为再出品
        if ((mode == null || mode.isBlank()) && productParam != null && !productParam.isBlank()) {
            mode = "RELIST";
        }
        if (mode == null || mode.isBlank()) {
            mode = "NEW";
        }
        request.setAttribute("mode", mode);

        // JP：コピー元がある場合はフォームに初期値を入れる / CN：有复制源时预填表单
        String copyFrom = request.getParameter("copyFrom");
        if ("RELIST".equalsIgnoreCase(mode) && productParam != null && !productParam.isBlank()) {
            try {
                int productId = Integer.parseInt(productParam);
                com.foodlink.dao.ProductDao pdao = new com.foodlink.dao.ProductDao();
                com.foodlink.dao.ProductDao.ProductRow p = pdao.findByIdAndProvider(productId, providerId);
                if (p == null) {
                    // 兼容旧数据：通过 foods 归属校验
                    p = pdao.findByIdForProviderViaFoods(productId, providerId);
                }
                if (p != null) {
                    request.setAttribute("copyProductId", p.id);
                    request.setAttribute("copyName", p.name);
                    request.setAttribute("copyDescription", p.description);
                    request.setAttribute("copyCategory", p.category);
                    request.setAttribute("copyPriceNormal", p.priceNormal);
                    request.setAttribute("copyImagePath", p.imagePath);
                    request.setAttribute("copyQuantity", 0);
                    // 默认受取時間：今天 ~ 23:59
                    java.time.LocalDate today = java.time.LocalDate.now();
                    java.time.LocalDateTime start = java.time.LocalDateTime.now();
                    java.time.LocalDateTime end = java.time.LocalDateTime.of(today, java.time.LocalTime.of(23, 59));
                    // JP：JSP側は fmt:formatDate で Timestamp を整形する
                    // CN：JSP 通过 fmt:formatDate 格式化 Timestamp
                    request.setAttribute("copyPickupStart", java.sql.Timestamp.valueOf(start));
                    request.setAttribute("copyPickupEnd", java.sql.Timestamp.valueOf(end));
                } else {
                    // 商品不存在，直接在页面显示错误并停留在表单
                    request.setAttribute("error", "商品が見つかりません。");
                    request.getRequestDispatcher("/WEB-INF/views/provider/food_new.jsp").forward(request, response);
                    return;
                }
            } catch (Exception e) {
                request.setAttribute("error", "商品IDが不正です。");
                request.getRequestDispatcher("/WEB-INF/views/provider/food_new.jsp").forward(request, response);
                return;
            }
        } else if ("RELIST".equalsIgnoreCase(mode)) {
            // 再出品なのに productId が無い
            request.setAttribute("error", "商品IDが不正です。");
            request.getRequestDispatcher("/WEB-INF/views/provider/food_new.jsp").forward(request, response);
            return;
        } else if (copyFrom != null && !copyFrom.isBlank()) {
            try {
                int foodId = Integer.parseInt(copyFrom);
                FoodDao dao = new FoodDao();
                Food src = dao.findById(foodId);
                if (src != null && src.getProviderId() == providerId) {
                    request.setAttribute("copyProductId", src.getProductId());
                    // JP：基本情報をコピー / CN：复制基础信息
                    request.setAttribute("copyName", src.getName());
                    request.setAttribute("copyDescription", src.getDescription());
                    request.setAttribute("copyCategory", src.getCategory());
                    request.setAttribute("copyUnit", src.getUnit());
                    request.setAttribute("copyPriceNormal", src.getPriceNormal());
                    request.setAttribute("copyPriceOffer", src.getPriceOffer());
                    request.setAttribute("copyImagePath", src.getImagePath());
                    request.setAttribute("copyExpiryDate", src.getExpiryDate());

                    // JP：数量は0、受取時間は今日のデフォルト / CN：数量=0，时间设为今天默认
                    request.setAttribute("copyQuantity", 0);
                    java.time.LocalDate today = java.time.LocalDate.now();
                    java.time.LocalDateTime start = java.time.LocalDateTime.now();
                    java.time.LocalDateTime end = java.time.LocalDateTime.of(today, java.time.LocalTime.of(23, 59));
                    request.setAttribute("copyPickupStart", java.sql.Timestamp.valueOf(start));
                    request.setAttribute("copyPickupEnd", java.sql.Timestamp.valueOf(end));
                }
            } catch (Exception e) {
                // JP：パース失敗時は無視 / CN：解析失败则忽略
            }
        }

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

        String mode = request.getParameter("mode");
        if (mode == null || mode.isBlank()) {
            mode = "NEW";
        }

        String productIdParam = request.getParameter("product_id");

        int quantity = Integer.parseInt(request.getParameter("quantity"));
        String unit = request.getParameter("unit");
        int priceOffer = Integer.parseInt(request.getParameter("price_offer"));
        String currency = "JPY"; // 默认 JPY
        Timestamp pickupStart = parseDatetimeLocal(request.getParameter("pickup_start"));
        Timestamp pickupEnd = parseDatetimeLocal(request.getParameter("pickup_end"));
        Date expiryDate = parseDate(request.getParameter("expiry_date"));

        String pn = request.getParameter("price_normal");
        Integer priceNormal = (pn == null || pn.isBlank()) ? null : Integer.valueOf(pn);

        // 图片上传保存（未选择新图时，复用已有图片）
        String imagePath = saveImageIfPresent(request);
        if (imagePath == null || imagePath.isBlank()) {
            String existingImagePath = request.getParameter("existingImagePath");
            if (existingImagePath != null && existingImagePath.startsWith("/uploads/")) {
                imagePath = existingImagePath;
            }
        }

        if ("RELIST".equalsIgnoreCase(mode)) {
            // 再出品：只插入 foods，不更新 products
            if (productIdParam == null || productIdParam.isBlank()) {
                response.sendRedirect(request.getContextPath() + "/provider/products?error=invalid_product");
                return;
            }
            try {
                int productId = Integer.parseInt(productIdParam);
                com.foodlink.dao.ProductDao.ProductRow p = new com.foodlink.dao.ProductDao().findByIdAndProvider(productId, providerId);
                if (p == null) {
                    p = new com.foodlink.dao.ProductDao().findByIdForProviderViaFoods(productId, providerId);
                }
                if (p == null) {
                    response.sendRedirect(request.getContextPath() + "/provider/products?error=not_found");
                    return;
                }
                foodDao.insertFoodWithProduct(productId, providerId,
                        quantity, unit, priceNormal, priceOffer, currency,
                        expiryDate, /*pickupLocation*/ null, pickupStart, pickupEnd,
                        imagePath);
            } catch (Exception e) {
                response.sendRedirect(request.getContextPath() + "/provider/products?error=invalid_product");
                return;
            }
        } else {
            // 新規商品登録：沿用旧逻辑，创建 product（ensureProduct）并插入 foods
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String category = request.getParameter("category");

            foodDao.insertFood(providerId, name, description, category,
                    quantity, unit, priceNormal, priceOffer, currency,
                    expiryDate, /*pickupLocation*/ null, pickupStart, pickupEnd,
                    imagePath);
        }

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

        
       // JP：アプリ外の固定ディレクトリに保存（再起動しても消えない）
       // CN：保存到项目外固定目录（重启/重新部署也不会丢）
       String uploadDirReal = "D:/foodlink_uploads/uploads/products";
       

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

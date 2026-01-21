package com.foodlink.controller.provider;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.foodlink.dao.DashboardDao;
import com.foodlink.model.DashboardAlertItem;
import com.foodlink.model.DashboardKpi;
import com.foodlink.model.DashboardTrendPoint;

/**
 * JP：商家ダッシュボード（簡易）
 * CN：商家端简易仪表盘
 */
@WebServlet("/provider/dashboard")
public class ProviderDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final DashboardDao dashboardDao = new DashboardDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // JP：ログイン・権限チェック / CN：登录与权限检查
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Integer providerId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (providerId == null || role == null || !"PROVIDER".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        // JP：KPI と Alert を取得 / CN：获取KPI与警告列表
        DashboardKpi kpi = dashboardDao.getDashboardKpi(providerId);
        List<DashboardAlertItem> alerts = dashboardDao.getAlertItems(providerId);
        List<DashboardTrendPoint> trends = dashboardDao.getWeeklyTrend(providerId);

        // JP：週次データをJSON配列としてJSPへ渡す / CN：将周趋势数据转成JSON数组传给JSP
        List<String> labels = new java.util.ArrayList<>();
        List<Integer> orderCounts = new java.util.ArrayList<>();
        List<Integer> salesAmounts = new java.util.ArrayList<>();
        for (DashboardTrendPoint p : trends) {
            labels.add(p.getDayLabel());
            orderCounts.add(p.getOrderCount());
            salesAmounts.add(p.getSalesAmount());
        }
        request.setAttribute("weeklyLabelsJson", toJsonStringArray(labels));
        request.setAttribute("weeklyOrderCountsJson", toJsonNumberArray(orderCounts));
        request.setAttribute("weeklySalesAmountsJson", toJsonNumberArray(salesAmounts));

        // JP：前日比計算（売上/出品/予約）
        // CN：计算前日比（销售/出品/预约）
        request.setAttribute("salesDiff", diffText(kpi.getTodaySalesAmount(), kpi.getYesterdaySalesAmount()));
        request.setAttribute("salesDiffClass", diffClass(kpi.getTodaySalesAmount(), kpi.getYesterdaySalesAmount()));
        request.setAttribute("foodsDiff", diffText(kpi.getTodayFoodsCount(), kpi.getYesterdayFoodsCount()));
        request.setAttribute("foodsDiffClass", diffClass(kpi.getTodayFoodsCount(), kpi.getYesterdayFoodsCount()));
        request.setAttribute("resvDiff", diffText(kpi.getTodayReservationsCount(), kpi.getYesterdayReservationsCount()));
        request.setAttribute("resvDiffClass", diffClass(kpi.getTodayReservationsCount(), kpi.getYesterdayReservationsCount()));

        request.setAttribute("kpi", kpi);
        request.setAttribute("alerts", alerts);
        request.setAttribute("trends", trends);

        // JP：画面へ転送 / CN：转发到页面
        request.getRequestDispatcher("/WEB-INF/views/provider/dashboard.jsp")
                .forward(request, response);
    }

    /**
     * JP：前日比の表示テキスト
     * CN：前日比显示文本
     */
    private String diffText(int today, int yesterday) {
        if (yesterday <= 0) {
            return "—";
        }
        double rate = ((double) (today - yesterday) / (double) yesterday) * 100.0;
        long rounded = Math.round(rate);
        if (rounded > 0) {
            return "↑ " + rounded + "%";
        }
        if (rounded < 0) {
            return "↓ " + Math.abs(rounded) + "%";
        }
        return "— 0%";
    }

    /**
     * JP：前日比の色クラス
     * CN：前日比颜色类名
     */
    private String diffClass(int today, int yesterday) {
        if (yesterday <= 0) {
            return "diff-none";
        }
        if (today > yesterday) {
            return "diff-up";
        }
        if (today < yesterday) {
            return "diff-down";
        }
        return "diff-none";
    }

    /**
     * JP：文字列リストをJSON配列に変換 / CN：字符串列表转JSON数组
     */
    private String toJsonStringArray(List<String> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            String v = list.get(i) == null ? "" : list.get(i);
            // JP：簡易エスケープ / CN：简单转义
            v = v.replace("\\", "\\\\").replace("\"", "\\\"");
            sb.append("\"").append(v).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * JP：数値リストをJSON配列に変換 / CN：数字列表转JSON数组
     */
    private String toJsonNumberArray(List<? extends Number> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            Number v = list.get(i);
            sb.append(v == null ? 0 : v.toString());
        }
        sb.append("]");
        return sb.toString();
    }
}

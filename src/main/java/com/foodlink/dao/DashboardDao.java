package com.foodlink.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.foodlink.model.DashboardAlertItem;
import com.foodlink.model.DashboardKpi;
import com.foodlink.model.DashboardTrendPoint;
import com.foodlink.util.DBUtil;

/**
 * JP：商家ダッシュボード用DAO（KPI + Alert）
 * CN：商家端仪表盘DAO（KPI + 警告列表）
 */
public class DashboardDao {

    /**
     * JP：ダッシュボードKPIを取得
     * CN：获取仪表盘KPI
     */
    public DashboardKpi getDashboardKpi(int providerId) {
        DashboardKpi kpi = new DashboardKpi();

        // JP/CN：KPI① 今日売上金額（受取完了のみ：PICKED_UP）
        String sqlTodaySales = """
                SELECT COALESCE(SUM(r.total_price), 0) AS amt
                FROM reservations r
                JOIN foods f ON r.food_id = f.id
                WHERE f.provider_id = ?
                  AND DATE(r.reserve_at) = CURDATE()
                  AND r.status = 'PICKED_UP'
                """;

        // JP/CN：KPI① 昨日売上金額（受取完了のみ：PICKED_UP / 前日比用）
        String sqlYesterdaySales = """
                SELECT COALESCE(SUM(r.total_price), 0) AS amt
                FROM reservations r
                JOIN foods f ON r.food_id = f.id
                WHERE f.provider_id = ?
                  AND DATE(r.reserve_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
                  AND r.status = 'PICKED_UP'
                """;

        // JP/CN：KPI① 今日出品数
        String sqlTodayFoods = """
                SELECT COALESCE(COUNT(*), 0) AS cnt
                FROM foods
                WHERE provider_id = ?
                  AND DATE(created_at) = CURDATE()
                """;

        // JP/CN：KPI① 昨日出品数（前日比用）
        String sqlYesterdayFoods = """
                SELECT COALESCE(COUNT(*), 0) AS cnt
                FROM foods
                WHERE provider_id = ?
                  AND DATE(created_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
                """;

        // JP/CN：KPI② 公開中（可售商品数）
        String sqlOpenFoods = """
                SELECT COALESCE(COUNT(*), 0) AS cnt
                FROM foods
                WHERE provider_id = ?
                  AND status = 'OPEN'
                  AND quantity > 0
                """;

        // JP/CN：KPI③ 今日予約数（商家名下）
        String sqlTodayReservations = """
                SELECT COALESCE(COUNT(*), 0) AS cnt
                FROM reservations r
                JOIN foods f ON r.food_id = f.id
                WHERE f.provider_id = ?
                  AND DATE(r.reserve_at) = CURDATE()
                """;

        // JP/CN：KPI③ 昨日予約数（前日比用）
        String sqlYesterdayReservations = """
                SELECT COALESCE(COUNT(*), 0) AS cnt
                FROM reservations r
                JOIN foods f ON r.food_id = f.id
                WHERE f.provider_id = ?
                  AND DATE(r.reserve_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
                """;

        try (Connection con = DBUtil.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sqlTodaySales)) {
                ps.setInt(1, providerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        kpi.setTodaySalesAmount(rs.getInt("amt"));
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlYesterdaySales)) {
                ps.setInt(1, providerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        kpi.setYesterdaySalesAmount(rs.getInt("amt"));
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlTodayFoods)) {
                ps.setInt(1, providerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        kpi.setTodayFoodsCount(rs.getInt("cnt"));
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlYesterdayFoods)) {
                ps.setInt(1, providerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        kpi.setYesterdayFoodsCount(rs.getInt("cnt"));
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlOpenFoods)) {
                ps.setInt(1, providerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        kpi.setOpenFoodsCount(rs.getInt("cnt"));
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlTodayReservations)) {
                ps.setInt(1, providerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        kpi.setTodayReservationsCount(rs.getInt("cnt"));
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlYesterdayReservations)) {
                ps.setInt(1, providerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        kpi.setYesterdayReservationsCount(rs.getInt("cnt"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return kpi;
    }

    /**
     * JP：Alert対象の在庫一覧を取得
     * CN：获取警告列表（需要处理的商品）
     */
    public List<DashboardAlertItem> getAlertItems(int providerId) {
        List<DashboardAlertItem> list = new ArrayList<>();

        String sql = """
                SELECT
                  r.id AS reservation_id,
                  r.reservation_code,
                  r.quantity,
                  r.reserve_at,
                  p.name AS food_name
                FROM reservations r
                JOIN foods f ON r.food_id = f.id
                JOIN products p ON f.product_id = p.id
                WHERE f.provider_id = ?
                  AND DATE(r.reserve_at) = CURDATE()
                  AND r.status = 'RESERVED'
                ORDER BY r.reserve_at DESC
                LIMIT 20
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, providerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DashboardAlertItem item = new DashboardAlertItem();
                    item.setReservationId(rs.getInt("reservation_id"));
                    item.setReservationCode(rs.getString("reservation_code"));
                    item.setFoodName(rs.getString("food_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setReserveAt(rs.getTimestamp("reserve_at"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    /**
     * JP：直近7日間の推移（注文数・売上）
     * CN：最近7天趋势（订单数/销售额）
     */
    public List<DashboardTrendPoint> getWeeklyTrend(int providerId) {
        List<DashboardTrendPoint> list = new ArrayList<>();

        String sql = """
                SELECT
                  DATE(r.reserve_at) AS d,
                  COALESCE(SUM(CASE WHEN r.status <> 'CANCELLED' THEN 1 ELSE 0 END), 0) AS order_cnt,
                  COALESCE(SUM(CASE WHEN r.status = 'PICKED_UP' THEN r.total_price ELSE 0 END), 0) AS sales_amt
                FROM reservations r
                JOIN foods f ON r.food_id = f.id
                WHERE f.provider_id = ?
                  AND r.reserve_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
                GROUP BY DATE(r.reserve_at)
                ORDER BY DATE(r.reserve_at)
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, providerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DashboardTrendPoint p = new DashboardTrendPoint();
                    p.setDayLabel(rs.getDate("d").toString());
                    p.setOrderCount(rs.getInt("order_cnt"));
                    p.setSalesAmount(rs.getInt("sales_amt"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}

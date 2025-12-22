package com.foodlink.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.foodlink.util.DBUtil;

/**
 * JP：予約DAO（在庫減算＋予約作成＋予約番号発行）
 * CN：预约DAO（扣库存 + 创建预约 + 生成预约编号）
 */
public class ReservationDao {

    /**
     * JP：予約を作成（在庫が足りない場合は例外）
     * CN：创建预约（库存不足则抛出异常）
     *
     * @param foodId      商品ID
     * @param receiverId  受取者ID
     * @param qty         予約数量
     * @param unitPrice   单价（这里用 food.price_offer）
     * @param pickupTime  受取日時（可为null）
     * @return reservationId（生成后的自增ID）
     */
    public int createReservation(int foodId, int receiverId, int qty, int unitPrice, Timestamp pickupTime) {

        // 你当前 reservations 表字段：
        // id, reservation_code(NULL可), food_id, receiver_id, quantity, unit_price, total_price,
        // reserve_at(DEFAULT CURRENT_TIMESTAMP), pickup_time(NULL可), status(DEFAULT 'RESERVED')

        String sqlInsert = """
            INSERT INTO reservations
              (reservation_code, food_id, receiver_id, quantity, unit_price, total_price, pickup_time, status)
            VALUES
              (NULL, ?, ?, ?, ?, ?, ?, 'RESERVED')
        """;

        // JP：在庫減算（在庫>=qty のときだけ成功）
        // CN：扣库存（仅当 quantity>=qty 时更新成功）
        String sqlDecrease = """
            UPDATE foods
            SET quantity = quantity - ?
            WHERE id = ?
              AND status = 'OPEN'
              AND quantity >= ?
        """;

        // JP：予約番号を発行（例：FL-202512-0031）
        // CN：生成预约编号（例如：FL-202512-0031）
        String sqlUpdateCode = """
            UPDATE reservations
            SET reservation_code = ?
            WHERE id = ?
        """;

        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);

            // 1) 扣库存
            try (PreparedStatement ps = con.prepareStatement(sqlDecrease)) {
                ps.setInt(1, qty);
                ps.setInt(2, foodId);
                ps.setInt(3, qty);

                int updated = ps.executeUpdate();
                if (updated == 0) {
                    con.rollback();
                    throw new RuntimeException("在庫不足 / 库存不足：foodId=" + foodId + ", qty=" + qty);
                }
            }

            // 2) 插入预约
            int reservationId;
            int totalPrice = unitPrice * qty;

            try (PreparedStatement ps = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, foodId);
                ps.setInt(2, receiverId);
                ps.setInt(3, qty);
                ps.setInt(4, unitPrice);
                ps.setInt(5, totalPrice);

                if (pickupTime == null) {
                    ps.setNull(6, java.sql.Types.TIMESTAMP);
                } else {
                    ps.setTimestamp(6, pickupTime);
                }

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        con.rollback();
                        throw new RuntimeException("予約IDの取得に失敗 / 获取预约ID失败");
                    }
                    reservationId = rs.getInt(1);
                }
            }

            // 3) 生成 reservation_code（按月份 + 4位id）
            String yyyyMM = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            String code = "FL-" + yyyyMM + "-" + String.format("%04d", reservationId);

            try (PreparedStatement ps = con.prepareStatement(sqlUpdateCode)) {
                ps.setString(1, code);
                ps.setInt(2, reservationId);
                ps.executeUpdate();
            }

            con.commit();
            return reservationId;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * JP：予約完了画面用に予約情報を取得（最低限）
     * CN：预约完成页取信息（最小字段）
     */
    public ReservationView findReservationViewById(int reservationId) {

        String sql = """
            SELECT
              r.id,
              r.reservation_code,
              r.quantity,
              r.unit_price,
              r.total_price,
              r.pickup_time,
              r.reserve_at,
              f.name AS food_name,
              f.pickup_location
            FROM reservations r
            JOIN foods f ON f.id = r.food_id
            WHERE r.id = ?
            LIMIT 1
        """;

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, reservationId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                ReservationView v = new ReservationView();
                v.id = rs.getInt("id");
                v.code = rs.getString("reservation_code");
                v.foodName = rs.getString("food_name");
                v.pickupLocation = rs.getString("pickup_location");
                v.quantity = rs.getInt("quantity");
                v.unitPrice = rs.getInt("unit_price");
                v.totalPrice = rs.getInt("total_price");
                v.pickupTime = rs.getTimestamp("pickup_time");
                v.reserveAt = rs.getTimestamp("reserve_at");
                return v;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * JP：JSP に渡すだけの簡易View
     * CN：只用于JSP展示的简单DTO
     */
    public static class ReservationView {
        public int id;
        public String code;
        public String foodName;
        public String pickupLocation;
        public int quantity;
        public int unitPrice;
        public int totalPrice;
        public Timestamp pickupTime;
        public Timestamp reserveAt;
    }
}

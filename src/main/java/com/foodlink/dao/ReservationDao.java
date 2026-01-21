package com.foodlink.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
     * @param foodId     商品ID
     * @param receiverId 受取者ID
     * @param qty        予約数量
     * @param unitPrice  单价（这里用 food.price_offer）
     * @param pickupTime 受取日時（可为null）
     * @return reservationId（生成后的自增ID）
     */
    public int createReservation(int foodId, int receiverId, int qty, int unitPrice, Timestamp pickupTime) {

        // 你当前 reservations 表字段：
        // id, reservation_code(NULL可), food_id, receiver_id, quantity, unit_price,
        // total_price,
        // reserve_at(DEFAULT CURRENT_TIMESTAMP), pickup_time(NULL可), status(DEFAULT
        // 'RESERVED')

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
     * JP：取消予約（RESERVEDのみ） / CN：取消预约（仅RESERVED）
     *
     * JP：reservation が receiverId に属し、status='RESERVED' かつ
     *     pickup_time が未設定または未来の場合のみ更新
     * CN：仅当 reservation 属于 receiverId、status='RESERVED'，且
     *     pickup_time 为 NULL 或未来时，才更新为 CANCELLED
     *
     * JP：true は更新成功 / CN：true 表示更新成功
     */
    public boolean cancelReservation(int reservationId, int receiverId) {
        String sqlSelect = """
                    SELECT food_id, quantity
                    FROM reservations
                    WHERE id = ? AND receiver_id = ? AND status = 'RESERVED'
                    FOR UPDATE
                """;
        String sqlUpdateReservation = """
                    UPDATE reservations
                    SET status = 'CANCELLED'
                    WHERE id = ? AND receiver_id = ? AND status = 'RESERVED'
                """;
        String sqlRestoreStock = """
                    UPDATE foods
                    SET quantity = quantity + ?,
                        status = CASE
                                   WHEN (pickup_end IS NULL OR pickup_end > NOW()) AND quantity + ? > 0 THEN 'OPEN'
                                   ELSE status
                                 END
                    WHERE id = ?
                """;

        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);

            int foodId;
            int qty;
            try (PreparedStatement ps = con.prepareStatement(sqlSelect)) {
                ps.setInt(1, reservationId);
                ps.setInt(2, receiverId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                    foodId = rs.getInt("food_id");
                    qty = rs.getInt("quantity");
                }
            }

            int updated;
            try (PreparedStatement ps = con.prepareStatement(sqlUpdateReservation)) {
                ps.setInt(1, reservationId);
                ps.setInt(2, receiverId);
                updated = ps.executeUpdate();
            }
            if (updated != 1) {
                con.rollback();
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sqlRestoreStock)) {
                ps.setInt(1, qty);
                ps.setInt(2, qty);
                ps.setInt(3, foodId);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * JP：キャンセル前の最小チェック用 / CN：取消前的最小检查信息
     */
    public ReservationStatusInfo findReservationStatusInfo(int reservationId, int receiverId) {
        String sql = """
                    SELECT status, pickup_time
                    FROM reservations
                    WHERE id = ? AND receiver_id = ?
                    LIMIT 1
                """;

        try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.setInt(2, receiverId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ReservationStatusInfo info = new ReservationStatusInfo();
                info.status = rs.getString("status");
                info.pickupTime = rs.getTimestamp("pickup_time");
                return info;
            }
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
                      r.status,
                      f.name AS food_name,
                      c.address AS company_address
                    FROM reservations r
                    JOIN foods f ON f.id = r.food_id
                    LEFT JOIN users u ON u.id = f.provider_id
                    LEFT JOIN companies c ON c.id = u.company_id
                    WHERE r.id = ?
                    LIMIT 1
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, reservationId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return null;

                ReservationView v = new ReservationView();
                v.id = rs.getInt("id");
                v.code = rs.getString("reservation_code");
                v.foodName = rs.getString("food_name");
                v.companyAddress = rs.getString("company_address");
                v.quantity = rs.getInt("quantity");
                v.unitPrice = rs.getInt("unit_price");
                v.totalPrice = rs.getInt("total_price");
                v.pickupTime = rs.getTimestamp("pickup_time");
                v.reserveAt = rs.getTimestamp("reserve_at");
                v.status = rs.getString("status");
                return v;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * JP：予約詳細（受取者権限チェック込み）/ CN：预约详情（含用户权限）
     */
    public ReservationView findReservationViewByIdAndReceiverId(int reservationId, int receiverId) {
        String sql = """
                    SELECT
                      r.id,
                      r.reservation_code,
                      r.quantity,
                      r.unit_price,
                      r.total_price,
                      r.pickup_time,
                      r.reserve_at,
                      r.status,
                      f.name AS food_name,
                      c.address AS company_address,
                      c.name AS company_name,
                      p.image_path AS image_path
                    FROM reservations r
                    JOIN foods f ON f.id = r.food_id
                    JOIN products p ON f.product_id = p.id
                    LEFT JOIN users u ON u.id = f.provider_id
                    LEFT JOIN companies c ON c.id = u.company_id
                    WHERE r.id = ? AND r.receiver_id = ?
                    LIMIT 1
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, reservationId);
            ps.setInt(2, receiverId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ReservationView v = new ReservationView();
                v.id = rs.getInt("id");
                v.code = rs.getString("reservation_code");
                v.foodName = rs.getString("food_name");
                v.companyAddress = rs.getString("company_address");
                v.companyName = rs.getString("company_name");
                v.imagePath = rs.getString("image_path");
                v.quantity = rs.getInt("quantity");
                v.unitPrice = rs.getInt("unit_price");
                v.totalPrice = rs.getInt("total_price");
                v.pickupTime = rs.getTimestamp("pickup_time");
                v.reserveAt = rs.getTimestamp("reserve_at");
                v.status = rs.getString("status");
                return v;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * JP：予約詳細（予約番号 + 受取者）/ CN：预约详情（预约号 + 用户）
     */
    public ReservationView findReservationViewByCodeAndReceiverId(String code, int receiverId) {
        String sql = """
                    SELECT
                      r.id,
                      r.reservation_code,
                      r.quantity,
                      r.unit_price,
                      r.total_price,
                      r.pickup_time,
                      r.reserve_at,
                      r.status,
                      f.name AS food_name,
                      c.address AS company_address,
                      c.name AS company_name,
                      p.image_path AS image_path
                    FROM reservations r
                    JOIN foods f ON f.id = r.food_id
                    JOIN products p ON f.product_id = p.id
                    LEFT JOIN users u ON u.id = f.provider_id
                    LEFT JOIN companies c ON c.id = u.company_id
                    WHERE r.reservation_code = ? AND r.receiver_id = ?
                    LIMIT 1
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setInt(2, receiverId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ReservationView v = new ReservationView();
                v.id = rs.getInt("id");
                v.code = rs.getString("reservation_code");
                v.foodName = rs.getString("food_name");
                v.companyAddress = rs.getString("company_address");
                v.companyName = rs.getString("company_name");
                v.imagePath = rs.getString("image_path");
                v.quantity = rs.getInt("quantity");
                v.unitPrice = rs.getInt("unit_price");
                v.totalPrice = rs.getInt("total_price");
                v.pickupTime = rs.getTimestamp("pickup_time");
                v.reserveAt = rs.getTimestamp("reserve_at");
                v.status = rs.getString("status");
                return v;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据 receiverId 查询该用户的预约列表，用于历史页展示
     */
    public List<ReservationView> findByReceiverId(int receiverId) {
        String sql = """
                    SELECT
                      r.id,
                      r.reservation_code,
                      r.quantity,
                      r.unit_price,
                      r.total_price,
                      r.pickup_time,
                      r.reserve_at,
                      r.status,
                      f.name AS food_name,
                      c.address AS company_address,
                      c.name AS company_name,
                      p.image_path AS image_path
                    FROM reservations r
                    JOIN foods f ON f.id = r.food_id
                    JOIN products p ON f.product_id = p.id
                    LEFT JOIN users u ON u.id = f.provider_id
                    LEFT JOIN companies c ON c.id = u.company_id
                    WHERE r.receiver_id = ?
                    ORDER BY r.reserve_at DESC
                """;

        List<ReservationView> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, receiverId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReservationView v = new ReservationView();
                    v.id = rs.getInt("id");
                    v.code = rs.getString("reservation_code");
                    v.foodName = rs.getString("food_name");
                    v.companyAddress = rs.getString("company_address");
                    v.companyName = rs.getString("company_name");
                    v.imagePath = rs.getString("image_path");
                    v.quantity = rs.getInt("quantity");
                    v.unitPrice = rs.getInt("unit_price");
                    v.totalPrice = rs.getInt("total_price");
                    v.pickupTime = rs.getTimestamp("pickup_time");
                    v.reserveAt = rs.getTimestamp("reserve_at");
                    v.status = rs.getString("status");
                    list.add(v);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * JP：商家用 予約一覧（任意フィルタ）
     * CN：商家端预约列表（可选过滤）
     */
    public List<ProviderReservationRow> findProviderReservations(int providerId, String status, String pickupDate, String period, int page, int size) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("  r.id, r.reservation_code, r.reserve_at, r.pickup_time, ");
        sql.append("  r.quantity, r.total_price, r.status, ");
        sql.append("  f.name AS food_name, ");
        sql.append("  u.name AS receiver_name, r.receiver_id ");
        sql.append("FROM reservations r ");
        sql.append("JOIN foods f ON r.food_id = f.id ");
        sql.append("LEFT JOIN users u ON r.receiver_id = u.id ");
        List<Object> params = new ArrayList<>();
        appendProviderReservationFilters(sql, params, providerId, status, pickupDate, period);

        sql.append("ORDER BY r.reserve_at DESC ");
        sql.append("LIMIT ? OFFSET ? ");
        int offset = (page - 1) * size;
        params.add(size);
        params.add(offset);

        List<ProviderReservationRow> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProviderReservationRow row = new ProviderReservationRow();
                    row.id = rs.getInt("id");
                    row.code = rs.getString("reservation_code");
                    row.reserveAt = rs.getTimestamp("reserve_at");
                    row.pickupTime = rs.getTimestamp("pickup_time");
                    row.quantity = rs.getInt("quantity");
                    row.totalPrice = rs.getInt("total_price");
                    row.status = rs.getString("status");
                    row.foodName = rs.getString("food_name");
                    row.receiverName = rs.getString("receiver_name");
                    row.receiverId = rs.getInt("receiver_id");
                    list.add(row);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countProviderReservations(int providerId, String status, String pickupDate, String period) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM reservations r ");
        sql.append("JOIN foods f ON r.food_id = f.id ");
        sql.append("LEFT JOIN users u ON r.receiver_id = u.id ");
        List<Object> params = new ArrayList<>();
        appendProviderReservationFilters(sql, params, providerId, status, pickupDate, period);

        try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void appendProviderReservationFilters(StringBuilder sql, List<Object> params, int providerId, String status, String pickupDate, String period) {
        sql.append("WHERE f.provider_id = ? ");
        params.add(providerId);

        if (status != null && !status.isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND r.status = ? ");
            params.add(status);
        }

        if (pickupDate != null && !pickupDate.isEmpty()) {
            sql.append("AND DATE(r.pickup_time) = ? ");
            params.add(pickupDate);
        }

        // 期間フィルタ：pickup_time 基準 / CN：按受取予定日時筛选
        if (period != null && !"ALL".equalsIgnoreCase(period)) {
            switch (period) {
                case "TODAY" -> {
                    sql.append("AND DATE(r.pickup_time) = CURDATE() ");
                }
                case "7" -> {
                    sql.append("AND r.pickup_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 7 DAY) ");
                }
                case "30" -> {
                    sql.append("AND r.pickup_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 30 DAY) ");
                }
                default -> {
                    // no-op for unknown
                }
            }
        }
    }

    /**
     * JP：商家用 受取済み更新（RESERVEDのみ）
     * CN：商家端确认受取（仅RESERVED）
     */
    public boolean markReservationPickedUp(int reservationId, int providerId) {
        String sql = """
                    UPDATE reservations r
                    JOIN foods f ON r.food_id = f.id
                    SET r.status = 'PICKED_UP'
                    WHERE r.id = ? AND f.provider_id = ? AND r.status = 'RESERVED'
                """;

        try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.setInt(2, providerId);
            int updated = ps.executeUpdate();
            return updated == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * JP：期限切れ予約を自動更新（RESERVED -> EXPIRED）
     * CN：自动将过期预约更新为 EXPIRED（RESERVED -> EXPIRED）
     */
    public int expireOverdueReservations(int graceMinutes) {
        // JP：Java側で締切時刻を計算 / CN：在Java侧计算截止时间
        long cutoffMillis = System.currentTimeMillis() - (graceMinutes * 60L * 1000L);
        Timestamp cutoff = new Timestamp(cutoffMillis);

        String sqlSelect = """
                    SELECT id, food_id, quantity
                    FROM reservations
                    WHERE status = 'RESERVED'
                      AND pickup_time IS NOT NULL
                      AND pickup_time < ?
                """;
        String sqlUpdateReservation = """
                    UPDATE reservations
                    SET status = 'EXPIRED'
                    WHERE id = ? AND status = 'RESERVED' AND pickup_time < ?
                """;
        String sqlRestoreStock = """
                    UPDATE foods
                    SET quantity = quantity + ?
                    WHERE id = ?
                """;

        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            int updatedCount = 0;

            try (PreparedStatement psSelect = con.prepareStatement(sqlSelect)) {
                psSelect.setTimestamp(1, cutoff);
                try (ResultSet rs = psSelect.executeQuery()) {
                    while (rs.next()) {
                        int reservationId = rs.getInt("id");
                        int foodId = rs.getInt("food_id");
                        int qty = rs.getInt("quantity");

                        try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdateReservation)) {
                            psUpdate.setInt(1, reservationId);
                            psUpdate.setTimestamp(2, cutoff);
                            int rows = psUpdate.executeUpdate();
                            if (rows == 1) {
                                // JP：期限切れ確定時のみ在庫を戻す / CN：仅在成功过期时恢复库存
                                try (PreparedStatement psRestore = con.prepareStatement(sqlRestoreStock)) {
                                    psRestore.setInt(1, qty);
                                    psRestore.setInt(2, foodId);
                                    psRestore.executeUpdate();
                                }
                                updatedCount++;
                            }
                        }
                    }
                }
            }

            con.commit();
            return updatedCount;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * JP：商家用 予約状態確認（所有権含む）
     * CN：商家端预约状态检查（含所有权）
     */
    public ProviderReservationStatusInfo findProviderReservationStatus(int reservationId, int providerId) {
        String sql = """
                    SELECT r.status
                    FROM reservations r
                    JOIN foods f ON r.food_id = f.id
                    WHERE r.id = ? AND f.provider_id = ?
                    LIMIT 1
                """;

        try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.setInt(2, providerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ProviderReservationStatusInfo info = new ProviderReservationStatusInfo();
                info.status = rs.getString("status");
                return info;
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
        public String companyAddress;
        public String companyName;
        public String imagePath;
        public int quantity;
        public int unitPrice;
        public int totalPrice;
        public Timestamp pickupTime;
        public Timestamp reserveAt;
        public String status;

        // Add getters for JSP EL compatibility
        public int getId() {
            return id;
        }

        public String getCode() {
            return code;
        }

        public String getFoodName() {
            return foodName;
        }

        public String getCompanyAddress() {
            return companyAddress;
        }

        public String getCompanyName() {
            return companyName;
        }

        public String getImagePath() {
            return imagePath;
        }

        public int getQuantity() {
            return quantity;
        }

        public int getUnitPrice() {
            return unitPrice;
        }

        public int getTotalPrice() {
            return totalPrice;
        }

        public Timestamp getPickupTime() {
            return pickupTime;
        }

        public Timestamp getReserveAt() {
            return reserveAt;
        }

        public String getStatus() {
            return status;
        }
    }

    /**
     * JP：状態チェック用DTO / CN：状态检查用DTO
     */
    public static class ReservationStatusInfo {
        public String status;
        public Timestamp pickupTime;

        public String getStatus() {
            return status;
        }

        public Timestamp getPickupTime() {
            return pickupTime;
        }
    }

    /**
     * JP：商家一覧表示用DTO / CN：商家列表显示DTO
     */
    public static class ProviderReservationRow {
        public int id;
        public String code;
        public Timestamp reserveAt;
        public Timestamp pickupTime;
        public int quantity;
        public int totalPrice;
        public String status;
        public String foodName;
        public String receiverName;
        public int receiverId;

        public int getId() {
            return id;
        }

        public String getCode() {
            return code;
        }

        public Timestamp getReserveAt() {
            return reserveAt;
        }

        public Timestamp getPickupTime() {
            return pickupTime;
        }

        public int getQuantity() {
            return quantity;
        }

        public int getTotalPrice() {
            return totalPrice;
        }

        public String getStatus() {
            return status;
        }

        public String getFoodName() {
            return foodName;
        }

        public String getReceiverName() {
            return receiverName;
        }

        public int getReceiverId() {
            return receiverId;
        }
    }

    /**
     * JP：商家用 予約状態 / CN：商家端预约状态
     */
    public static class ProviderReservationStatusInfo {
        public String status;

        public String getStatus() {
            return status;
        }
    }
}

package com.foodlink.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.foodlink.model.Food;
import com.foodlink.util.DBUtil;
/**
 * food 表 DAO
 */
public class FoodDao {
	 public void insertFood(int providerId,
             String name, String description, String category,
             int quantity, String unit,
             Integer priceNormal, int priceOffer, String currency,
             Date expiryDate,
             String pickupLocation, Timestamp pickupStart, Timestamp pickupEnd,
             String imagePath) {

String sql = """
INSERT INTO foods
(provider_id, name, description, category, quantity, unit,
price_normal, price_offer, currency, expiry_date,
pickup_location, pickup_start, pickup_end,
image_path)
VALUES
(?, ?, ?, ?, ?, ?,
?, ?, ?, ?,
?, ?, ?,
?)
""";

try (Connection con = DBUtil.getConnection();
PreparedStatement ps = con.prepareStatement(sql)) {

ps.setInt(1, providerId);
ps.setString(2, name);
ps.setString(3, description);
ps.setString(4, category);
ps.setInt(5, quantity);
ps.setString(6, unit);

if (priceNormal == null) ps.setNull(7, Types.INTEGER);
else ps.setInt(7, priceNormal);

ps.setInt(8, priceOffer);
ps.setString(9, currency);

if (expiryDate == null) ps.setNull(10, Types.DATE);
else ps.setDate(10, expiryDate);

ps.setString(11, pickupLocation);

if (pickupStart == null) ps.setNull(12, Types.TIMESTAMP);
else ps.setTimestamp(12, pickupStart);

if (pickupEnd == null) ps.setNull(13, Types.TIMESTAMP);
else ps.setTimestamp(13, pickupEnd);

if (imagePath == null) ps.setNull(14, Types.VARCHAR);
else ps.setString(14, imagePath);

ps.executeUpdate();

} catch (SQLException e) {
throw new RuntimeException(e);
}
}
	 /**
	     * 根据 provider_id 查询该商家的商品列表
	     * JP：出品者ごとの在庫一覧を取得
	     * CN：按企业ID查询库存列表
	     */
	    public List<Food> findByProviderId(int providerId) {

	        // ✅ 建议按 created_at 新→旧排序，符合“刚上架的排在前面”
	        String sql = """
	            SELECT
	              id, provider_id, name, description, category, quantity, unit,
	              price_normal, price_offer, currency, expiry_date,
	              pickup_location, pickup_start, pickup_end,
	              image_path, status, created_at
	            FROM foods
	            WHERE provider_id = ?
	            ORDER BY created_at DESC
	        """;

	        List<Food> list = new ArrayList<>();

	        try (Connection con = DBUtil.getConnection();
	             PreparedStatement ps = con.prepareStatement(sql)) {

	            // 绑定参数：provider_id
	            ps.setInt(1, providerId);

	            try (ResultSet rs = ps.executeQuery()) {
	                while (rs.next()) {
	                    Food f = new Food();

	                    // ====== 基本字段 ======
	                    f.setId(rs.getInt("id"));
	                    f.setProviderId(rs.getInt("provider_id"));
	                    f.setName(rs.getString("name"));
	                    f.setDescription(rs.getString("description"));
	                    f.setCategory(rs.getString("category"));
	                    f.setQuantity(rs.getInt("quantity"));
	                    f.setUnit(rs.getString("unit"));

	                    // price_normal 可为 NULL，所以要用 getObject 判断
	                    Integer priceNormal = (Integer) rs.getObject("price_normal");
	                    f.setPriceNormal(priceNormal);

	                    f.setPriceOffer(rs.getInt("price_offer"));
	                    f.setCurrency(rs.getString("currency"));

	                    f.setExpiryDate(rs.getDate("expiry_date"));
	                    f.setPickupLocation(rs.getString("pickup_location"));
	                    f.setPickupStart(rs.getTimestamp("pickup_start"));
	                    f.setPickupEnd(rs.getTimestamp("pickup_end"));

	                    f.setImagePath(rs.getString("image_path"));
	                    f.setStatus(rs.getString("status"));
	                    f.setCreatedAt(rs.getTimestamp("created_at"));

	                    list.add(f);
	                }
	            }

	        } catch (SQLException e) {
	            // 作品阶段：RuntimeException 让错误显性化，方便你定位
	            throw new RuntimeException(e);
	        }

	        return list;
	    }

}

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
 * JP：Food用DAO  CN：Food 数据访问层
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

			if (priceNormal == null)
				ps.setNull(7, Types.INTEGER);
			else
				ps.setInt(7, priceNormal);

			ps.setInt(8, priceOffer);
			ps.setString(9, currency);

			if (expiryDate == null)
				ps.setNull(10, Types.DATE);
			else
				ps.setDate(10, expiryDate);

			ps.setString(11, pickupLocation);

			if (pickupStart == null)
				ps.setNull(12, Types.TIMESTAMP);
			else
				ps.setTimestamp(12, pickupStart);

			if (pickupEnd == null)
				ps.setNull(13, Types.TIMESTAMP);
			else
				ps.setTimestamp(13, pickupEnd);

			if (imagePath == null)
				ps.setNull(14, Types.VARCHAR);
			else
				ps.setString(14, imagePath);

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

	/**
	* JP：受取者ホーム用：公開中（OPEN）の商品一覧を取得（最小検索付き）
	* CN：给受取者主页用：查询 OPEN 商品列表（带最小搜索）
	*/
	public List<Food> findOpenFoodsForReceiver(String keyword, String category, String area, String sort) {
		List<Food> list = new ArrayList<>();

		// =========================
		// JP：WHERE条件を必要な分だけ組み立て
		// CN：按需拼 WHERE 条件
		// =========================
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append("  id, provider_id, name, description, category, quantity, unit, ");
		sql.append("  price_normal, price_offer, currency, ");
		sql.append("  expiry_date, pickup_location, pickup_start, pickup_end, ");
		sql.append("  image_path, status, created_at ");
		sql.append("FROM foods ");
		sql.append("WHERE status = 'OPEN' ");

		// JP：キーワード（商品名 or 説明）
		// CN：关键词（商品名/描述）
		boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
		if (hasKeyword) {
			sql.append("AND (name LIKE ? OR description LIKE ?) ");
		}

		// JP：カテゴリ
		// CN：分类
		boolean hasCategory = category != null && !category.trim().isEmpty();
		if (hasCategory) {
			sql.append("AND category = ? ");
		}

		// JP：エリア（受取場所の部分一致）
		// CN：地区（取货地点模糊匹配）
		boolean hasArea = area != null && !area.trim().isEmpty();
		if (hasArea) {
			sql.append("AND pickup_location LIKE ? ");
		}

		// JP：並び替え
		// CN：排序
		if ("price".equals(sort)) {
			// JP：特別価格（price_offer）安い順 / CN：特价升序
			sql.append("ORDER BY price_offer ASC, id DESC ");
		} else {
			// JP：新着順（id DESC）/ CN：最新
			sql.append("ORDER BY id DESC ");
		}

		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql.toString())) {

			int idx = 1;

			if (hasKeyword) {
				String like = "%" + keyword.trim() + "%";
				ps.setString(idx++, like);
				ps.setString(idx++, like);
			}
			if (hasCategory) {
				ps.setString(idx++, category.trim());
			}
			if (hasArea) {
				String like = "%" + area.trim() + "%";
				ps.setString(idx++, like);
			}

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Food f = new Food();

					// JP：必要項目を埋める（カード表示に必要な分）
					// CN：填充卡片显示需要的字段
					f.setId(rs.getInt("id"));
					f.setProviderId(rs.getInt("provider_id"));
					f.setName(rs.getString("name"));
					f.setDescription(rs.getString("description"));
					f.setCategory(rs.getString("category"));
					f.setQuantity(rs.getInt("quantity"));
					f.setUnit(rs.getString("unit"));

					// price_normal は NULL あり得るので getObject
					Integer normal = (Integer) rs.getObject("price_normal");
					f.setPriceNormal(normal);

					f.setPriceOffer(rs.getInt("price_offer"));
					f.setCurrency(rs.getString("currency"));

					f.setPickupLocation(rs.getString("pickup_location"));
					f.setPickupEnd(rs.getTimestamp("pickup_end"));

					f.setImagePath(rs.getString("image_path"));

					list.add(f);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	// JP：受取者トップ用：最新のOPEN商品をN件だけ取得
	// CN：接收者首页：只获取最新的OPEN商品N条
	public List<Food> findLatestOpenFoods(int limit) {
		List<Food> list = new ArrayList<>();

		String sql = "SELECT id, provider_id, name, description, category, quantity, unit, " +
				"       price_normal, price_offer, pickup_location, pickup_end, image_path, created_at " +
				"FROM foods " +
				"WHERE status = 'OPEN' " +
				"ORDER BY created_at DESC " +
				"LIMIT ?";

		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, limit);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Food f = new Food();
					f.setId(rs.getInt("id"));
					f.setProviderId(rs.getInt("provider_id"));
					f.setName(rs.getString("name"));
					f.setDescription(rs.getString("description"));
					f.setCategory(rs.getString("category"));
					f.setQuantity(rs.getInt("quantity"));
					f.setUnit(rs.getString("unit"));
					f.setPriceNormal((Integer) rs.getObject("price_normal"));
					f.setPriceOffer((Integer) rs.getObject("price_offer"));
					f.setPickupLocation(rs.getString("pickup_location"));
					f.setPickupEnd(rs.getTimestamp("pickup_end"));
					f.setImagePath(rs.getString("image_path"));
					list.add(f);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}
	
	
    // 商品详情页面：根据商品id查询
	public Food findById(int id) {

		// ⚠️【日本語】カラム名はあなたのDB定義に合わせて修正してください
		// ⚠️【中文】列名按你真实数据库字段改（下面按常见写法给）
		String sql = """
				    SELECT
				        id,
				        provider_id,
				        name,
				        description,
				        category,
				        quantity,
				        unit,
				        price_normal,
				        price_offer,
				        currency,
				        expiry_date,
				        pickup_location,
				        pickup_start,
				        pickup_end,
				        image_path,
				        status,
				        created_at
				    FROM foods
				    WHERE id = ?
				    LIMIT 1
				""";

		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;

				Food f = new Food();

				// ===== 基本 =====
				f.setId(rs.getInt("id"));
				f.setProviderId(rs.getInt("provider_id"));
				f.setName(rs.getString("name"));
				f.setDescription(rs.getString("description"));
				f.setCategory(rs.getString("category"));

				// ===== 数量 =====
				f.setQuantity(rs.getInt("quantity"));
				f.setUnit(rs.getString("unit"));

				// ===== 价格 =====
				// priceNormal 是 Integer，允许 null（你 Bean 设计得很好）
				int pn = rs.getInt("price_normal");
				f.setPriceNormal(rs.wasNull() ? null : pn);

				f.setPriceOffer(rs.getInt("price_offer"));
				f.setCurrency(rs.getString("currency"));

				// ===== 日期/时间 =====
				f.setExpiryDate(rs.getDate("expiry_date")); // java.sql.Date -> Date
				f.setPickupStart(rs.getTimestamp("pickup_start"));
				f.setPickupEnd(rs.getTimestamp("pickup_end"));

				// ===== 位置/图片/状态 =====
				f.setPickupLocation(rs.getString("pickup_location"));
				f.setImagePath(rs.getString("image_path")); // 例：/uploads/products/xxx.jpg
				f.setStatus(rs.getString("status"));
				f.setCreatedAt(rs.getTimestamp("created_at"));

				return f;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}

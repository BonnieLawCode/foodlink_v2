package com.foodlink.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.foodlink.util.DBUtil;

/**
 * products 一覧表示用 DAO
 */
public class ProductDao {

	public List<ProductRow> findByProvider(int providerId, String keyword, int page, int size) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append("  p.id, p.provider_id, p.name, p.category, p.description, p.image_path, p.price_normal, p.created_at, ");
		sql.append("  lf.price_offer AS last_price_offer, lf.quantity AS last_quantity, lf.status AS last_status ");
		sql.append("FROM products p ");
		sql.append("LEFT JOIN ( ");
		sql.append("    SELECT f1.* FROM foods f1 ");
		sql.append("    JOIN (SELECT product_id, MAX(created_at) AS max_created FROM foods GROUP BY product_id) t ");
		sql.append("      ON t.product_id = f1.product_id AND t.max_created = f1.created_at ");
		sql.append(") lf ON lf.product_id = p.id ");
		sql.append("WHERE p.provider_id = ? ");
		List<Object> params = new ArrayList<>();
		params.add(providerId);

		if (keyword != null && !keyword.isEmpty()) {
			sql.append("AND (p.name LIKE ? OR p.category LIKE ?) ");
			String like = "%" + keyword + "%";
			params.add(like);
			params.add(like);
		}

		sql.append("ORDER BY p.id DESC ");
		sql.append("LIMIT ? OFFSET ? ");
		int offset = (page - 1) * size;
		params.add(size);
		params.add(offset);

		List<ProductRow> list = new ArrayList<>();
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ProductRow row = mapRow(rs);
					list.add(row);
				}
			}
			return list;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public ProductRow findByIdAndProvider(int productId, int providerId) {
		// JP：last_* カラムは mapRow で参照するためダミーを付与
		// CN：mapRow 会读取 last_* 列，这里用 NULL 占位避免列不存在异常
		String sql = """
				SELECT id, provider_id, name, category, description, image_path, price_normal, created_at,
				       NULL AS last_price_offer, NULL AS last_quantity, NULL AS last_status
				FROM products
				WHERE id = ? AND provider_id = ?
				LIMIT 1
				""";
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, productId);
			ps.setInt(2, providerId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
			return null;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 兼容旧数据：通过 foods 关联校验归属（当 products.provider_id 异常时）
	 */
	public ProductRow findByIdForProviderViaFoods(int productId, int providerId) {
		// JP：兼容旧数据，同样补齐 last_* 占位列，避免 mapRow 取列报错
		// CN：同样补充 last_* 列，防止 mapRow 因列不存在而抛异常
		String sql = """
				SELECT p.id, p.provider_id, p.name, p.category, p.description, p.image_path, p.price_normal, p.created_at,
				       NULL AS last_price_offer, NULL AS last_quantity, NULL AS last_status
				FROM products p
				JOIN foods f ON f.product_id = p.id
				WHERE p.id = ? AND f.provider_id = ?
				LIMIT 1
				""";
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, productId);
			ps.setInt(2, providerId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
			return null;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public int countByProvider(int providerId, String keyword) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM products p WHERE p.provider_id = ? ");
		List<Object> params = new ArrayList<>();
		params.add(providerId);
		if (keyword != null && !keyword.isEmpty()) {
			sql.append("AND (p.name LIKE ? OR p.category LIKE ?) ");
			String like = "%" + keyword + "%";
			params.add(like);
			params.add(like);
		}
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

	private ProductRow mapRow(ResultSet rs) throws SQLException {
		ProductRow r = new ProductRow();
		r.id = rs.getInt("id");
		r.providerId = rs.getInt("provider_id");
		r.name = rs.getString("name");
		r.category = rs.getString("category");
		r.description = rs.getString("description");
		r.imagePath = rs.getString("image_path");
		int pn = rs.getInt("price_normal");
		if (rs.wasNull()) {
			r.priceNormal = null;
		} else {
			r.priceNormal = pn;
		}
		r.createdAt = rs.getTimestamp("created_at");
		int lp = rs.getInt("last_price_offer");
		if (rs.wasNull()) {
			r.lastPriceOffer = null;
		} else {
			r.lastPriceOffer = lp;
		}
		int lq = rs.getInt("last_quantity");
		if (rs.wasNull()) {
			r.lastQuantity = null;
		} else {
			r.lastQuantity = lq;
		}
		r.lastStatus = rs.getString("last_status");
		return r;
	}

	public static class ProductRow {
		public int id;
		public int providerId;
		public String name;
		public String category;
		public String description;
		public String imagePath;
		public Integer priceNormal;
		public java.sql.Timestamp createdAt;
		public Integer lastPriceOffer;
		public Integer lastQuantity;
		public String lastStatus;

		public int getId() { return id; }
		public String getName() { return name; }
		public String getCategory() { return category; }
		public String getDescription() { return description; }
		public String getImagePath() { return imagePath; }
		public Integer getPriceNormal() { return priceNormal; }
		public java.sql.Timestamp getCreatedAt() { return createdAt; }
		public Integer getLastPriceOffer() { return lastPriceOffer; }
		public Integer getLastQuantity() { return lastQuantity; }
		public String getLastStatus() { return lastStatus; }
	}
}

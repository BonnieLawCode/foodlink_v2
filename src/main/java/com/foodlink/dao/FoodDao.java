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

		try (Connection con = DBUtil.getConnection()) {
			con.setAutoCommit(false);

			int productId = ensureProduct(con, providerId, name, category, description, priceNormal, imagePath);

			String sqlFood = """
					INSERT INTO foods
					  (product_id, provider_id,
					   name, description, category,
					   price_offer, quantity, unit, currency,
					   expiry_date, pickup_start, pickup_end,
					   image_path, price_normal, status)
					VALUES
					  (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'OPEN')
				""";

			try (PreparedStatement ps = con.prepareStatement(sqlFood)) {
				ps.setInt(1, productId);
				ps.setInt(2, providerId);
				// legacy fields kept for schema兼容（来自 products 信息）
				ps.setString(3, name);
				ps.setString(4, description);
				ps.setString(5, category);

				ps.setInt(6, priceOffer);
				ps.setInt(7, quantity);
				ps.setString(8, unit);
				ps.setString(9, currency);

				if (expiryDate == null)
					ps.setNull(10, Types.DATE);
				else
					ps.setDate(10, expiryDate);

				if (pickupStart == null)
					ps.setNull(11, Types.TIMESTAMP);
				else
					ps.setTimestamp(11, pickupStart);

				if (pickupEnd == null)
					ps.setNull(12, Types.TIMESTAMP);
				else
					ps.setTimestamp(12, pickupEnd);

				if (imagePath == null)
					ps.setNull(13, Types.VARCHAR);
				else
					ps.setString(13, imagePath);

				if (priceNormal == null)
					ps.setNull(14, Types.INTEGER);
				else
					ps.setInt(14, priceNormal);

				ps.executeUpdate();
			}

			con.commit();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 再出品用：既存 product を指定し、foods だけ新規追加（products は一切更新しない）
	 */
	public void insertFoodWithProduct(int productId, int providerId,
			int quantity, String unit,
			Integer priceNormal, int priceOffer, String currency,
			Date expiryDate,
			String pickupLocation, Timestamp pickupStart, Timestamp pickupEnd,
			String imagePath) {

		String selectProduct = """
				SELECT name, description, category, price_normal, image_path
				FROM products
				WHERE id = ? AND provider_id = ?
				LIMIT 1
				""";

		String sqlFood = """
				INSERT INTO foods
				  (product_id, provider_id,
				   name, description, category,
				   price_offer, quantity, unit, currency,
				   expiry_date, pickup_start, pickup_end,
				   image_path, price_normal, status)
				VALUES
				  (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'OPEN')
				""";

		try (Connection con = DBUtil.getConnection()) {
			con.setAutoCommit(false);

			String name;
			String description;
			String category;
			Integer pnFromProduct = null;
			String imageFromProduct = null;

			try (PreparedStatement ps = con.prepareStatement(selectProduct)) {
				ps.setInt(1, productId);
				ps.setInt(2, providerId);
				try (ResultSet rs = ps.executeQuery()) {
					if (!rs.next()) {
						con.rollback();
						throw new RuntimeException("product not found or not owned: productId=" + productId);
					}
					name = rs.getString("name");
					description = rs.getString("description");
					category = rs.getString("category");
					int pn = rs.getInt("price_normal");
					if (!rs.wasNull()) {
						pnFromProduct = pn;
					}
					imageFromProduct = rs.getString("image_path");
				}
			}

			// 画像は：优先用户上传，其次沿用 product 的 image_path
			String finalImage = imagePath;
			if ((finalImage == null || finalImage.isBlank()) && imageFromProduct != null) {
				finalImage = imageFromProduct;
			}

			try (PreparedStatement ps = con.prepareStatement(sqlFood)) {
				ps.setInt(1, productId);
				ps.setInt(2, providerId);
				ps.setString(3, name);
				ps.setString(4, description);
				ps.setString(5, category);

				ps.setInt(6, priceOffer);
				ps.setInt(7, quantity);
				ps.setString(8, unit);
				ps.setString(9, currency);

				if (expiryDate == null)
					ps.setNull(10, Types.DATE);
				else
					ps.setDate(10, expiryDate);

				if (pickupStart == null)
					ps.setNull(11, Types.TIMESTAMP);
				else
					ps.setTimestamp(11, pickupStart);

				if (pickupEnd == null)
					ps.setNull(12, Types.TIMESTAMP);
				else
					ps.setTimestamp(12, pickupEnd);

				if (finalImage == null)
					ps.setNull(13, Types.VARCHAR);
				else
					ps.setString(13, finalImage);

				if (priceNormal == null && pnFromProduct == null)
					ps.setNull(14, Types.INTEGER);
				else
					ps.setInt(14, priceNormal != null ? priceNormal : pnFromProduct);

				ps.executeUpdate();
			}

			con.commit();
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

		String sql = """
				    SELECT
				      f.id, f.product_id, f.provider_id,
				      p.name, p.description, p.category,
				      f.quantity, f.unit,
				      p.price_normal, f.price_offer, f.currency, f.expiry_date,
				      f.pickup_start, f.pickup_end,
				      p.image_path, f.status, f.created_at
				    FROM foods f
				    JOIN products p ON f.product_id = p.id
				    WHERE f.provider_id = ?
				    ORDER BY
				      CASE WHEN f.status = 'OPEN' THEN 0 ELSE 1 END, -- OPENを上に
				      f.created_at DESC,                                -- 登録日新しい順
				      f.id DESC                                         -- 安定ソート
				""";

		List<Food> list = new ArrayList<>();

		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			// 绑定参数：provider_id
			ps.setInt(1, providerId);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapFood(rs));
				}
			}

		} catch (SQLException e) {
			// 作品阶段：RuntimeException 让错误显性化，方便你定位
			throw new RuntimeException(e);
		}

		return list;
	}

	/**
	 * JP：在庫管理用（筛选/排序）/ CN：库存管理筛选与排序
	 */
	public List<Food> findByProviderIdWithFilters(int providerId, String view, String status, int days, String q) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append("  f.id, f.product_id, f.provider_id, ");
		sql.append("  p.name, p.description, p.category, ");
		sql.append("  f.quantity, f.unit, ");
		sql.append("  p.price_normal, f.price_offer, f.currency, f.expiry_date, ");
		sql.append("  f.pickup_start, f.pickup_end, ");
		sql.append("  p.image_path, f.status, f.created_at ");
		sql.append("FROM foods f ");
		sql.append("JOIN products p ON f.product_id = p.id ");
		sql.append("WHERE f.provider_id = ? ");

		List<Object> params = new ArrayList<>();
		params.add(providerId);

		Timestamp now = new Timestamp(System.currentTimeMillis());

		// JP：表示範囲（登録日基準） / CN：显示范围（按注册日）
		if ("recent".equalsIgnoreCase(view)) {
			if (days <= 1) {
				// JP：今日＝日付一致 / CN：今日=日期一致
				sql.append("AND DATE(f.created_at) = CURDATE() ");
			} else {
				long cutoffMillis = now.getTime() - (days * 24L * 60L * 60L * 1000L);
				Timestamp cutoff = new Timestamp(cutoffMillis);
				sql.append("AND f.created_at >= ? ");
				params.add(cutoff);
			}
		} else if ("history".equalsIgnoreCase(view)) {
			sql.append("AND ( f.created_at < ? OR f.status IN ('CLOSED','EXPIRED') ) ");
			params.add(now);
		}

		// JP：状态筛选 / CN：状态过滤
		if (status != null && !"ALL".equalsIgnoreCase(status)) {
			sql.append("AND f.status = ? ");
			params.add(status);
		}

		// JP：关键字搜索（商品名/受取場所）/ CN：关键字搜索
		if (q != null && !q.trim().isEmpty()) {
			sql.append("AND (p.name LIKE ? ) "); // pickup_location 廃止
			String like = "%" + q.trim() + "%";
			params.add(like);
			params.add(like);
		}

		// JP：OPEN优先，其次按截止时间（近的在上），最后按创建时间
		// CN：OPEN 置顶，其次按 pickup_end（越近越上），最后按创建时间
		sql.append("ORDER BY ");
		sql.append("  CASE WHEN f.status = 'OPEN' THEN 0 ELSE 1 END, "); // OPEN置顶
		sql.append("  f.created_at DESC, ");                               // 登録日新しい順
		sql.append("  f.id DESC ");                                        // 安定ソート

		List<Food> list = new ArrayList<>();
		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapFood(rs));
				}
			}
			return list;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * JP：在庫管理用（分页）/ CN：库存管理分页
	 */
	public List<Food> findByProviderIdWithFiltersPaged(int providerId, String view, String status, int days, String q,
			int page, int size) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append("  f.id, f.product_id, f.provider_id, ");
		sql.append("  p.name, p.description, p.category, ");
		sql.append("  f.quantity, f.unit, ");
		sql.append("  p.price_normal, f.price_offer, f.currency, f.expiry_date, ");
		sql.append("  f.pickup_start, f.pickup_end, ");
		sql.append("  p.image_path, f.status, f.created_at ");
		sql.append("FROM foods f ");
		sql.append("JOIN products p ON f.product_id = p.id ");

		List<Object> params = new ArrayList<>();
		appendProviderFilters(sql, params, providerId, view, status, days, q);

		sql.append("ORDER BY ");
		sql.append("  CASE WHEN f.status = 'OPEN' THEN 0 ELSE 1 END, "); // OPEN置顶
		sql.append("  f.created_at DESC, ");                               // 登録日新しい順
		sql.append("  f.id DESC ");                                        // 安定ソート
		sql.append("LIMIT ? OFFSET ? ");

		int offset = (page - 1) * size;
		params.add(size);
		params.add(offset);

		List<Food> list = new ArrayList<>();
		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapFood(rs));
				}
			}
			return list;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * JP：在庫管理用（件数）/ CN：库存管理总数
	 */
	public int countByProviderIdWithFilters(int providerId, String view, String status, int days, String q) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) ");
		sql.append("FROM foods f ");
		sql.append("JOIN products p ON f.product_id = p.id ");

		List<Object> params = new ArrayList<>();
		appendProviderFilters(sql, params, providerId, view, status, days, q);

		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql.toString())) {
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

	/**
	 * JP：过滤条件拼装 / CN：拼装筛选条件
	 */
	private void appendProviderFilters(StringBuilder sql, List<Object> params, int providerId,
			String view, String status, int days, String q) {
		sql.append("WHERE f.provider_id = ? ");
		params.add(providerId);

		Timestamp now = new Timestamp(System.currentTimeMillis());

		if ("recent".equalsIgnoreCase(view)) {
			if (days <= 1) {
				// JP：今日＝日付一致 / CN：今日=日期一致
				sql.append("AND DATE(f.created_at) = CURDATE() ");
			} else {
				long cutoffMillis = now.getTime() - (days * 24L * 60L * 60L * 1000L);
				Timestamp cutoff = new Timestamp(cutoffMillis);
				sql.append("AND f.created_at >= ? ");
				params.add(cutoff);
			}
		} else if ("history".equalsIgnoreCase(view)) {
			sql.append("AND ( f.created_at < ? OR f.status IN ('CLOSED','EXPIRED') ) ");
			params.add(now);
		}

		if (status != null && !"ALL".equalsIgnoreCase(status)) {
			sql.append("AND f.status = ? ");
			params.add(status);
		}

		if (q != null && !q.trim().isEmpty()) {
			sql.append("AND (p.name LIKE ? ) "); // pickup_location 廃止
			String like = "%" + q.trim() + "%";
			params.add(like);
			params.add(like);
		}
	}

	/**
	* JP：受取者ホーム用：公開中（OPEN）の商品一覧を取得（最小検索付き）
	* CN：给受取者主页用：查询 OPEN 商品列表（带最小搜索）
	*/
	public List<Food> findOpenFoodsForReceiver(String keyword, String category, String area, String sort) {
		List<Food> list = new ArrayList<>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append("  f.id, f.product_id, f.provider_id, ");
		sql.append("  p.name, p.description, p.category, ");
		sql.append("  f.quantity, f.unit, ");
		sql.append("  p.price_normal, f.price_offer, f.currency, ");
		sql.append("  f.expiry_date, f.pickup_start, f.pickup_end, ");
		sql.append("  p.image_path, f.status, f.created_at, ");
		sql.append("  c.address AS company_address, ");
		sql.append("  c.name AS company_name ");
		sql.append("FROM foods f ");
		sql.append("JOIN products p ON f.product_id = p.id ");
		sql.append("LEFT JOIN users u ON u.id = f.provider_id ");
		sql.append("LEFT JOIN companies c ON c.id = u.company_id ");
		sql.append("WHERE f.status = 'OPEN' ");
		sql.append("AND f.pickup_end IS NOT NULL AND f.pickup_end > ? ");
		sql.append("AND f.quantity > 0 ");

		// JP：キーワード（商品名 or 説明）
		// CN：关键词（商品名/描述）
		boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
		if (hasKeyword) {
			sql.append("AND (p.name LIKE ? OR p.description LIKE ?) ");
		}

		// JP：カテゴリ
		// CN：分类
		boolean hasCategory = category != null && !category.trim().isEmpty();
		if (hasCategory) {
			sql.append("AND p.category = ? ");
		}

		// JP：エリア（受取場所の部分一致）
		// CN：地区（取货地点模糊匹配）
		boolean hasArea = area != null && !area.trim().isEmpty();
		if (hasArea) {
			// pickup_location は廃止、住所検索は会社住所側で今後対応
		}

		// JP：並び替え
		// CN：排序
		if ("price".equals(sort)) {
			// JP：特別価格（price_offer）安い順 / CN：特价升序
			sql.append("ORDER BY f.price_offer ASC, f.id DESC ");
		} else {
			sql.append("ORDER BY f.id DESC ");
		}

		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql.toString())) {

			int idx = 1;
			ps.setTimestamp(idx++, new Timestamp(System.currentTimeMillis()));

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
					list.add(mapFood(rs));
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

		String sql = """
				SELECT
				  f.id, f.product_id, f.provider_id,
				  p.name, p.description, p.category,
				  f.quantity, f.unit,
				  p.price_normal, f.price_offer, f.currency,
				  f.pickup_end, p.image_path, f.created_at,
				  c.address AS company_address,
				  c.name    AS company_name
				FROM foods f
				JOIN products p ON f.product_id = p.id
				LEFT JOIN users u ON u.id = f.provider_id
				LEFT JOIN companies c ON c.id = u.company_id
				WHERE f.status = 'OPEN'
				  AND f.pickup_end IS NOT NULL AND f.pickup_end > ?
				  AND f.quantity > 0
				ORDER BY f.created_at DESC
				LIMIT ?
				""";

		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			ps.setInt(2, limit);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapFood(rs));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	// 商品详情页面：根据商品id查询
	public Food findById(int id) {

		String sql = """
				    SELECT
				        f.id,
				        f.product_id,
				        f.provider_id,
				        p.name,
				        p.description,
				        p.category,
				        f.quantity,
				        f.unit,
				        p.price_normal,
				        f.price_offer,
				        f.currency,
				        f.expiry_date,
				        f.pickup_start,
				        f.pickup_end,
				        p.image_path,
				        f.status,
				        f.created_at,
				        c.address AS company_address
				    FROM foods f
				    JOIN products p ON f.product_id = p.id
				    LEFT JOIN users u ON u.id = f.provider_id
				    LEFT JOIN companies c ON c.id = u.company_id
				    WHERE f.id = ?
				    LIMIT 1
				""";

		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;

				return mapFood(rs);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// JP：在庫を減らす（在庫が足りる時だけ）
	// CN：扣库存（只有库存足够时才成功）
	// 返回：true=扣成功 / false=库存不足或商品不存在
	public boolean decreaseQuantityIfEnough(int foodId, int qty) {
		String sqlDecrease = """
				    UPDATE foods
				    SET quantity = quantity - ?
				    WHERE id = ? AND quantity >= ?
				""";
		String sqlCloseIfEmpty = """
				    UPDATE foods
				    SET status = 'CLOSED'
				    WHERE id = ? AND quantity = 0 AND status = 'OPEN'
				""";

		try (Connection con = DBUtil.getConnection()) {
			con.setAutoCommit(false);

			int updated;
			try (PreparedStatement ps = con.prepareStatement(sqlDecrease)) {
				ps.setInt(1, qty);
				ps.setInt(2, foodId);
				ps.setInt(3, qty);
				updated = ps.executeUpdate();
			}
			if (updated != 1) {
				con.rollback();
				return false;
			}

			// JP：在庫が0になったら CLOSED にする / CN：库存为0则置为CLOSED
			try (PreparedStatement ps = con.prepareStatement(sqlCloseIfEmpty)) {
				ps.setInt(1, foodId);
				ps.executeUpdate();
			}

			con.commit();
			return true;

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * JP：OPEN -> CLOSED（販売終了）
	 * CN：OPEN -> CLOSED（销售结束）
	 */
	public boolean closeFood(int foodId, int providerId) {
		String sql = """
				    UPDATE foods
				    SET status = 'CLOSED'
				    WHERE id = ? AND provider_id = ? AND status = 'OPEN'
				""";

		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, foodId);
			ps.setInt(2, providerId);
			return ps.executeUpdate() == 1;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * JP：期限切れのOPEN商品をEXPIREDへ更新
	 * CN：把过期的OPEN商品更新为EXPIRED
	 */
	public int expireOpenFoods(int graceMinutes) {
		// JP：Java側で締切時刻を計算 / CN：在Java侧计算截止时间
		long cutoffMillis = System.currentTimeMillis() - (graceMinutes * 60L * 1000L);
		Timestamp cutoff = new Timestamp(cutoffMillis);

		String sql = """
				    UPDATE foods
				    SET status = 'EXPIRED'
				    WHERE status = 'OPEN'
				      AND pickup_end IS NOT NULL
				      AND pickup_end < ?
				""";

		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setTimestamp(1, cutoff);
			return ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * JP：防御的なステータス修正（数量ゼロや期限切れを一覧表示前に整合させる）
	 * CN：防御性修正：列表前纠正库存/过期状态
	 */
	public void fixStatusesForProvider(int providerId) {
		String sqlExpire = """
				    UPDATE foods
				    SET status = 'EXPIRED'
				    WHERE provider_id = ?
				      AND pickup_end IS NOT NULL
				      AND pickup_end <= NOW()
				      AND status != 'EXPIRED'
				""";
		String sqlCloseZero = """
				    UPDATE foods
				    SET status = 'CLOSED'
				    WHERE provider_id = ?
				      AND quantity <= 0
				      AND status = 'OPEN'
				""";

		try (Connection con = DBUtil.getConnection()) {
			con.setAutoCommit(false);
			try (PreparedStatement ps = con.prepareStatement(sqlExpire)) {
				ps.setInt(1, providerId);
				ps.executeUpdate();
			}
			try (PreparedStatement ps = con.prepareStatement(sqlCloseZero)) {
				ps.setInt(1, providerId);
				ps.executeUpdate();
			}
			con.commit();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * JP：既存 product を取得（なければ作成）/ CN：获取或创建 product
	 */
	private int ensureProduct(Connection con, int providerId, String name, String category, String description,
			Integer priceNormal, String imagePath) throws SQLException {
		String select = """
				SELECT id, image_path FROM products
				WHERE provider_id = ? AND name = ? AND category = ?
				LIMIT 1
				""";
		try (PreparedStatement ps = con.prepareStatement(select)) {
			ps.setInt(1, providerId);
			ps.setString(2, name);
			ps.setString(3, category);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					int pid = rs.getInt("id");
					return pid;
				}
			}
		}

		String insert = """
				INSERT INTO products
				  (provider_id, name, description, category, price_normal, image_path)
				VALUES
				  (?, ?, ?, ?, ?, ?)
				""";
		try (PreparedStatement ps = con.prepareStatement(insert, PreparedStatement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, providerId);
			ps.setString(2, name);
			ps.setString(3, description);
			ps.setString(4, category);
			if (priceNormal == null) {
				ps.setNull(5, Types.INTEGER);
			} else {
				ps.setInt(5, priceNormal);
			}
			if (imagePath == null) {
				ps.setNull(6, Types.VARCHAR);
			} else {
				ps.setString(6, imagePath);
			}
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		}
		throw new SQLException("failed to ensure product");
	}

	/**
	 * JP：ResultSet → Food マッピング（products との JOIN 前提の列名） / CN：结果集映射
	 */
	private Food mapFood(ResultSet rs) throws SQLException {
		Food f = new Food();
		f.setId(rs.getInt("id"));
		f.setProductId(rs.getInt("product_id"));
		f.setProviderId(rs.getInt("provider_id"));
		f.setName(rs.getString("name"));
		f.setDescription(rs.getString("description"));
		f.setCategory(rs.getString("category"));
		f.setQuantity(rs.getInt("quantity"));
		f.setUnit(rs.getString("unit"));

		Integer priceNormal = (Integer) rs.getObject("price_normal");
		f.setPriceNormal(priceNormal);

		f.setPriceOffer(rs.getInt("price_offer"));
		f.setCurrency(rs.getString("currency"));
		f.setExpiryDate(rs.getDate("expiry_date"));
		// JP：pickup_location 列は廃止済み / CN：pickup_location 已废止，不再读取
		f.setPickupStart(rs.getTimestamp("pickup_start"));
		f.setPickupEnd(rs.getTimestamp("pickup_end"));
		f.setImagePath(rs.getString("image_path"));
		f.setStatus(rs.getString("status"));
		f.setCreatedAt(rs.getTimestamp("created_at"));
		// JP：会社住所/会社名（存在する場合のみセット）/ CN：公司地址/公司名，有列时才填充
		try {
			f.setCompanyAddress(rs.getString("company_address"));
		} catch (SQLException ignore) {
			// 列がないSELECTでは無視
		}
		try {
			f.setCompanyName(rs.getString("company_name"));
		} catch (SQLException ignore) {
			// 列がないSELECTでは無視
		}
		return f;
	}

	/**
	 * JP：詳細表示用に company_address を含めて1件取得
	 * CN：详情页专用，连同店铺地址(company.address)一起取得
	 */
	public FoodDetailView findDetailWithCompany(int foodId) {
		String sql = """
				SELECT
				  f.*,
				  c.address AS company_address,
				  c.name    AS company_name
				FROM foods f
				LEFT JOIN users u ON u.id = f.provider_id
				LEFT JOIN companies c ON c.id = u.company_id
				WHERE f.id = ?
				LIMIT 1
				""";
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, foodId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapFoodDetail(rs);
				}
			}
			return null;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private FoodDetailView mapFoodDetail(ResultSet rs) throws SQLException {
		FoodDetailView v = new FoodDetailView();
		v.id = rs.getInt("id");
		v.productId = rs.getInt("product_id");
		v.providerId = rs.getInt("provider_id");
		v.name = rs.getString("name");
		v.description = rs.getString("description");
		v.category = rs.getString("category");
		v.quantity = rs.getInt("quantity");
		v.unit = rs.getString("unit");
		v.priceNormal = (Integer) rs.getObject("price_normal");
		v.priceOffer = rs.getInt("price_offer");
		v.currency = rs.getString("currency");
		v.expiryDate = rs.getDate("expiry_date");
		v.pickupStart = rs.getTimestamp("pickup_start");
		v.pickupEnd = rs.getTimestamp("pickup_end");
		v.imagePath = rs.getString("image_path");
		v.status = rs.getString("status");
		v.createdAt = rs.getTimestamp("created_at");
		v.companyAddress = rs.getString("company_address");
		v.companyName = rs.getString("company_name");
		return v;
	}

	/**
	 * JP：详情页用のDTO（foods + company）
	 * CN：详情页专用 DTO（包含店铺地址）
	 */
	public static class FoodDetailView {
		public int id;
		public int productId;
		public int providerId;
		public String name;
		public String description;
		public String category;
		public int quantity;
		public String unit;
		public Integer priceNormal;
		public int priceOffer;
		public String currency;
		public Date expiryDate;
		public Timestamp pickupStart;
		public Timestamp pickupEnd;
		public String imagePath;
		public String status;
		public Timestamp createdAt;
		public String companyAddress;
		public String companyName;

		// Getter（JSP EL 用）
		public int getId() { return id; }
		public int getProductId() { return productId; }
		public int getProviderId() { return providerId; }
		public String getName() { return name; }
		public String getDescription() { return description; }
		public String getCategory() { return category; }
		public int getQuantity() { return quantity; }
		public String getUnit() { return unit; }
		public Integer getPriceNormal() { return priceNormal; }
		public int getPriceOffer() { return priceOffer; }
		public String getCurrency() { return currency; }
		public Date getExpiryDate() { return expiryDate; }
		public Timestamp getPickupStart() { return pickupStart; }
		public Timestamp getPickupEnd() { return pickupEnd; }
		public String getImagePath() { return imagePath; }
		public String getStatus() { return status; }
		public Timestamp getCreatedAt() { return createdAt; }
		public String getCompanyAddress() { return companyAddress; }
		public String getCompanyName() { return companyName; }
	}
}

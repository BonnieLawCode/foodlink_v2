<%@ page contentType="text/html; charset=UTF-8"%>

<%-- 
  JP：JSTL（繰り返し・条件分岐）を使うためのtaglib
  CN：使用 JSTL（循环/判断）需要引入 taglib
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%--
  JP：日時フォーマットに使う fmt taglib
  CN：用 fmt 来格式化 Timestamp 显示
--%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<%--
  JP：ContextPath（例：/foodlink_v2）を変数に保存
  CN：把项目 ContextPath（例如 /foodlink_v2）存成变量
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%--
  JP：ヘッダー部に表示するページタイトル
  CN：顶部 header 用的页面标题
--%>
<%
request.setAttribute("pageTitle", "在庫管理");
%>
<%
//JP：サイドバーで「在庫管理」を選択状態にする
// CN：让侧边栏“在庫管理”处于高亮选中状态
request.setAttribute("activeMenu", "food_list");
%>


<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>在庫管理 | もったいナビ</title>

<%-- 
    JP：共通レイアウトCSS（あなたの配置：webapp/assets/css/）
    CN：通用布局 CSS（你的路径在 assets/css 下）
    ※ v=1 はキャッシュ対策 / v=1 防止浏览器缓存旧CSS
  --%>
<link rel="stylesheet" href="${ctx}/assets/css/provider-layout.css?v=1">

<%-- 
    JP：在庫ページ専用CSS（まだ無くてもOK、後で作ってもOK）
    CN：库存页面专用 CSS（没有也没关系，先占位）
  --%>
<%-- <link rel="stylesheet" href="${ctx}/assets/css/provider-food-list.css?v=1"> --%>

<style>
/* 
      JP：今は簡単にtableだけ最低限整える（あとでCSSファイルへ移動OK）
      CN：先临时把 table 样式写在页面里（之后可挪到 CSS 文件）
    */
.filter-bar {
	display: flex;
	flex-wrap: wrap;
	gap: 12px;
	align-items: center;
	margin: 8px 0 14px;
}

.filter-bar label {
	font-size: 13px;
	color: #333;
}

.filter-bar select,
.filter-bar input[type="text"] {
	padding: 6px 8px;
	border: 1px solid #ddd;
	border-radius: 6px;
}

.table-wrap {
	margin-top: 14px;
}

table {
	border-collapse: collapse;
	width: 100%;
	background: #fff;
}

th, td {
	border: 1px solid #e5e5e5;
	padding: 10px 12px;
	vertical-align: top;
}

th {
	background: #fafafa;
	font-weight: 700;
	position: sticky;
	top: 0;
	z-index: 1;
}

tbody tr:hover {
	background: #f7f7f7;
}

img {
	width: 86px;
	height: 86px;
	object-fit: cover;
	border-radius: 10px;
}

.muted {
	color: #777;
	font-size: 12px;
	line-height: 1.5;
}

.food-name {
	font-size: 18px;
	font-weight: 800;
	margin-bottom: 6px;
}

.link {
	color: #5b36c7;
	text-decoration: none;
	font-weight: 700;
}

.link:hover {
	text-decoration: underline;
}

.btn {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	gap: 6px;
	padding: 6px 12px;
	border-radius: 8px;
	font-size: 13px;
	text-decoration: none;
	cursor: pointer;
	border: 1px solid transparent;
	width: 130px; /* 操作列のボタン幅を統一 */
	box-sizing: border-box;
	text-align: center;
}

.btn-primary {
	background: #2563eb;
	color: #fff;
	border-color: #2563eb;
}

.btn-primary:hover {
	opacity: 0.92;
}

.btn-secondary {
	background: #fff;
	color: #666;
	border-color: #d0d0d0;
}

.btn-secondary:hover {
	border-color: #b5b5b5;
	color: #444;
}

.btn-ghost {
	background: #eaf2ff;
	color: #2563eb;
	border-color: #d7e7ff;
	padding: 4px 10px;
}

.btn-ghost:hover {
	background: #dbeafe;
}

.btn-stop {
	background: #fff;
	color: #dc2626;
	border: 1px solid #d0d0d0;
	padding: 6px 12px;
	margin: 8px auto 0;
	border-radius: 8px;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	text-decoration: none;
	box-sizing: border-box;
	width: 130px; /* 他ボタンと幅を統一 */
}

.btn-stop:hover {
	background: #fef2f2;
}

.btn-icon::before {
	content: "⟳";
	font-size: 12px;
}

.status-badge {
	display: inline-block;
	padding: 2px 8px;
	border-radius: 999px;
	font-size: 12px;
	background: #f2f2f2;
	color: #333;
}

.status-open {
	background: #e8f6ec;
	color: #1f6b3a;
}

.status-closed {
	background: #f3f3f3;
	color: #666;
}

.status-expired {
	background: #fff3e5;
	color: #8a4b08;
}

.action-buttons {
	display: flex;
	flex-direction: column;
	align-items: center; /* 横方向センター寄せ */
	gap: 8px; /* ボタン間の統一余白 */
}

.pagination {
	display: flex;
	gap: 6px;
	justify-content: center;
	margin: 18px 0 6px;
	align-items: center;
}

.page-item {
	display: inline-flex;
	align-items: center;
}

.page-count {
	font-size: 13px;
	color: #333;
	margin-right: 8px;
}

.page-link {
	padding: 6px 10px;
	border: 1px solid #ddd;
	border-radius: 6px;
	text-decoration: none;
	color: #333;
	font-size: 13px;
}

.page-link.active {
	background: #222;
	color: #fff;
	border-color: #222;
}

.page-link.disabled {
	color: #aaa;
	pointer-events: none;
}
</style>
</head>

<body>

	<div class="app">

		<%-- 
    JP：左サイドバー（共通部品）を読み込み
    CN：引入左侧 sidebar（公共组件）
  --%>
		<jsp:include page="/WEB-INF/views/provider/_sidebar.jsp" />

		<%-- 右側コンテンツ / 右侧内容区域 --%>
		<main class="content">
			<div class="content-inner">

				<%-- 
        JP：上部ヘッダー（ページタイトル + 検索UI）
        CN：顶部 header（标题 + 搜索框）
      --%>
				<jsp:include page="/WEB-INF/views/provider/_header.jsp" />

				<div class="content-card">
				<c:set var="msg" value="${param.msg}" />
				<c:set var="error" value="${param.error}" />
				<c:if test="${not empty msg or not empty error}">
					<div id="fl-food-msg" style="margin: 8px 0 12px; padding: 10px 12px; border: 1px solid #ddd; background: #fafafa; color: #333;">
						<c:choose>
							<c:when test="${msg == 'close_success'}">販売終了に更新しました。</c:when>
							<c:when test="${error == 'not_found'}">対象の商品が見つかりません。</c:when>
							<c:otherwise>エラーが発生しました。時間を置いてもう一度お試しください。</c:otherwise>
						</c:choose>
					</div>
				</c:if>
				<c:if test="${msg == 'close_success'}">
					<script>
						setTimeout(function () {
							var el = document.getElementById('fl-food-msg');
							if (el) el.style.display = 'none';
						}, 2000);
					</script>
				</c:if>

				<form method="get" action="${ctx}/provider/foods" class="filter-bar">
					<label>
						表示範囲：
						<select name="view">
							<option value="recent" <c:if test="${view == 'recent'}">selected</c:if>>最近</option>
							<option value="all" <c:if test="${view == 'all'}">selected</c:if>>すべて</option>
							<option value="history" <c:if test="${view == 'history'}">selected</c:if>>履歴</option>
						</select>
					</label>
					<label>
						状態：
						<select name="status">
							<option value="ALL" <c:if test="${status == 'ALL'}">selected</c:if>>すべて</option>
							<option value="OPEN" <c:if test="${status == 'OPEN'}">selected</c:if>>公開中</option>
							<option value="CLOSED" <c:if test="${status == 'CLOSED'}">selected</c:if>>販売終了</option>
							<option value="EXPIRED" <c:if test="${status == 'EXPIRED'}">selected</c:if>>期限切れ</option>
						</select>
					</label>
					<label>
						期間：
						<select name="days">
							<option value="1" <c:if test="${days == 1}">selected</c:if>>今日</option>
							<option value="7" <c:if test="${days == 7}">selected</c:if>>7日</option>
							<option value="30" <c:if test="${days == 30}">selected</c:if>>30日</option>
						</select>
					</label>
					<label>
						検索：
						<input type="text" name="q" value="${q}" placeholder="商品名">
					</label>
					<label>
						件数：
						<select name="size">
							<option value="5" <c:if test="${pageSize == 5}">selected</c:if>>5</option>
							<option value="10" <c:if test="${pageSize == 10}">selected</c:if>>10</option>
							<option value="20" <c:if test="${pageSize == 20}">selected</c:if>>20</option>
						</select>
					</label>
					<button type="submit" class="btn btn-primary">適用</button>
					<a class="btn btn-secondary" href="${ctx}/provider/foods">リセット</a>
				</form>

				<div class="table-wrap">
					<table>
						<thead>
							<tr>
								<th>画像</th>
								<th>商品</th>
								<th>価格</th>
								<th>在庫</th>
								<th>状態</th>
								<th>受け取り時間</th>
								<th>登録日</th>
								<th>操作</th>
							</tr>
						</thead>

						<tbody>
							<%-- JP：foods が空なら「まだ商品がありません」CN：如果 foods 为空，显示提示--%>
							<c:if test="${empty foods}">
								<tr>
									<td colspan="9">まだ商品がありません。先に「新規商品登録」から追加してください。</td>
								</tr>
							</c:if>

							<%-- JP：foods を繰り返して行を出す / CN：循环输出每行 --%>
							<c:forEach var="f" items="${foods}">
								<tr>

									<%-- 画像 / 图片 --%>

									<td><c:choose>
											<c:when test="${not empty f.imagePath}">
		<%--
        JP：DBの imagePath がある場合はそれを表示
        CN：DB 里有 imagePath 就显示它
        ※ ただしファイルが存在しない(404)場合もあるので onerror でデフォルト画像へ
        ※ 但可能文件不存在(404)，所以用 onerror 自动换默认图
        --%>
												<img class="thumb" src="${ctx}${f.imagePath}" alt="food"
													onerror="this.onerror=null; this.src='${ctx}/assets/img/noimage.jpg';">
											</c:when>

											<c:otherwise>
		<%-- JP：imagePath が無い場合もデフォルト画像を表示 CN：没有 imagePath 时也显示默认图 --%>
												<img class="thumb" src="${ctx}/assets/img/noimage.jpg"
													alt="no image">
											</c:otherwise>
										</c:choose></td>



									<%-- 商品信息 / 商品信息 --%>
									<td>
										<div class="food-name" title="ID: ${f.id}">${f.name}</div>
										<div class="muted">カテゴリ：${f.category}</div>
									</td>

									<%-- 价格 / 价格 --%>
									<td>
										<div>${f.priceOffer} JPY</div>
									</td>

									<%-- 在庫 / 库存 --%>
									<td>${f.quantity}${f.unit}</td>

									<%-- 状態 / 状态 --%>
									<td>
										<c:choose>
											<c:when test="${f.status == 'OPEN' and f.quantity > 0}">
												<span class="status-badge status-open">公開中</span>
											</c:when>
											<c:when test="${f.status == 'CLOSED'}">
												<span class="status-badge status-closed">販売終了</span>
											</c:when>
											<c:when test="${f.status == 'EXPIRED'}">
												<span class="status-badge status-expired">期限切れ</span>
											</c:when>
											<c:otherwise>
												<%-- 安全兜底：数量0时即使状态为OPEN也显示販売終了 --%>
												<c:choose>
													<c:when test="${f.quantity <= 0}">
														<span class="status-badge status-closed">販売終了</span>
													</c:when>
													<c:otherwise>
														<span class="status-badge">${f.status}</span>
													</c:otherwise>
												</c:choose>
											</c:otherwise>
										</c:choose>
									</td>

									<%-- 受け取り時間 / 取货时间（格式化显示） --%>
									<td>
										<c:choose>
											<c:when test="${not empty f.pickupStart and not empty f.pickupEnd}">
												<fmt:formatDate var="pickupStartDay" value="${f.pickupStart}" pattern="M/d" />
												<fmt:formatDate var="pickupEndDay" value="${f.pickupEnd}" pattern="M/d" />
												<fmt:formatDate var="pickupStartTime" value="${f.pickupStart}" pattern="H:mm" />
												<fmt:formatDate var="pickupEndTime" value="${f.pickupEnd}" pattern="H:mm" />
												<c:choose>
													<c:when test="${pickupStartDay == pickupEndDay}">
														${pickupStartDay} ${pickupStartTime} – ${pickupEndTime}
													</c:when>
													<c:otherwise>
														${pickupStartDay} ${pickupStartTime} – ${pickupEndDay} ${pickupEndTime}
													</c:otherwise>
												</c:choose>
											</c:when>

											<c:when test="${not empty f.pickupStart and empty f.pickupEnd}">
												<fmt:formatDate value="${f.pickupStart}" pattern="M/d H:mm" />
											</c:when>

											<c:otherwise>
												<span class="muted">未設定</span>
											</c:otherwise>
										</c:choose>
									</td>

									<%-- 登録日 / 创建时间（格式化显示） --%>
									<td><c:choose>
											<c:when test="${not empty f.createdAt}">
												<fmt:formatDate value="${f.createdAt}"
													pattern="yyyy/MM/dd HH:mm" />
											</c:when>
											<c:otherwise>
												<span class="muted">-</span>
											</c:otherwise>
										</c:choose></td>

									<%-- 操作 / 操作按钮 --%>
									<td>
										<div class="action-buttons">
											<a class="btn btn-secondary" href="${ctx}/provider/products?focusProductId=${f.productId}">商品管理へ</a>
											<c:if test="${f.status == 'OPEN' and f.quantity > 0}">
												<form method="post" action="${ctx}/provider/foods/close" style="display:inline;">
													<input type="hidden" name="foodId" value="${f.id}">
													<button type="submit" class="btn btn-stop">販売終了</button>
												</form>
											</c:if>
										</div>
									</td>

								</tr>
							</c:forEach>

						</tbody>
					</table>
				</div>

				<c:if test="${totalPages > 0}">
					<c:url var="pageBase" value="/provider/foods">
						<c:param name="view" value="${view}" />
						<c:param name="status" value="${status}" />
						<c:param name="days" value="${days}" />
						<c:param name="q" value="${q}" />
						<c:param name="size" value="${pageSize}" />
					</c:url>

					<c:set var="totalCountSafe" value="${empty totalCount ? (totalPages * pageSize) : totalCount}" />

					<div class="pagination">
						<span class="page-item page-count">全 ${totalCountSafe} 件</span>

						<c:choose>
							<c:when test="${currentPage > 1}">
								<a class="page-item page-link" href="${pageBase}&page=${currentPage - 1}">‹ 前へ</a>
							</c:when>
							<c:otherwise>
								<span class="page-item page-link disabled">‹ 前へ</span>
							</c:otherwise>
						</c:choose>

						<c:forEach var="p" begin="1" end="${totalPages}">
							<c:choose>
								<c:when test="${p == currentPage}">
									<span class="page-item page-link active">${p}</span>
								</c:when>
								<c:otherwise>
									<a class="page-item page-link" href="${pageBase}&page=${p}">${p}</a>
								</c:otherwise>
							</c:choose>
						</c:forEach>

						<c:choose>
							<c:when test="${currentPage < totalPages}">
								<a class="page-item page-link" href="${pageBase}&page=${currentPage + 1}">次へ ›</a>
							</c:when>
							<c:otherwise>
								<span class="page-item page-link disabled">次へ ›</span>
							</c:otherwise>
						</c:choose>
					</div>
				</c:if>

			</div>
			</div>
		</main>

	</div>

</body>
</html>

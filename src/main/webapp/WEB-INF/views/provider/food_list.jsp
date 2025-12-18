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
	padding: 14px;
	vertical-align: top;
}

th {
	background: #fafafa;
	font-weight: 700;
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

				<%-- 
        JP：新規商品登録へのリンク（在庫ページの上に配置）
        CN：跳转到新规商品登记页的入口
      --%>
				<p style="margin: 0 0 12px 0;">
					<a class="link" href="${ctx}/provider/foods/new">＋ 新規商品登録</a>
				</p>

				<div class="table-wrap">
					<table>
						<thead>
							<tr>
								<th>画像</th>
								<th>商品</th>
								<th>価格</th>
								<th>在庫</th>
								<th>状態</th>
								<th>受け取り場所</th>
								<th>受け取り時間</th>
								<th>登録日</th>
							</tr>
						</thead>

						<tbody>
							<%-- JP：foods が空なら「まだ商品がありません」CN：如果 foods 为空，显示提示--%>
							<c:if test="${empty foods}">
								<tr>
									<td colspan="8">まだ商品がありません。先に「新規商品登録」から追加してください。</td>
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
										<div class="food-name">${f.name}</div>
										<div class="muted">カテゴリ：${f.category}</div>
										<div class="muted">ID：${f.id}</div>
									</td>

									<%-- 价格 / 价格 --%>
									<td>
										<div>特別：${f.priceOffer} ${f.currency}</div> <c:if
											test="${not empty f.priceNormal}">
											<div class="muted">通常：${f.priceNormal} ${f.currency}</div>
										</c:if>
									</td>

									<%-- 在庫 / 库存 --%>
									<td>${f.quantity}${f.unit}</td>

									<%-- 状態 / 状态 --%>
									<td>${f.status}</td>

									<%-- 受け取り場所 / 取货地点 --%>
									<td>${f.pickupLocation}</td>

									<%-- 受け取り時間 / 取货时间（格式化显示） --%>
									<td><c:choose>
											<c:when test="${not empty f.pickupStart and not empty f.pickupEnd}">
												<fmt:formatDate value="${f.pickupStart}" pattern="yyyy/MM/dd HH:mm" />
												～
												<fmt:formatDate value="${f.pickupEnd}" pattern="yyyy/MM/dd HH:mm" />
											</c:when>

											<c:when
												test="${not empty f.pickupStart and empty f.pickupEnd}">
												<fmt:formatDate value="${f.pickupStart}"
													pattern="yyyy/MM/dd HH:mm" />
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

								</tr>
							</c:forEach>

						</tbody>
					</table>
				</div>

			</div>
		</main>

	</div>

</body>
</html>

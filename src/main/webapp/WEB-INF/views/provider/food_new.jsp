<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<%
// JP：header表示用タイトル
// CN：header 显示用标题
request.setAttribute("pageTitle", "新規商品登録");
%>
<%
//JP：サイドバーで「新規商品登録」を選択状態にする
// CN：让侧边栏“新規商品登録”处于高亮选中状态
  request.setAttribute("activeMenu", "food_new");
%>

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>新規商品登録 | もったいナビ</title>
<%-- 共通レイアウトCSS / 通用布局CSS --%>
<link rel="stylesheet" href="${ctx}/assets/css/provider-layout.css?v=1">

<%-- ページ専用CSS（先空でもOK）/ 页面专用CSS（可先不写） --%>
<link rel="stylesheet" href="${ctx}/css/provider-food-new.css">
</head>
<body>
	<div class="app">

		<%-- 左サイドバー / 左侧栏 --%>
		<jsp:include page="/WEB-INF/views/provider/_sidebar.jsp" />

		<%-- 右コンテンツ / 右侧内容 --%>
		<main class="content">
			<div class="content-inner">

				<%-- 上部ヘッダー / 顶部标题+搜索 --%>
				<jsp:include page="/WEB-INF/views/provider/_header.jsp" />

				<%-- ↓ 这里放你的表单主体（你之前的form内容原封不动搬过来） --%>

				<c:if test="${not empty error}">
					<p style="color: red">${error}</p>
				</c:if>

				<form method="post" action="${ctx}/provider/foods/create"
					enctype="multipart/form-data">
					<div>
						<label>商品名：</label><br> <input type="text" name="name"
							required>
					</div>

					<div>
						<label>カテゴリ：</label><br> <select name="category">
							<option value="">選択してください</option>
							<option value="弁当">弁当</option>
							<option value="パン">パン</option>
							<option value="惣菜">惣菜</option>
							<option value="飲料">飲料</option>
							<option value="その他">その他</option>
						</select>
					</div>

					<div>
						<label>通常価格（円）：</label><br> <input type="number"
							name="price_normal" min="0">
					</div>

					<div>
						<label>特別価格（円）：</label><br> <input type="number"
							name="price_offer" min="0" required>
					</div>

					<div>
						<label>販売可能数：</label><br> <input type="number"
							name="quantity" min="1" required>
					</div>

					<div>
						<label>単位（例：個）：</label><br> <input type="text" name="unit"
							value="個" required>
					</div>

					<div>
						<label>受け取り場所：</label><br> <input type="text"
							name="pickup_location" required>
					</div>

					<div>
						<label>受け取り開始：</label><br> <input type="datetime-local"
							name="pickup_start">
					</div>

					<div>
						<label>受け取り終了：</label><br> <input type="datetime-local"
							name="pickup_end">
					</div>

					<div>
						<label>賞味期限：</label><br> <input type="date"
							name="expiry_date">
					</div>

					<div>
						<label>商品説明：</label><br>
						<textarea name="description" rows="4"></textarea>
					</div>

					<div>
						<label>画像アップロード：</label><br> <input type="file" name="image"
							accept="image/*">
						<p style="font-size: 12px; color: #666;">jpg/png/webp 推奨</p>
					</div>

					<button type="submit">登録する</button>
				</form>

			</div>
		</main>

	</div>

</body>
</html>

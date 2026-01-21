<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
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
<style>
	.fl-form-wrap {
		max-width: 1100px;
	}

	.fl-form-grid {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 24px;
	}

	.fl-col {
		display: flex;
		flex-direction: column;
		gap: 16px;
	}

	.fl-field {
		display: flex;
		flex-direction: column;
		gap: 6px;
	}

	.fl-row-2 {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 12px;
	}

	.fl-field label {
		font-weight: 700;
	}

	.fl-field input,
	.fl-field select,
	.fl-field textarea {
		padding: 10px 12px;
		border: 1px solid #ddd;
		border-radius: 8px;
		font-size: 14px;
		background: #fff;
	}

	.fl-field textarea {
		min-height: 120px;
		resize: vertical;
	}

	.fl-req {
		color: #d33;
		font-weight: 700;
		margin-left: 4px;
	}

	.fl-submit {
		margin-top: 8px;
		padding: 12px 16px;
		border: none;
		border-radius: 10px;
		background: #222;
		color: #fff;
		font-weight: 700;
		cursor: pointer;
	}

	.fl-submit:hover {
		opacity: 0.92;
	}

	@media (max-width: 980px) {
		.fl-form-grid {
			grid-template-columns: 1fr;
		}
	}
</style>
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

				<div class="content-card">
				<%-- ↓ 这里放你的表单主体（你之前的form内容原封不动搬过来） --%>

				<c:if test="${not empty error}">
					<p style="color: red">${error}</p>
				</c:if>
				<c:if test="${mode == 'RELIST'}">
					<p style="color:#666; font-size:12px; margin:4px 0 10px;">
						※ 商品情報は再出品では変更できません。変更する場合は商品管理から編集してください。
					</p>
				</c:if>

				<form method="post" action="${ctx}/provider/foods/create"
					enctype="multipart/form-data" class="fl-form-wrap">
					<input type="hidden" name="mode" value="${mode}" />
					<c:if test="${not empty copyProductId}">
						<input type="hidden" name="product_id" value="${copyProductId}" />
					</c:if>
					<div class="fl-form-grid">
						<div class="fl-col">
							<div class="fl-field">
								<label>商品名<span class="fl-req">*</span></label>
								<input type="text" name="name" value="${copyName}" required placeholder="例：唐揚げ弁当"
									<c:if test="${mode == 'RELIST'}">readonly</c:if>>
							</div>

							<div class="fl-field">
								<label>カテゴリ<span class="fl-req">*</span></label>
								<select name="category" required <c:if test="${mode == 'RELIST'}">disabled</c:if>>
									<option value="">選択してください</option>
									<option value="弁当" <c:if test="${copyCategory == '弁当'}">selected</c:if>>弁当</option>
									<option value="パン" <c:if test="${copyCategory == 'パン'}">selected</c:if>>パン</option>
									<option value="惣菜" <c:if test="${copyCategory == '惣菜'}">selected</c:if>>惣菜</option>
									<option value="飲料" <c:if test="${copyCategory == '飲料'}">selected</c:if>>飲料</option>
									<option value="その他" <c:if test="${copyCategory == 'その他'}">selected</c:if>>その他</option>
								</select>
								<c:if test="${mode == 'RELIST'}">
									<input type="hidden" name="category" value="${copyCategory}">
								</c:if>
							</div>

							<div class="fl-row-2">
								<div class="fl-field">
									<label>通常価格（円）</label>
									<input type="number" name="price_normal" min="0" value="${copyPriceNormal}" placeholder="例：500"
										<c:if test="${mode == 'RELIST'}">readonly</c:if>>
								</div>
								<div class="fl-field">
									<label>特別価格（円）<span class="fl-req">*</span></label>
									<input type="number" name="price_offer" min="0" value="${copyPriceOffer}" required placeholder="例：300">
								</div>
							</div>

							<div class="fl-row-2">
								<div class="fl-field">
									<label>販売可能数<span class="fl-req">*</span></label>
									<input type="number" name="quantity" min="0" value="${copyQuantity}" required placeholder="例：10">
								</div>
								<div class="fl-field">
									<label>単位（例：個）<span class="fl-req">*</span></label>
									<input type="text" name="unit" value="${empty copyUnit ? '個' : copyUnit}" required placeholder="例：個">
								</div>
							</div>

							<div class="fl-field">
								<label>商品説明</label>
								<textarea name="description" rows="4" placeholder="例：冷めても美味しい唐揚げ弁当です。"
									<c:if test="${mode == 'RELIST'}">readonly</c:if>>${copyDescription}</textarea>
								<c:if test="${mode == 'RELIST'}">
									<input type="hidden" name="description" value="${copyDescription}">
								</c:if>
							</div>
						</div>

						<div class="fl-col">
							<%-- 受け取り場所は会社住所に統一。入力欄は廃止（今後必要なら表示のみを検討） --%>

							<c:if test="${not empty copyPickupStart}">
								<fmt:formatDate var="pickupStartStr" value="${copyPickupStart}" pattern="yyyy-MM-dd'T'HH:mm" />
							</c:if>
							<c:if test="${not empty copyPickupEnd}">
								<fmt:formatDate var="pickupEndStr" value="${copyPickupEnd}" pattern="yyyy-MM-dd'T'HH:mm" />
							</c:if>

							<div class="fl-row-2">
								<div class="fl-field">
									<label>受け取り開始</label>
									<input type="datetime-local" name="pickup_start" value="${pickupStartStr}">
								</div>
								<div class="fl-field">
									<label>受け取り終了</label>
									<input type="datetime-local" name="pickup_end" value="${pickupEndStr}">
								</div>
							</div>

							<div class="fl-field">
								<label>賞味期限</label>
								<input type="date" name="expiry_date">
							</div>

							<div class="fl-field">
								<label>画像アップロード</label>
								<c:if test="${not empty copyImagePath}">
									<div style="margin-top:6px;">
										<img src="${ctx}${copyImagePath}" alt="preview" style="width:86px; height:86px; object-fit:cover; border-radius:8px; border:1px solid #eee;">
										<input type="hidden" name="existingImagePath" value="${copyImagePath}">
									</div>
								</c:if>
								<input type="file" name="image" accept="image/*" <c:if test="${mode == 'RELIST'}">disabled</c:if>>
								<p style="font-size: 12px; color: #666;">jpg/png/webp 推奨</p>
							</div>

							<button type="submit" class="fl-submit">登録する</button>
						</div>
					</div>
				</form>
				</div>

			</div>
		</main>

	</div>

</body>
</html>

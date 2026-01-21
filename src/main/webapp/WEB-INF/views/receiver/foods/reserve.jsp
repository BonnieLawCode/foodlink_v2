<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8" />
<title>予約確認 | もったいナビ</title>

<!-- 共通CSS -->
<link rel="stylesheet" href="${ctx}/assets/css/receiver-layout.css?v=2" />
<!-- 预约页专用CSS -->
<link rel="stylesheet" href="${ctx}/assets/css/receiver-reserve.css?v=1" />
</head>


<body>
	<jsp:include page="/WEB-INF/views/receiver/_header.jsp" />
	<jsp:include page="/WEB-INF/views/receiver/_drawer.jsp" />

	<main class="page">
		<div class="panel">

			<!-- 返回 -->
			<a class="fl-back" href="${ctx}/receiver/foods/detail?id=${food.id}">←
				商品詳細に戻る</a>

			<h1 class="fl-page-title">予約内容の確認</h1>

			<div class="fl-detail2 fl-detail2--reserve">
				<!-- 左：图片 -->
				<div class="fl-detail2__left">
					<c:choose>
						<c:when test="${not empty food.imagePath}">
							<img class="fl-detail2__img" src="${ctx}${food.imagePath}"
								alt="${food.name}" />
						</c:when>
						<c:otherwise>
							<img class="fl-detail2__img" src="${ctx}/assets/img/noimage.jpg"
								alt="no image" />
						</c:otherwise>
					</c:choose>
					<div class="fl-note">
						<div class="fl-note__title">【注意事項】</div>
						<ul>
							<li>受取時間を過ぎるとキャンセル扱いとなる可能性があります。</li>
							<li>予約後のキャンセルは原則できません。</li>
							<li>受取時のお支払いとなります。必ず現金をご用意ください。</li>
						</ul>
					</div>
				</div>

				<!-- 右：信息 -->
				<div class="fl-detail2__right">
					<div class="fl-detail2__titleRow">
						<h2 class="fl-detail2__title">${food.name}</h2>
					</div>

					<div class="fl-detail2__priceRow">
						<span class="fl-price-normal"> <c:if
								test="${not empty food.priceNormal}">
                通常価格：¥<fmt:formatNumber value="${food.priceNormal}" />
							</c:if>
						</span> <span class="fl-price-offer"> 単価：¥<fmt:formatNumber
								value="${food.priceOffer}" />
						</span>
					</div>

					<!-- 表单：提交预约 -->
					<form class="fl-reserve-form" method="post"
						action="${ctx}/receiver/reserve">
						<input type="hidden" id="flTotalHidden" name="totalPrice"
							value="0">
						<!-- hidden identifiers for backend -->
						<input type="hidden" name="foodId" value="${food.id}" />
						<input type="hidden" name="id" value="${food.id}" />

						<div class="fl-infoBlock">
							<div class="fl-infoBlock__title">【商品情報】</div>

							<div class="fl-kv">
								<div class="fl-kv__k">数量</div>
								<div class="fl-kv__v">
									<div class="fl-qty-wrapper">
										<input type="number" id="flQty" name="qty" class="fl-qty-input" 
											min="1" max="10" value="1" />
										<span id="flQtyMsg" class="fl-qty-msg"></span>
									</div>
									<span class="fl-stock">在庫：${food.quantity}${food.unit}</span>
								</div>
							</div>

							<div class="fl-kv">
								<div class="fl-kv__k">受取場所</div>
								<div class="fl-kv__v">
									<c:choose>
										<c:when test="${not empty food.companyAddress}">${food.companyAddress}</c:when>
										<c:otherwise><span class="muted">-</span></c:otherwise>
									</c:choose>
									<span class="fl-map-link">→ Google Maps（準備中）</span>
								</div>
							</div>

							<div class="fl-kv">
								<div class="fl-kv__k">受取可能時間</div>
								<div class="fl-kv__v">${pickupLabel}</div>
							</div>

							<div class="fl-kv">
								<div class="fl-kv__k">受取時間</div>
								<div class="fl-kv__v">
									<input type="datetime-local" id="flPickupTime" name="pickupTime" 
										class="fl-pickup-time" required />
									<span id="flTimeMsg" class="fl-time-msg"></span>
								</div>
							</div>

							<div class="fl-kv">
								<div class="fl-kv__k">賞味期限</div>
								<div class="fl-kv__v">
									<c:choose>
										<c:when test="${not empty food.expiryDate}">
											<fmt:formatDate value="${food.expiryDate}"
												pattern="yyyy-MM-dd" />
										</c:when>
										<c:otherwise>（未設定）</c:otherwise>
									</c:choose>
								</div>
							</div>
						</div>
						<div class="fl-reserve-bottom">
							<!-- 合計（自動計算） -->
							<div class="fl-total">
								<div class="fl-total__label">合計金額</div>
								<div class="fl-total__value">
									<span id="flTotalYen">¥0</span> <span class="fl-total__tax">（税込）</span>
								</div>
							</div>

							<!-- 主按钮：居中 -->
							<div class="fl-detail2__actions fl-center">
								<button class="fl-btn fl-btn-reserve fl-btn-wide" type="submit">予約を確定する</button>
							</div>
						</div>

					</form>

				</div>
			</div>

		</div>
	</main>
	<script>
		// JSP -> JS：单价（这里用特价/单价）
		window.FL_UNIT_PRICE = ${food.priceOffer};
		window.FL_MAX_QTY = ${food.quantity};
		window.FL_PICKUP_LABEL = '${pickupLabel}';
		window.FL_FOOD_ID = ${food.id};
		
		// 解析受取可能時間范围（格式: "2025-12-17 15:20～23:18" 或 "YYYY-MM-DD HH:MM～HH:MM"）
		window.FL_PICKUP_RANGE = '${pickupLabel}';
	</script>
	<script src="${ctx}/assets/js/receiver-reserve.js?v=3"></script>

</body>
</html>

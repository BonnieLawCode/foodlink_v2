<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>商品詳細 | もったいナビ</title>

<!-- 你项目正在用的布局CSS -->
<link rel="stylesheet" href="${ctx}/assets/css/receiver-layout.css?v=5">
</head>

<body>
	<jsp:include page="/WEB-INF/views/receiver/_header.jsp" />
	<jsp:include page="/WEB-INF/views/receiver/_drawer.jsp" />

	<main class="page">
		<div class="panel">

			<c:set var="f" value="${food}" />

			<!-- ✅ 顶部：返回主页按钮（主内容最上边） -->
			<div class="fl-detail-top">
				<a class="fl-back" href="${ctx}/receiver/home"> <span
					class="fl-back__icon">←</span> ホームに戻る
				</a>
			</div>

			<!-- ✅ 主体：左图右信息，宽度大体一致 -->
			<section class="fl-detail2">

				<!-- 左：图片 -->
				<div class="fl-detail2__left">
					<c:choose>
						<c:when test="${not empty f.imagePath}">
							<c:set var="img" value="${f.imagePath}" />
							<c:if test="${!img.startsWith('/')}">
								<c:set var="img" value="/${img}" />
							</c:if>
							<img class="fl-detail2__img" src="${ctx}${img}" alt="${f.name}">
						</c:when>
						<c:otherwise>
							<img class="fl-detail2__img"
								src="${ctx}/uploads/products/noimage.jpg" alt="no image">
						</c:otherwise>
					</c:choose>
				</div>

				<!-- 右：商品信息 -->
				<div class="fl-detail2__right">

					<!-- ① 第一行：商品名 + 右侧小字剩余数量 -->
					<div class="fl-detail2__row1">
						<h1 class="fl-detail2__title">
							<c:out value="${f.name}" />
						</h1>
						<div class="fl-detail2__stock">
							残り
							<c:out value="${f.quantity}" />
							<c:out value="${f.unit}" />
						</div>
					</div>

					<!-- ② 第二行：价格，突出显示特别价格 -->
					<div class="fl-detail2__price">
						<span class="fl-price-normal"> 通常価格： <c:choose>
								<c:when test="${f.priceNormal != null}">
                  ¥<c:out value="${f.priceNormal}" />
								</c:when>
								<c:otherwise>—</c:otherwise>
							</c:choose>
						</span> <span class="fl-price-arrow">→</span> <span
							class="fl-price-offer"> 特別価格：¥<c:out
								value="${f.priceOffer}" />
						</span>
					</div>

					<!-- ③ 第三部分：商品说明（保留换行显示） -->
					<div class="fl-detail2__block">
						<h2 class="fl-detail2__h">【商品説明】</h2>
						<!-- white-space: pre-line 用 CSS 来保留换行 -->
						<p class="fl-detail2__text">
							<c:out value="${f.description}" />
						</p>
					</div>

					<!-- ④ 第四部分：受取情报 -->
					<div class="fl-detail2__block">
						<h2 class="fl-detail2__h">【受取情報】</h2>

						<div class="fl-info-list">

							<div class="fl-info-row">
								<div class="fl-info-k">受取場所</div>
								<div class="fl-info-v">
									<c:out value="${f.pickupLocation}" />
									<a class="fl-map-link" href="#" aria-disabled="true"
										onclick="return false;"> → Google Maps（準備中） </a>
								</div>
							</div>

							<div class="fl-info-row">
								<div class="fl-info-k">受取可能時間</div>
								<div class="fl-info-v">
									<c:set var="startDate"
										value="${fn:substring(f.pickupStart, 0, 10)}" />
									<c:set var="endDate"
										value="${fn:substring(f.pickupEnd,   0, 10)}" />
									<c:set var="startHM"
										value="${fn:substring(f.pickupStart, 11, 16)}" />
									<c:set var="endHM"
										value="${fn:substring(f.pickupEnd,   11, 16)}" />

									<c:choose>
										<%-- 同一天：显示「本日 14:15～22:20」 --%>
										<c:when test="${startDate == endDate}">
											<span class="fl-time">本日 ${startHM}～${endHM}</span>
										</c:when>

										<%-- 跨天：显示完整日期时间（备用） --%>
										<c:otherwise>
											<span class="fl-time">${startDate}
												${startHM}～${endDate} ${endHM}</span>
										</c:otherwise>
									</c:choose>
								</div>
							</div>


							<div class="fl-info-row">
								<div class="fl-info-k">賞味期限</div>
								<div class="fl-info-v">
									<c:out value="${f.expiryDate}" />
								</div>
							</div>

						</div>
					</div>


					<!-- ⑤ 最后：进入预约确定界面按钮 -->
					<div class="fl-detail2__actions">
						<a class="fl-btn fl-btn-reserve"
							href="${ctx}/receiver/reserve?id=${f.id}"> 予約へ進む </a>
					</div>

				</div>
			</section>

		</div>

	</main>
	<script src="${ctx}/assets/js/receiver-drawer.js?v=1"></script>
</body>
</html>

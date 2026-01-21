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
			<section class="fl-detail2 fl-detail2--detail">

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
						<div class="fl-detail2__headline">
							<h1 class="fl-detail2__title">
							<c:out value="${f.name}" />
							</h1>
							<a class="fl-shop-link" href="#" onclick="return false;">
								<span class="fl-shop-logo" aria-hidden="true">
									<svg xmlns="http://www.w3.org/2000/svg" fill="none"
										viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"
										width="18" height="18">
										<path stroke-linecap="round" stroke-linejoin="round"
											d="M13.5 21v-7.5a.75.75 0 0 1 .75-.75h3a.75.75 0 0 1 .75.75V21m-4.5 0H2.36m11.14 0H18m0 0h3.64m-1.39 0V9.349M3.75 21V9.349m0 0a3.001 3.001 0 0 0 3.75-.615A2.993 2.993 0 0 0 9.75 9.75c.896 0 1.7-.393 2.25-1.016a2.993 2.993 0 0 0 2.25 1.016c.896 0 1.7-.393 2.25-1.015a3.001 3.001 0 0 0 3.75.614m-16.5 0a3.004 3.004 0 0 1-.621-4.72l1.189-1.19A1.5 1.5 0 0 1 5.378 3h13.243a1.5 1.5 0 0 1 1.06.44l1.19 1.189a3 3 0 0 1-.621 4.72M6.75 18h3.75a.75.75 0 0 0 .75-.75V13.5a.75.75 0 0 0-.75-.75H6.75a.75.75 0 0 0-.75.75v3.75c0 .414.336.75.75.75Z" />
									</svg>
								</span>
								<span class="fl-shop-name">
									<c:choose>
										<c:when test="${not empty f.companyName}">
											<c:out value="${f.companyName}" />
										</c:when>
										<c:otherwise>-</c:otherwise>
									</c:choose>
								</span>
							</a>
						</div>
						<div class="fl-detail2__stock">
							残り
							<c:out value="${f.quantity}" />
							<c:out value="${f.unit}" />
						</div>
					</div>

					<!-- ② 第二行：价格，突出显示特别价格 -->
					<div class="fl-detail2__price">
						<div class="fl-price-current">
							<span class="fl-price-label">特別価格</span>
							<span class="fl-price-offer">¥<c:out value="${f.priceOffer}" /></span>
						</div>
						<div class="fl-price-was">
							<span class="fl-price-label">通常価格</span>
							<c:choose>
								<c:when test="${f.priceNormal != null}">
									<span class="fl-price-normal">¥<c:out value="${f.priceNormal}" /></span>
								</c:when>
								<c:otherwise>
									<span class="fl-price-normal is-empty">—</span>
								</c:otherwise>
							</c:choose>
						</div>
					</div>

					<!-- ③ 第三部分：商品说明（保留换行显示） -->
					<div class="fl-detail2__block">
						<p class="fl-detail2__section-title">商品説明</p>
						<!-- white-space: pre-line 用 CSS 来保留换行 -->
						<p class="fl-detail2__text">
							<c:out value="${f.description}" />
						</p>
					</div>

					<!-- ④ 第四部分：受取情报 -->
					<div class="fl-detail2__block">
						<p class="fl-detail2__section-title">受取情報</p>

						<div class="fl-info-list">

							<div class="fl-info-row">
								<div class="fl-info-k">受取場所</div>
								<div class="fl-info-v">
									<c:choose>
										<c:when test="${not empty f.companyAddress}">
											<c:out value="${f.companyAddress}" />
										</c:when>
										<c:otherwise>
											<span class="muted">-</span>
										</c:otherwise>
									</c:choose>
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

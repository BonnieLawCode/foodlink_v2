<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>もったいナビ | 食品ロスを減らす受け取りサービス</title>

<%-- JP：受取者側CSS / CN：受取者端CSS --%>
<link rel="stylesheet" href="${ctx}/assets/css/receiver-layout.css?v=1">
</head>

<body>
	<%-- JP：共通ヘッダー / CN：共通 header（顶部固定） --%>
	<jsp:include page="/WEB-INF/views/receiver/_header.jsp" />

	<%-- JP：共通ドロワー / CN：共通抽屉菜单 --%>
	<jsp:include page="/WEB-INF/views/receiver/_drawer.jsp" />

	<%-- JP：本文 / CN：主体 --%>
	<main class="page">
		<div class="panel">
			<!-- ===== Banner Slider (JP: バナー / CN: 轮播) ===== -->
			<section class="fl-slider" aria-label="もったいナビのお知らせ">
				<div class="fl-slider__viewport" id="flSlider">

					<!-- Slide 1 -->
					<a class="fl-slide is-active" href="${ctx}/receiver/home"> <img
						src="${ctx}/assets/img/banner01.jpg" alt="もったいナビ">
						<div class="fl-slide__content">
							<p class="fl-slide__eyebrow">もったいナビ</p>
							<h2 class="fl-slide__title">
								食品ロスを減らす<br>やさしい受け取り
							</h2>
							<p class="fl-slide__desc">近くのお店で、今日中に受け取れる食品を見つけよう</p>
						</div>
					</a>

					<!-- Slide 2 -->
					<a class="fl-slide" href="${ctx}/receiver/home"> <img
						src="${ctx}/assets/img/banner02.jpg" alt="今週の特集">
						<div class="fl-slide__content">
							<p class="fl-slide__eyebrow">今週の特集</p>
							<h2 class="fl-slide__title">
								夕方受け取り<br>おすすめ特集
							</h2>
							<p class="fl-slide__desc">パン・お弁当・惣菜が、夕方からお得に</p>
						</div>
					</a>

					<!-- Slide 3 -->
					<a class="fl-slide" href="${ctx}/receiver/home"> <img
						src="${ctx}/assets/img/banner03.jpg" alt="使い方">
						<div class="fl-slide__content">
							<p class="fl-slide__eyebrow">かんたん3ステップ</p>
							<h2 class="fl-slide__title">
								探す・予約・<br>受け取るだけ
							</h2>
							<p class="fl-slide__desc">面倒な手続きはありません</p>
						</div>
					</a>

					<!-- dots -->
					<div class="fl-dots" id="flDots">
						<button type="button" class="fl-dot is-active"></button>
						<button type="button" class="fl-dot"></button>
						<button type="button" class="fl-dot"></button>
					</div>

				</div>
			</section>



			<%-- 検索ボックス / 搜索框 --%>
			<form class="search-box" method="get" action="${ctx}/receiver/home">
				<div class="search-bar">
					<div class="search-seg is-flex">
						<svg class="seg-icon" viewBox="0 0 24 24" fill="none"
							stroke="currentColor" stroke-width="2" stroke-linecap="round"
							stroke-linejoin="round" aria-hidden="true">
							<circle cx="11" cy="11" r="7"></circle>
							<line x1="16.65" y1="16.65" x2="21" y2="21"></line>
						</svg>
						<input type="text" name="keyword" placeholder="パン・弁当・飲料"
							value="${fn:escapeXml(keyword)}">
					</div>

					<span class="seg-divider"></span>

					<div class="search-seg">
						<div class="seg-select">
							<select name="category">
								<option value="" <c:if test="${empty category}">selected</c:if>>すべて</option>
								<option value="弁当" <c:if test="${category=='弁当'}">selected</c:if>>弁当</option>
								<option value="パン" <c:if test="${category=='パン'}">selected</c:if>>パン</option>
								<option value="惣菜" <c:if test="${category=='惣菜'}">selected</c:if>>惣菜</option>
								<option value="飲料" <c:if test="${category=='飲料'}">selected</c:if>>飲料</option>
								<option value="その他"
									<c:if test="${category=='その他'}">selected</c:if>>その他</option>
							</select>
						</div>
					</div>

					<span class="seg-divider"></span>

					<div class="search-seg is-flex">
						<svg class="seg-icon" viewBox="0 0 24 24" fill="none"
							stroke="currentColor" stroke-width="2" stroke-linecap="round"
							stroke-linejoin="round" aria-hidden="true">
							<path d="M12 21s7-7.4 7-12a7 7 0 1 0-14 0c0 4.6 7 12 7 12z"></path>
							<circle cx="12" cy="9" r="2.5"></circle>
						</svg>
						<input type="text" name="area" placeholder="大阪駅 / 梅田"
							value="${fn:escapeXml(area)}">
					</div>

					<span class="seg-divider"></span>

					<div class="search-seg">
						<div class="seg-select">
							<select name="sort">
								<option value="new"
									<c:if test="${empty sort || sort=='new'}">selected</c:if>>新着順</option>
								<option value="price"
									<c:if test="${sort=='price'}">selected</c:if>>価格順</option>
							</select>
						</div>
					</div>

					<div class="search-actions">
						<a class="btn-reset" href="${ctx}/receiver/home" aria-label="リセット">
							<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"
								stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
								aria-hidden="true">
								<line x1="6" y1="6" x2="18" y2="18"></line>
								<line x1="18" y1="6" x2="6" y2="18"></line>
							</svg>
						</a>
						<button type="submit" class="btn-primary">検索する</button>
					</div>
				</div>
			</form>

			<%-- 商品カード（3列）/ 三列卡片 --%>
			<section class="grid3">

				<c:if test="${empty requestScope.foodList}">
					<div class="empty">
						JP：該当する商品がありません。<br> CN：没有符合条件的商品。
					</div>
				</c:if>

				<c:forEach var="f" items="${requestScope.foodList}">


					<article class="card">
						<a class="card-link-overlay"
							href="${ctx}/receiver/foods/detail?id=${f.id}"
							aria-label="${f.name}"></a>

						<div class="thumb">
							<c:choose>
								<c:when test="${not empty f.imagePath}">
									<img src="${ctx}${f.imagePath}" alt="food"
										onerror="this.onerror=null;this.src='${ctx}/assets/img/noimage.jpg';">
								</c:when>
								<c:otherwise>
									<img src="${ctx}/assets/img/noimage.jpg" alt="noimage">
								</c:otherwise>
							</c:choose>
						</div>

						<div class="meta">
							<span class="badge">${f.category}</span> <span class="remain">残り
								${f.quantity} ${f.unit}</span>
						</div>

						<h3 class="title">${f.name}</h3>
						<div class="card-shop">
							<c:choose>
								<c:when test="${not empty f.companyName}">
									<c:out value="${f.companyName}" />
								</c:when>
								<c:otherwise>-</c:otherwise>
							</c:choose>
						</div>

						<p class="desc">
							<c:out value="${f.description}" />
						</p>

						<div class="price">
							<c:if test="${f.priceNormal != null}">
								<span class="normal">¥<fmt:formatNumber
										value="${f.priceNormal}" /></span>
							</c:if>
							<span class="offer">¥<fmt:formatNumber
									value="${f.priceOffer}" /></span>
						</div>

						<div class="info">
							<div>
								受取場所：
								<c:choose>
									<c:when test="${not empty f.companyAddress}">
										<c:out value="${f.companyAddress}" />
									</c:when>
									<c:otherwise>-</c:otherwise>
								</c:choose>
							</div>
							<div>
								受取可能：
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

					</article>

				</c:forEach>
			</section>

		</div>
		<div style="text-align: center; margin: 24px 0;">
			<a href="${ctx}/receiver/foods" style="font-weight: 800;">もっと見る</a>
		</div>


	</main>
	<%-- JP：ドロワーJS / CN：抽屉JS --%>
	<script src="${ctx}/assets/js/receiver-drawer.js?v=1"></script>
	<script src="${ctx}/assets/js/receiver-slider.js?v=1" defer></script>
	


</body>
</html>

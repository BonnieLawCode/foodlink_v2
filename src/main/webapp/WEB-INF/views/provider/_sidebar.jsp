<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%-- 
  JP：左側サイドバー（全provider画面共通）
  CN：左侧侧边栏（所有企业页面共用）
--%>

<aside class="sidebar">
	<div class="brand">
		<div class="brand-logo">もったいナビ</div>
	</div>

	<nav class="menu">
		<c:set var="active" value="${requestScope.activeMenu}" />

		<%-- ホーム / 首页 --%>
		<a class="menu-item<c:if test='${active eq "home"}'> active</c:if>"
			href="${pageContext.request.contextPath}/home"> ホーム </a>

		<%-- 商品管理 --%>
		<a class="menu-item<c:if test='${active eq "products"}'> active</c:if>"
			href="${pageContext.request.contextPath}/provider/products"> 商品管理 </a>

		<%-- 新規商品登録 / 添加商品 --%>

		<a
			class="menu-item<c:if test='${active eq "food_new"}'> active</c:if>"
			href="${pageContext.request.contextPath}/provider/foods/new">
			新規商品登録 </a>


		<%-- 在庫管理 / 库存管理 --%>


		<a
			class="menu-item<%-- 先写基础class --%>
   <c:if test="${active eq 'food_list'}"> active</c:if>"
			href="${pageContext.request.contextPath}/provider/foods"> 在庫管理 </a>


		<%-- 下面先占位（功能以后做） --%>
		
		<a class="menu-item <c:if test='${active eq "reservations"}'> active</c:if>"
        href="${pageContext.request.contextPath}/provider/reservations">予約一覧
        </a>
		<a class="menu-item disabled" href="javascript:void(0)">レポート</a>
		<a class="menu-item disabled" href="javascript:void(0)">アカウント設定</a>

	</nav>

	<div class="contact-card">
		<div class="contact-title">contact us</div>

		<div class="contact-row">
			<div class="avatar" aria-hidden="true">
				<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="size-6" width="24" height="24">
					<path stroke-linecap="round" stroke-linejoin="round" d="M13.5 21v-7.5a.75.75 0 0 1 .75-.75h3a.75.75 0 0 1 .75.75V21m-4.5 0H2.36m11.14 0H18m0 0h3.64m-1.39 0V9.349M3.75 21V9.349m0 0a3.001 3.001 0 0 0 3.75-.615A2.993 2.993 0 0 0 9.75 9.75c.896 0 1.7-.393 2.25-1.016a2.993 2.993 0 0 0 2.25 1.016c.896 0 1.7-.393 2.25-1.015a3.001 3.001 0 0 0 3.75.614m-16.5 0a3.004 3.004 0 0 1-.621-4.72l1.189-1.19A1.5 1.5 0 0 1 5.378 3h13.243a1.5 1.5 0 0 1 1.06.44l1.19 1.189a3 3 0 0 1-.621 4.72M6.75 18h3.75a.75.75 0 0 0 .75-.75V13.5a.75.75 0 0 0-.75-.75H6.75a.75.75 0 0 0-.75.75v3.75c0 .414.336.75.75.75Z" />
				</svg>
			</div>

			<div class="company-name">
				<c:choose>
					<c:when test="${not empty sessionScope.providerName}">
						<%-- 
            JP：セッションから会社名（店名）を表示
            CN：显示 session 中的公司名/店名
          --%>
          ${sessionScope.providerName}
        </c:when>
					<c:otherwise>
						<%-- 
            JP：未設定の場合の表示（仮）
            CN：如果 session 没有值，显示占位
          --%>
          ○○会社
        </c:otherwise>
				</c:choose>
			</div>

			<%-- 
      JP：ログアウト（クリックできるリンク）
      CN：登出按钮（可点击链接）
    --%>
			<a class="logout-btn"
				href="${pageContext.request.contextPath}/logout" title="ログアウト"
				aria-label="ログアウト"> <!--
    JP：ログアウト用アイコン（SVG）
    CN：登出图标（SVG），清晰稳定
  --> <svg class="logout-icon" width="20" height="20"
					viewBox="0 0 24 24" aria-hidden="true">
    <!-- door -->
    <path d="M10 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h5" fill="none"
						stroke="currentColor" stroke-width="2" stroke-linecap="round" />
    <!-- arrow -->
    <path d="M17 16l4-4-4-4" fill="none" stroke="currentColor"
						stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
    <path d="M21 12H10" fill="none" stroke="currentColor"
						stroke-width="2" stroke-linecap="round" />
  </svg>
			</a>

		</div>
	</div>

</aside>

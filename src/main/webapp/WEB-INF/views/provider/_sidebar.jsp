<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%-- 
  JP：左側サイドバー（全provider画面共通）
  CN：左侧侧边栏（所有企业页面共用）
--%>

<aside class="sidebar">
	<div class="brand">
		<div class="brand-logo">FoodLink</div>
	</div>

	<nav class="menu">
		<c:set var="active" value="${requestScope.activeMenu}" />

		<%-- ホーム / 首页 --%>
		<a class="menu-item<c:if test='${active eq "home"}'> active</c:if>"
			href="${pageContext.request.contextPath}/home"> ホーム </a>


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
			<div class="avatar"></div>

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

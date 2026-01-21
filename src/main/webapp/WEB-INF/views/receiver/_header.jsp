<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<header class="topbar">
  <div class="topbar-left">
    <%-- JP：ハンバーガー / CN：汉堡菜单按钮 --%>
    <button type="button" class="icon-btn" id="menuBtn" aria-label="menu">☰</button>
    <a class="brand" href="${ctx}/home">もったいナビ</a>
  </div>

  <div class="topbar-right">
    <%-- JP：未ログイン / CN：未登录 --%>
    <c:if test="${empty sessionScope.userName}">
      <a class="toplink" href="${ctx}/login">ログイン</a>
      <a class="toplink" href="${ctx}/register">新規登録</a>
    </c:if>

    <%-- JP：ログイン済 / CN：已登录 --%>
    <c:if test="${not empty sessionScope.userName}">
      <span class="welcome">ようこそ、${sessionScope.userName}さん</span>
      <a class="toplink" href="${ctx}/logout" aria-label="ログアウト">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="size-6" width="18" height="18" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0 0 13.5 3h-6a2.25 2.25 0 0 0-2.25 2.25v13.5A2.25 2.25 0 0 0 7.5 21h6a2.25 2.25 0 0 0 2.25-2.25V15m3 0 3-3m0 0-3-3m3 3H9" />
        </svg>
      </a>
      <a class="toplink" href="${ctx}/receiver/history">受取履歴</a>
      
    </c:if>
  </div>
</header>

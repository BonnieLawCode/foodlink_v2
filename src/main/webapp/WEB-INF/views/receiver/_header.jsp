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
      <a class="toplink" href="${ctx}/logout">⎋</a>
      <a class="toplink" href="${ctx}/receiver/history">受取履歴</a>
      
    </c:if>
  </div>
</header>

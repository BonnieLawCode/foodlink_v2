<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="overlay" id="overlay"></div>

<aside class="drawer" id="drawer">
  <div class="drawer-head">
    <a class="drawer-brand" href="${ctx}/home">もったいナビ</a>
  </div>

  <nav class="drawer-menu">
    <a class="drawer-item" href="${ctx}/home">ホーム</a>
    <a class="drawer-item" href="${ctx}/flow">利用フロー</a>
    <a class="drawer-item" href="${ctx}/provider/login">企業の方はこちら</a>
    <a class="drawer-item" href="${ctx}/admin/login">管理者向け</a>

    <%-- JP：ログイン済なら追加 / CN：已登录才显示 --%>
    <c:if test="${not empty sessionScope.userName}">
      <a class="drawer-item" href="${ctx}/receiver/history">受取履歴</a>
      <a class="drawer-item" href="${ctx}/logout">ログアウト</a>
    </c:if>
  </nav>
</aside>

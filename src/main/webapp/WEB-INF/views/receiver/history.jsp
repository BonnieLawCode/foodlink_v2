<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>受取履歴 | FoodLink</title>
<link rel="stylesheet" href="${ctx}/assets/css/receiver-layout.css?v=1">
</head>

<body>
  <jsp:include page="/WEB-INF/views/receiver/_header.jsp" />
  <jsp:include page="/WEB-INF/views/receiver/_drawer.jsp" />

  <div class="page">
    <div class="panel">
      <div style="font-weight:900; font-size:20px;">受取履歴</div>
      <div style="color:#666; margin-top:8px;">（ここは後でDBと連携します）</div>
    </div>
  </div>

  <script src="${ctx}/assets/js/receiver-drawer.js?v=1"></script>
</body>
</html>

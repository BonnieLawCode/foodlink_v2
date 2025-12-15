<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>FoodLink - Login</title>
</head>
<body>
  <h2>ログイン</h2>

  <c:if test="${not empty error}">
    <p style="color:red">${error}</p>
  </c:if>

  <form method="post" action="${ctx}/login">
    <div>
      <label>メール：</label>
      <input type="email" name="email" required>
    </div>
    <div>
      <label>パスワード：</label>
      <input type="password" name="password" required>
    </div>
    <button type="submit">ログイン</button>
  </form>
</body>
</html>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>FoodLink - Login</title>
  <style>
    body {
      margin: 0;
      font-family: system-ui, -apple-system, "Segoe UI", "Noto Sans JP", sans-serif;
      background: #f6f6f6;
      color: #111;
    }

    .login-page {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 24px;
    }

    .login-card {
      width: min(420px, 92vw);
      background: #fff;
      border: 1px solid #e5e5e5;
      border-radius: 14px;
      box-shadow: 0 8px 18px rgba(0, 0, 0, 0.08);
      padding: 28px 26px 24px;
    }

    .login-title {
      margin: 0 0 18px;
      font-size: 20px;
      font-weight: 800;
    }

    .login-field {
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-bottom: 16px;
    }

    .login-label {
      font-weight: 700;
      font-size: 13px;
      color: #333;
    }

    .login-input {
      height: 40px;
      padding: 0 12px;
      border: 1px solid #d9d9d9;
      border-radius: 10px;
      font-size: 14px;
      background: #fff;
    }

    .login-input:focus {
      outline: none;
      border-color: #b8b8b8;
      box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.06);
    }

    .login-error {
      margin: 0 0 12px;
      color: #c62828;
      font-weight: 700;
      font-size: 13px;
    }

    .login-btn {
      width: 100%;
      height: 44px;
      border: none;
      border-radius: 10px;
      background: #2c2c2c;
      color: #fff;
      font-size: 14px;
      font-weight: 800;
      cursor: pointer;
    }

    .login-btn:hover {
      background: #1f1f1f;
    }
  </style>
</head>
<body>
  <div class="login-page">
    <div class="login-card">
      <h2 class="login-title">ログイン</h2>

      <c:if test="${not empty error}">
        <p class="login-error">${error}</p>
      </c:if>

      <form method="post" action="${ctx}/login" autocomplete="off">
        <div class="login-field">
          <label class="login-label">メールアドレス</label>
          <input class="login-input" type="email" name="email" required placeholder="example@email.com">
        </div>
        <div class="login-field">
          <label class="login-label">パスワード</label>
          <input class="login-input" type="password" name="password" required placeholder="••••••••" autocomplete="new-password">
        </div>
        <button class="login-btn" type="submit">ログイン</button>
      </form>
    </div>
  </div>
</body>
</html>

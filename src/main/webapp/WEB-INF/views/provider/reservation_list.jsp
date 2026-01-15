<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%
  // JP：header表示用タイトル
  // CN：header 显示用标题
  request.setAttribute("pageTitle", "予約一覧");
%>

<%
  // JP：サイドバーで「予約一覧」を選択状態にする（※値は_sidebar.jsp側に合わせて変更）
  // CN：让侧边栏“予約一覧”处于高亮选中状态（※值要和 _sidebar.jsp 的判断一致）
  request.setAttribute("activeMenu", "reservations");
%>

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>予約一覧 | もったいナビ</title>

<%-- 共通レイアウトCSS / 通用布局CSS --%>
<link rel="stylesheet" href="${ctx}/assets/css/provider-layout.css?v=2">

<%-- ページ専用CSS（先空でもOK）/ 页面专用CSS（可先不写） --%>
<link rel="stylesheet" href="${ctx}/css/provider-reservation-list.css">
</head>

<body>
  <div class="app">

    <%-- 左サイドバー / 左侧栏 --%>
    <jsp:include page="/WEB-INF/views/provider/_sidebar.jsp" />

    <%-- 右コンテンツ / 右侧内容 --%>
    <main class="content">
      <div class="content-inner">

        <%-- 上部ヘッダー / 顶部header（共通） --%>
        <jsp:include page="/WEB-INF/views/provider/_header.jsp" />

        <%-- =========================
             JP：この下がページ本文（今は空の壳）
             CN：下面是页面主体（现在先留空壳）
             ========================= --%>

        <%-- メッセージ表示 / 消息显示 --%>
        <c:set var="msg" value="${param.msg}" />
        <c:set var="error" value="${param.error}" />
        <c:if test="${not empty msg or not empty error}">
          <div style="margin:12px 0; padding:10px 12px; border:1px solid #ddd; background:#fafafa; color:#333;">
            <c:choose>
              <c:when test="${msg == 'picked_up_success'}">受取済として更新しました。</c:when>
              <c:when test="${error == 'not_authorized'}">権限がありません。</c:when>
              <c:when test="${error == 'not_found'}">対象の予約が見つかりません。</c:when>
              <c:when test="${error == 'status_changed'}">予約の状態が変更されました。ページを更新してください。</c:when>
              <c:otherwise>エラーが発生しました。時間を置いてもう一度お試しください。</c:otherwise>
            </c:choose>
          </div>
        </c:if>

        <%-- 簡易フィルタ / 简易过滤 --%>
        <form method="get" action="${ctx}/provider/reservations" style="margin:12px 0;">
          <label>
            状態：
            <select name="status">
              <option value="ALL" <c:if test="${status == 'ALL'}">selected</c:if>>全て</option>
              <option value="RESERVED" <c:if test="${status == 'RESERVED'}">selected</c:if>>受取待ち</option>
              <option value="PICKED_UP" <c:if test="${status == 'PICKED_UP'}">selected</c:if>>受取済</option>
              <option value="CANCELLED" <c:if test="${status == 'CANCELLED'}">selected</c:if>>キャンセル</option>
              <option value="NO_SHOW" <c:if test="${status == 'NO_SHOW'}">selected</c:if>>未受取（期限切れ）</option>
            </select>
          </label>
          <label style="margin-left:12px;">
            受取予定日：
            <input type="date" name="pickupDate" value="${pickupDate}" />
          </label>
          <button type="submit" class="btn">検索</button>
        </form>

        <table class="fl-table" width="100%" border="0" cellspacing="0" cellpadding="8">
          <thead>
            <tr>
              <th>予約番号</th>
              <th>予約日時</th>
              <th>商品名</th>
              <th>数量</th>
              <th>受取予定日時</th>
              <th>予約者</th>
              <th>状態</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach items="${reservations}" var="r">
              <tr>
                <td>${r.code}</td>
                <td><fmt:formatDate value="${r.reserveAt}" pattern="yyyy-MM-dd HH:mm" /></td>
                <td>${r.foodName}</td>
                <td>${r.quantity}</td>
                <td>
                  <c:choose>
                    <c:when test="${not empty r.pickupTime}">
                      <fmt:formatDate value="${r.pickupTime}" pattern="yyyy-MM-dd HH:mm" />
                    </c:when>
                    <c:otherwise>—</c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${not empty r.receiverName}">${r.receiverName}</c:when>
                    <c:otherwise>${r.receiverId}</c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${r.status == 'RESERVED'}">受取待ち</c:when>
                    <c:when test="${r.status == 'PICKED_UP'}">受取済</c:when>
                    <c:when test="${r.status == 'CANCELLED'}">キャンセル</c:when>
                    <c:when test="${r.status == 'NO_SHOW'}">未受取（期限切れ）</c:when>
                    <c:otherwise>—</c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <a href="#" class="btn" title="準備中">詳細</a>
                  <c:if test="${r.status == 'RESERVED'}">
                    <form method="post" action="${ctx}/provider/reservations/pickedUp" style="display:inline;" onsubmit="return confirm('受取を確認しますか？');">
                      <input type="hidden" name="reservationId" value="${r.id}" />
                      <button type="submit" class="btn">受取を確認</button>
                    </form>
                  </c:if>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty reservations}">
              <tr>
                <td colspan="8" style="text-align:center; color:#666; padding:24px;">予約はありません。</td>
              </tr>
            </c:if>
          </tbody>
        </table>

      </div>
    </main>

  </div>
</body>
</html>

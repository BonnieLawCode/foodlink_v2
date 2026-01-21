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
<link rel="stylesheet" href="${ctx}/assets/css/provider-layout.css?v=3">

<%-- ページ専用CSS（先空でもOK）/ 页面专用CSS（可先不写） --%>
<link rel="stylesheet" href="${ctx}/css/provider-reservation-list.css">
<style>
table.fl-table { border-collapse: collapse; width: 100%; background: #fff; }
th, td { border: 1px solid #e5e5e5; padding: 10px 12px; vertical-align: top; }
th { background: #fafafa; font-weight: 700; position: sticky; top: 0; z-index: 1; }
tbody tr:hover { background: #f7f7f7; }
.filter-bar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; margin: 8px 0 14px; }
.filter-bar label { font-size: 13px; color: #333; }
.filter-bar select, .filter-bar input[type="date"] { padding: 6px 8px; border: 1px solid #ddd; border-radius: 6px; }
.btn { display: inline-flex; align-items: center; justify-content: center; gap: 6px; padding: 6px 12px; border-radius: 8px; font-size: 13px; text-decoration: none; cursor: pointer; border: 1px solid transparent; }
.btn-primary { background: #2563eb; color: #fff; border-color: #2563eb; }
.btn-primary:hover { opacity: 0.92; }
.btn-secondary { background: #fff; color: #666; border-color: #d0d0d0; }
.btn-secondary:hover { border-color: #b5b5b5; color: #444; }
.status-badge { display: inline-block; padding: 4px 10px; border-radius: 999px; font-size: 12px; }
.status-cancelled { background: #fef2f2; color: #b91c1c; }
.status-expired { background: #fff7ed; color: #c2410c; }
.status-done { background: #ecfdf3; color: #15803d; }
.status-reserved { background: #e0f2fe; color: #1d4ed8; }
.pagination { display: flex; gap: 8px; align-items: center; margin-top: 12px; justify-content: center; }
.page-link { padding: 6px 10px; border: 1px solid #ddd; border-radius: 6px; text-decoration: none; color: #333; }
.page-link.active { background: #2563eb; color: #fff; border-color: #2563eb; }
.page-link.disabled { color: #aaa; cursor: not-allowed; background: #f5f5f5; }
.content-card { background: #fff; border-radius: 14px; padding: 14px; }
</style>
</head>

<body>
  <jsp:useBean id="now" class="java.util.Date" scope="page" />
  <div class="app">

    <%-- 左サイドバー / 左侧栏 --%>
    <jsp:include page="/WEB-INF/views/provider/_sidebar.jsp" />

    <%-- 右コンテンツ / 右侧内容 --%>
    <main class="content">
      <div class="content-inner">

        <%-- 上部ヘッダー / 顶部header（共通） --%>
        <jsp:include page="/WEB-INF/views/provider/_header.jsp" />

        <div class="content-card">
        <%-- =========================
             JP：この下がページ本文（今は空の壳）
             CN：下面是页面主体（现在先留空壳）
             ========================= --%>

        <%-- メッセージ表示 / 消息显示 --%>
        <c:set var="graceMinutes" value="${empty initParam['expire.graceMinutes'] ? 15 : initParam['expire.graceMinutes']}" />
        <c:set var="graceMs" value="${graceMinutes * 60 * 1000}" />
        <c:set var="msg" value="${param.msg}" />
        <c:set var="error" value="${param.error}" />
        <c:if test="${not empty msg or not empty error}">
          <div id="fl-msg" style="margin:12px 0; padding:10px 12px; border:1px solid #ddd; background:#fafafa; color:#333;">
            <c:choose>
              <c:when test="${msg == 'picked_up_success'}">受取済として更新しました。</c:when>
              <c:when test="${error == 'not_authorized'}">権限がありません。</c:when>
              <c:when test="${error == 'not_found'}">対象の予約が見つかりません。</c:when>
              <c:when test="${error == 'status_changed'}">予約の状態が変更されました。ページを更新してください。</c:when>
              <c:otherwise>エラーが発生しました。時間を置いてもう一度お試しください。</c:otherwise>
            </c:choose>
          </div>
        </c:if>
        <c:if test="${msg == 'picked_up_success'}">
          <script>
            setTimeout(function () {
              var el = document.getElementById('fl-msg');
              if (el) el.style.display = 'none';
            }, 2000);
          </script>
        </c:if>

        <%-- フィルタバー / 筛选区 --%>
        <form method="get" action="${ctx}/provider/reservations" class="filter-bar">
          <label>
            状態：
            <select name="status">
              <option value="ALL" <c:if test="${status == 'ALL'}">selected</c:if>>全て</option>
              <option value="RESERVED" <c:if test="${status == 'RESERVED'}">selected</c:if>>受取待ち</option>
              <option value="PICKED_UP" <c:if test="${status == 'PICKED_UP'}">selected</c:if>>受取済</option>
              <option value="CANCELLED" <c:if test="${status == 'CANCELLED'}">selected</c:if>>キャンセル</option>
              <option value="EXPIRED" <c:if test="${status == 'EXPIRED'}">selected</c:if>>期限切れ</option>
              <option value="NO_SHOW" <c:if test="${status == 'NO_SHOW'}">selected</c:if>>未受取（確定）</option>
            </select>
          </label>
          <label>
            期間：
            <select name="period">
              <option value="ALL" <c:if test="${period == 'ALL'}">selected</c:if>>全て</option>
              <option value="TODAY" <c:if test="${period == 'TODAY'}">selected</c:if>>今日</option>
              <option value="7" <c:if test="${period == '7'}">selected</c:if>>7日</option>
              <option value="30" <c:if test="${period == '30'}">selected</c:if>>30日</option>
            </select>
          </label>
          <label>
            受取予定日：
            <input type="date" name="pickupDate" value="${pickupDate}" />
          </label>
          <button type="submit" class="btn btn-primary">適用</button>
          <a class="btn btn-secondary" href="${ctx}/provider/reservations">リセット</a>
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
              <c:set var="delayMs" value="${not empty r.pickupTime ? (now.time - r.pickupTime.time) : 0}" />
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
                    <c:when test="${r.status == 'RESERVED'}">
                      <span class="status-badge status-reserved">受取待ち</span>
                    </c:when>
                    <c:when test="${r.status == 'PICKED_UP'}">
                      <span class="status-badge status-done">受取済</span>
                    </c:when>
                    <c:when test="${r.status == 'CANCELLED'}">
                      <span class="status-badge status-cancelled">キャンセル</span>
                    </c:when>
                    <c:when test="${r.status == 'EXPIRED'}">
                      <span class="status-badge status-expired">期限切れ</span>
                    </c:when>
                    <c:when test="${r.status == 'NO_SHOW'}">
                      <span class="status-badge status-expired">未受取（確定）</span>
                    </c:when>
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

        <c:if test="${totalPages > 0}">
          <c:url var="pageBase" value="/provider/reservations">
            <c:param name="status" value="${status}" />
            <c:param name="period" value="${period}" />
            <c:param name="pickupDate" value="${pickupDate}" />
            <c:param name="size" value="${pageSize}" />
          </c:url>

          <c:set var="totalCountSafe" value="${empty totalCount ? (totalPages * pageSize) : totalCount}" />

          <div class="pagination">
            <span class="page-item page-count">全 ${totalCountSafe} 件</span>

            <c:choose>
              <c:when test="${currentPage > 1}">
                <a class="page-item page-link" href="${pageBase}&page=${currentPage - 1}">‹ 前へ</a>
              </c:when>
              <c:otherwise>
                <span class="page-item page-link disabled">‹ 前へ</span>
              </c:otherwise>
            </c:choose>

            <c:forEach var="p" begin="1" end="${totalPages}">
              <c:choose>
                <c:when test="${p == currentPage}">
                  <span class="page-item page-link active">${p}</span>
                </c:when>
                <c:otherwise>
                  <a class="page-item page-link" href="${pageBase}&page=${p}">${p}</a>
                </c:otherwise>
              </c:choose>
            </c:forEach>

            <c:choose>
              <c:when test="${currentPage < totalPages}">
                <a class="page-item page-link" href="${pageBase}&page=${currentPage + 1}">次へ ›</a>
              </c:when>
              <c:otherwise>
                <span class="page-item page-link disabled">次へ ›</span>
              </c:otherwise>
            </c:choose>
          </div>
        </c:if>

        </div>
      </div>
    </main>

  </div>
</body>
</html>

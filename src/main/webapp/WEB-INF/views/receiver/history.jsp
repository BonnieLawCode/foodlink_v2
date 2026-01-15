<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>受取履歴 | FoodLink</title>
<link rel="stylesheet" href="${ctx}/assets/css/receiver-layout.css?v=1">
</head>

<body>
  <jsp:useBean id="now" class="java.util.Date" scope="page" />
  <jsp:include page="/WEB-INF/views/receiver/_header.jsp" />
  <jsp:include page="/WEB-INF/views/receiver/_drawer.jsp" />

  <div class="page">
    <div class="panel">
      <div style="font-weight:900; font-size:20px;">受取履歴</div>
      <div style="color:#666; margin-top:8px;">以下に予約と受取状況を表示します。</div>
      <%-- JP: PRG用メッセージ表示 / CN: PRG重定向后的提示信息 --%>
      <c:set var="msg" value="${param.msg}" />
      <c:set var="error" value="${param.error}" />
      <c:if test="${not empty msg or not empty error}">
        <div style="margin-top:12px; padding:10px 12px; border:1px solid #ddd; background:#fafafa; color:#333;">
          <c:choose>
            <c:when test="${msg == 'cancel_success'}">
              予約をキャンセルしました。
            </c:when>
            <c:when test="${error == 'not_found'}">
              対象の予約が見つかりません。
            </c:when>
            <c:when test="${error == 'state_changed'}">
              予約の状態が変更されました。ページを更新してください。
            </c:when>
            <c:when test="${error == 'pickup_expired'}">
              受取時間を過ぎたためキャンセルできません。
            </c:when>
            <c:otherwise>
              エラーが発生しました。時間を置いてもう一度お試しください。
            </c:otherwise>
          </c:choose>
        </div>
      </c:if>

      <div style="margin-top:18px;">
        <table class="fl-history-table" width="100%" border="0" cellspacing="0" cellpadding="8">
          <thead>
            <tr>
              <th>予約番号</th>
              <th>商品名</th>
              <th>店舗名</th>
              <th>受取日時</th>
              <th>金額</th>
              <th>状態</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach items="${reservations}" var="r">
              <c:set var="status" value="${empty r.status ? 'RESERVED' : r.status}" />
              <%-- JP: 受取日時が未来の場合のみキャンセル可 / CN: 仅在受取时间之前可取消 --%>
              <c:set var="canCancel" value="${status == 'RESERVED' and (not empty r.pickupTime) and r.pickupTime.time gt now.time}" />
              <tr data-rid="${r.id}">
                <td>${r.code}</td>
                <td>${r.foodName}</td>
                <td>${r.pickupLocation}</td>
                <td>
                  <c:choose>
                    <c:when test="${not empty r.pickupTime}">
                      <fmt:formatDate value="${r.pickupTime}" pattern="yyyy-MM-dd HH:mm"/>
                    </c:when>
                    <c:otherwise>
                      <span style="color:#999;" title="受取時間未設定">未設定</span>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td>¥<fmt:formatNumber value="${r.totalPrice}"/></td>
                <td class="fl-status-cell">
                  <c:choose>
                    <c:when test="${status == 'RESERVED'}">
                      <span title="受取待ちの予約です（確認待ちではありません）">予約中（受取待ち）</span>
                    </c:when>
                    <c:when test="${status == 'PICKED_UP'}">
                      <span title="受取が完了しました">受取完了</span>
                    </c:when>
                    <c:when test="${status == 'CANCELLED'}">
                      <span title="ユーザーによりキャンセルされました">キャンセル</span>
                    </c:when>
                    <c:when test="${status == 'NO_SHOW'}">
                      <span title="期限内に受取が確認できませんでした">未受取（期限切れ）</span>
                    </c:when>
                    <c:otherwise>
                      <span title="予約状況の確認中">予約中（受取待ち）</span>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td class="fl-actions-cell">
                  <c:choose>
                    <c:when test="${canCancel}">
                      <form class="fl-cancel-form" method="post" action="${ctx}/receiver/reserve/cancel">
                        <input type="hidden" name="reservationId" value="${r.id}" />
                        <button type="submit" class="fl-btn fl-btn-cancel">キャンセル</button>
                      </form>
                    </c:when>
                    <c:otherwise><span style="display:inline-block; width:100%; text-align:center;">—</span></c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty reservations}">
              <tr>
                <td colspan="7" style="text-align:center; color:#666; padding:24px;">受取履歴がまだありません。</td>
              </tr>
            </c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <script src="${ctx}/assets/js/receiver-drawer.js?v=1"></script>
  <script src="${ctx}/assets/js/receiver-history.js?v=6"></script>
</body>
</html>

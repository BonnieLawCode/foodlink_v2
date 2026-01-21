<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
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

      <div class="fl-history-list">
        <c:forEach items="${reservations}" var="r">
          <c:set var="status" value="${empty r.status ? 'RESERVED' : r.status}" />
          <%-- JP: 受取日時が未来の場合のみキャンセル可 / CN: 仅在受取时间之前可取消 --%>
          <c:set var="canCancel" value="${status == 'RESERVED' and (not empty r.pickupTime) and r.pickupTime.time gt now.time}" />
          <div class="fl-history-card" data-rid="${r.id}">
            <div class="fl-history-meta">
              <div class="fl-history-meta-left">
                <span class="fl-meta-date">
                  <c:choose>
                    <c:when test="${not empty r.pickupTime}">
                      <fmt:formatDate value="${r.pickupTime}" pattern="yyyy-MM-dd HH:mm"/>
                    </c:when>
                    <c:otherwise>受取日時 未設定</c:otherwise>
                  </c:choose>
                </span>
                <span class="fl-meta-sep">·</span>
                <span class="fl-meta-code">予約番号：${r.code}</span>
                <span class="fl-meta-sep">·</span>
                <span class="fl-meta-shop">
                  <c:choose>
                    <c:when test="${not empty r.companyName}">${r.companyName}</c:when>
                    <c:otherwise>-</c:otherwise>
                  </c:choose>
                </span>
              </div>
              <div class="fl-history-status">
                <c:choose>
                  <c:when test="${status == 'RESERVED'}">
                    <span class="fl-status-badge fl-status-reserved" title="受取待ちの予約です（確認待ちではありません）">予約中（受取待ち）</span>
                  </c:when>
                  <c:when test="${status == 'PICKED_UP'}">
                    <span class="fl-status-badge fl-status-completed" title="受取が完了しました">受取完了</span>
                  </c:when>
                  <c:when test="${status == 'CANCELLED'}">
                    <span class="fl-status-badge fl-status-cancelled" title="ユーザーによりキャンセルされました">キャンセル</span>
                  </c:when>
                  <c:when test="${status == 'EXPIRED'}">
                    <span class="fl-status-badge fl-status-expired" title="受取時間を過ぎたため期限切れになりました">期限切れ</span>
                  </c:when>
                  <c:when test="${status == 'NO_SHOW'}">
                    <span class="fl-status-badge fl-status-expired" title="期限内に受取が確認できませんでした">未受取（期限切れ）</span>
                  </c:when>
                  <c:otherwise>
                    <span class="fl-status-badge fl-status-reserved" title="予約状況の確認中">予約中（受取待ち）</span>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>

            <div class="fl-history-body">
              <div class="fl-history-item">
                <div class="fl-history-thumb" aria-hidden="true">
                  <c:choose>
                    <c:when test="${not empty r.imagePath}">
                      <c:set var="img" value="${r.imagePath}" />
                      <c:if test="${!fn:startsWith(img, '/')}">
                        <c:set var="img" value="/${img}" />
                      </c:if>
                      <img src="${ctx}${img}" alt="${r.foodName}">
                    </c:when>
                    <c:otherwise>
                      <img src="${ctx}/assets/img/noimage.jpg" alt="no image">
                    </c:otherwise>
                  </c:choose>
                </div>
                <div class="fl-history-info">
                  <div class="fl-history-name">${r.foodName}</div>
                  <div class="fl-history-place">
                    受取場所：
                    <c:choose>
                      <c:when test="${not empty r.companyAddress}">${r.companyAddress}</c:when>
                      <c:otherwise><span class="fl-muted">-</span></c:otherwise>
                    </c:choose>
                  </div>
                </div>
              </div>

              <div class="fl-history-side">
                <div class="fl-history-qty">x${r.quantity}</div>
                <div class="fl-history-amount">¥<fmt:formatNumber value="${r.totalPrice}"/></div>
                <div class="fl-history-actions">
                  <c:choose>
                    <c:when test="${status == 'RESERVED'}">
                      <a class="fl-btn fl-btn-detail-orange" href="${ctx}/receiver/reservation/detail?code=${r.code}">詳細</a>
                      <c:if test="${canCancel}">
                        <form class="fl-cancel-form" method="post" action="${ctx}/receiver/reserve/cancel" style="display:inline;">
                          <input type="hidden" name="reservationId" value="${r.id}" />
                          <button type="submit" class="fl-btn fl-btn-cancel">キャンセル</button>
                        </form>
                      </c:if>
                    </c:when>
                    <c:otherwise><span class="fl-actions-empty">—</span></c:otherwise>
                  </c:choose>
                </div>
              </div>
            </div>
          </div>
        </c:forEach>
        <c:if test="${empty reservations}">
          <div class="fl-history-empty">受取履歴がまだありません。</div>
        </c:if>
      </div>
    </div>
  </div>

  <script src="${ctx}/assets/js/receiver-drawer.js?v=1"></script>
  <script src="${ctx}/assets/js/receiver-history.js?v=6"></script>
</body>
</html>

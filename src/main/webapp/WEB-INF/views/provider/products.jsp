<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<%
request.setAttribute("pageTitle", "商品管理");
request.setAttribute("activeMenu", "products");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>商品管理 | もったいナビ</title>
<link rel="stylesheet" href="${ctx}/assets/css/provider-layout.css?v=3">
<style>
table.fl-table { border-collapse: collapse; width: 100%; background: #fff; table-layout: fixed; }
th, td { border: 1px solid #e5e5e5; padding: 10px 12px; vertical-align: top; }
th { background: #fafafa; font-weight: 700; position: sticky; top: 0; z-index: 1; }
tbody tr:hover { background: #f7f7f7; }
/* 列宽：商品 / 詳細 / 画像 / 価格 / 状態 / 操作 */
.fl-table th:nth-child(1) { width: 26%; }
.fl-table th:nth-child(2) { width: 26%; }
.fl-table th:nth-child(3) { width: 14%; }
.fl-table th:nth-child(4) { width: 12%; }
.fl-table th:nth-child(5) { width: 10%; }
.fl-table th:nth-child(6) { width: 12%; }
.filter-bar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; margin: 8px 0 14px; }
.filter-bar input[type="text"] { padding: 6px 8px; border: 1px solid #ddd; border-radius: 6px; }
.btn { display: inline-flex; align-items: center; justify-content: center; gap: 6px; padding: 6px 12px; border-radius: 8px; font-size: 13px; text-decoration: none; cursor: pointer; border: 1px solid transparent; }
.btn-primary { background: #2563eb; color: #fff; border-color: #2563eb; }
.btn-primary:hover { opacity: 0.92; }
.btn-secondary { background: #fff; color: #666; border-color: #d0d0d0; }
.btn-secondary:hover { border-color: #b5b5b5; color: #444; }
.btn-ghost { background: #eaf2ff; color: #2563eb; border-color: #d7e7ff; padding: 4px 10px; }
.btn-ghost:hover { background: #dbeafe; }
.status-badge { display: inline-block; padding: 4px 10px; border-radius: 999px; font-size: 12px; }
.status-open { background: #e0f2fe; color: #1d4ed8; }
.status-closed { background: #f1f5f9; color: #475569; }
.status-expired { background: #fff7ed; color: #c2410c; }
.pagination { display: flex; gap: 8px; align-items: center; margin-top: 12px; justify-content: center; }
.page-link { padding: 6px 10px; border: 1px solid #ddd; border-radius: 6px; text-decoration: none; color: #333; }
.page-link.active { background: #2563eb; color: #fff; border-color: #2563eb; }
.page-link.disabled { color: #aaa; cursor: not-allowed; background: #f5f5f5; }
.content-card { background: #fff; border-radius: 14px; padding: 14px; }
.focus-row { background: #fef9c3; }
.muted { color: #777; font-size: 12px; }
.product-name { font-weight: 800; font-size: 18px; margin-bottom: 4px; }
.product-cat { font-size: 12px; color: #666; }
.desc-clamp { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden; font-size: 13px; color:#444; line-height:1.5; }
.actions { display: flex; flex-direction: column; gap: 6px; }
.thumb { width: 70px; height: 70px; object-fit: cover; border-radius: 10px; border:1px solid #eee; }
</style>
</head>
<body>
  <div class="app">
    <jsp:include page="/WEB-INF/views/provider/_sidebar.jsp" />
    <main class="content">
      <div class="content-inner">
        <jsp:include page="/WEB-INF/views/provider/_header.jsp" />
        <div class="content-card">
          <div style="display:flex; justify-content: space-between; align-items:center; gap:12px; margin-bottom:12px;">
            <form method="get" action="${ctx}/provider/products" class="filter-bar" style="margin:0; flex:1;">
              <input type="text" name="q" value="${q}" placeholder="商品名/カテゴリ" style="flex:1; min-width:220px;">
              <button type="submit" class="btn btn-primary">適用</button>
              <a class="btn btn-secondary" href="${ctx}/provider/products">リセット</a>
            </form>
            <a class="btn btn-primary" href="${ctx}/provider/foods/new?mode=NEW">＋ 新規商品登録</a>
          </div>

          <table class="fl-table">
            <thead>
              <tr>
                <th>商品</th>
                <th>商品詳細</th>
                <th>画像</th>
                <th>価格</th>
                <th>状態</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach items="${products}" var="p">
                <tr id="product-${p.id}">
                <td>
                  <div class="product-name">${p.name}</div>
                  <div class="product-cat">カテゴリ：${p.category}</div>
                </td>
                <td>
                  <c:set var="descText" value="${empty p.description ? '説明なし' : p.description}" />
                  <div class="desc-clamp" title="${descText}">${descText}</div>
                </td>
                  <td>
                    <c:if test="${not empty p.imagePath}">
                      <img src="${ctx}${p.imagePath}" alt="${p.name}" class="thumb">
                    </c:if>
                    <c:if test="${empty p.imagePath}">
                      <span class="muted">No Image</span>
                    </c:if>
                  </td>
                  <td>
                    <c:choose>
                      <c:when test="${not empty p.lastPriceOffer}">${p.lastPriceOffer} 円</c:when>
                      <c:when test="${not empty p.priceNormal}">${p.priceNormal} 円</c:when>
                      <c:otherwise>-</c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                    <c:choose>
                      <c:when test="${p.lastStatus == 'OPEN'}"><span class="status-badge status-open">公開中</span></c:when>
                      <c:when test="${p.lastStatus == 'EXPIRED'}"><span class="status-badge status-expired">期限切れ</span></c:when>
                      <c:when test="${p.lastStatus == 'CLOSED'}"><span class="status-badge status-closed">販売終了</span></c:when>
                      <c:otherwise><span class="muted">-</span></c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                    <div class="actions">
                      <a class="btn btn-ghost" href="${ctx}/provider/foods/new?productId=${p.id}&mode=RELIST">再出品</a>
                      <a class="btn btn-secondary" href="#" onclick="return false;">編集（準備中）</a>
                    </div>
                  </td>
                </tr>
              </c:forEach>
              <c:if test="${empty products}">
                <tr>
                  <td colspan="6" style="text-align:center; color:#666; padding:24px;">商品がありません。</td>
                </tr>
              </c:if>
            </tbody>
          </table>

          <c:if test="${totalPages > 0}">
            <c:url var="pageBase" value="/provider/products">
              <c:param name="q" value="${q}" />
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

  <script>
    document.addEventListener('DOMContentLoaded', function() {
      var fid = '${focusProductId}';
      if (fid) {
        var row = document.getElementById('product-' + fid);
        if (row) {
          row.classList.add('focus-row');
          row.scrollIntoView({behavior:'smooth', block:'center'});
          setTimeout(function(){ row.classList.remove('focus-row'); }, 2000);
        }
      }
    });
  </script>
</body>
</html>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>FoodLink | 受取者ホーム</title>

<%-- JP：受取者側CSS / CN：受取者端CSS --%>
<link rel="stylesheet" href="${ctx}/assets/css/receiver-layout.css?v=1">
</head>

<body>
  <%-- JP：共通ヘッダー / CN：共通 header（顶部固定） --%>
  <jsp:include page="/WEB-INF/views/receiver/_header.jsp" />

  <%-- JP：共通ドロワー / CN：共通抽屉菜单 --%>
  <jsp:include page="/WEB-INF/views/receiver/_drawer.jsp" />

  <%-- JP：本文（今は壳） / CN：主体（目前只做壳） --%>
  <div class="page">

    <div class="panel">
      <%-- JP：検索条件パネル（仮） / CN：检索条件区（占位） --%>
      <div style="display:flex; gap:14px; flex-wrap:wrap; align-items:flex-end;">
        <div>
          <div style="font-weight:800; margin-bottom:6px;">キーワード</div>
          <input type="text" placeholder="例：パン／弁当／飲料"
                 style="padding:10px 12px; border-radius:12px; border:1px solid #ddd; width:220px;">
        </div>

        <div>
          <div style="font-weight:800; margin-bottom:6px;">カテゴリ</div>
          <select style="padding:10px 12px; border-radius:12px; border:1px solid #ddd; width:180px;">
            <option>すべて</option>
          </select>
        </div>

        <div>
          <div style="font-weight:800; margin-bottom:6px;">エリア（駅・地区）</div>
          <input type="text" placeholder="例：大阪駅／梅田"
                 style="padding:10px 12px; border-radius:12px; border:1px solid #ddd; width:220px;">
        </div>

        <div>
          <div style="font-weight:800; margin-bottom:6px;">並び替え</div>
          <select style="padding:10px 12px; border-radius:12px; border:1px solid #ddd; width:160px;">
            <option>新着順</option>
          </select>
        </div>

        <button type="button"
                style="padding:12px 16px; border-radius:12px; border:1px solid #ddd; background:#fff; font-weight:800;">
          この条件で検索
        </button>

        <button type="button"
                style="padding:12px 16px; border-radius:12px; border:1px solid #ddd; background:#fff; font-weight:800;">
          条件をクリア
        </button>
      </div>
    </div>

    <div class="grid3">
      <%-- JP：商品カード（仮）/ CN：商品卡片（占位） --%>
      <div class="card">
        <div class="img-placeholder"></div>
        <div style="font-weight:900; font-size:18px;">日替わり弁当</div>
        <div style="color:#666; margin-top:8px;">（ダミー表示）</div>
      </div>

      <div class="card">
        <div class="img-placeholder"></div>
        <div style="font-weight:900; font-size:18px;">ミックスサンド</div>
        <div style="color:#666; margin-top:8px;">（ダミー表示）</div>
      </div>

      <div class="card">
        <div class="img-placeholder"></div>
        <div style="font-weight:900; font-size:18px;">緑茶（500ml）</div>
        <div style="color:#666; margin-top:8px;">（ダミー表示）</div>
      </div>
    </div>

  </div>

  <%-- JP：ドロワーJS / CN：抽屉JS --%>
  <script src="${ctx}/assets/js/receiver-drawer.js?v=1"></script>
</body>
</html>

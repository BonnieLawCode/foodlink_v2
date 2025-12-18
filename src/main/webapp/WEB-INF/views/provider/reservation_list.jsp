<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

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

        <div style="padding: 16px; color: #666;">
          <%-- JP：仮テキスト（不要なら削除OK）/ CN：占位文字（不需要可删除） --%>
          予約一覧ページ（準備中）
        </div>

      </div>
    </main>

  </div>
</body>
</html>

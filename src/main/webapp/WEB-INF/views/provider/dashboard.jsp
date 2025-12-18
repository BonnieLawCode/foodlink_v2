<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%--
  JP：ContextPath（例：/foodlink_v2）を変数に入れる
  CN：把项目 ContextPath（例如 /foodlink_v2）存成变量
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%--
  JP：ヘッダーに表示するタイトル
  CN：顶部 header 显示的标题
--%>
<%
request.setAttribute("pageTitle", "ホーム");

// JP：サイドバーの選択状態（active）
// CN：侧边栏高亮当前菜单（对应 _sidebar.jsp 的判定）
request.setAttribute("activeMenu", "home");
%>

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>ホーム | もったいナビ</title>

<%--
    JP：共通レイアウトCSS（あなたの配置：webapp/assets/css/）
    CN：通用布局 CSS（你放在 assets/css 下）
    ※ v=数字はキャッシュ対策 / v=数字 防缓存
  --%>
<link rel="stylesheet" href="${ctx}/assets/css/provider-layout.css?v=2">
</head>

<body>

	<div class="app">

		<%-- JP：左サイドバー（共通） / CN：左侧 sidebar（共用组件） --%>
		<jsp:include page="/WEB-INF/views/provider/_sidebar.jsp" />

		<%-- JP：右側コンテンツ / CN：右侧内容区 --%>
		<main class="content">
			<div class="content-inner">

		<%-- JP：上部ヘッダー（共通） / CN：顶部 header（共用组件） --%>
		<jsp:include page="/WEB-INF/views/provider/_header.jsp" />

				<%--
        JP：メインコンテンツは一旦空でOK（後でカードやグラフを置く）
        CN：主体内容先空着（之后你可以放统计卡片/图表）
      --%>
				<div style="padding: 12px 0;">
					<p style="margin: 0; color: #777;">（ここにダッシュボード内容を追加予定）</p>
				</div>

			</div>
		</main>

	</div>

</body>
</html>

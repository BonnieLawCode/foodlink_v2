<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

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
<link rel="stylesheet" href="${ctx}/assets/css/provider-layout.css?v=3">
<style>
	/* JP：簡易ダッシュボード用の最小スタイル / CN：简易仪表盘的最小样式 */
	.kpi-grid {
		display: grid;
		grid-template-columns: repeat(4, 1fr);
		gap: 12px;
		margin-top: 8px;
	}

	.kpi-card {
		border: 1px solid #e8e8e8;
		border-radius: 12px;
		padding: 14px;
		background: #fff;
	}

	.kpi-card.is-attention {
		background: #fff6ee;
		border-color: #f2c6a4;
		box-shadow: 0 2px 8px rgba(220, 120, 60, 0.15);
	}

	.kpi-title {
		color: #666;
		font-size: 12px;
		margin: 0 0 6px 0;
	}

	.kpi-value {
		font-size: 22px;
		font-weight: 800;
		margin: 0 0 4px 0;
	}

	.kpi-card.is-attention .kpi-value {
		font-size: 24px;
	}

	.kpi-sub {
		font-size: 12px;
		color: #888;
		margin: 0;
	}

	.kpi-diff {
		font-size: 12px;
		margin: 6px 0 0 0;
	}

	.diff-up {
		color: #dc2626;
		font-weight: 700;
	}

	.diff-down {
		color: #2563eb;
		font-weight: 700;
	}

	.diff-none {
		color: #999;
	}

	.dash-lower {
		display: grid;
		grid-template-columns: 2fr 1fr;
		gap: 16px;
		margin-top: 16px;
	}

	.chart-card {
		border: 1px solid #eee;
		border-radius: 12px;
		padding: 12px;
		background: #fff;
		height: 360px;
		position: relative;
	}

	.chart-title {
		margin: 0;
		font-size: 16px;
	}

	.chart-sub {
		margin: 4px 0 0 0;
		color: #777;
		font-size: 12px;
	}

	#weeklyTrendChart {
		width: 100%;
		height: calc(100% - 44px);
		display: block;
	}

	.task-card {
		border: 1px solid #eee;
		border-radius: 12px;
		padding: 12px;
		background: #fff;
	}

	.task-head {
		display: flex;
		align-items: center;
		gap: 8px;
		justify-content: space-between;
	}

	.task-title {
		margin: 0;
		font-size: 16px;
	}

	.task-meta {
		display: flex;
		align-items: center;
		gap: 8px;
		color: #777;
		font-size: 12px;
	}

	.task-badge {
		display: inline-block;
		padding: 2px 8px;
		border-radius: 999px;
		background: #fff1e6;
		color: #b45309;
		font-weight: 700;
		font-size: 12px;
	}

	.task-table {
		width: 100%;
		border-collapse: collapse;
		margin-top: 12px;
	}

	.task-table th,
	.task-table td {
		border-bottom: 1px solid #eee;
		padding: 12px 8px;
		text-align: left;
	}

	.task-table thead th {
		color: #666;
		font-size: 12px;
	}

	.task-table tbody tr:hover {
		background: #faf7f3;
		box-shadow: inset 0 0 0 1px #f5e7db;
	}

	.alert-empty {
		color: #666;
		padding: 12px 0;
	}

	.task-link {
		display: inline-block;
		padding: 6px 10px;
		border-radius: 8px;
		text-decoration: none;
		font-size: 12px;
		background: #f59e0b;
		color: #fff;
	}

	@media (max-width: 980px) {
		.kpi-grid {
			grid-template-columns: repeat(2, 1fr);
		}
		.dash-lower {
			grid-template-columns: 1fr;
		}
	}
</style>
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

				<div class="content-card">
					<%--
					  JP：ダッシュボードKPI（簡易）
					  CN：仪表盘KPI（简易）
					--%>
					<div class="kpi-grid">
						<div class="kpi-card">
							<p class="kpi-title">売上金額（受取完了）</p>
							<p class="kpi-value">
								¥<c:out value="${kpi.todaySalesAmount}" default="0" />
							</p>
							<p class="kpi-sub">本日売上</p>
							<p class="kpi-diff ${salesDiffClass}">
								<c:out value="${salesDiff}" default="—" />
							</p>
						</div>
						<div class="kpi-card">
							<p class="kpi-title">今日出品数</p>
							<p class="kpi-value">
								<c:out value="${kpi.todayFoodsCount}" default="0" />
							</p>
							<p class="kpi-sub">本日新規登録</p>
							<p class="kpi-diff ${foodsDiffClass}">
								<c:out value="${foodsDiff}" default="—" />
							</p>
						</div>
						<div class="kpi-card">
							<p class="kpi-title">今日予約数</p>
							<p class="kpi-value">
								<c:out value="${kpi.todayReservationsCount}" default="0" />
							</p>
							<p class="kpi-sub">本日受付分</p>
							<p class="kpi-diff ${resvDiffClass}">
								<c:out value="${resvDiff}" default="—" />
							</p>
						</div>
						<div class="kpi-card">
							<p class="kpi-title">公開中（可售商品数）</p>
							<p class="kpi-value">
								<c:out value="${kpi.openFoodsCount}" default="0" />
							</p>
							<p class="kpi-sub">現在販売可能</p>
						</div>
					</div>

					<div class="dash-lower">
						<%-- JP：分析区（Chart.js） / CN：趋势图（Chart.js） --%>
						<div class="chart-card">
							<h3 class="chart-title">本週の注文数・売上推移</h3>
							<p class="chart-sub">過去7日間の実績</p>
							<canvas id="weeklyTrendChart" height="320"></canvas>
						</div>

						<%-- JP：行动区（待处理预约） / CN：行动区（今日待处理预约） --%>
						<div class="task-card">
							<div class="task-head">
								<h3 class="task-title">本日の対応待ち</h3>
								<div class="task-meta">
									<span class="task-badge"><c:out value="${fn:length(alerts)}" default="0" />件</span>
									<span>本日中に対応が必要</span>
								</div>
							</div>
							<c:choose>
								<c:when test="${empty alerts}">
									<div class="alert-empty">現在、対応が必要な予約はありません</div>
								</c:when>
								<c:otherwise>
									<table class="task-table">
										<thead>
											<tr>
												<th>予約番号</th>
												<th>商品名</th>
												<th>数量</th>
												<th>予約日時</th>
												<th>操作</th>
											</tr>
										</thead>
										<tbody>
											<c:forEach items="${alerts}" var="a">
												<tr>
													<td><c:out value="${a.reservationCode}" /></td>
													<td><c:out value="${a.foodName}" /></td>
													<td><c:out value="${a.quantity}" /></td>
													<td>
														<c:choose>
															<c:when test="${not empty a.reserveAt}">
																<fmt:formatDate value="${a.reserveAt}" pattern="yyyy-MM-dd HH:mm" />
															</c:when>
															<c:otherwise>—</c:otherwise>
														</c:choose>
													</td>
													<td>
														<a class="task-link" href="${ctx}/provider/reservations">予約内容を確認</a>
													</td>
												</tr>
											</c:forEach>
										</tbody>
									</table>
								</c:otherwise>
							</c:choose>
						</div>
					</div>
				</div>

			</div>
		</main>

	</div>

	<%-- JP：Chart.js（ローカル） / CN：本地 Chart.js --%>
	<script src="${pageContext.request.contextPath}/assets/js/chart.umd.min.js"></script>
	<%-- JP：サーバーから渡された7日分データ / CN：服务器传入的7天数据 --%>
	<script>
		window.__DASHBOARD__ = window.__DASHBOARD__ || {};
		window.__DASHBOARD__.weekly = {
			labels: ${weeklyLabelsJson},
			orderCounts: ${weeklyOrderCountsJson},
			salesAmounts: ${weeklySalesAmountsJson}
		};
	</script>
	<%-- JP：ダッシュボード専用JS / CN：仪表盘专用JS --%>
	<script src="${pageContext.request.contextPath}/assets/js/provider-dashboard.js"></script>
</body>
</html>

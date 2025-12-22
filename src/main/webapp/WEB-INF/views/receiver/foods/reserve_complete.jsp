<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8" />
<title>予約完了 | もったいナビ</title>

<link rel="stylesheet" href="${ctx}/assets/css/receiver-layout.css?v=2" />
<link rel="stylesheet" href="${ctx}/assets/css/receiver-reserve.css?v=1" />
</head>

<body>
	<jsp:include page="/WEB-INF/views/receiver/_header.jsp" />
	<jsp:include page="/WEB-INF/views/receiver/_drawer.jsp" />

	<main class="page">
		<div class="panel">

			<div class="fl-complete">
				<div class="fl-complete__card">
					<div class="fl-complete__title">予約が完了しました</div>

					<div class="fl-complete__rows">
						<div class="fl-row">
							<div class="fl-row__k">予約番号</div>
							<div class="fl-row__v">${rv.code}</div>
						</div>

						<div class="fl-row">
							<div class="fl-row__k">商品</div>
							<div class="fl-row__v">${rv.foodName}</div>
						</div>

						<div class="fl-row">
							<div class="fl-row__k">受取日時</div>
							<div class="fl-row__v">${pickupDisplay}</div>
						</div>

						<div class="fl-row">
							<div class="fl-row__k">受取場所</div>
							<div class="fl-row__v">${rv.pickupLocation}</div>
						</div>

						<div class="fl-row">
							<div class="fl-row__k">数量</div>
							<div class="fl-row__v">${rv.quantity}</div>
						</div>

						<div class="fl-row">
							<div class="fl-row__k">支払予定金額</div>
							<div class="fl-row__v">
								¥
								<fmt:formatNumber value="${rv.totalPrice}" />
							</div>
						</div>
					</div>
				</div>

				<div class="fl-complete__actions">
					<a class="fl-btn fl-btn-detail fl-btn-wide"
						href="${ctx}/receiver/home">ホームに戻る</a> <a
						class="fl-btn fl-btn-reserve fl-btn-wide"
						href="${ctx}/receiver/history">予約履歴を見る</a>
				</div>

				<div class="fl-note fl-note--center">
					<div class="fl-note__title">【注意事項】</div>
					<ul>
						<li>時間内に受け取りがない場合、キャンセル扱いとなります。</li>
						<li>予約後のキャンセルは原則できません。</li>
						<li>受け取り時の支払いとなります。必ず現金をご用意ください。</li>
					</ul>
				</div>
			</div>

		</div>
	</main>
</body>
</html>

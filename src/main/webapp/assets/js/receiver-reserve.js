// assets/js/receiver-reserve.js
// JP：数量変更で合計金額を自動更新
// CN：数量变化时自动更新合计金额

(function () {
  // ① 必要な要素
  const qtyEl = document.getElementById("flQty");
  const totalEl = document.getElementById("flTotalYen");
  const totalHiddenEl = document.getElementById("flTotalHidden");

  // ② 单价（从 JSP 注入）
  const unitPrice = Number(window.FL_UNIT_PRICE || 0);

  // 如果页面上没找到元素，直接退出（避免报错）
  if (!qtyEl || !totalEl || unitPrice <= 0) return;

  // ③ 格式化日元
  function formatYen(n) {
    // n 可能是 NaN，先兜底
    const v = Number(n);
    if (!Number.isFinite(v)) return "¥0";
    return "¥" + Math.trunc(v).toLocaleString("ja-JP");
  }

  // ④ 计算并更新 UI
  function updateTotal() {
    const qty = Number(qtyEl.value || 1);
    const total = unitPrice * qty;

    totalEl.textContent = formatYen(total);

    if (totalHiddenEl) {
      totalHiddenEl.value = String(total);
    }
  }

  // ⑤ 绑定事件 + 初次计算
  qtyEl.addEventListener("change", updateTotal);
  updateTotal();
})();

// assets/js/receiver-reserve.js
// JP：予約確認ページの機能実装
// CN：预约确认页面的功能实现

(function () {
  // ① 必要な要素の取得
  const qtyInput = document.getElementById("flQty");
  const qtyMsgEl = document.getElementById("flQtyMsg");
  const pickupTimeInput = document.getElementById("flPickupTime");
  const timeMsgEl = document.getElementById("flTimeMsg");
  const totalEl = document.getElementById("flTotalYen");
  const totalHiddenEl = document.getElementById("flTotalHidden");

  // ② 全局变量（JSPから注入）
  const unitPrice = Number(window.FL_UNIT_PRICE || 0);
  const maxQty = Number(window.FL_MAX_QTY || 10);
  const pickupLabel = window.FL_PICKUP_LABEL || "";

  // 调试：打印所有全局变量
  console.log("=== PAGE LOAD DEBUG ===");
  console.log("FL_UNIT_PRICE:", window.FL_UNIT_PRICE);
  console.log("FL_MAX_QTY:", window.FL_MAX_QTY);
  console.log("FL_PICKUP_LABEL:", window.FL_PICKUP_LABEL);
  console.log("FL_PICKUP_RANGE:", window.FL_PICKUP_RANGE);
  console.log("pickupTimeInput element:", pickupTimeInput);

  // 要素が見つからない場合は処理中断
  if (!qtyInput || !totalEl || unitPrice <= 0) return;

  // ③ ユーティリティ関数
  function formatYen(n) {
    const v = Number(n);
    if (!Number.isFinite(v)) return "¥0";
    return "¥" + Math.trunc(v).toLocaleString("ja-JP");
  }

  // 日付フォーマット (YYYY-MM-DDTHH:MM形式)
  function formatDateTimeLocal(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  // 時刻を10分間隔に調整
  function roundTo10Minutes(date) {
    const minutes = date.getMinutes();
    const roundedMinutes = Math.round(minutes / 10) * 10;
    date.setMinutes(roundedMinutes);
    date.setSeconds(0);
    date.setMilliseconds(0);
    return date;
  }

  // ④ 数量検証 + Ajax
  function validateQty() {
    const qty = Number(qtyInput.value);
    let msg = "";
    let isValid = true;

    if (isNaN(qty) || qty < 1) {
      msg = "❌ 1以上の数値を入力してください";
      isValid = false;
    } else if (qty > maxQty) {
      msg = `❌ 在庫不足です（最大: ${maxQty}個）`;
      isValid = false;
      qtyInput.value = maxQty;
    } else {
      isValid = true;
    }

    // 正常情况下隐藏提示，错误时显示
    if (isValid) {
      qtyMsgEl.textContent = "";
      qtyMsgEl.className = "fl-qty-msg";
    } else {
      qtyMsgEl.textContent = msg;
      qtyMsgEl.className = "fl-qty-msg error";
    }

    updateTotal();
    return isValid;
  }

  // ⑤ 受取時間の検証
  function validatePickupTime() {
    // 如果时间输入框不存在，直接返回true
    if (!pickupTimeInput) return true;

    const timeStr = pickupTimeInput.value;
    let msg = "";
    let isValid = true;

    if (!timeStr) {
      msg = "❌ 受取時間を選択してください";
      isValid = false;
    } else {
      // 检查是否在可用时间范围内
      if (!isWithinPickupRange(timeStr)) {
        msg = "❌ 受取可能時間の範囲外です";
        isValid = false;
      } else {
        isValid = true;
      }
    }

    // 正常情况下隐藏提示，错误时显示
    if (isValid) {
      timeMsgEl.textContent = "";
      timeMsgEl.className = "fl-time-msg";
    } else {
      timeMsgEl.textContent = msg;
      timeMsgEl.className = "fl-time-msg error";
    }

    return isValid;
  }

  // ⑤-1 检查时间是否在可用范围内
  function isWithinPickupRange(timeStr) {
    const pickupRange = window.FL_PICKUP_RANGE || "";

    console.log("=== isWithinPickupRange DEBUG ===");
    console.log("pickupRange原始值:", pickupRange);
    console.log("timeStr:", timeStr);

    if (!pickupRange) {
      console.log("没有设置时间范围，允许任何时间");
      return true;
    }

    // 解析用户选择的日期/时间 (expect YYYY-MM-DDTHH:MM)
    const [userDate, userTime] = (timeStr || "").split("T");
    if (!userDate || !userTime) {
      console.log("用户时间格式不正确，拒绝");
      return false;
    }
    const [userHour, userMin] = userTime.split(":").map(Number);
    if (Number.isNaN(userHour) || Number.isNaN(userMin)) return false;

    // 处理"本日 HH:MM～HH:MM"格式
    if (pickupRange.includes("本日")) {
      const timeMatch = pickupRange.match(/(\d{1,2}):(\d{2})\s*[～~]\s*(\d{1,2}):(\d{2})/);
      console.log("检测到'本日'格式，timeMatch:", timeMatch);
      if (!timeMatch) return true; // 无法解析则不阻止
      const [, sH, sM, eH, eM] = timeMatch.map((v) => v);

      // 确认用户选择的是今天
      const today = new Date();
      const todayStr = formatDateTimeLocal(today).split("T")[0];
      if (userDate !== todayStr) {
        console.log("用户选择日期不是今天，返回false");
        return false;
      }

      const userTotalMin = userHour * 60 + userMin;
      const startTotalMin = parseInt(sH, 10) * 60 + parseInt(sM, 10);
      const endTotalMin = parseInt(eH, 10) * 60 + parseInt(eM, 10);
      const ok = userTotalMin >= startTotalMin && userTotalMin <= endTotalMin;
      console.log("本日比较 =>", userTotalMin, startTotalMin, endTotalMin, ok);
      return ok;
    }

    // 处理带具体日期的范围: "YYYY-MM-DD HH:MM～HH:MM"
    const rangeMatch = pickupRange.match(/(\d{4}-\d{2}-\d{2})\s+(\d{1,2}):(\d{2})\s*[～~]\s*(\d{1,2}):(\d{2})/);
    console.log("rangeMatch:", rangeMatch);
    if (!rangeMatch) return true; // 不能解析则允许

    const [, rangeDate, sH, sM, eH, eM] = rangeMatch;
    if (userDate !== rangeDate) {
      console.log("用户选择日期与范围日期不符", userDate, rangeDate);
      return false;
    }
    const userTotalMin = userHour * 60 + userMin;
    const startTotalMin = parseInt(sH, 10) * 60 + parseInt(sM, 10);
    const endTotalMin = parseInt(eH, 10) * 60 + parseInt(eM, 10);
    const ok = userTotalMin >= startTotalMin && userTotalMin <= endTotalMin;
    console.log("日期范围比较 =>", userTotalMin, startTotalMin, endTotalMin, ok);
    return ok;
  }

  // ⑥ 合計金額更新
  function updateTotal() {
    const qty = Number(qtyInput.value || 1);
    const total = unitPrice * qty;

    totalEl.textContent = formatYen(total);
    if (totalHiddenEl) {
      totalHiddenEl.value = String(total);
    }
  }

  // ⑦ 受取時間の初期化 + 10分単位のステップ設定
  function initPickupTime() {
    if (!pickupTimeInput) return;

    const pickupRange = window.FL_PICKUP_RANGE || "";
    console.log("=== initPickupTime DEBUG ===", pickupRange);

    // 不强制设置步长，用户只需选择范围内时间即可

    const now = new Date();
    // 优先处理 "本日" 格式
    if (pickupRange.includes("本日")) {
      const timeMatch = pickupRange.match(/(\d{1,2}):(\d{2})\s*[～~]\s*(\d{1,2}):(\d{2})/);
      console.log("init 本日 timeMatch:", timeMatch);
      if (timeMatch) {
        const [, sH, sM] = timeMatch;
        const initialDate = new Date(now.getFullYear(), now.getMonth(), now.getDate(), parseInt(sH, 10), parseInt(sM, 10), 0);
        if (initialDate < now) {
          const next = new Date(now.getTime() + 30 * 60000);
          pickupTimeInput.value = formatDateTimeLocal(next);
        } else {
          pickupTimeInput.value = formatDateTimeLocal(initialDate);
        }
        return;
      }
    }

    // 处理带日期的范围
    const rangeMatch = pickupRange.match(/(\d{4}-\d{2}-\d{2})\s+(\d{1,2}):(\d{2})\s*[～~]\s*(\d{1,2}):(\d{2})/);
    console.log("init rangeMatch:", rangeMatch);
    if (rangeMatch) {
      const [, rangeDate, sH, sM] = rangeMatch;
      const [y, m, d] = rangeDate.split("-").map(Number);
      const initialDate = new Date(y, m - 1, d, parseInt(sH, 10), parseInt(sM, 10), 0);
      if (initialDate < now) {
        const next = new Date(now.getTime() + 30 * 60000);
        pickupTimeInput.value = formatDateTimeLocal(next);
      } else {
        pickupTimeInput.value = formatDateTimeLocal(initialDate);
      }
      return;
    }

    // 无法解析则使用now+30min
    const next = new Date(now.getTime() + 30 * 60000);
    pickupTimeInput.value = formatDateTimeLocal(next);
  }

  // ⑧ 绑定事件
  qtyInput.addEventListener("input", validateQty);
  qtyInput.addEventListener("change", validateQty);

  if (pickupTimeInput) {
    pickupTimeInput.addEventListener("input", validatePickupTime);
    pickupTimeInput.addEventListener("change", validatePickupTime);
  }

  // 表单提交时再做一次完整校验
  const formEl = qtyInput.form || document.querySelector("form");
  if (formEl) {
    formEl.addEventListener("submit", function (e) {
      const qtyValid = validateQty();
      const timeValid = validatePickupTime();
      if (!qtyValid || !timeValid) {
        e.preventDefault();
        alert("入力内容を確認してください");
      }
    });
  }

  // ⑨ 初期化
  if (pickupTimeInput) initPickupTime();
  validatePickupTime();
  validateQty();
})();

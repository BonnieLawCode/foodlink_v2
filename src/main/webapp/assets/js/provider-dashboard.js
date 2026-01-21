// JP：ダッシュボードの週次推移チャート（二軸） / CN：仪表盘周趋势图（双Y轴）
(() => {
  const canvas = document.getElementById("weeklyTrendChart");
  if (!canvas) {
    console.warn("[dashboard] weeklyTrendChart not found");
    return;
  }
  if (typeof Chart === "undefined") {
    console.warn("[dashboard] Chart.js not loaded");
    return;
  }

  // JP：JSPで埋め込んだデータを使用 / CN：使用JSP内嵌的数据
  const weekly = (window.__DASHBOARD__ && window.__DASHBOARD__.weekly) || null;
  if (!weekly || !weekly.labels || weekly.labels.length === 0) {
    console.warn("[dashboard] weekly data is empty");
    return;
  }

  // JP：数値配列を安全に正規化 / CN：安全转换数值数组
  const orders = (weekly.orderCounts || []).map((v) => Number(v) || 0);
  const sales = (weekly.salesAmounts || []).map((v) => Number(v) || 0);

  // JP：最大値を計算し、軸の伸びすぎを抑える / CN：计算最大值，防止Y轴过度拉伸
  const orderMax = Math.max(5, ...orders, 0);
  const salesMax = Math.max(100, ...sales, 0);
  const orderMaxCeil = Math.ceil(orderMax * 1.1 + 1);
  const salesMaxCeil = Math.ceil(salesMax * 1.1 + 1);

  // JP：既存インスタンスがあれば破棄 / CN：若已有实例先销毁
  if (window.__DASHBOARD__ && window.__DASHBOARD__.weeklyChart) {
    window.__DASHBOARD__.weeklyChart.destroy();
  }

  window.__DASHBOARD__ = window.__DASHBOARD__ || {};
  window.__DASHBOARD__.weeklyChart = new Chart(canvas, {
    type: "line",
    data: {
      labels: weekly.labels,
      datasets: [
        {
          label: "注文数",
          data: orders,
          borderColor: "#2563eb",
          backgroundColor: "rgba(37, 99, 235, 0.12)",
          tension: 0.35,
          pointRadius: 3,
          yAxisID: "yOrders"
        },
        {
          label: "売上金額",
          data: sales,
          borderColor: "#f59e0b",
          backgroundColor: "rgba(245, 158, 11, 0.12)",
          tension: 0.35,
          pointRadius: 3,
          yAxisID: "ySales"
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: true },
        tooltip: {
          callbacks: {
            // JP：売上に¥を付ける / CN：卖上添加¥
            label: function (ctx) {
              const label = ctx.dataset.label || "";
              const value = ctx.parsed.y;
              if (label === "売上金額") {
                return label + ": ¥" + value;
              }
              return label + ": " + value;
            }
          }
        }
      },
      scales: {
        yOrders: {
          type: "linear",
          position: "left",
          beginAtZero: true,
          min: 0,
          max: orderMaxCeil,
          grid: { color: "#f3f4f6" }
        },
        ySales: {
          type: "linear",
          position: "right",
          beginAtZero: true,
          min: 0,
          max: salesMaxCeil,
          grid: { drawOnChartArea: false },
          ticks: {
            callback: function (value) {
              return "¥" + value;
            }
          }
        },
        x: {
          grid: { display: false }
        }
      }
    }
  });
})();

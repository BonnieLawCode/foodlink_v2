/**
 * FoodLink / もったいナビ - Banner Slider
 * JP: バナーの自動スライダー（ドット切替対応）
 * CN: 主页轮播（自动切换 + 圆点切换 + hover暂停）
 *
 * 使い方 / 用法：
 * 1) HTML側：
 *    - .fl-slider__viewport の中に .fl-slide を複数置く
 *    - .fl-dots の中に .fl-dot をスライド数と同じ数だけ置く
 * 2) JSPで読み込み（defer 推奨）：
 *    <script src="${ctx}/assets/js/receiver-slider.js?v=1" defer></script>
 *
 * 注意 / Note：
 * - 轮播容器 id 不强制，但推荐每个轮播有独立容器（本代码支持多轮播）
 */

(function() {
	"use strict";

	/**
	 * 初始化某一个 slider
	 * @param {HTMLElement} viewport - .fl-slider__viewport
	 */
	function initSlider(viewport) {
		const slides = viewport.querySelectorAll(".fl-slide");
		const dotsWrap = viewport.querySelector(".fl-dots");
		const dots = dotsWrap ? dotsWrap.querySelectorAll(".fl-dot") : [];

		// ===== 基本检查 / Basic checks =====
		if (!slides.length) return;

		// dot 数量不匹配也能运行（只是不显示点点切换）
		const hasDots = dots.length === slides.length && dots.length > 0;

		// ===== 状态 / State =====
		let idx = 0;
		let timer = null;
		const intervalMs = Number(viewport.dataset.interval || 4500); // 可通过 data-interval 自定义

		// ===== 工具函数 / Utils =====
		function setActive(nextIdx) {
			// 边界处理 / Clamp
			if (nextIdx < 0) nextIdx = slides.length - 1;
			if (nextIdx >= slides.length) nextIdx = 0;

			// 移除旧 active
			slides[idx].classList.remove("is-active");
			slides[idx].classList.remove("is-fade"); // 动画 class（可选）

			if (hasDots) {
				dots[idx].classList.remove("is-active");
				dots[idx].setAttribute("aria-current", "false");
			}

			// 更新 index
			idx = nextIdx;

			// 添加新 active
			slides[idx].classList.add("is-active");
			slides[idx].classList.add("is-fade"); // 触发淡入动画（CSS 可选）

			if (hasDots) {
				dots[idx].classList.add("is-active");
				dots[idx].setAttribute("aria-current", "true");
			}
		}

		function next() {
			setActive(idx + 1);
		}

		function start() {
			stop();
			timer = window.setInterval(next, intervalMs);
		}

		function stop() {
			if (timer) {
				window.clearInterval(timer);
				timer = null;
			}
		}

		function restart() {
			stop();
			start();
		}

		// ===== 初始对齐 / Sync initial state =====
		// 如果 HTML 已经有 is-active，就以它为准
		let found = -1;
		slides.forEach((s, i) => {
			if (s.classList.contains("is-active")) found = i;
		});
		if (found >= 0) idx = found;

		// 统一点点状态
		if (hasDots) {
			dots.forEach((d, i) => {
				if (i === idx) {
					d.classList.add("is-active");
					d.setAttribute("aria-current", "true");
				} else {
					d.classList.remove("is-active");
					d.setAttribute("aria-current", "false");
				}
			});
		}

		// ===== 事件：点点点击 / Dot click =====
		if (hasDots) {
			dots.forEach((dot, i) => {
				dot.addEventListener("click", function() {
					setActive(i);
					restart(); // 点击后重置计时
				});
			});
		}

		// ===== 事件：hover 暂停（PC） / pause on hover =====
		viewport.addEventListener("mouseenter", stop);
		viewport.addEventListener("mouseleave", start);

		// ===== 事件：页面隐藏时暂停 / pause when tab is hidden =====
		document.addEventListener("visibilitychange", function() {
			if (document.hidden) stop();
			else start();
		});

		// ===== 启动 / Start =====
		start();
	}

	/**
	 * 页面加载完成后初始化所有 slider
	 */
	document.addEventListener("DOMContentLoaded", function() {
		const viewports = document.querySelectorAll(".fl-slider__viewport");
		viewports.forEach(initSlider);
	});
})();

// JP：ドロワーメニュー開閉 / CN：抽屉菜单开关
(function () {
  const btn = document.getElementById("menuBtn");
  const drawer = document.getElementById("drawer");
  const overlay = document.getElementById("overlay");

  if (!btn || !drawer || !overlay) return;

  function openDrawer() {
    drawer.classList.add("open");
    overlay.classList.add("show");
  }

  function closeDrawer() {
    drawer.classList.remove("open");
    overlay.classList.remove("show");
  }

  btn.addEventListener("click", openDrawer);
  overlay.addEventListener("click", closeDrawer);

  // JP：ESCキーでも閉じる / CN：按 ESC 也关闭
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") closeDrawer();
  });
})();

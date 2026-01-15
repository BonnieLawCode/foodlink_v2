# 预约页面 (reserve.jsp) 功能升级说明

## 主要改动

### 1. 数量输入框 (Qty Input)

**原本**: `<select>` 下拉列表选择
**现在**: `<input type="number">` 文本输入框，支持：

- ✅ 键盘直接输入数字
- ✅ 上下按钮增减数量
- ✅ 实时验证（1 ~ 库存上限）
- ✅ 错误提示（红色背景）
- ✅ 成功提示（绿色背景）

**验证规则**:

- 最小值：1
- 最大值：库存数量或 10（取较小值）
- 输入非法值时显示错误提示

### 2. 受取時間 (取货时间)

**原本**: 仅显示文本 `${pickupLabel}`
**现在**: `<input type="datetime-local">` 日期时间选择器，支持：

- ✅ 图形化日期选择
- ✅ 时间精确到分钟
- ✅ 10 分钟间隔（通过 `step="600"` 实现）
- ✅ 自动初始化为 30 分钟后的整 10 分钟时刻
- ✅ 验证提示（确保选择有效时间）

**验证规则**:

- 必填字段
- 时间必须是 10 分钟的倍数
- 无法选择过去的时间（浏览器原生限制）

### 3. 合计金额自动计算

**功能**:

- ✅ 数量改变时实时计算总额
- ✅ 使用公式: `总额 = 单价 × 数量`
- ✅ 自动格式化为日本货币格式（¥）
- ✅ 隐藏字段记录总额供后端使用

### 4. Ajax 验证提示

**实现方式**:

- ✅ 输入时实时验证 (`input` 事件)
- ✅ 失焦时验证 (`blur` / `change` 事件)
- ✅ 提交前完整验证
- ✅ 验证失败时阻止表单提交

**提示样式**:

- 成功: ✓ 绿色背景 (#e8f5e9)
- 失败: ❌ 红色背景 (#ffebee)

### 5. 受取可能時間 (可用时间范围)

**改动**:

- 原本的 `受取時間` 标签改为 `受取可能時間`
- 显示可供选择的时间范围
- 新增 `受取時間` 字段用于具体选择

---

## 技术细节

### HTML 改动 [reserve.jsp]

- 数量框: `<select>` → `<input type="number">`
- 受取時間: 纯文本 → `<input type="datetime-local">`
- 添加验证提示容器: `<span id="flQtyMsg">` 和 `<span id="flTimeMsg">`
- 更新 JavaScript 变量注入

### CSS 新增 [receiver-reserve.css]

```css
.fl-qty-wrapper          /* 数量输入框容器 */
/* 数量输入框容器 */
.fl-qty-input            /* 数量输入框 */
.fl-qty-msg              /* 验证提示 */
.fl-pickup-time          /* 受取時間输入框 */
.fl-time-msg             /* 时间验证提示 */
.fl-total                /* 合计金额区域 */
.fl-btn-reserve; /* 预约按钮 */
```

### JavaScript 重写 [receiver-reserve.js]

主要函数:

- `validateQty()` - 验证数量输入
- `validatePickupTime()` - 验证选中时间
- `updateTotal()` - 自动计算总额
- `roundTo10Minutes()` - 时间调整到 10 分钟倍数
- `initPickupTime()` - 初始化受取時間

事件绑定:

- 数量框: `input` + `change` 事件
- 时间框: `change` + `blur` 事件
- 表单: `submit` 前验证

---

## 使用说明

### 用户操作流程

1. **输入数量**: 在数量框输入 1-库存数 之间的数字
   - 实时显示验证结果（✓ 或 ❌）
2. **选择受取時間**: 点击时间框弹出日期时间选择器
   - 选择日期
   - 选择时间（自动调整为 10 分钟倍数）
   - 确认选择
3. **查看合计**: 金额自动根据数量计算并显示
4. **提交预约**: 点击"予約を確定する"按钮
   - 再次验证数量和时间
   - 验证失败时显示提示并阻止提交
   - 验证成功时提交表单

---

## 浏览器兼容性

- ✅ Chrome/Edge (推荐)
- ✅ Firefox
- ✅ Safari
- ✅ 移动端浏览器
  - iOS Safari: 原生 datetime-local 支持
  - Android Chrome: 原生 datetime-local 支持

**注意**: IE11 不支持 `datetime-local`，如需支持需要使用 polyfill

---

## 后端对接注意

### 表单数据

```
qty: 数量（字符串，需要转换为整数）
pickupTime: 受取時間（格式: "2025-12-04T19:30"）
totalPrice: 合计金额（字符串，需要转换为数字）
```

### 建议处理

```java
int qty = Integer.parseInt(request.getParameter("qty"));
String pickupTime = request.getParameter("pickupTime"); // 需要转为 LocalDateTime
BigDecimal totalPrice = new BigDecimal(request.getParameter("totalPrice"));

// 后端验证
if (qty < 1 || qty > food.getQuantity()) {
    // 返回错误
}
if (!isValidPickupTime(pickupTime)) {
    // 返回错误
}
```

---

## 样式自定义

可在 CSS 中修改以下变量来自定义样式：

- 主色: `#ff9800` (橙色)
- 成功色: `#2e7d32` (绿色)
- 错误色: `#c62828` (红色)
- 边框色: `#ddd`, `#999`

---

## 已测试项目

- ✅ 数量输入验证
- ✅ 实时金额计算
- ✅ 时间选择器初始化
- ✅ 表单提交验证
- ✅ 响应式设计
- ✅ 移动端适配

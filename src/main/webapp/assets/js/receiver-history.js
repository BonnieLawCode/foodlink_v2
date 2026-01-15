// assets/js/receiver-history.js
// JP: 受取履歴のキャンセル確認 / CN: 受取履歴页面的取消确认

(function () {
  const table = document.querySelector('.fl-history-table');
  if (!table) return;

  function errorMessage(code) {
    switch (code) {
      case 'not_found':
        return '対象の予約が見つかりません。';
      case 'state_changed':
        return '予約の状態が変更されました。ページを更新してください。';
      case 'pickup_expired':
        return '受取時間を過ぎたためキャンセルできません。';
      default:
        return 'エラーが発生しました。時間を置いてもう一度お試しください。';
    }
  }

  // JP: フォーム送信前に確認ダイアログ / CN: 表单提交前确认
  table.querySelectorAll('form.fl-cancel-form').forEach(form => {
    const btn = form.querySelector('.fl-btn-cancel');
    if (!btn) return;

    form.addEventListener('submit', function (e) {
      const tr = btn.closest('tr');
      const codeCell = tr ? tr.querySelector('td') : null;
      const code = codeCell ? codeCell.textContent : '';
      const message = code ? `予約 ${code} をキャンセルしてもよろしいですか？` : '予約をキャンセルしてもよろしいですか？';

      if (!window.confirm(message)) {
        e.preventDefault();
        return;
      }

      // JP: JSが使える場合はAjaxで即時反映 / CN: 支持JS时用Ajax即时更新
      if (!window.fetch) {
        // JP: 古いブラウザは通常送信 / CN: 老浏览器走正常提交
        return;
      }

      e.preventDefault();
      btn.disabled = true;

      const formData = new FormData(form);
      formData.append('ajax', '1');
      const params = new URLSearchParams();
      formData.forEach((value, key) => {
        params.append(key, value);
      });

      fetch(form.action, {
        method: 'POST',
        credentials: 'same-origin',
        headers: {
          'X-Requested-With': 'XMLHttpRequest',
          'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
        },
        body: params.toString()
      })
        .then(resp => {
          return resp.text().then(text => ({ resp, text }));
        })
        .then(({ resp, text }) => {
          const ct = resp.headers.get('content-type') || '';
          const looksLikeJson = ct.includes('application/json');
          let data = null;
          if (looksLikeJson && text) {
            try {
              data = JSON.parse(text);
            } catch (e) {
              console.error('Cancel JSON parse error:', e, text);
            }
          }

          if (!looksLikeJson) {
            // JP: JSON以外はログイン画面等のHTMLの可能性 / CN: 非JSON可能是登录页面HTML
            if (resp.redirected && resp.url) {
              window.location.href = resp.url;
            } else {
              window.location.reload();
            }
            return;
          }

          if (resp.ok && data && data.success) {
            const statusCell = tr ? tr.querySelector('.fl-status-cell') : null;
            if (statusCell) {
              statusCell.innerHTML = '<span title="ユーザーによりキャンセルされました">キャンセル</span>';
            }
            const actionsCell = tr ? tr.querySelector('.fl-actions-cell') : null;
            if (actionsCell) {
              actionsCell.innerHTML = '<span style="display:inline-block; width:100%; text-align:center; color:#888;">キャンセル済み</span>';
            }
          } else {
            const code = data && (data.error || data.message) ? (data.error || data.message) : 'system_error';
            if (code === 'not_authenticated') {
              // JP: 未ログインはログインへ / CN: 未登录则跳转登录
              window.location.href = (window.CTX || '') + '/login';
              return;
            }
            alert(errorMessage(code));
            btn.disabled = false;
          }
        })
        .catch(() => {
          alert(errorMessage('system_error'));
          btn.disabled = false;
        });
    });
  });
})();

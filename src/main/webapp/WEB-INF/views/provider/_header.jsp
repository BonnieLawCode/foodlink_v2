<%@ page contentType="text/html; charset=UTF-8" %>

<%--
  JP：右側上部ヘッダー（ページタイトル + 検索ボックス）
  CN：右侧顶部栏（页面标题 + 搜索框）
--%>

<header class="content-header">
  <h1 class="page-title"><%= request.getAttribute("pageTitle") %></h1>

  <div class="search-box">
    <%-- 
      JP：検索は後で実装でOK（今はUIだけ）
      CN：搜索功能可以后做（现在先做UI）
    --%>
    <input type="text" placeholder="検索" />
    <span class="search-icon">🔍</span>
  </div>
</header>

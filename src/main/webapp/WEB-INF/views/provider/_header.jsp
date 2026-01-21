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
    <span class="search-icon" aria-hidden="true">
      <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="size-6" width="18" height="18">
        <path stroke-linecap="round" stroke-linejoin="round" d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607Z" />
      </svg>
    </span>
  </div>
</header>

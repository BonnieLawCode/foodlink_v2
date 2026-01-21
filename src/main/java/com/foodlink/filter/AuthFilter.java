package com.foodlink.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * JP：ログイン必須エリアの認証フィルター
 * CN：登录保护过滤器（只拦截需要登录的路径）
 */
@WebFilter(urlPatterns = { "/receiver/*", "/provider/*", "/admin/*" })
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no-op
    }

    @Override
    public void doFilter(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String ctx = req.getContextPath();
        String uri = req.getRequestURI();

        if (isPublicPath(uri, ctx)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        Object userId = (session == null) ? null : session.getAttribute("userId");
        if (userId != null) {
            chain.doFilter(request, response);
            return;
        }

        // JP：未ログインは元URLを保存してログインへ / CN：未登录保存原始URL
        HttpSession newSession = req.getSession(true);
        String redirectTo = uri;
        if (req.getQueryString() != null && !req.getQueryString().isEmpty()) {
            redirectTo += "?" + req.getQueryString();
        }
        newSession.setAttribute("redirectTo", redirectTo);

        resp.sendRedirect(ctx + "/login");
    }

    @Override
    public void destroy() {
        // no-op
    }

    private boolean isPublicPath(String uri, String ctx) {
        if (uri.equals(ctx + "/home")) {
            return true;
        }
        if (uri.equals(ctx + "/login") || uri.equals(ctx + "/logout") || uri.equals(ctx + "/index.jsp")) {
            return true;
        }
        if (uri.startsWith(ctx + "/css/") || uri.startsWith(ctx + "/js/") || uri.startsWith(ctx + "/images/")) {
            return true;
        }
        if (uri.startsWith(ctx + "/assets/")) {
            return true;
        }
        return false;
    }
}

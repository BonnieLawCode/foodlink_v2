<%@ page contentType="text/html; charset=UTF-8"%>
<%
    // JP/CN：通过 index.jsp 进入时强制清理登录状态
    if (session != null) {
        session.invalidate();
    }
    response.sendRedirect(request.getContextPath() + "/home");
%>

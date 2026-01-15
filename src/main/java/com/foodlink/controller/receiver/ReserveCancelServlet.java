package com.foodlink.controller.receiver;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.foodlink.dao.ReservationDao;
import com.foodlink.dao.ReservationDao.ReservationStatusInfo;

/**
 * 处理预约取消请求
 */
@WebServlet("/receiver/reserve/cancel")
public class ReserveCancelServlet extends HttpServlet {

    private final ReservationDao reservationDao = new ReservationDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        // JP: デバッグ用ログ / CN: 调试日志
        System.out.println("[ReserveCancelServlet] POST /receiver/reserve/cancel");
        boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))
                || "1".equals(req.getParameter("ajax"));
        System.out.println("[ReserveCancelServlet] isAjax=" + isAjax);

        HttpSession session = req.getSession(false);
        if (session == null) {
            // JP：未ログインならログインへ / CN：未登录跳转登录
            if (isAjax) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                System.out.println("[ReserveCancelServlet] no session");
                writeJson(resp, false, "not_authenticated");
            } else {
                resp.sendRedirect(req.getContextPath() + "/login");
            }
            return;
        }

        Integer receiverId = (Integer) session.getAttribute("receiverId");
        if (receiverId == null) {
            receiverId = (Integer) session.getAttribute("userId");
        }
        if (receiverId == null) {
            // JP：ユーザー情報なし / CN：无用户信息
            if (isAjax) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                System.out.println("[ReserveCancelServlet] no receiverId");
                writeJson(resp, false, "not_authenticated");
            } else {
                resp.sendRedirect(req.getContextPath() + "/login");
            }
            return;
        }

        String sRid = req.getParameter("reservationId");
        int rid = -1;
        try {
            rid = Integer.parseInt(sRid);
        } catch (Exception e) {
            // JP：不正パラメータ / CN：参数非法
            if (isAjax) {
                System.out.println("[ReserveCancelServlet] invalid reservationId=" + sRid);
                writeJson(resp, false, "system_error");
            } else {
                redirectWithError(resp, req.getContextPath(), "system_error");
            }
            return;
        }
        if (rid <= 0) {
            System.out.println("[ReserveCancelServlet] reservationId <= 0: " + rid);
            if (isAjax) {
                writeJson(resp, false, "system_error");
            } else {
                redirectWithError(resp, req.getContextPath(), "system_error");
            }
            return;
        }

        try {
            // JP：最小情報で事前チェック / CN：取消前的最小信息检查
            ReservationStatusInfo info = reservationDao.findReservationStatusInfo(rid, receiverId);
            if (info == null) {
                System.out.println("[ReserveCancelServlet] not found: rid=" + rid + ", receiverId=" + receiverId);
                if (isAjax) {
                    writeJson(resp, false, "not_found");
                } else {
                    redirectWithError(resp, req.getContextPath(), "not_found");
                }
                return;
            }

            if (!"RESERVED".equalsIgnoreCase(info.status)) {
                System.out.println("[ReserveCancelServlet] state_changed: status=" + info.status);
                if (isAjax) {
                    writeJson(resp, false, "state_changed");
                } else {
                    redirectWithError(resp, req.getContextPath(), "state_changed");
                }
                return;
            }

            if (info.pickupTime == null) {
                // JP：受取時間未設定はキャンセル不可 / CN：受取时间未设置不可取消
                System.out.println("[ReserveCancelServlet] pickupTime null");
                if (isAjax) {
                    writeJson(resp, false, "pickup_expired");
                } else {
                    redirectWithError(resp, req.getContextPath(), "pickup_expired");
                }
                return;
            }

            // JP：受取時間を過ぎたらキャンセル不可 / CN：超过受取时间不可取消
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (!info.pickupTime.after(now)) {
                System.out.println("[ReserveCancelServlet] pickup expired: pickupTime=" + info.pickupTime + ", now=" + now);
                if (isAjax) {
                    writeJson(resp, false, "pickup_expired");
                } else {
                    redirectWithError(resp, req.getContextPath(), "pickup_expired");
                }
                return;
            }

            boolean ok = reservationDao.cancelReservation(rid, receiverId);
            if (ok) {
                System.out.println("[ReserveCancelServlet] cancel success: rid=" + rid);
                if (isAjax) {
                    writeJson(resp, true, "cancel_success");
                } else {
                    redirectWithMsg(resp, req.getContextPath(), "cancel_success");
                }
            } else {
                // JP：同時更新など / CN：并发导致状态已变
                System.out.println("[ReserveCancelServlet] cancel failed: rid=" + rid);
                if (isAjax) {
                    writeJson(resp, false, "state_changed");
                } else {
                    redirectWithError(resp, req.getContextPath(), "state_changed");
                }
            }
        } catch (Exception ex) {
            // JP: サーバーログで原因確認 / CN: 用服务器日志定位原因
            ex.printStackTrace();
            System.out.println("[ReserveCancelServlet] system_error: rid=" + rid + ", receiverId=" + receiverId);
            if (isAjax) {
                writeJson(resp, false, "system_error");
            } else {
                redirectWithError(resp, req.getContextPath(), "system_error");
            }
        }
    }

    // JP：成功メッセージ付きで履歴へ戻る / CN：带成功提示返回履历页
    private void redirectWithMsg(HttpServletResponse resp, String ctx, String msg) throws IOException {
        resp.sendRedirect(ctx + "/receiver/history?msg=" + msg);
    }

    // JP：エラーメッセージ付きで履歴へ戻る / CN：带错误提示返回履历页
    private void redirectWithError(HttpServletResponse resp, String ctx, String error) throws IOException {
        resp.sendRedirect(ctx + "/receiver/history?error=" + error);
    }

    // JP：Ajax用JSON応答 / CN：Ajax JSON 响应
    private void writeJson(HttpServletResponse resp, boolean success, String code) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print("{\"success\":" + success + ",\"message\":\"" + code + "\"}");
        }
    }
}

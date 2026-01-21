package com.foodlink.listener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.foodlink.dao.ReservationDao;

/**
 * JP：予約期限切れの自動更新スケジューラ
 * CN：预约过期自动更新调度器
 */
@WebListener
public class ReservationExpireScheduler implements ServletContextListener {

    // JP：デフォルト設定 / CN：默认配置
    private static final int DEFAULT_GRACE_MINUTES = 15;
    private static final int DEFAULT_INTERVAL_MINUTES = 10;

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        // JP：context-param から読み込み（未設定ならデフォルト）
        // CN：从 context-param 读取（未设置则用默认）
        int graceMinutes = parseInt(ctx.getInitParameter("expire.graceMinutes"), DEFAULT_GRACE_MINUTES);
        int intervalMinutes = parseInt(ctx.getInitParameter("expire.intervalMinutes"), DEFAULT_INTERVAL_MINUTES);

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                ReservationDao dao = new ReservationDao();
                int updated = dao.expireOverdueReservations(graceMinutes);
                // JP：ログは必要なら追加 / CN：如需日志可在此补充
            } catch (Exception e) {
                // JP：例外はログに残す / CN：异常记录到日志
                e.printStackTrace();
            }
        }, 0, intervalMinutes, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // JP：スケジューラ停止（スレッドリーク防止） / CN：停止调度器防止线程泄漏
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private int parseInt(String value, int def) {
        if (value == null) {
            return def;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return def;
        }
    }
}

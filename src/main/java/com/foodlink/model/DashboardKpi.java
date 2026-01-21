package com.foodlink.model;

/**
 * JP：商家ダッシュボードのKPI表示用DTO
 * CN：商家端仪表盘KPI展示用DTO
 */
public class DashboardKpi {
    private int todayFoodsCount;
    private int yesterdayFoodsCount;
    private int openFoodsCount;
    private int todayReservationsCount;
    private int yesterdayReservationsCount;
    private int todaySalesAmount;
    private int yesterdaySalesAmount;

    public int getTodayFoodsCount() {
        return todayFoodsCount;
    }

    public void setTodayFoodsCount(int todayFoodsCount) {
        this.todayFoodsCount = todayFoodsCount;
    }

    public int getYesterdayFoodsCount() {
        return yesterdayFoodsCount;
    }

    public void setYesterdayFoodsCount(int yesterdayFoodsCount) {
        this.yesterdayFoodsCount = yesterdayFoodsCount;
    }

    public int getOpenFoodsCount() {
        return openFoodsCount;
    }

    public void setOpenFoodsCount(int openFoodsCount) {
        this.openFoodsCount = openFoodsCount;
    }

    public int getTodayReservationsCount() {
        return todayReservationsCount;
    }

    public void setTodayReservationsCount(int todayReservationsCount) {
        this.todayReservationsCount = todayReservationsCount;
    }

    public int getYesterdayReservationsCount() {
        return yesterdayReservationsCount;
    }

    public void setYesterdayReservationsCount(int yesterdayReservationsCount) {
        this.yesterdayReservationsCount = yesterdayReservationsCount;
    }

    public int getTodaySalesAmount() {
        return todaySalesAmount;
    }

    public void setTodaySalesAmount(int todaySalesAmount) {
        this.todaySalesAmount = todaySalesAmount;
    }

    public int getYesterdaySalesAmount() {
        return yesterdaySalesAmount;
    }

    public void setYesterdaySalesAmount(int yesterdaySalesAmount) {
        this.yesterdaySalesAmount = yesterdaySalesAmount;
    }
}

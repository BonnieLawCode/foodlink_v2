package com.foodlink.model;

/**
 * JP：ダッシュボード用の週次推移DTO
 * CN：仪表盘周趋势DTO
 */
public class DashboardTrendPoint {
    private String dayLabel;
    private int orderCount;
    private int salesAmount;

    public String getDayLabel() {
        return dayLabel;
    }

    public void setDayLabel(String dayLabel) {
        this.dayLabel = dayLabel;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public int getSalesAmount() {
        return salesAmount;
    }

    public void setSalesAmount(int salesAmount) {
        this.salesAmount = salesAmount;
    }
}

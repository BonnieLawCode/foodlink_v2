package com.foodlink.model;

/**
 * JP：ダッシュボード警告（Alert）用DTO
 * CN：仪表盘警告（Alert）展示用DTO
 */
public class DashboardAlertItem {
    private int reservationId;
    private String reservationCode;
    private String foodName;
    private int quantity;
    private java.sql.Timestamp reserveAt;

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservationCode() {
        return reservationCode;
    }

    public void setReservationCode(String reservationCode) {
        this.reservationCode = reservationCode;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public java.sql.Timestamp getReserveAt() {
        return reserveAt;
    }

    public void setReserveAt(java.sql.Timestamp reserveAt) {
        this.reserveAt = reserveAt;
    }
}

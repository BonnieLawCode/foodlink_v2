package com.foodlink.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * food 表的数据模型（Model）
 * 用于 DAO 查询结果 → JSP 展示
 */
public class Food {
	private int id;
    private int providerId;
    private String name;
    private String description;
    private String category;
    private int quantity;
    private String unit;
    private Integer priceNormal;     // 可为 null
    private int priceOffer;
    private String currency;
    private Date expiryDate;         // 可为 null
    private String pickupLocation;
    private Timestamp pickupStart;   // 可为 null
    private Timestamp pickupEnd;     // 可为 null
    private String imagePath;        // 可为 null
    private String status;
    private Timestamp createdAt;

    // ====== Getter / Setter（省略业务逻辑，只做数据承载）======
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProviderId() { return providerId; }
    public void setProviderId(int providerId) { this.providerId = providerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Integer getPriceNormal() { return priceNormal; }
    public void setPriceNormal(Integer priceNormal) { this.priceNormal = priceNormal; }

    public int getPriceOffer() { return priceOffer; }
    public void setPriceOffer(int priceOffer) { this.priceOffer = priceOffer; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public Timestamp getPickupStart() { return pickupStart; }
    public void setPickupStart(Timestamp pickupStart) { this.pickupStart = pickupStart; }

    public Timestamp getPickupEnd() { return pickupEnd; }
    public void setPickupEnd(Timestamp pickupEnd) { this.pickupEnd = pickupEnd; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

}

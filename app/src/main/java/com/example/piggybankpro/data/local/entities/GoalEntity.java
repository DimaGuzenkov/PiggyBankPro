package com.example.piggybankpro.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.piggybankpro.data.local.converters.DateConverter;

import java.util.Date;
import java.util.UUID;

@Entity(
        tableName = "goals",
        foreignKeys = @ForeignKey(
                entity = GoalEntity.class,
                parentColumns = "id",
                childColumns = "parent_id",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = {"parent_id"}),
                @Index(value = {"target_date"})
        }
)
@TypeConverters(DateConverter.class)
public class GoalEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id = UUID.randomUUID().toString();

    @ColumnInfo(name = "title", defaultValue = "Новая цель")
//    @NonNull
    private String title = "Новая цель";

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "target_amount")
    private Double targetAmount;

    @ColumnInfo(name = "current_amount", defaultValue = "0.0")
    @NonNull
    private Double currentAmount = 0.0;

    @ColumnInfo(name = "calculated_amount", defaultValue = "0.0")
    @NonNull
    private Double calculatedAmount = 0.0;

    @ColumnInfo(name = "currency", defaultValue = "RUB")
    private String currency = "RUB";

    @ColumnInfo(name = "target_date")
    private Date targetDate;

    @ColumnInfo(name = "created_at")
    private Date createdAt = new Date();

    @ColumnInfo(name = "updated_at")
    private Date updatedAt = new Date();

    @ColumnInfo(name = "color")
    private Integer color;

    @ColumnInfo(name = "goal_url")
    private String goalUrl;

    @ColumnInfo(name = "parent_id")
    private String parentId;

    @ColumnInfo(name = "order_position", defaultValue = "0")
    private Integer orderPosition = 0;

    @ColumnInfo(name = "is_completed", defaultValue = "0")
    private Boolean isCompleted = false;

    @ColumnInfo(name = "completed_date")
    private Date completedDate;

    public Date getCompletedDate2() {
        return completedDate2;
    }

    public void setCompletedDate2(Date completedDate2) {
        this.completedDate2 = completedDate2;
    }

    @ColumnInfo(name = "completed_date2")
    private Date completedDate2;

    public GoalEntity() {
    }

    @Ignore
    public GoalEntity(@NonNull String title) {
        this.title = title;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @Ignore
    public GoalEntity(@NonNull String title, Double targetAmount, Date targetDate) {
        this.title = title;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = new Date();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = new Date();
    }

    public Double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(Double targetAmount) {
        this.targetAmount = targetAmount;
        this.updatedAt = new Date();
    }

    @NonNull
    public Double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(Double currentAmount) {
        this.currentAmount = currentAmount;
        this.updatedAt = new Date();

        if (targetAmount != null && currentAmount >= targetAmount) {
            this.isCompleted = true;
            this.completedDate = new Date();
        } else if (this.isCompleted) {
            this.isCompleted = false;
            this.completedDate = null;
        }
    }

    @NonNull
    public Double getCalculatedAmount() {
        return calculatedAmount;
    }

    public void setCalculatedAmount(@NonNull Double calculatedAmount) {
        this.calculatedAmount = calculatedAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
        this.updatedAt = new Date();
    }

    public Date getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(Date targetDate) {
        this.targetDate = targetDate;
        this.updatedAt = new Date();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
        this.updatedAt = new Date();
    }

    public String getGoalUrl() {
        return goalUrl;
    }

    public void setGoalUrl(String goalUrl) {
        this.goalUrl = goalUrl;
        this.updatedAt = new Date();
    }

//    @NonNull
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
        this.updatedAt = new Date();
    }

    public Integer getOrderPosition() {
        return orderPosition;
    }

    public void setOrderPosition(Integer orderPosition) {
//        if (orderPosition < 0) {
//            throw new IllegalArgumentException("Order position should be grater or equals zero");
//        }
        this.orderPosition = Math.max(orderPosition, 0);
        this.updatedAt = new Date();
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean completed) {
        isCompleted = completed;
        this.updatedAt = new Date();
        if (completed) {
            this.completedDate = new Date();
        } else {
            this.completedDate = null;
        }
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
        this.updatedAt = new Date();
    }

    public Double getProgressPercentage() {
        if (targetAmount == null || targetAmount == 0) {
            return null;
        }
        return (currentAmount / targetAmount) * 100;
    }

    public Long getDaysRemaining() {
        if (targetDate == null) {
            return null;
        }
        long diff = targetDate.getTime() - new Date().getTime();
        if (diff < 0) {
            return 0L;
        }

        return diff / (1000 * 60 * 60 * 24);
    }

    public Double getAmountNeeded() {
        if (targetAmount == null) {
            return null;
        }
        return Math.max(0, targetAmount - currentAmount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoalEntity that = (GoalEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
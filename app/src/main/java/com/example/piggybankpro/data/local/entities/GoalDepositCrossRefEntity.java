package com.example.piggybankpro.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.piggybankpro.data.local.converters.IdConverter;

@Entity(
        tableName = "goal_deposit_cross_ref",
        primaryKeys = {"goal_id", "auto_deposit_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = GoalEntity.class,
                        parentColumns = "id",
                        childColumns = "goal_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = AutoDepositEntity.class,
                        parentColumns = "id",
                        childColumns = "auto_deposit_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = {"goal_id"}),
                @Index(value = {"auto_deposit_id"}),
                @Index(value = {"goal_id", "auto_deposit_id"}, unique = true)
        }
)
@TypeConverters(IdConverter.class)
public class GoalDepositCrossRefEntity {
    @ColumnInfo(name = "goal_id")
    @NonNull
    private Id goalId = new Id();

    @ColumnInfo(name = "goal_title")
    @NonNull
    private String goalTitle = "";

    @ColumnInfo(name = "auto_deposit_id")
    @NonNull
    private Id autoDepositId = new Id();

    @ColumnInfo(name = "amount")
    private Double amount;

    @ColumnInfo(name = "created_at")
    private Long createdAt = System.currentTimeMillis();

    @ColumnInfo(name = "updated_at")
    private Long updatedAt = System.currentTimeMillis();

    public GoalDepositCrossRefEntity() {
    }

    @Ignore
    public GoalDepositCrossRefEntity(@NonNull Id goalId, @NonNull String goalTitle, @NonNull Id autoDepositId) {
        this.goalId = goalId;
        this.goalTitle = goalTitle;
        this.autoDepositId = autoDepositId;
    }

    @NonNull
    public Id getGoalId() {
        return goalId;
    }

    public void setGoalId(Id goalId) {
        this.goalId = goalId;
        this.updatedAt = System.currentTimeMillis();
    }

    @NonNull
    public String getGoalTitle() {
        return goalTitle;
    }

    public void setGoalTitle(@NonNull String goalTitle) {
        this.goalTitle = goalTitle;
    }

    @NonNull
    public Id getAutoDepositId() {
        return autoDepositId;
    }

    public void setAutoDepositId(Id autoDepositId) {
        this.autoDepositId = autoDepositId;
        this.updatedAt = System.currentTimeMillis();
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
        this.updatedAt = System.currentTimeMillis();
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GoalDepositCrossRefEntity that = (GoalDepositCrossRefEntity) o;

        if (!goalId.equals(that.goalId)) return false;
        return autoDepositId.equals(that.autoDepositId);
    }

    @Override
    public int hashCode() {
        int result = goalId.hashCode();
        result = 31 * result + autoDepositId.hashCode();
        return result;
    }
}
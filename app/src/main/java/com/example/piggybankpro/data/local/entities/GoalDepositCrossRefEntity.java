package com.example.piggybankpro.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

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
public class GoalDepositCrossRefEntity {
    @ColumnInfo(name = "goal_id")
    @NonNull
    private String goalId;

    @ColumnInfo(name = "goal_title")
    @NonNull
    private String goalTitle;

    @ColumnInfo(name = "auto_deposit_id")
    @NonNull
    private String autoDepositId;

    @ColumnInfo(name = "amount")
    private Double amount;

    @ColumnInfo(name = "created_at")
    private Long createdAt = System.currentTimeMillis();

    @ColumnInfo(name = "updated_at")
    private Long updatedAt = System.currentTimeMillis();

    public GoalDepositCrossRefEntity() {
        goalId = "";
        goalTitle = "";
        autoDepositId = "";
    }

    @Ignore
    public GoalDepositCrossRefEntity(@NonNull String goalId, @NonNull String goalTitle, @NonNull String autoDepositId) {
        this.goalId = goalId;
        this.goalTitle = goalTitle;
        this.autoDepositId = autoDepositId;
    }

    @NonNull
    public String getGoalId() {
        return goalId;
    }

    public void setGoalId(String goalId) {
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
    public String getAutoDepositId() {
        return autoDepositId;
    }

    public void setAutoDepositId(String autoDepositId) {
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
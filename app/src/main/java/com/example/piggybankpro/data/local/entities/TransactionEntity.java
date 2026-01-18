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
        tableName = "transactions",
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
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {
                @Index(value = {"goal_id"}),
                @Index(value = {"auto_deposit_id"}),
                @Index(value = {"transaction_date"}),
                @Index(value = {"transaction_type"}),
        }
)
@TypeConverters(DateConverter.class)
public class TransactionEntity {

    public static final int TYPE_DEPOSIT = 1;
    public static final int TYPE_WITHDRAWAL = 2;
    public static final int TYPE_TRANSFER = 3;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id = UUID.randomUUID().toString();

    @ColumnInfo(name = "goal_id")
    private String goalId;

    @ColumnInfo(name = "transfer_goal_name")
    private String transferGoalName = null;

    @ColumnInfo(name = "auto_deposit_id")
    private String autoDepositId;

    @ColumnInfo(name = "amount")
    private Double amount;

    @ColumnInfo(name = "currency", defaultValue = "RUB")
    private String currency = "RUB";

    @ColumnInfo(name = "transaction_type", defaultValue = "1")
    private Integer transactionType = TYPE_DEPOSIT;

    @ColumnInfo(name = "transaction_date")
    private Date transactionDate = new Date();

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "created_at")
    private Date createdAt = new Date();

    @ColumnInfo(name = "updated_at")
    private Date updatedAt = new Date();

    public TransactionEntity() {
    }

    @Ignore
    public TransactionEntity(String goalId, Double amount, String description, Integer transactionType) {
        this.goalId = goalId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description != null && !description.isEmpty() ? description : getTransactionTypeString();
        this.transactionDate = new Date();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @Ignore
    public TransactionEntity(String goalId, String transferGoalName, Double amount, String description, Integer transactionType) {
        this.goalId = goalId;
        this.transferGoalName = transferGoalName;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description != null && !description.isEmpty() ? description : getTransactionTypeString();
        this.transactionDate = new Date();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @Ignore
    public TransactionEntity(String goalId, String autoDepositId, String autoDepositName, Double amount) {
        this.goalId = goalId;
        this.autoDepositId = autoDepositId;
        this.amount = amount;
        this.transactionType = TYPE_DEPOSIT;
        this.description = "Автопополнение " + autoDepositName;
        this.transactionDate = new Date();
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

    public String getGoalId() {
        return goalId;
    }

    public void setGoalId(String goalId) {
        this.goalId = goalId;
        this.updatedAt = new Date();
    }

    public String getAutoDepositId() {
        return autoDepositId;
    }

    public void setAutoDepositId(String autoDepositId) {
        this.autoDepositId = autoDepositId;
        this.updatedAt = new Date();
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
        this.updatedAt = new Date();
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
        this.updatedAt = new Date();
    }

    public Integer getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(Integer transactionType) {
        this.transactionType = transactionType;
        this.updatedAt = new Date();
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
        this.updatedAt = new Date();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getTransactionTypeString() {
        switch (transactionType) {
            case TYPE_DEPOSIT: return "Пополнение";
            case TYPE_WITHDRAWAL: return "Списание";
            case TYPE_TRANSFER: return getTransferString();
            default: return "Неизвестно";
        }
    }

    private String getTransferString() {
        if (transferGoalName == null) {
            return "Перевод";
        }
        if (amount > 0) {
            return "Перевод из " + transferGoalName;
        }
        return "Перевод в " + transferGoalName;
    }

    @NonNull
    @Override
    public String toString() {
        return "TransactionEntity{" +
                "id='" + id + '\'' +
                ", goalId='" + goalId + '\'' +
                ", amount=" + amount +
                ", type=" + getTransactionTypeString() +
                ", date=" + transactionDate +
                '}';
    }

    public String getTransferGoalName() {
        return transferGoalName;
    }

    public void setTransferGoalName(String transferGoalName) {
        this.transferGoalName = transferGoalName;
    }
}
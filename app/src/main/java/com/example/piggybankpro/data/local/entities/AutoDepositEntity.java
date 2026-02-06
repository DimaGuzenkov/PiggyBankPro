package com.example.piggybankpro.data.local.entities;

import static com.example.piggybankpro.presentation.utils.DateUtils.*;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.piggybankpro.data.local.converters.DateConverter;
import com.example.piggybankpro.data.local.converters.IdConverter;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Entity(
        tableName = "auto_deposits",
        indices = {
                @Index(value = {"is_active"}),
                @Index(value = {"next_execution_date"})
        }
)
@TypeConverters({DateConverter.class, IdConverter.class})
public class AutoDepositEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private Id id = new Id();

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "amount")
    private Double amount;

    @ColumnInfo(name = "period_type", defaultValue = "4")
    private Integer periodType = PERIOD_MONTHLY;

    @ColumnInfo(name = "start_date")
    private Date startDate = new Date();

    @ColumnInfo(name = "next_execution_date")
    private Date nextExecutionDate;

    @ColumnInfo(name = "is_active", defaultValue = "1")
    private Boolean isActive = true;

    @ColumnInfo(name = "created_at")
    private Date createdAt = new Date();

    @ColumnInfo(name = "updated_at")
    private Date updatedAt = new Date();

    public AutoDepositEntity() {
        calculateNextExecution();
    }

    @Ignore
    public AutoDepositEntity(String name, Double amount, Integer periodType) {
        this.name = name;
        this.amount = amount;
        this.periodType = periodType;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        calculateNextExecution();
    }

    @NonNull
    public Id getId() {
        return id;
    }

    public void setId(@NonNull Id id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = new Date();
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
        this.updatedAt = new Date();
    }

    public Integer getPeriodType() {
        return periodType;
    }

    public void setPeriodType(Integer periodType) {
        this.periodType = periodType;
        this.updatedAt = new Date();
        calculateNextExecution();
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
        this.updatedAt = new Date();
        calculateNextExecution();
    }

    public Date getNextExecutionDate() {
        return nextExecutionDate;
    }

    public void setNextExecutionDate(Date nextExecutionDate) {
        this.nextExecutionDate = nextExecutionDate;
        this.updatedAt = new Date();
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
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

    public void calculateNextExecution() {
        if (startDate == null) {
            startDate = new Date();
        }

        Date now = new Date();
        if (nextExecutionDate != null && nextExecutionDate.after(now)) {
            return;
        }

        Date baseDate = nextExecutionDate != null ? nextExecutionDate : startDate;

        nextExecutionDate = calculateNextDate(baseDate, periodType);
    }


}
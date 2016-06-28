/*
 * Stallion Core: A Modern Web Framework
 *
 * Copyright (C) 2015 - 2016 Stallion Software LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 2 of
 * the License, or (at your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
 * License for more details. You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl-2.0.html>.
 *
 *
 *
 */

package io.stallion.tests.sql;

import io.stallion.dataAccess.ModelBase;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="stallion_test_payment")
public class Payment extends ModelBase {
    private Long date;
    private String accountId;
    private int amount;
    private boolean onTime;
    private String payee;
    private String memo;

    @Column
    public Long getDate() {
        return date;
    }

    public Payment setDate(Long date) {
        this.date = date;
        return this;
    }

    @Column
    public String getAccountId() {
        return accountId;
    }

    public Payment setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    @Column
    public int getAmount() {
        return amount;
    }

    public Payment setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    @Column
    public boolean isOnTime() {
        return onTime;
    }

    public Payment setOnTime(boolean onTime) {
        this.onTime = onTime;
        return this;
    }

    @Column
    public String getPayee() {
        return payee;
    }

    public Payment setPayee(String payee) {
        this.payee = payee;
        return this;
    }

    @Column
    public String getMemo() {
        return memo;
    }

    public Payment setMemo(String memo) {
        this.memo = memo;
        return this;
    }
}

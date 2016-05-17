/*
 * Stallion: A Modern Content Management System
 *
 * Copyright (C) 2015 - 2016 Patrick Fitzsimmons.
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

package io.stallion.tests.mysql;

import io.stallion.dataAccess.ModelBase;

import javax.persistence.Column;
import javax.persistence.Table;


@Table(name="stallion_test_house")
public class House extends ModelBase {

    private int buildYear = 0;
    private String address = "";
    private String postalCode = "";
    private int squareFeet = 0;
    private boolean vacant = false;
    private boolean taxesPaid = true;
    private boolean condemned = false;

    @Column
    public int getBuildYear() {
        return buildYear;
    }

    public House setBuildYear(int buildYear) {
        this.buildYear = buildYear;
        return this;
    }

    @Column
    public String getAddress() {
        return address;
    }

    public House setAddress(String address) {
        this.address = address;
        return this;
    }

    @Column
    public String getPostalCode() {
        return postalCode;
    }

    public House setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    @Column
    public int getSquareFeet() {
        return squareFeet;
    }

    public House setSquareFeet(int squareFeet) {
        this.squareFeet = squareFeet;
        return this;
    }

    @Column
    public boolean isVacant() {
        return vacant;
    }

    public House setVacant(boolean vacant) {
        this.vacant = vacant;
        return this;
    }

    @Column
    public boolean isTaxesPaid() {
        return taxesPaid;
    }

    public House setTaxesPaid(boolean taxesPaid) {
        this.taxesPaid = taxesPaid;
        return this;
    }

    @Column
    public boolean isCondemned() {
        return condemned;
    }

    public House setCondemned(boolean condemned) {
        this.condemned = condemned;
        return this;
    }
}

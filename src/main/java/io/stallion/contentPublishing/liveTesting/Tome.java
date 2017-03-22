/*
 * Copyright (c) 2015-2016 Stallion Software LLC
 *
 * This file is part of Stallion Publisher.
 *
 * Stallion Publisher is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International license.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International license
 * along with this program. If not, see <https://creativecommons.org/licenses/by-nc-sa/4.0/>.
 */

package io.stallion.contentPublishing.liveTesting;

import io.stallion.dataAccess.ModelBase;

import javax.persistence.Column;
import java.time.ZonedDateTime;


public class Tome extends ModelBase {
    private String title;
    private String author;
    private ZonedDateTime publishedAt;
    private float price;
    private float bulkPrice;
    private int numberSold;
    private ZonedDateTime updatedAt;
    private ZonedDateTime lastSoldAt;
    private TomeStatus status;


    @Column
    public String getTitle() {
        return title;
    }

    public Tome setTitle(String title) {
        this.title = title;
        return this;
    }

    @Column
    public String getAuthor() {
        return author;
    }

    public Tome setAuthor(String author) {
        this.author = author;
        return this;
    }

    @Column
    public ZonedDateTime getPublishedAt() {
        return publishedAt;
    }

    public Tome setPublishedAt(ZonedDateTime publishedAt) {
        this.publishedAt = publishedAt;
        return this;
    }

    @Column
    public float getPrice() {
        return price;
    }

    public Tome setPrice(float price) {
        this.price = price;
        return this;
    }

    @Column
    public float getBulkPrice() {
        return bulkPrice;
    }

    public Tome setBulkPrice(float bulkPrice) {
        this.bulkPrice = bulkPrice;
        return this;
    }

    @Column
    public int getNumberSold() {
        return numberSold;
    }

    public Tome setNumberSold(int numberSold) {
        this.numberSold = numberSold;
        return this;
    }

    @Column
    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Tome setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    @Column
    public ZonedDateTime getLastSoldAt() {
        return lastSoldAt;
    }

    public Tome setLastSoldAt(ZonedDateTime lastSoldAt) {
        this.lastSoldAt = lastSoldAt;
        return this;
    }

    @Column
    public TomeStatus getStatus() {
        return status;
    }

    public Tome setStatus(TomeStatus status) {
        this.status = status;
        return this;
    }
}

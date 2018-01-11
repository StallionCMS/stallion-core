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

package io.stallion.dataAccess.filtering;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.list;
import static io.stallion.utils.Literals.map;

/**
 * A class holding the paged results from a filter operation.
 *
 * @param <T>
 */
public class Pager<T> {
    private List<T> currentItems = new ArrayList<T>();
    private int pageCount = 0;
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private boolean hasNextPage = false;
    private boolean hasPreviousPage = false;
    private int nextPageNumber = 2;
    private int previousPageNumber = 0;
    private Map<String, Double> averages = map();
    private Map<String, Double> sums = map();




    /**
     * All the returned items.
     *
     * @return
     */
    public List<T> getItems() {
        return getCurrentItems();
    }

    /**
     * Alias for getItems()
     * @return
     */
    @JsonIgnore
    public List<T> getCurrentItems() {
        return currentItems;
    }



    /**
     * The total available pages of items matching the filterchain
     * @return
     */
    public int getPageCount() {
        return pageCount;
    }



    /**
     * The current page number of data, starting at 1
     * @return
     */
    public int getCurrentPage() {
        return currentPage;
    }



    /**
     * The number of items per page
     *
     * @return
     */
    public int getItemsPerPage() {
        return itemsPerPage;
    }



    /**
     * True if there is a next page of data.
     * @return
     */
    public boolean isHasNextPage() {
        return hasNextPage;
    }

    /**
     * True if there is a previous page of data (false if the page number is 1)
     * @return
     */
    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }


    /**
     * The page number for the next page
     * @return
     */
    public int getNextPageNumber() {
        return nextPageNumber;
    }


    /**
     * The page number for the previous page.
     * @return
     */
    public int getPreviousPageNumber() {
        return previousPageNumber;
    }

    public Pager setCurrentItems(List<T> currentItems) {
        this.currentItems = currentItems;
        return this;
    }

    public Pager setPageCount(int pageCount) {
        this.pageCount = pageCount;
        return this;
    }

    public Pager setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        return this;
    }

    public Pager setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
        return this;
    }

    public Pager setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
        return this;
    }

    public Pager setHasPreviousPage(boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
        return this;
    }

    public Pager setNextPageNumber(int nextPageNumber) {
        this.nextPageNumber = nextPageNumber;
        return this;
    }

    public Pager setPreviousPageNumber(int previousPageNumber) {
        this.previousPageNumber = previousPageNumber;
        return this;
    }

    public List<Integer> getSurroundingPages() {
        List<Integer> pages = list();
        int start = currentPage - 4;
        if (start < 1) {
            start = 1;
        }
        int end = start + 7;
        if (end > pageCount) {
            end = pageCount;
        }
        for(int x = start; x <= end; x++) {
            pages.add(x);
        }
        return pages;
    }

    public Map<String, Double> getAverages() {
        return averages;
    }

    public Pager setAverages(Map<String, Double> averages) {
        this.averages = averages;
        return this;
    }

    public Map<String, Double> getSums() {
        return sums;
    }

    public Pager setSums(Map<String, Double> sums) {
        this.sums = sums;
        return this;
    }


    // surrounding(2)
    // firstSurroundingAndLast(1)
    // PageLink(isElippisis, pageNumber, isFirst, isLast, isCurrent, cssClass)
    // getPageLinks().setIncludeElippis(false).setSurrounding(3).includeFirst(true).includeLast(false).setIsCurrentClass('current-page')
}

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

package io.stallion.dal.filtering;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

import static io.stallion.utils.Literals.list;

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

    void setCurrentItems(List<T> currentItems) {
        this.currentItems = currentItems;
    }

    /**
     * The total available pages of items matching the filterchain
     * @return
     */
    public int getPageCount() {
        return pageCount;
    }

    void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    /**
     * The current page number of data, starting at 1
     * @return
     */
    public int getCurrentPage() {
        return currentPage;
    }

    void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * The number of items per page
     *
     * @return
     */
    public int getItemsPerPage() {
        return itemsPerPage;
    }

    void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    /**
     * True if there is a next page of data.
     * @return
     */
    public boolean isHasNextPage() {
        return hasNextPage;
    }

    void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    /**
     * True if there is a previous page of data (false if the page number is 1)
     * @return
     */
    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }

    void setHasPreviousPage(boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }

    /**
     * The page number for the next page
     * @return
     */
    public int getNextPageNumber() {
        return nextPageNumber;
    }

    void setNextPageNumber(int nextPageNumber) {
        this.nextPageNumber = nextPageNumber;
    }

    /**
     * The page number for the previous page.
     * @return
     */
    public int getPreviousPageNumber() {
        return previousPageNumber;
    }

    void setPreviousPageNumber(int previousPageNumber) {
        this.previousPageNumber = previousPageNumber;
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



    // surrounding(2)
    // firstSurroundingAndLast(1)
    // PageLink(isElippisis, pageNumber, isFirst, isLast, isCurrent, cssClass)
    // getPageLinks().setIncludeElippis(false).setSurrounding(3).includeFirst(true).includeLast(false).setIsCurrentClass('current-page')
}

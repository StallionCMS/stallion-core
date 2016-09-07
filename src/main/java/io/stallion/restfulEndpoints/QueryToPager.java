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

package io.stallion.restfulEndpoints;

import java.util.*;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.Model;
import io.stallion.dataAccess.ModelController;
import io.stallion.dataAccess.filtering.*;
import io.stallion.requests.IRequest;
import io.stallion.services.Log;
import io.stallion.utils.json.JSON;
import org.apache.commons.lang3.StringUtils;


public class QueryToPager<T extends Model> {
    /*
       .filter()
    .exclude()
    .allSortable()
    .allFilters()
    .allowedFilters()
    .allowedSortable()
    .searchFields()
    .pageSize()
    .pager()
    .chain();

     */
    private FilterChain<T> chain;
    private ModelController<T> controller;
    private IRequest request;
    private List<String> _allowedFilters = list();
    private List<String> _allowedSortable = list();
    private List<String> _searchFields = list();
    private boolean _allFilters = false;
    private boolean _allSorts = false;
    private Integer _pageSize = 50;
    private boolean _requestProcessed = false;
    private String defaultSort = "";





    public QueryToPager(IRequest request, ModelController<T> controller) {
        this(request, controller, controller.filterChain());
    }

    public QueryToPager(IRequest request, ModelController<T> controller, FilterChain<T> chain) {
        this.request = request;
        this.controller = controller;
        this.chain = chain;
    }

    public QueryToPager<T> filter(String name, Object value, FilterOperator op) {
        this.chain = chain.filterBy(name, value, op);
        return this;
    }

    public QueryToPager<T> exclude(String name, Object value, FilterOperator op) {
        this.chain = chain.excludeBy(name, value, op);
        return this;
    }

    public QueryToPager<T> allowedFilters(String ...fields) {
        this._allowedFilters = asList(fields);
        return this;
    }

    public QueryToPager<T> allowedSortable(String...fields) {
        this._allowedSortable = asList(fields);
        return this;
    }

    public QueryToPager<T> searchFields(String...fields) {
        this._searchFields = asList(fields);
        return this;
    }





    public QueryToPager<T> andAnyOf(Or...ors) {
        this.chain = chain.andAnyOf(ors);
        return this;
    }

    public QueryToPager<T> allSortable() {
        this._allSorts= true;
        return this;
    }

    public QueryToPager<T> allFilters() {
        this._allFilters = true;
        return this;
    }

    public QueryToPager<T> pageSize(Integer pageSize) {
        this._pageSize = pageSize;
        return this;
    }


    public FilterChain<T> chain() {
        if (!_requestProcessed) {
            process();
            this._requestProcessed = true;
        }
        return this.chain;
    }

    public Pager<T> pager() {
        if (!_requestProcessed) {
            process();
            this._requestProcessed = true;
        }
        Integer page = 1;
        if (!empty(request.getParameter("page"))) {
            page = Integer.parseInt(request.getParameter("page"));
        }

        return this.chain.pager(page, _pageSize);

    }

    protected void process() {
        // ?filters=&search=&page=&sort=&filter_by...
        Map<String, String> params = this.request.getQueryParams();
        String search = params.getOrDefault("search", null);
        if (!empty(search)) {
            if (!empty(_searchFields)) {
                this.chain = chain.search(search, asArray(_searchFields, String.class));
            } else {
                Log.warn("Search included, but no search fields defined");
            }
        }
        String filters = params.getOrDefault("filters", null);
        if (!empty(filters)) {
            List<LinkedHashMap> filterObjects = JSON.parseList(filters);
            for (LinkedHashMap<String, Object> o: filterObjects) {
                String field = o.get("name").toString();
                if (_allFilters && !_allowedFilters.contains(field)) {
                    Log.warn("Filter not allowed: " + field);
                    continue;
                }

                Object value = o.get("value");
                if (value instanceof Collection) {
                    value = new ArrayList((Collection) value);
                }
                String operation = (String)o.getOrDefault("op", "=");
                this.chain = chain.filter(field, value, operation);
            }
        }
        String sort = or(params.getOrDefault("sort", null), defaultSort);
        if (!empty(sort)) {
            SortDirection dir = SortDirection.ASC;
            if (sort.startsWith("-")) {
                sort = sort.substring(1);
                dir = SortDirection.DESC;
            }
            if (_allSorts && !_allowedSortable.contains(sort)) {
                Log.warn("Sort not allowed: " + sort);
            } else {
                this.chain = chain.sortBy(sort, dir);
            }
        }

        for(String filter: request.getQueryParamAsList("filter_by")) {
            if (empty(filter) || !filter.contains(":")) {
                continue;
            }
            String[] parts = StringUtils.split(filter, ":", 3);
            String key = parts[0];
            if (_allFilters && !_allowedFilters.contains(key)) {
                Log.warn("Filter not allowed: " + key);
                continue;
            }
            String val;
            String op = "eq";
            if (parts.length > 2) {
                op = parts[1];
                val = parts[2];
            } else {
                val = parts[1];
            }
            this.chain = chain.filter(key, val, op);

        }



    }

    public QueryToPager setDefaultSort(String defaultSort) {
        this.defaultSort = defaultSort;
        return this;
    }
}

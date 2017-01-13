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

import io.stallion.dataAccess.Model;
import io.stallion.dataAccess.LocalMemoryStash;
import io.stallion.exceptions.UsageException;
import io.stallion.reflection.PropertyComparator;
import io.stallion.reflection.PropertyUtils;
import io.stallion.services.Log;
import io.stallion.utils.Literals;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;


import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

import static io.stallion.utils.Literals.asArray;
import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.list;

/**
 * A FilterChain is the default way by which data from a ModelController is actually queried
 * and accessed.
 *
 * You use it as follows:
 *
 * List&lt;Books&gt; books = MyBooksController.instance()
 *     .filter('published', true')
 *     .filter('author', 'Mark Twain')
 *     .exclude('publishDate.year', 2014)
 *     .sort('publishDate', 'desc')
 *     .all();
 *
 * You can chain together as many "filter" commands as you want. The chain is not actually executed
 * until you call one of the access methods: all(), first(), count(), groupBy()
 *
 *
 * @param <T>
 */
public class FilterChain<T extends Model> implements Iterable<T> {
    private List<T> originalObjects;
    private List<T> objects;
    protected ArrayList<FilterOperation> operations = new ArrayList<FilterOperation>();
    private String sortField = "";
    private SortDirection sortDirection;
    private String bucket;
    private String extraCacheKey = "";
    private boolean _includeDeleted = false;
    private Integer matchingCount = 0;
    private LocalMemoryStash<T> stash;

    public FilterChain(String bucket, LocalMemoryStash<T> stash) {
        this.setBucket(bucket);
        this.stash = stash;
        this.originalObjects = new ArrayList();
    }

    public FilterChain(String bucket, List<T> originalObjects, LocalMemoryStash<T> stash) {
        this.setBucket(bucket);
        this.stash = stash;
        this.originalObjects = (List<T>)originalObjects;
    }
    public FilterChain(String bucket, List<T> originalObjects, FilterOperation op, LocalMemoryStash<T> stash) {
        this.setBucket(bucket);
        this.stash = stash;
        this.originalObjects = originalObjects;
        operations.add(op);
    }

    public FilterChain(FilterChain chain, LocalMemoryStash<T> stash) {
        this.setBucket(chain.getBucket());
        this.stash = stash;
        this.operations = (ArrayList<FilterOperation>)chain.operations.clone();
        this.sortField = chain.sortField;
        this.sortDirection = chain.sortDirection;
        this.originalObjects = chain.originalObjects;
    }

    /**
     * Do a basic equality filter
     *
     * @param name
     * @param val
     * @return
     */
    public FilterChain<T> filter(String name, Object val) {
        return filter(name, val, "eq");
    }

    /**
     * Do a basic equality filter.
     *
     * @param name
     * @param value
     * @return
     */
    public FilterChain<T> filter(String name, Comparable value)  {
        return filter(name, value, "eq");
    }

    /**
     * Add a filter to the chain
     * @param name
     * @param value
     * @param op
     * @return
     */
    public FilterChain<T> filter(String name, Object value, String op) {
        return filterBy(name, value, FilterOperator.fromString(op));
    }

    public FilterChain<T> filter(String name, Comparable value, String op) {
        return filterBy(name, value, FilterOperator.fromString(op));
    }

    /**
     * Add a filter to the chain with a custom operator
     * @param name
     * @param value
     * @param op
     * @return
     */
    public FilterChain<T> filterBy(String name, Object value, FilterOperator op) {

        FilterOperation fop = new FilterOperation();
        fop.setFieldName(name);
        fop.setOperator(op);
        fop.setOriginalValue(value);
        if (value instanceof Iterable) {
            fop.setIterable(value);
        }
        return cloneChainAndAddOperation(fop);
    }


    /**
     * Add a filter to the chain with a custom operator
     * @param name
     * @param value
     * @param op
     * @return
     */
    public FilterChain<T> filterBy(String name, Comparable value, FilterOperator op) {
        FilterOperation fop = new FilterOperation();
        fop.setFieldName(name);
        fop.setOperator(op);
        fop.setOriginalValue(value);
        return cloneChainAndAddOperation(fop);
    }


    /**
     * Searches for @value in all @fields, using a case-insensitive
     * string contains search.
     *
     * @param value
     * @param value
     * @return
     */
    public FilterChain<T> search(String value, String...fields) {
        if (fields.length == 0) {
            throw new UsageException("You must include at least one field to search");
        }
        if (Literals.empty(value)) {
            throw new UsageException("value must be at least one character long");
        }

        List<Or> ors = list();
        for (String field: fields) {
            ors.add(new Or(field, value, FilterOperator.LIKE));
        }
        FilterChain<T> chain = andAnyOf(asArray(ors, Or.class));
        return chain;
    }


    /**
     * Excludes all matching items instead of including them.
     *
     * @param name
     * @param value
     * @return
     */
    public FilterChain<T> exclude(String name, Object value)  {
        return exclude(name, value, "eq");
    }


    /**
     * Excludes all matching items instead of including them.
     *
     * @param name
     * @param value
     * @param op
     * @return
     */
    public FilterChain<T> exclude(String name, Object value, String op) {
        return excludeBy(name, value, FilterOperator.fromString(op));
    }

    /**
     * Excludes all matching items instead of including them.
     *
     * @param name
     * @param value
     * @param op
     * @return
     */
    public FilterChain<T> excludeBy(String name, Object value, FilterOperator op)  {
        FilterOperation fop = new FilterOperation();
        fop.setFieldName(name);
        fop.setOperator(op);
        fop.setOriginalValue(value);
        fop.setIsExclude(true);
        return cloneChainAndAddOperation(fop);
    }

    public FilterChain<T> andAnyOf(Map<String, Object> allowedMatches) {
        List<Or> ors = list();
        for (Map.Entry<String, Object> allowed: allowedMatches.entrySet()) {
            ors.add(new Or(allowed.getKey(), allowed.getValue()));
        }
        return andAnyOf(asArray(ors, Or.class));
    }

    public FilterChain<T> andAnyOf(List<String>...tuples) {
        List<Or> ors = list();
        for (List<String> tuple: tuples) {
            Or or;
            if (tuple.size() == 2) {
                or = new Or(tuple.get(0), tuple.get(1));
            } else if (tuple.size() == 3) {
                or = new Or(tuple.get(0), tuple.get(1), tuple.get(2));
            } else {
                throw new UsageException("When calling andAnyOf, you must pass in a list of strings that either [field, value] or [field, value, op]");
            }
            ors.add(or);
        }
        return andAnyOf(asArray(ors, Or.class));
    }

    public FilterChain<T> andAnyOf(Or...ors) {
        if (ors.length == 0) {
            return this;
        }
        FilterOperation operation = new FilterOperation();
        operation.setOrOperation(true);
        operation.setOrSubOperations(list());
        for(Or or: ors) {
            FilterOperation subOp = new FilterOperation();
            subOp.setOriginalValue(or.getValue());
            subOp.setFieldName(or.getField());
            subOp.setOperator(or.getOp());
            operation.getOrSubOperations().add(subOp);
        }
        return cloneChainAndAddOperation(operation);
    }

    public FilterChain<T> excludeAnyOf(Or...ors) {
        if (ors.length == 0) {
            return this;
        }
        FilterOperation operation = new FilterOperation();
        operation.setOrOperation(true);
        operation.setOrSubOperations(list());
        operation.setIsExclude(true);
        for(Or or: ors) {
            FilterOperation subOp = new FilterOperation();
            subOp.setOriginalValue(or.getValue());
            subOp.setFieldName(or.getField());
            subOp.setOperator(or.getOp());
            operation.getOrSubOperations().add(subOp);
        }
        return cloneChainAndAddOperation(operation);
    }


    protected FilterChain<T> cloneChainAndAddOperation(FilterOperation operation) {
        FilterChain<T> chain = newCopy();
        chain.operations.add(operation);
        return chain;
    }

    /**
     * Create a copy this filter chain.
     *
     * @return
     */
    protected FilterChain<T> newCopy() {
        FilterChain<T> chain = new FilterChain<T>(this.getBucket(), originalObjects, stash);
        chain.operations = (ArrayList<FilterOperation>)operations.clone();
        chain.setIncludeDeleted(getIncludeDeleted());
        return chain;
    }

    /**
     * Count the objects matching this filter, grouped by fieldNames.
     *
     * @param fieldNames
     * @return
     */
    public List<FilterGroup<T>> countBy(String...fieldNames) {
        Object cached = getCached("countBy");
        if (cached != null) {
            return (List<FilterGroup<T>>)cached;
        }
        if (objects == null) {
            process();
        }

        HashMap<String, FilterGroup<T>> groupByGroupKey = new HashMap<>();
        boolean[] hasDot = new boolean[fieldNames.length];
        for (int i=0; i<fieldNames.length;i++) {
            hasDot[i] = fieldNames[i].contains(".");
        }
        List<String> groupKeys = new ArrayList<>();
        for(T o: objects) {
            String groupKey = "";
            for (int i=0; i<fieldNames.length; i++) {
                String fieldName = fieldNames[i];
                if (hasDot[i]) {

                } else {
                    groupKey += PropertyUtils.getProperty(o, fieldName).toString() + Literals.GSEP;
                }
            }
            if (!groupByGroupKey.containsKey(groupKey)) {
                groupByGroupKey.put(groupKey, new FilterGroup<T>(groupKey));
                groupKeys.add(groupKey);
                groupByGroupKey.get(groupKey).getItems().add(o);
            }
            groupByGroupKey.get(groupKey).incrCount();
        }
        List<FilterGroup<T>> groups = new ArrayList<>();
        for (String groupKey: groupKeys) {
            groups.add(groupByGroupKey.get(groupKey));
        }
        setCached("countBy", groups);
        return groups;
    }

    /**
     * Group the matching objects by field names. So if I wanted a group of blog posts
     * written by some author, grouped by year/month I would do:
     *
     * BlogPostController.instance()
     *     .filter("author", "Mark Twain")
     *     .groupBy("publishDate.year", "publishDate.month");
     *
     *
     *
     * @param fieldNames
     * @return
     */
    public List<FilterGroup<T>> groupBy(String...fieldNames) {
        Object cached = getCached("groupBy");
        if (cached != null) {
            return (List<FilterGroup<T>>)cached;
        }
        if (objects == null) {
            process();
        }
        List<FilterGroup<T>> groups = groupBy(objects, fieldNames);
        setCached("groupBy", groups);
        return groups;
    }

    /**
     * Do a groupBy of the passed in objects.
     *
     * @param items
     * @param fieldNames
     * @param <Y>
     * @return
     */
    private <Y> List<FilterGroup<Y>> groupBy(List<Y> items, String...fieldNames) {

        List<List<String>> subGroupFields = new ArrayList<>();
        List<String> prior = new ArrayList<>();
        for (String fieldName: fieldNames) {
            prior.add(fieldName);
            subGroupFields.add(prior);
        }




        HashMap<String, FilterGroup<Y>> groupByGroupKey = new HashMap<>();
        boolean[] hasDot = new boolean[fieldNames.length];
        for (int i=0; i<fieldNames.length;i++) {
            hasDot[i] = fieldNames[i].contains(".");
        }
        List<String> groupKeys = new ArrayList<>();


        for(Y o: items) {
            String groupKey = "";
            for (int i=0; i<fieldNames.length; i++) {
                String fieldName = fieldNames[i];
                if (hasDot[i]) {
                    groupKey += PropertyUtils.getDotProperty(o, fieldName).toString() + Literals.GSEP;
                } else {
                    groupKey += PropertyUtils.getProperty(o, fieldName).toString() + Literals.GSEP;
                }
            }
            if (!groupByGroupKey.containsKey(groupKey)) {
                FilterGroup newGroup = new FilterGroup<Y>(groupKey);
                groupByGroupKey.put(groupKey, newGroup);
                groupKeys.add(groupKey);
            }
            groupByGroupKey.get(groupKey).getItems().add(o);
        }
        List<FilterGroup<Y>> groups = new ArrayList<>();
        for (String groupKey: groupKeys) {
            groups.add(groupByGroupKey.get(groupKey));
        }

        /*
        Group previousGroup = null;
        for (int i=0; i<groups.size(); i++) {
            Group group = groups.get(i);
            if (previousGroup == null) {
                for(List<String> subGroup: subGroupFields) {
                    group.getFirstOfs().add(subGroup);
                }
            }
            if (i+1 == groups.size()) {
                for(List<String> subGroup: subGroupFields) {
                    group.getLastOfs().add(subGroup);
                }
            }
            if (previousGroup == null) {
                previousGroup = group;
                continue;
            }
            List<String> previousVals = Arrays.asList(StringUtils.split(previousGroup.getKey(), Literals.GSEP));
            List<String> curVals = Arrays.asList(StringUtils.split(group.getKey(), Literals.GSEP));
            for(int k=0;k< subGroupFields.size(); k++) {
                List<String> subGroup = subGroupFields.get(k);
                if (!curVals.subList(0, k+1).equals(previousVals.subList(0, k+1))) {
                    previousGroup.getLastOfs().add(subGroup);
                    group.getFirstOfs().add(subGroup);
                }
            }
            previousGroup = group;
        }
            */
        return groups;
    }


    /**
     * Executes the filters and returns all matching items.
     *
     * @return
     */
    public List<T> all()  {
        List<T> cached = (List<T>)getCached("all");
        if (cached != null) {
            return cached;
        }
        if (objects == null) {
            process();
        }
        setCached("all", objects);
        return objects;
    }

    /**
     * Executes the filters and returns true if there are no matching items found.
     *
     * @return
     */
    public boolean empty()  {
        Boolean cached = (Boolean)getCached("empty");
        if (cached != null) {
            return cached;
        }
        if (objects == null) {
            process();
        }
        boolean result = objects.size() == 0;
        setCached("empty", result);
        return result;
    }

    /**
     * Alias for empty(), no matching items.
     * @return
     */
    public boolean isEmpty()  {
        return empty();
    }

    /**
     * Executes the filters, and returns a list of all values of the given property name
     * for all matching objects.
     *
     * @param name
     * @param <Y>
     * @return
     */
    public <Y> List<Y> column(String name) {
        List<Y> items = list();
        for(T item: all()) {
            items.add((Y)PropertyUtils.getPropertyOrMappedValue(item, name));
        }
        return items;
    }

    /**
     * Executes the filters and returns the total matching count.
     *
     * @return
     */
    public int count()  {
        Integer cached = (Integer)getCached("count");
        if (cached != null) {
            return cached;
        }

        if (objects == null) {
            process();
        }
        int theCount = objects.size();
        setCached("count", theCount);
        return theCount;
    }

    /**
     * Executes the filters and returns the first matching item.
     *
     * @return
     */
    public T first()  {
        T cached = (T)getCached("first");
        if (cached != null) {
            if (getStash() instanceof LocalMemoryStash) {
                cached = getStash().getController().detach(cached);
            }
            return cached;
        }
        process(1, 1, false);
        List<T> things = objects;
        if (things.size() > 0) {
            T thing = things.get(0);
            setCached("first", thing);
            if (getStash() instanceof LocalMemoryStash) {
                thing = getStash().detach(thing);
            }
            return thing;
        } else {
            return null;
        }

    }


    /**
     * Adds a sort direction the filter chain response.
     *
     * @param fieldName
     * @param direction - either asc or desc
     * @return
     */
    public FilterChain<T> sort(String fieldName, String direction)  {
        return sortBy(fieldName, SortDirection.fromString(direction));
    }

    /**
     * Adds a sort direction the filter chain response.
     *
     * @param fieldName
     * @param direction
     * @return
     */
    public FilterChain<T> sortBy(String fieldName, SortDirection direction)  {
        FilterChain<T> chain = newCopy();
        chain.setSortField(fieldName);
        chain.setSortDirection(direction);
        return chain;
    }

    /**
     * Alias for pager(page, 10) (default page size of 10);
     * @param page
     * @return
     */
    public Pager<T> pager(Integer page)  {
        return this.pager(page, 10);
    }

    /**
     * Executes the filters and returns a pager object. A page object
     * has a subset of the items based on the desired page and the page size
     *
     * @param page - which page to return
     * @param size - how many items are on a page
     * @return
     */
    public Pager<T> pager(Integer page, Integer size)  {
        String methodKey = "pager"  + Literals.GSEP + page + Literals.GSEP + size;
        Object cached = getCached(methodKey);
        if (cached != null) {
            return (Pager)cached;
        }
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null) {
            size = 10;
        }

        if (objects == null) {
            process(page, size, true);
        }


        Pager pager = new Pager();
        pager.setCurrentItems(objects);
        if (objects.size() == 0) {
            return pager;
        }
        pager.setCurrentPage(page);
        pager.setItemsPerPage(size);
        pager.setHasPreviousPage(true);
        pager.setHasNextPage(true);
        pager.setPreviousPageNumber(page - 1);
        pager.setNextPageNumber(pager.getCurrentPage() + 1);

        Integer startingIndex = (pager.getCurrentPage() - 1) * pager.getItemsPerPage();
        Integer endingIndex = startingIndex + pager.getItemsPerPage();
        if (endingIndex > getMatchingCount()) {
            endingIndex = getMatchingCount();
        }
        if (endingIndex >= getMatchingCount()) {
            pager.setHasNextPage(false);
            pager.setNextPageNumber(pager.getCurrentPage());
        }
        if (pager.getCurrentPage() <= 1) {
            pager.setHasPreviousPage(false);
            pager.setPreviousPageNumber(pager.getCurrentPage());
        }
        if (size > 0) {
            pager.setPageCount((getMatchingCount() / size));
            if (getMatchingCount() % size > 0) {
                pager.setPageCount(pager.getPageCount() + 1);
            }
        }
        setCached(methodKey, pager);
        return pager;
    }


    /**
     * Alias for process(0, 100000, false)
     */
    protected void process() {
        process(0, 100000, false);
    }

    /**
     * Actually applies all filters and sorts to reduce the originalObjects into
     * a subset of matched objects.
     *
     * @param page - the page to start from when returning results
     * @param size - the number of results per page (the number of results to return)
     * @param fetchTotalMatching - hydrate the total matching count field, even if
     *                             we are returning a subet
     */
    protected void process(int page, int size, boolean fetchTotalMatching)  {
        List<T> availableItems = originalObjects;

        boolean hydrated = tryHydrateObjectsBasedOnUniqueKey();
        if (hydrated) {
            return;
        }

        // If we are filtering on a key, then we can reduce to a subset the number of items we have to look at
        for (FilterOperation op : operations) {
            if (op.isOrOperation()) {
                continue;
            }
            if (!op.getIsExclude() && op.getOperator().equals(FilterOperator.EQUAL) && stash.getUniqueFields().contains(op.getFieldName())) {
                T availableItem = stash.forUniqueKey(op.getFieldName(), op.getOriginalValue());
                if (availableItem != null) {
                    availableItems = list(availableItem);
                } else {
                    availableItems = list();
                }
                break;
            }
            if (!op.getIsExclude() && op.getOperator().equals(FilterOperator.EQUAL) && stash.getKeyFields().contains(op.getFieldName())) {
                availableItems = stash.listForKey(op.getFieldName(), op.getOriginalValue());
                if (availableItems == null) {
                    availableItems = list();
                }
                break;
            }
        }

        // Filter down availableItems into items, based on applying the filter operations to each item
        List<T> items = new ArrayList<T>();
        for(T o: availableItems) {
            Boolean exclude = false;
            if (getIncludeDeleted() != true && o.getDeleted() == true) {
                continue;
            }
            for (FilterOperation operation: getOperations()) {
                boolean matches = checkItemMatchesFilterAndExcludes(operation, o);
                if (!matches) {
                    exclude = true;
                    break;
                }
            }
            if (!exclude) {
                items.add(o);
            }
        }

        // Apply the sort
        if (!Literals.empty(getSortField())) {
            //BeanComparator beanComparator = new BeanComparator(getSortField());
            PropertyComparator comparator = new PropertyComparator(getSortField());
            Collections.sort(items, comparator);
            if (getSortDirection().equals(SortDirection.DESC)) {
                Collections.reverse(items);
            }
        }

        // Set the internal objects fields to the filtered and sorted items;
        objects = items;
        // Get the total count that matched the filtering
        matchingCount = objects.size();

        // If we are implementing paging, and want to limit the objects to a page,
        // we do so here.
        if (size > 0) {
            if (page < 1) {
                page = 1;
            }
            int start = (page - 1) * size;
            int end = start + size;
            if (end > objects.size()) {
                end = objects.size();
            }
            if (start >= objects.size()) {
                objects = new ArrayList<>();
            } else if (end > start) {
                objects = objects.subList(start, end);
            }
        }
    }

    private boolean tryHydrateObjectsBasedOnUniqueKey() {
        boolean matchedUnique = true;

        if (operations.size() > 1) {
            return false;
        }

        // If we are filtering on the id, or on a unique key, then we can short circuit the process
        for (FilterOperation op: operations) {
            if (!"=".equals(op.getOperator()) || op.getIsExclude()) {
                continue;
            }
            if (!op.getFieldName().equals("id") && !stash.getUniqueFields().contains(op.getFieldName())) {
                continue;
            }
            T o = null;
            if (op.getFieldName().equals("id")) {
                o = stash.forId((Long)op.getOriginalValue());
            } else {
                o = stash.forUniqueKey(op.getFieldName(), op.getOriginalValue());
            }
            if (o == null) {
                objects = list();
                return true;
            }
            if (!getIncludeDeleted() && o.getDeleted()) {
                objects = list();
                return true;
            }
            objects = list(o);
            matchingCount = 1;
            return true;
        }
        return false;
    }


    /**
     * Checks whether the object matches the complete FilterOperation, taking into account
     * OR subOperations and the isExcludes flag.
     *
     * @param op
     * @param o
     * @return
     */
    private boolean checkItemMatchesFilterAndExcludes(FilterOperation op, T o) {
        // Handle an OR operation by getting a list of or requirements and recursing down
        boolean matches = false;
        if (op.isOrOperation()) {
            for(FilterOperation subOp: op.getOrSubOperations()) {
                boolean thisMatches = checkItemMatchesFilterConditions(subOp, o);
                if (thisMatches){
                    matches = true;
                    break;
                }
            }
        } else {
            matches = checkItemMatchesFilterConditions(op, o);
        }
        if (op.getIsExclude()) {
            return !matches;
        } else {
            return matches;
        }
    }

    /**
     * Check whether the operation conditions match the object, without taking into account
     * isExcludes or "OR" subOperations.
     *
     * @param op
     * @param o
     * @return
     */
    private boolean checkItemMatchesFilterConditions(FilterOperation op, T o) {

        Object propValue = null;
        if (op.hasDot()) {
            propValue = PropertyUtils.getDotProperty(o, op.getFieldName());
        } else {
            propValue = PropertyUtils.getPropertyOrMappedValue(o, op.getFieldName());
        }

        // Filter out nulls
        if (propValue == null && op.getOriginalValue() != null) {
            return false;
        }
        if (propValue == null && op.getOriginalValue() == null && op.getOperator().equals(FilterOperator.EQUAL)) {
            return true;
        }

        // Handle the IN operator
        if (op.getOperator().equals(FilterOperator.IN)) {
            Boolean isIn = false;
            Iterable values = (Iterable)propValue;
            for(Object val: values) {
                if (val.equals(op.getOriginalValue())) {
                    isIn = true;
                    break;
                }
            }
            return isIn;
        }
        if (op.getOperator().equals(FilterOperator.ANY)) {
            Iterable vals = (Iterable)op.getOriginalValue();
            boolean matches = false;
            for (Object val: vals) {
                if (val.equals(propValue)) {
                    matches = true;
                    break;
                }
            }
            return matches;
        }

        // Apply a bunch of heuristics to make sure we are comparing like types,
        // we don't want to filter something out because we are comparing a Long to an Integer
        hydrateTypedValue(op, propValue);

        // Filter out nulls again, based on type conversion
        if (propValue == null && op.getTypedValue() != null) {
            Log.info("Null value: id:{0} field:{1} objVal:''{2}'' ", o.getId(), op.getFieldName(), propValue);
            return false;
        }
        Log.finest("Compare id:{0} field:{1} objVal:''{2}'' objValType:{3} filterTypedVal:''{4}'' filterValType: {5}", o.getId(), op.getFieldName(), propValue, propValue.getClass().getCanonicalName(), op.getTypedValue(), op.getTypedValue().getClass().getName());

        // When comparing booleans the string "true" should be considered true, and "false" false, this
        // is important when applying a filter coming from an untyped query string.
        if (op.getTypedValue() instanceof Boolean && propValue instanceof String) {
            return op.getTypedValue().toString().toLowerCase().equals((String) propValue.toString().toLowerCase());
        }

        if (op.getOperator().equals(FilterOperator.EQUAL)) {
            return op.getTypedValue().equals(propValue);
        }
        if (op.getOperator().equals(FilterOperator.NOT_EQUAL)) {
            return !op.getTypedValue().equals(propValue);
        }

        if (op.getOperator().equals(FilterOperator.LIKE)) {
            return StringUtils.containsIgnoreCase(propValue.toString(), op.getTypedValue().toString());
        }

        int i = op.getComparableValue().compareTo(propValue);
        if (op.getOperator().equals(FilterOperator.GREATER_THAN)) {
            return i < 0;
        }
        if (op.getOperator().equals(FilterOperator.LESS_THAN)) {
            return i > 0;
        }
        if (op.getOperator().equals(FilterOperator.GREATER_THAN_OR_EQUAL)) {
            return i <= 0;
        }
        if (op.getOperator().equals(FilterOperator.LESS_THAN_OR_EQUAL)) {
            return i >= 0;
        }
        throw new UsageException("You used an uninplemented filter operation: " + op);
    }




    /**
     * Hydrate the FilterOperation.typedValue based on gueessing or reflecting
     * on the type of the property as passed in.
     *
     * @param op
     * @param propValue
     */
    private void hydrateTypedValue(FilterOperation op, Object propValue) {
        if (op.getTypedValue() != null) {
            return;
        }
        if (op.getOriginalValue() == null) {
            return;
        }
        if (op.getOriginalValue().getClass().equals(propValue.getClass())) {
            op.setTypedValue(op.getOriginalValue());
            return;
        }
        if (op.getOriginalValue().getClass().equals(String.class)) {
            String val = (String) op.getOriginalValue();
            if (propValue != null) {
                if (propValue.getClass().equals(Integer.class)) {
                    op.setTypedValue(Integer.parseInt(val));
                } else if (propValue.getClass().equals(Long.class)) {
                    op.setTypedValue(Long.parseLong(val));
                } else if (propValue.getClass().equals(Boolean.class)) {
                    op.setTypedValue(Boolean.parseBoolean(val));

                } else {
                    op.setTypedValue(op.getOriginalValue());
                }
            } else {
                op.setTypedValue(op.getOriginalValue());
            }
        } else if (op.getOriginalValue() instanceof BigInteger) {
            op.setTypedValue(((BigInteger) op.getOriginalValue()).longValue());

        } else if (op.getOriginalValue() instanceof Integer && propValue instanceof Long) {
            op.setTypedValue(new Long((Integer) op.getOriginalValue()));
        } else if (op.getOriginalValue() instanceof Integer && propValue instanceof Float) {
            op.setTypedValue(((Integer) op.getOriginalValue()).floatValue());
        } else if (op.getOriginalValue() instanceof Integer && propValue instanceof Double) {
            op.setTypedValue(((Integer) op.getOriginalValue()).doubleValue());
        } else if (op.getOriginalValue() instanceof Long && propValue instanceof Double) {
            op.setTypedValue(((Long) op.getOriginalValue()).doubleValue());
        } else if (op.getOriginalValue() instanceof Long && propValue instanceof Float) {
            op.setTypedValue(((Long) op.getOriginalValue()).floatValue());
        } else if (propValue instanceof Boolean) {
            if (op.getOriginalValue() instanceof Integer || op.getOriginalValue() instanceof Long) {
                if ((Integer)op.getOriginalValue() == 0) {
                    op.setTypedValue(false);
                } else if ((Integer)op.getOriginalValue() == 1){
                    op.setTypedValue(true);
                }
            }
        }
        if (op.getTypedValue() == null) {
            op.setTypedValue(op.getOriginalValue());
        }
    }

    protected Object getCached(String methodName) {
        String key = buildKey(methodName);
        if (checkSkipCache(key)) {
            return null;
        }
        Object result = FilterCache.get(this.getBucket(), key);
        return result;
    }

    protected boolean checkSkipCache(String key) {
        return false;
    }

    protected void setCached(String methodName, Object val) {
        String key = buildKey(methodName);
        FilterCache.set(this.getBucket(), key, val);
    }

    private String buildKey(String methodName) {
        StringBuilder builder = new StringBuilder();
        builder.append(methodName + Literals.GSEP);
        if (this.originalObjects.size() > 0) {
            builder.append(this.originalObjects.get(0).getClass().getCanonicalName());
        }
        for (FilterOperation op: operations) {
            //Log.finest("fn: {0} op: {1} isExclude: {2} orgVal: {3}", op.getFieldName(), op.getOperator(), op.getIsExclude(), op.getOriginalValue());
            String ov = "<null>";
            if (op.getOriginalValue() != null) {
                ov = op.getOriginalValue().toString();
            }
            builder.append(op.getFieldName() + op.getOperator() + op.getIsExclude() + Literals.GSEP + ov + Literals.GSEP);
            if (op.isOrOperation()) {
                for (FilterOperation subOp: op.getOrSubOperations()) {
                    ov = "<null>";
                    if (subOp.getOriginalValue() != null) {
                        ov = subOp.getOriginalValue().toString();
                    }
                    builder.append(subOp.getFieldName() + subOp.getOperator() + subOp.getIsExclude() + Literals.GSEP + ov + Literals.GSEP);
                }
            }
        }
        builder.append(getIncludeDeleted());
        builder.append(getSortField());
        builder.append(getSortDirection());
        builder.append(Literals.GSEP + getExtraCacheKey());
        String fullKey = builder.toString();
        return DigestUtils.md5Hex(fullKey);
    }

    @Override
    public Iterator<T> iterator() {
        if (objects == null) {
            process();
        }
        return objects.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        if (objects == null) {
            try {
                process();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        objects.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        if (objects == null) {
            try {
                process();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return objects.spliterator();
    }



    public String getSortField() {
        return sortField;
    }

    protected void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    protected void setSortDirection(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String getBucket() {
        return bucket;
    }

    protected void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getExtraCacheKey() {
        return extraCacheKey;
    }

    protected void setExtraCacheKey(String extraCacheKey) {
        this.extraCacheKey = extraCacheKey;
    }

    public Boolean getIncludeDeleted() {
        return _includeDeleted;
    }

    protected FilterChain<T> setIncludeDeleted(Boolean includeDeleted) {
        this._includeDeleted = includeDeleted;
        return this;
    }

    public FilterChain<T> includeDeleted() {
        _includeDeleted = true;
        return this;
    }

    public ArrayList<FilterOperation> getOperations() {
        return operations;
    }

    protected Integer getMatchingCount() {
        return matchingCount;
    }

    protected void setMatchingCount(Integer matchingCount) {
        this.matchingCount = matchingCount;
    }

    protected List<T> getObjects() {
        return objects;
    }

    protected void setObjects(List<T> objects) {
        this.objects = objects;
    }

    LocalMemoryStash<T> getStash() {
        return stash;
    }

    FilterChain setStash(LocalMemoryStash<T> stash) {
        this.stash = stash;
        return this;
    }
}

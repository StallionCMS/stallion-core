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

package io.stallion.utils;


import com.google.common.collect.Lists;
import io.stallion.exceptions.AppException;
import io.stallion.settings.Settings;

import java.nio.charset.Charset;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Literals {

    public static final ZoneId UTC = ZoneId.of("UTC");
    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static final char GSEP = Character.forDigit(29, 10);

    /**
     * Null safe check for equals.
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean areEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        } else if (a == null && b != null) {
            return false;
        } else {
            return a.equals(b);
        }
    }

    public static String or(String obj, String defaultVal) {
        if (empty(obj)) {
            return defaultVal;
        } else {
            return obj;
        }
    }




    public static Long or(Long obj, Long defaultVal) {
        if (empty(obj)) {
            return defaultVal;
        } else {
            return obj;
        }
    }

    public static Integer or(Integer obj, Integer defaultVal) {
        if (empty(obj)) {
            return defaultVal;
        } else {
            return obj;
        }
    }


    public static <T> T or(T obj, T defaultVal) {
        if (obj == null) {
            return defaultVal;
        }
        if (obj instanceof String) {
            if (empty((String)obj)) {
                return defaultVal;
            }
        } else if (emptyObject(obj)) {
            return defaultVal;
        }
        return obj;
    }

    public static String firstTruthy(String...things) {
        for (String thing: things) {
            if (!empty(thing)) {
                return thing;
            }
        }
        return "";
    }

    /**
     * Imitation of Python's [2:-7] syntax that never throws errors while slicing
     * and that accepts negative indexes
     *
     * @param s
     * @param start
     * @param end
     * @return
     */
    public static String slice(String s, int start, int end) {
        if (end < 0) {
            end = s.length() + end;
        }
        if (start < 0) {
            start = s.length() + start;
        }
        if (end < start) {
            return "";
        }
        if (end > s.length()) {
            end = s.length();
        }
        return s.substring(start, end);
    }

    public static <T extends List> T slice(T l, int start, int end) {
        if (end < 0) {
            end = l.size() + end;
        }
        if (start < 0) {
            start = l.size() + start;
        }
        if (end > l.size()) {
            end = l.size();
        }
        if (start > l.size() || start > end){
            return (T)l.subList(0, 0);
        }
        if (end < start) {
            end = start;
        }

        return (T)l.subList(start, end);
    }



    public static <T extends List> T truncate(T l, int length){
        if (l.size() < length) {
            return l;
        }
        return (T)l.subList(0, length);
    }

    public static String truncate(String s, int length) {
        if (s == null) {
            return "";
        }
        if (s.length() < length) {
            return s;
        }
        return s.substring(0, length);
    }

    /**
     * A truncat the tries to truncate on a sentence or word boundary.
     *
     * @param s
     * @param length
     * @return
     */
    public static String truncateSmart(String s, int length) {
        if (s == null) {
            return "";
        }
        if (s.length() < length) {
            return s;
        }
        s = s.substring(0, length);
        int i = s.lastIndexOf('.');
        if (i > 20) {
            s = s.substring(0, i + 1);
        } else {
            i = s.lastIndexOf(' ');
            if (i > 10) {
                s = s.substring(0, i);
            }
        }
        return s;
    }

    public static long mils() {
        return DateUtils.mils();
    }

    public static ZonedDateTime utcNow() {
        return DateUtils.utcNow();
    }


    public static <T> T[] array(T...elems){
        return elems;
    }

    public static <T> Set<T> set(T...elems){
        return new HashSet<T>( list(elems) );
    }

    public static <T> List<T> list(T...elems){
        ArrayList<T> myList = new ArrayList<>();
        for(T ele: elems) {
            myList.add(ele);
        }
        return myList;
    }

    public static <K, V> Map<K, V> map(Map.Entry<K, V>...entries) {
        HashMap<K, V> myMap = new HashMap<>();
        for (Map.Entry<K, V> entry: entries) {
            myMap.put(entry.getKey(), entry.getValue());
        }
        return myMap;
    }

    public static <K, V> Map<K, V> defaultMap(Class<? extends V> cls, Map.Entry<K, V>...entries) {
        HashMap<K, V> myMap = new DefaultMap<>(cls);
        for (Map.Entry<K, V> entry: entries) {
            myMap.put(entry.getKey(), entry.getValue());
        }
        return myMap;
    }

    public static <K, V> Map<K, V> defaultMap(V prototype, Map.Entry<K, V>...entries) {
        HashMap<K, V> myMap = new DefaultMap<>(prototype);
        for (Map.Entry<K, V> entry: entries) {
            myMap.put(entry.getKey(), entry.getValue());
        }
        return myMap;
    }


    public static <K, V> Map.Entry<K, V> val(K key, V value) {
        return new AbstractMap.SimpleEntry(key, value);
    }

    public static <T> List<T> asList(T ...things) {
        return Arrays.asList(things);
    }

    public static <T> T[] asArray(List<T> myList, Class<? extends T> cls) {
        T[] array = (T[]) java.lang.reflect.Array.newInstance(cls, myList.size());
        return myList.toArray(array);
    }

    public static boolean emptyInstance(Object o) {
        if (o == null) {
            return true;
        }
        if (o instanceof IEmpty) {
            return true;
        }
        return false;
    }

    public static boolean emptyObject(Object o) {
        if (o == null) {
            return true;
        }
        if (o instanceof IEmpty) {
            return true;
        }
        if (o instanceof String) {
            return "".equals(o);
        }
        if (o instanceof Long) {
            return (Long)o == 0L;
        }
        if (o instanceof Integer) {
            return (Integer)o == 0;
        }
        if (o instanceof Double) {
            return (Double)o == 0.0;
        }
        if (o instanceof Float) {
            return (Float)o == 0.0;
        }
        if (o instanceof Collection) {
            return ((List)o).size() == 0;
        }
        if (o instanceof Map) {
            return ((Map)o).size() == 0;
        }
        return false;
    }


    public static boolean empty(ZonedDateTime dt) {
        if (dt == null) {
            return true;
        }
        return false;
    }


    public static boolean empty(Object[] objects) {
        if (objects == null) {
            return true;
        }
        if (objects.length == 0){
            return true;
        }
        return false;
    }

    public static boolean empty(Collection a) {
        if (a == null) {
            return true;
        }
        if (a.size() == 0) {
            return true;
        }
        return false;
    }

    public static boolean empty(Map a) {
        if (a == null) {
            return true;
        }
        if (a.size() == 0) {
            return true;
        }
        return false;
    }

    public static boolean empty(CharSequence a) {
        if (a == null) {
            return true;
        }
        if (a.length() == 0) {
            return true;
        }
        return false;
    }

    public static boolean empty(int a) {
        if (a == 0) {
            return true;
        }
        return false;
    }

    public static boolean empty(Integer a) {
        if (a == null) {
            return true;
        }
        if (a == 0) {
            return true;
        }
        return false;
    }
    public static boolean empty(long a) {
        if (a == 0) {
            return true;
        }
        return false;
    }
    public static boolean empty(Long a) {
        if (a == null) {
            return true;
        }
        if (a == 0) {
            return true;
        }

        return false;
    }


    /**
     * Simple, Python style list comprehensions helper:
     *
     * List&lt;Integer&gt; listB = apply(listA, a -&gt; a * a);
     *
     * Hat tip: http://stackoverflow.com/questions/26205409/java-8-idiomatic-way-to-apply-a-lambda-to-a-list-returning-another-list
     * @param coll
     * @param mapper
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> List<R> apply(Collection<T> coll, Function<? super T, ? extends R> mapper) {
        return coll.stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * Simple, Python style list comprehensions helper:
     *
     * List&lt;Integer&gt; listB = apply(listA, a -&gt; !empty(a));
     *
     * Hat tip: http://stackoverflow.com/questions/26205409/java-8-idiomatic-way-to-apply-a-lambda-to-a-list-returning-another-list
     * @param coll
     * @param filterer
     * @param <T>
     * @return
     */
    public static <T> List<T> filter(Collection<T> coll, Predicate<? super T> filterer) {
        return coll.stream().filter(filterer).collect(Collectors.toList());
    }

    public static <T> List<T> filterEmpty(Collection<T> things) {
        return filter(things, e->!emptyObject(e));
    }

    /**
     * safeLoop() returns an iterator that will run a maximum of "max" times before throwing an exception
     *
     * Use safeLoop() instead of a while loop:
     *
     * Do not do:
     *
     * while (true) {
     *     // some logic
     *     if (endCondition) {
     *         break;
     *     }
     * }
     *
     * Instead do:
     *
     * for(int x: safeLoop(1000)) {
     *     // some logic
     *     if (endCondition) {
     *         break;
     *     }
     * }
     *
     * That way if your logic goes awry, you won't hard crash the server by eating up all the CPU.
     *
     * while() loop bugs are some of the worst bugs you can write, because they take down the entire server, and there is
     * no easy way to find out which while() loop triggered the problem.
     *
     * Thus to avoid the problem altogether, we use safeLoop();
     *
     * @param max
     * @return
     */
    public static Iterable<Integer> safeLoop(int max) {
        return new SafeLoop(max);
    }


    private static class SafeLoop implements Iterable {

        private int max = 0;

        public SafeLoop(int maxRunTimes) {
            max = maxRunTimes;
        }
        @Override
        public Iterator iterator() {
            return new SafeLoopIterator(max);
        }

    }

    private static class SafeLoopIterator  implements Iterator {
        private int index = 0;
        private int max = 0;

        public SafeLoopIterator(Integer maxRunTimes) {
            this.max = maxRunTimes;
        }

        public boolean hasNext() {
            if (index < max) {
                return true;
            }
            throw new AppException("A safeLoop() has run more times than expected. Emergency stop triggered after " + index + " loops");
            //implement...
        }

        public Integer next() {
            if (index >= max) {
                throw new AppException("A safeLoop() has run more times than expected. Emergency stop triggered after " + index + " loops");
            }
            return index++;
        }

        public void remove() {
            //implement... if supported.
        }
    }


}

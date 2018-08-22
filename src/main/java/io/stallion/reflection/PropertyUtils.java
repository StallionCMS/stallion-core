/*
 * This file is copyright Bitronix Software.
 *
 * Bitronix Transaction Manager
 *
 * https://github.com/brettwooldridge/bitronix-hp/blob/master/btm/src/main/java/bitronix/tm/utils/PropertyUtils.java
 *
 * Copyright (c) 2010, Bitronix Software.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA 02110-1301 USA
 */

package io.stallion.reflection;

import io.stallion.dataAccess.MappedModel;
import io.stallion.utils.GeneralUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

import static io.stallion.utils.Literals.UTC;
import static io.stallion.utils.Literals.list;

/**
 * Utility class for getting and setting bean properties via reflection.
 *
 */
public final class PropertyUtils {

    private static HashMap<String, Object> lookupCache = new HashMap<>();

    private PropertyUtils() {
    }

    public static  void resetCache() {
        lookupCache = new HashMap<>();
    }

    /**
     * Set a direct or indirect property (dotted property: prop1.prop2.prop3) on the target object. This method tries
     * to be smart in the way that intermediate properties currently set to null are set if it is possible to create
     * and set an object. Conversions from propertyValue to the proper destination type are performed when possible.
     * @param target the target object on which to set the property.
     * @param propertyName the name of the property to set.
     * @param propertyValue the value of the property to set.
     * @throws PropertyException if an error happened while trying to set the property.
     */
    public static void setProperty(Object target, String propertyName, Object propertyValue) throws PropertyException {
        String[] propertyNames = propertyName.split("\\.");

        StringBuffer visitedPropertyName = new StringBuffer();
        Object currentTarget = target;
        int i = 0;
        while (i < propertyNames.length -1) {
            String name = propertyNames[i];
            Object result = callGetter(currentTarget, name);
            if (result == null) {
                // try to instanciate the object & set it in place
                Class propertyType = getPropertyType(target, name);
                try {
                    result = propertyType.newInstance();
                } catch (InstantiationException ex) {
                    throw new PropertyException("cannot set property '" + propertyName + "' - '" + name + "' is null and cannot be auto-filled", ex);
                } catch (IllegalAccessException ex) {
                    throw new PropertyException("cannot set property '" + propertyName + "' - '" + name + "' is null and cannot be auto-filled", ex);
                }
                callSetter(currentTarget, name, result);
            }

            currentTarget = result;
            visitedPropertyName.append(name);
            visitedPropertyName.append('.');
            i++;

            // if it's a Properties object -> the non-visited part of the key should be used
            // as this Properties' object key so stop iterating over the dotted properties.
            if (currentTarget instanceof Properties)
                break;
        }

        String lastPropertyName = propertyName.substring(visitedPropertyName.length(), propertyName.length());
        if (currentTarget instanceof Properties) {
            Properties p = (Properties) currentTarget;
            p.setProperty(lastPropertyName, propertyValue.toString());

        } else {
            setDirectProperty(currentTarget, lastPropertyName, propertyValue);
        }
    }

    /**
     * Build a map of direct javabeans properties of the target object. Only read/write properties (ie: those who have
     * both a getter and a setter) are returned.
     * @param target the target object from which to get properties names.
     * @return a Map of String with properties names as key and their values
     * @throws PropertyException if an error happened while trying to get a property.
     */
    public static Map<String, Object> getProperties(Object target, Class<? extends Annotation>...excludeAnnotations) throws PropertyException {
        Map<String, Object> properties = new HashMap<String, Object>();
        Class clazz = target.getClass();
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String name = method.getName();
            Boolean hasExcludeAnno = false;
            if (excludeAnnotations.length > 0) {
                for(Class<? extends Annotation> anno: excludeAnnotations) {
                    if (method.isAnnotationPresent(anno)) {
                        hasExcludeAnno = true;
                    }
                }
            }
            if (hasExcludeAnno) {
                continue;
            }
            if (method.getModifiers() == Modifier.PUBLIC && method.getParameterTypes().length == 0 && (name.startsWith("get") || name.startsWith("is"))
                    && containsSetterForGetter(clazz, method)) {
                String propertyName;
                if (name.startsWith("get"))
                    propertyName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                else if (name.startsWith("is"))
                    propertyName = Character.toLowerCase(name.charAt(2)) + name.substring(3);
                else
                    throw new PropertyException("method '" + name + "' is not a getter, thereof no setter can be found");

                try {
                    Object propertyValue = method.invoke(target, (Object[]) null);  // casting to (Object[]) b/c of javac 1.5 warning
                    if (propertyValue != null && propertyValue instanceof Properties) {
                        Map propertiesContent = getNestedProperties(propertyName, (Properties) propertyValue);
                        properties.putAll(propertiesContent);
                    }
                    else {
                        properties.put(propertyName, propertyValue);
                    }
                } catch (IllegalAccessException ex) {
                    throw new PropertyException("cannot set property '" + propertyName + "' - '" + name + "' is null and cannot be auto-filled", ex);
                } catch (InvocationTargetException ex) {
                    throw new PropertyException("cannot set property '" + propertyName + "' - '" + name + "' is null and cannot be auto-filled", ex);
                }

            } // if
        } // for

        return properties;
    }

    public static List<String> getPropertyNames(Class clazz) throws PropertyException {
        Method[] methods = clazz.getMethods();
        List<String> names = list();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String name = method.getName();
            if (method.getModifiers() == Modifier.PUBLIC && method.getParameterTypes().length == 0 && (name.startsWith("get") || name.startsWith("is"))
                    && containsSetterForGetter(clazz, method)) {
                String propertyName;
                if (name.startsWith("get"))
                    propertyName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                else
                    propertyName = Character.toLowerCase(name.charAt(2)) + name.substring(3);
                names.add(propertyName);
            }
        }
        return names;
    }

    public static boolean propertyHasAnnotation(Class cls, String name, Class<? extends Annotation> anno) {
        return getAnnotationForProperty(cls, name, anno) != null;
    }

    public static <T extends Annotation> T getAnnotationForProperty(Class cls, String name, Class<T> anno) {
        String postFix = name.toUpperCase();
        if (name.length() > 1) {
            postFix = name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        Method method = null;
        try {
            method = cls.getMethod("get" + postFix);
        } catch (NoSuchMethodException e) {

        }
        if (method == null) {
            try {
                method = cls.getMethod("is" + postFix);
            } catch (NoSuchMethodException e) {

            }
        }
        if (method == null) {
            return null;
        }
        if (method.getModifiers() != Modifier.PUBLIC) {
            return null;
        }
        if (method.isAnnotationPresent(anno)) {
            return method.getDeclaredAnnotation(anno);
        }
        return null;
    }





    public static Boolean isReadable(Object obj, String name) {
        String cacheKey = obj.getClass().getCanonicalName() + "|" + name;
        if (lookupCache.containsKey(cacheKey)) {
            return (Boolean)lookupCache.get(cacheKey);
        }

        String postFix = name.toUpperCase();
        if (name.length() > 1) {
            postFix = name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        Method method = null;
        try {
            method = obj.getClass().getMethod("get" + postFix, (Class<?>[]) null);
        } catch (NoSuchMethodException e) {

        }
        if (method == null) {
            try {
                method = obj.getClass().getMethod("is" + postFix, (Class<?>[]) null);
            } catch (NoSuchMethodException e) {

            }
        }
        if (method == null) {
            lookupCache.put(cacheKey, false);
            return false;
        }
        if (method.getModifiers() == Modifier.PUBLIC) {
            lookupCache.put(cacheKey, true);
            return true;
        }
        lookupCache.put(cacheKey, false);
        return false;
    }

    public static Boolean isWriteable(Object target, String propertyName) {
        if (propertyName == null || "".equals(propertyName))
            throw new PropertyException("encountered invalid null or empty property name");
        String setterName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        Method[] methods = target.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(setterName)  &&  method.getParameterTypes().length == 1) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsSetterForGetter(Class clazz, Method method) {
        String methodName = method.getName();
        String setterName;

        if (methodName.startsWith("get"))
            setterName = "set" + methodName.substring(3);
        else if (methodName.startsWith("is"))
            setterName = "set" + methodName.substring(2);
        else
            throw new PropertyException("method '" + methodName + "' is not a getter, thereof no setter can be found");

        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method1 = methods[i];
            if (method1.getName().equals(setterName))
                return true;
        }
        return false;
    }

    /**
     * Get a direct or indirect property (dotted property: prop1.prop2.prop3) on the target object.
     *
     * @param target the target object from which to get the property.
     * @param propertyName the name of the property to get.
     * @return the value of the specified property.
     * @throws PropertyException if an error happened while trying to get the property.
     */
    public static Object getProperty(Object target, String propertyName) throws PropertyException {
        String[] propertyNames = propertyName.split("\\.");
        Object currentTarget = target;

        for (int i = 0; i < propertyNames.length; i++) {
            String name = propertyNames[i];
            Object result = callGetter(currentTarget, name);

            if (result == null && i < propertyNames.length -1)
                throw new PropertyException("cannot get property '" + propertyName + "' - '" + name + "' is null");
            currentTarget = result;
        }

        return currentTarget;
    }

    public static Object getDotProperty(Object target, String propertyName) throws PropertyException {
        if (!propertyName.contains(".")) {
            return getProperty(target, propertyName);
        }
        String[] parts = StringUtils.split(propertyName, '.');
        Object thing = target;
        for (String part: parts) {
            Object oldThing = thing;
            if (PropertyUtils.isReadable(thing, part)) {
                thing = PropertyUtils.getProperty(thing, part);
            } else if (thing instanceof Map) {
                thing = ((Map)thing).get(part);
            } else {
                throw new PropertyException("Cannot read property " + part + " of object " + thing + " of class " + thing.getClass().getName());
            }
            if (thing == null) {
                return null;
            }
        }
        return thing;
    }

    public static Object getPropertyOrMappedValue(Object target, String propertyName) throws PropertyException {
        if (isReadable(target, propertyName)) {
            return getProperty(target, propertyName);
        } else if (target instanceof MappedModel) {
            MappedModel model = (MappedModel)target;
            return model.get(propertyName);
        } else {
            throw new PropertyException("Cannot read property " + propertyName + " of object " + target + " of class " + target.getClass().getName());
        }
    }

    /**
     * Set a {@link Map} of direct or indirect properties on the target object.
     * @param target the target object on which to set the properties.
     * @param properties a {@link Map} of String/Object pairs.
     *
     * @throws PropertyException if an error happened while trying to set a property.
     */
    public static void setProperties(Object target, Map properties) throws PropertyException {
        Iterator it = properties.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            setProperty(target, name, value);
        }
    }

    /**
     * Return a comma-separated String of r/w properties of the specified object.
     * @param obj the object to introspect.
     * @return a a comma-separated String of r/w properties.
     */
    public static String propertiesToString(Object obj) {
        StringBuffer sb = new StringBuffer();
        Map properties = new TreeMap(getProperties(obj));
        Iterator it = properties.keySet().iterator();
        while (it.hasNext()) {
            String property = (String) it.next();
            Object val = getProperty(obj, property);
            sb.append(property);
            sb.append("=");
            sb.append(val);
            if (it.hasNext())
                sb.append(", ");
        }
        return sb.toString();
    }

    /**
     * Set a direct property on the target object. Conversions from propertyValue to the proper destination type
     * are performed whenever possible.
     *
     * @param target the target object on which to set the property.
     * @param propertyName the name of the property to set.
     * @param propertyValue the value of the property to set.
     * @throws PropertyException if an error happened while trying to set the property.
     */
    private static void setDirectProperty(Object target, String propertyName, Object propertyValue) throws PropertyException {
        Method setter = getSetter(target, propertyName);
        if (setter == null && !(target instanceof Map)) {
            throw new PropertyException("no writable setter for '" + propertyName + "' in class '" + target.getClass().getName() + "'");
        }
        Object transformedPropertyValue = propertyValue;
        if (setter != null && propertyValue != null) {
            Class parameterType = setter.getParameterTypes()[0];
            if (parameterType != null) {
                transformedPropertyValue = transform(propertyValue, parameterType);
            }
        }
        try {
            if (propertyValue != null) {
                if (setter == null) {
                    ((Map)target).put(propertyName, transformedPropertyValue);
                } else {
                    setter.invoke(target, new Object[]{transformedPropertyValue});
                }
            } else {
                if (setter == null) {
                    ((Map)target).put(propertyName, transformedPropertyValue);
                }   else {
                    setter.invoke(target, new Object[]{null});
                }
            }
        } catch (IllegalAccessException ex) {
            throw new PropertyException("property '" + propertyName + "' is not accessible", ex);
        } catch (InvocationTargetException ex) {
            throw new PropertyException("property '" + propertyName + "' access threw an exception", ex);
        } catch (Exception ex) {
            String msg = "Error setting property " + target.getClass().getSimpleName() + "." + propertyName + " to value " + transformedPropertyValue;
            throw new PropertyException(msg, ex);
        }
    }

    private static Map getNestedProperties(String prefix, Properties properties) {
        Map result = new HashMap();
        Iterator it = properties.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String name = (String) entry.getKey();
            String value = (String) entry.getValue();
            result.put(prefix + '.' + name, value);
        }
        return result;
    }

    /**
     * Try to transform the passed in value into the destinationClass, via a applying a boatload of
     * heuristics.
     *
     * @param value
     * @param destinationClass
     * @return
     */
    public static Object transform(Object value, Class destinationClass) {
        if (value == null) {
            if (destinationClass.equals(boolean.class)) {
                return false;
            }
            return null;
        }
        if (value.getClass() == destinationClass)
            return value;
        if (destinationClass.isInstance(value)) {
            return value;
        }

        // If target type is Date and json was a long, convert the long to a date
        if (destinationClass == Date.class && (value.getClass() == long.class || value.getClass() == Long.class)) {
            return new Date((long)value);
        }
        // Convert integers to longs, if target type is long
        if ((destinationClass == Long.class || destinationClass == long.class) && (value.getClass() == int.class || value.getClass() == Integer.class)) {
            return new Long((int)value);
        }
        // Convert ints and longs to ZonedDateTime, if ZonedDateTime was a long
        if (destinationClass == ZonedDateTime.class && (
                value.getClass() == long.class ||
                value.getClass() == Long.class ||
                value.getClass() == int.class ||
                value.getClass() == Integer.class
                )
            ) {
            if (value.getClass() == Integer.class || value.getClass() == int.class) {
                return ZonedDateTime.ofInstant(Instant.ofEpochMilli(((int) value) * 1000), GeneralUtils.UTC);
            } else {
                return ZonedDateTime.ofInstant(Instant.ofEpochMilli((long) value), GeneralUtils.UTC);
            }
        }

        if (destinationClass == ZonedDateTime.class && value instanceof Timestamp) {
            return ZonedDateTime.ofInstant(((Timestamp) value).toInstant(), UTC);
        }
        if (destinationClass == Long.class && value instanceof BigInteger) {
            return ((BigInteger) value).longValue();
        }

        if (destinationClass == ZonedDateTime.class && (value.getClass() == double.class || value.getClass() == Double.class)) {
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(Math.round((Double)value)), GeneralUtils.UTC);
        }

        // Convert Strings to Enums, if target type was an enum
        if (destinationClass.isEnum()) {
            return Enum.valueOf(destinationClass, value.toString());
        }

        if ((destinationClass == boolean.class || destinationClass == Boolean.class)) {
            if (value instanceof String) {
                return Boolean.valueOf((String) value);
            } else if (value instanceof Integer) {
                return (Integer)value > 0;
            } else if (value instanceof Long) {
                return (Long)value > 0;
            }  else if (value == null) {
                return false;
            }
        }

        if ((destinationClass == byte.class || destinationClass == Byte.class)  &&  value.getClass() == String.class) {
            return new Byte((String) value);
        }
        if ((destinationClass == short.class || destinationClass == Short.class)  &&  value.getClass() == String.class) {
            return new Short((String) value);
        }
        if ((destinationClass == int.class || destinationClass == Integer.class)  &&  value.getClass() == String.class) {
            return new Integer((String) value);
        }
        if ((destinationClass == long.class || destinationClass == Long.class)  &&  value.getClass() == String.class) {
            return new Long((String) value);
        }
        if ((destinationClass == long.class || destinationClass == Long.class)  &&  value instanceof Integer) {
            return new Long((Integer) value);
        }
        if ((destinationClass == float.class || destinationClass == Float.class)  &&  value.getClass() == String.class) {
            return new Float((String) value);
        }
        if ((destinationClass == float.class || destinationClass == Float.class)  &&  value.getClass() == Integer.class) {
            return ((Integer)value).floatValue();
        }
        if ((destinationClass == float.class || destinationClass == Float.class)  &&  value.getClass() == Long.class) {
            return ((Long)value).floatValue();
        }
        if ((destinationClass == float.class || destinationClass == Float.class)  &&  value.getClass() == Double.class) {
            return ((Double)value).floatValue();
        }


        if ((destinationClass == double.class || destinationClass == Double.class)  &&  value.getClass() == Long.class) {
            return ((Long)value).floatValue();
        }


        if ((destinationClass == float.class || destinationClass == Float.class)  &&  value.getClass() == String.class) {
            return new Float((String) value);
        }

        if ((destinationClass == double.class || destinationClass == Double.class)  &&  value.getClass() == String.class) {
            return new Double((String) value);
        }

        // If the type mis-match is due to boxing, just return the value
        if (    value.getClass() == boolean.class || value.getClass() == Boolean.class ||
                value.getClass() == byte.class || value.getClass() == Byte.class ||
                value.getClass() == short.class || value.getClass() == Short.class ||
                value.getClass() == int.class || value.getClass() == Integer.class ||
                value.getClass() == long.class || value.getClass() == Long.class ||
                value.getClass() == float.class || value.getClass() == Float.class ||
                value.getClass() == double.class || value.getClass() == Double.class
                )
            return value;

        throw new PropertyException("cannot convert values of type '" + value.getClass().getName() + "' into type '" + destinationClass + "'");
    }

    private static void callSetter(Object target, String propertyName, Object parameter) throws PropertyException {
        Method setter = getSetter(target, propertyName);
        try {
            setter.invoke(target, new Object[] {parameter});
        } catch (IllegalAccessException ex) {
            throw new PropertyException("property '" + propertyName + "' is not accessible", ex);
        } catch (InvocationTargetException ex) {
            throw new PropertyException("property '" + propertyName + "' access threw an exception", ex);
        }
    }

    private static Object callGetter(Object target, String propertyName) throws PropertyException {
        Method getter = getGetter(target, propertyName);
        try {
            return getter.invoke(target, (Object[]) null); // casting to (Object[]) b/c of javac 1.5 warning
        } catch (IllegalAccessException ex) {
            throw new PropertyException("property '" + propertyName + "' is not accessible", ex);
        } catch (InvocationTargetException ex) {
            throw new PropertyException("property '" + propertyName + "' access threw an exception", ex);
        }
    }

    private static Method getSetter(Object target, String propertyName) {
        if (propertyName == null || "".equals(propertyName))
            throw new PropertyException("encountered invalid null or empty property name");
        String setterName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        Method[] methods = target.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(setterName)  &&  method.getParameterTypes().length == 1) {
                return method;
            }
        }
        return null;
    }

    public static Method getGetter(Object target, String propertyName) {
        String cacheKey = "getGetter" + "|" + target.getClass().getCanonicalName() + "|" + propertyName;

        if (lookupCache.containsKey(cacheKey)) {
            return (Method)lookupCache.get(cacheKey);
        }
        String getterName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        String getterIsName = "is" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        Method[] methods = target.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if ((method.getName().equals(getterName) || method.getName().equals(getterIsName))  &&  !method.getReturnType().equals(void.class)  &&  method.getParameterTypes().length == 0) {
                lookupCache.put(cacheKey, method);
                return method;
            }
        }
        lookupCache.put(cacheKey, null);
        throw new PropertyException("no readable property '" + propertyName + "' in class '" + target.getClass().getName() + "'");
    }

    private static Class getPropertyType(Object target, String propertyName) {
        String getterName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        String getterIsName = "is" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        Method[] methods = target.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if ((method.getName().equals(getterName) || method.getName().equals(getterIsName))  &&  !method.getReturnType().equals(void.class)  &&  method.getParameterTypes().length == 0) {
                return method.getReturnType();
            }
        }
        throw new PropertyException("no property '" + propertyName + "' in class '" + target.getClass().getName() + "'");
    }

}

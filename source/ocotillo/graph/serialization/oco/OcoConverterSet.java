/**
 * Copyright © 2014-2015 Paolo Simonetto
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ocotillo.graph.serialization.oco;

import java.util.HashMap;
import java.util.Map;

/**
 * A collection of converters for the most common data types.
 */
public class OcoConverterSet {

    private final Map<Class<?>, OcoValueConverter<?>> typeClassMap = new HashMap<>();
    private final Map<String, OcoValueConverter<?>> typeNameMap = new HashMap<>();

    /**
     * Construct a collection of default converters.
     */
    public OcoConverterSet() {
        put(new OcoValueConverter.BooleanConverter());
        put(new OcoValueConverter.IntegerConverter());
        put(new OcoValueConverter.DoubleConverter());
        put(new OcoValueConverter.StringConverter());
        put(new OcoValueConverter.CoordinatesConverter());
        put(new OcoValueConverter.ColorConverter());
        put(new OcoValueConverter.NodeShapeConverter());
        put(new OcoValueConverter.EdgeShapeConverter());
        put(new OcoValueConverter.ControlPointsConverter());
    }

    /**
     * Adds or substitutes a default converter to the collection.
     *
     * @param converter the converter.
     */
    public final void put(OcoValueConverter<?> converter) {
        typeClassMap.put(converter.typeClass(), converter);
        typeNameMap.put(converter.typeName(), converter);
    }

    /**
     * Gets the default converter associated with the given type.
     *
     * @param <T> the type of object handled.
     * @param typeClass the type supported by the converter.
     * @return the converter.
     */
    @SuppressWarnings("unchecked")
    public <T> OcoValueConverter<T> get(Class<T> typeClass) {
        return (OcoValueConverter<T>) typeClassMap.get(typeClass);
    }

    /**
     * Gets the default converter associated with the given type.
     *
     * @param <T> the type of object handled.
     * @param typeName the type supported by the converter.
     * @return the converter.
     */
    @SuppressWarnings("unchecked")
    public <T> OcoValueConverter<T> get(String typeName) {
        return (OcoValueConverter<T>) typeNameMap.get(typeName);
    }

    /**
     * Verifies if it contains a default converter for the giver type.
     *
     * @param typeClass the type class.
     * @return true if the collection contains it, false otherwise.
     */
    public boolean contains(Class<?> typeClass) {
        return typeClassMap.containsKey(typeClass);
    }

    /**
     * Verifies if it contains a default converter for the giver type.
     *
     * @param typeName the type name.
     * @return true if the collection contains it, false otherwise.
     */
    public boolean contains(String typeName) {
        return typeNameMap.containsKey(typeName);
    }

    /**
     * Removes a default converter from the collection.
     *
     * @param typeClass the class for which removing the converter.
     */
    public void remove(Class<?> typeClass) {
        OcoValueConverter<?> removedConverter = typeClassMap.remove(typeClass);
        String typeName = null;
        for (Map.Entry<String, OcoValueConverter<?>> entry : typeNameMap.entrySet()) {
            if (removedConverter == entry.getValue()) {
                typeName = entry.getKey();
            }
        }
        typeNameMap.remove(typeName);
    }

    /**
     * Removes a default converter from the collection.
     *
     * @param typeName the class name for which removing the converter.
     */
    public void remove(String typeName) {
        OcoValueConverter<?> removedConverter = typeNameMap.remove(typeName);
        Class<?> typeClass = null;
        for (Map.Entry<Class<?>, OcoValueConverter<?>> entry : typeClassMap.entrySet()) {
            if (removedConverter == entry.getValue()) {
                typeClass = entry.getKey();
            }
        }
        typeClassMap.remove(typeClass);
    }

    /**
     * Clears the collection.
     */
    public void clear() {
        typeClassMap.clear();
        typeNameMap.clear();
    }

}

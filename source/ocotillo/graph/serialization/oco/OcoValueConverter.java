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

import ocotillo.geometry.Coordinates;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.StdAttribute.EdgeShape;
import ocotillo.graph.StdAttribute.NodeShape;
import ocotillo.graph.rendering.GraphicsTools;
import java.awt.Color;

/**
 * Converts values from graph to oco files and vice versa.
 *
 * @param <T> the type of value handled.
 */
public abstract class OcoValueConverter<T> {

    /**
     * Returns the object built according to the oco string specifications.
     *
     * @param value the specifications.
     * @return the built object.
     */
    public T ocoToGraphLib(String value) {
        return null;
    }

    /**
     * Returns the oco string that describes the given object.
     *
     * @param value the object.
     * @return the string description.
     */
    public String graphLibToOco(T value) {
        return value.toString();
    }

    /**
     * Returns the default value for this type.
     *
     * @return the default value.
     */
    public abstract T defaultValue();

    /**
     * Returns the type name, used to identify the type in the oco file.
     *
     * @return the default name.
     */
    public abstract String typeName();

    /**
     * Returns the type of values handled.
     *
     * @return the type class.
     */
    public abstract Class<?> typeClass();

    /**
     * Converter for boolean values.
     */
    public static class BooleanConverter extends OcoValueConverter<Boolean> {

        @Override
        public Boolean ocoToGraphLib(String value) {
            return Boolean.parseBoolean(value);
        }

        @Override
        public Boolean defaultValue() {
            return false;
        }

        @Override
        public String typeName() {
            return "Boolean";
        }

        @Override
        public Class<?> typeClass() {
            return Boolean.class;
        }

    }

    /**
     * Converter for integer values.
     */
    public static class IntegerConverter extends OcoValueConverter<Integer> {

        @Override
        public Integer ocoToGraphLib(String value) {
            return Integer.parseInt(value);
        }

        @Override
        public Integer defaultValue() {
            return 0;
        }

        @Override
        public String typeName() {
            return "Integer";
        }

        @Override
        public Class<?> typeClass() {
            return Integer.class;
        }

    }

    /**
     * Converter for double values.
     */
    public static class DoubleConverter extends OcoValueConverter<Double> {

        @Override
        public Double ocoToGraphLib(String value) {
            return Double.parseDouble(value);
        }

        @Override
        public Double defaultValue() {
            return 0.0;
        }

        @Override
        public String typeName() {
            return "Double";
        }

        @Override
        public Class<?> typeClass() {
            return Double.class;
        }

    }

    /**
     * Converter for string values.
     */
    public static class StringConverter extends OcoValueConverter<String> {

        @Override
        public String ocoToGraphLib(String value) {
            return value;
        }

        @Override
        public String defaultValue() {
            return "";
        }

        @Override
        public String typeName() {
            return "String";
        }

        @Override
        public Class<?> typeClass() {
            return String.class;
        }

    }

    /**
     * Converter for coordinates values.
     */
    public static class CoordinatesConverter extends OcoValueConverter<Coordinates> {

        @Override
        public Coordinates ocoToGraphLib(String value) {
            String[] components = value.split(",");
            Coordinates coordinates = new Coordinates(components.length);
            for (int i = 0; i < components.length; i++) {
                coordinates.setAt(i, Double.parseDouble(components[i]));
            }
            return coordinates;
        }

        @Override
        public String graphLibToOco(Coordinates value) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < value.dim(); i++) {
                builder.append(value.get(i)).append(",");
            }
            return builder.substring(0, builder.length() - 1);
        }

        @Override
        public Coordinates defaultValue() {
            return new Coordinates(0, 0);
        }

        @Override
        public String typeName() {
            return "Coordinates";
        }

        @Override
        public Class<?> typeClass() {
            return Coordinates.class;
        }

    }

    /**
     * Converter for color values.
     */
    public static class ColorConverter extends OcoValueConverter<Color> {

        @Override
        public Color ocoToGraphLib(String value) {
            return GraphicsTools.colorHexReader(value);
        }

        @Override
        public String graphLibToOco(Color value) {
            return GraphicsTools.colorHexWriter(value);
        }

        @Override
        public Color defaultValue() {
            return Color.BLACK;
        }

        @Override
        public String typeName() {
            return "Color";
        }

        @Override
        public Class<?> typeClass() {
            return Color.class;
        }

    }

    /**
     * Converter for node shape values.
     */
    public static class NodeShapeConverter extends OcoValueConverter<NodeShape> {

        @Override
        public NodeShape ocoToGraphLib(String value) {
            for (NodeShape candidateShape : NodeShape.values()) {
                if (value.equals(candidateShape.name())) {
                    return candidateShape;
                }
            }
            throw new IllegalArgumentException("The node shape " + value + " is not supported.");
        }

        @Override
        public String graphLibToOco(NodeShape value) {
            return value.name();
        }

        @Override
        public NodeShape defaultValue() {
            return NodeShape.cuboid;
        }

        @Override
        public String typeName() {
            return "NodeShape";
        }

        @Override
        public Class<?> typeClass() {
            return NodeShape.class;
        }

    }

    /**
     * Converter for edge shape values.
     */
    public static class EdgeShapeConverter extends OcoValueConverter<EdgeShape> {

        @Override
        public EdgeShape ocoToGraphLib(String value) {
            for (EdgeShape candidateShape : EdgeShape.values()) {
                if (value.equals(candidateShape.name())) {
                    return candidateShape;
                }
            }
            throw new IllegalArgumentException("The edge shape " + value + " is not supported.");
        }

        @Override
        public String graphLibToOco(EdgeShape value) {
            return value.name();
        }

        @Override
        public EdgeShape defaultValue() {
            return EdgeShape.polyline;
        }

        @Override
        public String typeName() {
            return "EdgeShape";
        }

        @Override
        public Class<?> typeClass() {
            return EdgeShape.class;
        }

    }

    /**
     * Converter for edge control points values.
     */
    public static class ControlPointsConverter extends OcoValueConverter<ControlPoints> {

        @Override
        public ControlPoints ocoToGraphLib(String value) {
            ControlPoints controlPoints = new ControlPoints();
            String[] pointStrings = value.split(" ");
            for (String pointString : pointStrings) {
                String[] components = pointString.split(",");
                Coordinates coordinates = new Coordinates(components.length);
                for (int i = 0; i < components.length; i++) {
                    coordinates.setAt(i, Double.parseDouble(components[i]));
                }
                controlPoints.add(coordinates);
            }
            return controlPoints;

        }

        @Override
        public String graphLibToOco(ControlPoints value) {
            StringBuilder builder = new StringBuilder();
            for (Coordinates point : value) {
                for (int i = 0; i < point.dim(); i++) {
                    builder.append(point.get(i)).append(",");
                }
                builder.append(" ");
            }
            return builder.substring(0, Math.max(builder.length() - 1, 0));
        }

        @Override
        public ControlPoints defaultValue() {
            return new ControlPoints();
        }

        @Override
        public String typeName() {
            return "ControlPoints";
        }

        @Override
        public Class<?> typeClass() {
            return ControlPoints.class;
        }

    }

}

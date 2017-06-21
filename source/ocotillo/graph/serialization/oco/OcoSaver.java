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

import ocotillo.graph.Graph;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes oco files.
 */
public class OcoSaver {

    public OcoConverterSet converters = new OcoConverterSet();    // TODO

    /**
     * Reads a file and generates a graph.
     *
     * @param file the input dot file.
     * @return the generated graph.
     */
    public Graph readFile(File file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("The file " + file.getName() + "is not readable.");
        }
        return read(lines);
    }

    /**
     * Reads the given oco lines and generates a graph.
     *
     * @param lines the lines in oco format.
     * @return the generated graph.
     */
    public Graph read(List<String> lines) {
        OcoReader reader = new OcoReader(converters);
        return reader.read(lines);
    }

    /**
     * Writes a graph in the given file.
     *
     * @param graph the graph to write.
     * @param file the destination file.
     */
    public void writeFile(Graph graph, File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            List<String> lines = write(graph);
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException ex) {
            System.err.println("The file " + file.getName() + "is not writable.");
        }
    }

    /**
     * Writes a graph in the oco format.
     *
     * @param graph the input graph.
     * @return the graph description in oco format.
     */
    public List<String> write(Graph graph) {
        OcoWriter writer = new OcoWriter(converters);
        return writer.write(graph);
    }

}

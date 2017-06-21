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
package ocotillo.gui.quickview;

import ocotillo.graph.Graph;
import ocotillo.gui.GraphCanvas;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class QuickView extends JFrame {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a QuickView for the graph.
     *
     * @param graph the graph to be visualized.
     */
    public QuickView(Graph graph) {
        setTitle("Graph QuickView");
        add(new GraphCanvas(graph));
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * Runs a new instance of QuickView to display the given graph.
     *
     * @param graph the graph to be visualized.
     */
    public static void showNewWindow(final Graph graph) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                QuickView sk = new QuickView(graph);
                sk.setVisible(true);
            }
        });

    }

}

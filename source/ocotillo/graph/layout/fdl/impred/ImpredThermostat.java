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
package ocotillo.graph.layout.fdl.impred;

/**
 * Temperature controller for the ImPrEd algorithm.
 */
public abstract class ImpredThermostat extends ImpredElement {

    protected double temperature;

    protected abstract void updateTemperature(int currentIteration, int numberOfIterations);

    /**
     * Thermostat that keeps the temperature constant throughout the
     * computation.
     */
    protected static class ConstantTemperature extends ImpredThermostat {

        public ConstantTemperature(double temperature) {
            assert (0 <= temperature && temperature <= 1) : "The temperature must be in the range [0,1]";
            this.temperature = temperature;
        }

        @Override
        protected void updateTemperature(int currentIteration, int numberOfIterations) {
        }

    }

    /**
     * Thermostat that decreases linearly the temperature according to the
     * iterations executed.
     */
    protected static class LinearCoolDown extends ImpredThermostat {

        public LinearCoolDown() {
            this.temperature = 1;
        }

        @Override
        protected void updateTemperature(int currentIteration, int numberOfIterations) {
            temperature = ((double) numberOfIterations - currentIteration) / numberOfIterations;
        }

    }
}

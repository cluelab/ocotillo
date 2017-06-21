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
package ocotillo.graph.rendering.svg;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class SvgDocumentTest {

    @Test
    public void testTransformColorAlpha() throws Exception {
        String original = "<rect fill=\"#00000000\" />";
        String desired = "<rect fill=\"#000000\" opacity=\"0.0\" />";
        
        String tranformed = Whitebox.<String>invokeMethod(SvgDocument.class, "transformColorAlpha", original);
        assertThat(tranformed, is(desired));

        original = "<rect stroke=\"#00000000\" />";
        desired = "<rect stroke=\"#000000\" stroke-opacity=\"0.0\" />";
        
        tranformed = Whitebox.<String>invokeMethod(SvgDocument.class, "transformColorAlpha", original);
        assertThat(tranformed, is(desired));

        original = "<rect fill=\"#00000000\" stroke=\"#00000000\" />";
        desired = "<rect fill=\"#000000\" opacity=\"0.0\" stroke=\"#000000\" stroke-opacity=\"0.0\" />";
        
        tranformed = Whitebox.<String>invokeMethod(SvgDocument.class, "transformColorAlpha", original);
        assertThat(tranformed, is(desired));
    }

}

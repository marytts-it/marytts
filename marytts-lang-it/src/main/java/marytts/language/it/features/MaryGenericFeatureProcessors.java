/**
 * Portions Copyright 2006-2007 DFKI GmbH.
 * Portions Copyright 2001 Sun Microsystems, Inc.
 * Portions Copyright 1999-2001 Language Technologies Institute, 
 * Carnegie Mellon University.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * Permission is hereby granted, free of charge, to use and distribute
 * this software and its documentation without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of this work, and to
 * permit persons to whom this work is furnished to do so, subject to
 * the following conditions:
 * 
 * 1. The code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 * 2. Any modifications must be clearly marked as such.
 * 3. Original authors' names are not deleted.
 * 4. The authors' names are not used to endorse or promote products
 *    derived from this software without specific prior written
 *    permission.
 *
 * DFKI GMBH AND THE CONTRIBUTORS TO THIS WORK DISCLAIM ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS, IN NO EVENT SHALL DFKI GMBH NOR THE
 * CONTRIBUTORS BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
 * PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS
 * ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */

package marytts.language.it.features;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import marytts.datatypes.MaryXML;
import marytts.features.ByteValuedFeatureProcessor;

import marytts.unitselection.select.Target;
import marytts.util.dom.MaryDomUtils;
import marytts.util.string.ByteStringTranslator;

import org.w3c.dom.Element;
import org.w3c.dom.traversal.TreeWalker;

/**
 * A collection of feature for Italian processors that operate on Target objects. 

 * 
 * @author Fabio Tesser
 * 
 */
public class MaryGenericFeatureProcessors

extends marytts.features.MaryGenericFeatureProcessors {

	/**
	 * Determine the prosodic property of a target for Italian
	 * 
	 * @author Fabio Tesser
	 * 
	 */
	public static class Selection_Prosody implements ByteValuedFeatureProcessor {

		protected TargetElementNavigator navigator;
		protected ByteStringTranslator values = new ByteStringTranslator(
				new String[] { "0", "stressed", "pre-nucleareeeee", "nuclear",
						"finalHigh", "finalLow", "final" });
		private Set<String> lowEndtones = new HashSet<String>(
				Arrays.asList(new String[] { "L-", "L-%", "L-L%" }));
		private Set<String> highEndtones = new HashSet<String>(
				Arrays.asList(new String[] { "H-", "!H-", "H-%", "H-L%",
						"!H-%", "H-^H%", "!H-^H%", "L-H%", "H-H%" }));

		public Selection_Prosody(TargetElementNavigator syllableNavigator) {
			this.navigator = syllableNavigator;
		}

		public String getName() {
			return "selection_prosody";
		}

		public String[] getValues() {
			return values.getStringValues();
		}

		/**
		 * Determine the prosodic property of the target
		 * 
		 * @param target
		 *            the target
		 * @return 0 - unstressed, 1 - stressed, 2 - pre-nucleareeeee accent 3 -
		 *         nuclear accent, 4 - phrase final high, 5 - phrase final low,
		 *         6 - phrase final (with unknown high/low status).
		 */
		public byte process(Target target) {
			// first find out if syllable is stressed
			Element syllable = navigator.getElement(target);
			if (syllable == null)
				return (byte) 0;
			boolean stressed = false;
			if (syllable.getAttribute("stress").equals("1")) {
				stressed = true;
			}
			// find out if we have an accent
			boolean accented = syllable.hasAttribute("accent");
			boolean nuclear = true; // relevant only if accented == true
			// find out the position of the target
			boolean phraseFinal = false;
			String endtone = null;
			Element sentence = (Element) MaryDomUtils.getAncestor(syllable,
					MaryXML.SENTENCE);
			if (sentence == null)
				return 0;
			TreeWalker tw = MaryDomUtils.createTreeWalker(sentence,
					MaryXML.SYLLABLE, MaryXML.BOUNDARY);
			tw.setCurrentNode(syllable);
			Element e = (Element) tw.nextNode();
			if (e != null) {
				if (e.getTagName().equals(MaryXML.BOUNDARY)) {
					phraseFinal = true;
					endtone = e.getAttribute("tone");
				}
				if (accented) { // look forward for any accent
					while (e != null) {
						if (e.getTagName().equals(MaryXML.SYLLABLE)
								&& e.hasAttribute("accent")) {
							nuclear = false;
							break;
						}
						e = (Element) tw.nextNode();
					}
				}
			}
			// Now, we know:
			// stressed or not
			// accented or not
			// if accented, nuclear or not
			// if final, the endtone

			if (accented) {
				if (nuclear) {
					return values.get("nuclear");
				} else {
					return values.get("pre-nucleareeeee");
				}
			} else if (phraseFinal) {
				if (endtone != null && highEndtones.contains(endtone)) {
					return values.get("finalHigh");
				} else if (endtone != null && lowEndtones.contains(endtone)) {
					return values.get("finalLow");
				} else {
					return values.get("final");
				}
			} else if (stressed) {
				return values.get("stressed");
			}
			return (byte) 0;// return unstressed
		}
	}

}

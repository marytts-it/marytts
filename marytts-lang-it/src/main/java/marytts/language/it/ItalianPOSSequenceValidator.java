package marytts.language.it;

import java.util.Arrays;
import opennlp.tools.postag.TagDictionary;
import opennlp.tools.util.SequenceValidator;

/**
 * Implement a POSSequenceValidator to support deterministic symbols dictionary.
 * It permits to add constraints on tokens allowed to have deterministic labels.
 * It is useful to force punctuation symbols to have a specific label.
 * The following line is an example of a row in the deterministic dictionary file:
 * $PUNCT , . ! ? ;
 * 
 * In this way only the tokens , . ! ? ; are allowed to have the POS $PUNCT, and no others 
 * word are allowed to have the $PUNCT POS.
 * 
 * @author Fabio Tesser
 */
public class ItalianPOSSequenceValidator implements SequenceValidator<String> {
	// Classic tagDictionary
	protected TagDictionary tagDictionary;

	/**
	 * Inverse Tag dictionary used for deterministic token like punctuation
	 * symbols The dictionary file format is inverted to respect the
	 * tagdictionary the first column is for the POS label, the following are a
	 * list of ONLY tokens admitted to have the previous POS label
	 */
	protected TagDictionary deterministicSymbolsTagDictionary;

	public ItalianPOSSequenceValidator(TagDictionary tagDictionary, TagDictionary deterministic_symbols_tagdict) {
		this.tagDictionary = tagDictionary;
		this.deterministicSymbolsTagDictionary = deterministic_symbols_tagdict;
	}

	// DefaultPOSSequenceValidator 
	 /* public boolean validSequence(int i, String[] inputSequence, String[] outcomesSequence, String outcome) {
		if (tagDictionary == null) {
			System.out.println("tagDictionary = null");
			return true;
		} else {
			String[] tags = tagDictionary.getTags(inputSequence[i].toString());
			if (tags == null) {
				return true;
			} else {
				return Arrays.asList(tags).contains(outcome);
			}
		}
	}
	*/
	
	/*
	 * (non-Javadoc)
	 * deterministicSymbolsTagDictionary SequenceValidator for punctuation
	 * @see opennlp.tools.util.SequenceValidator#validSequence(int, T[], java.lang.String[], java.lang.String)
	 */
	public boolean validSequence(int i, String[] inputSequence, String[] outcomesSequence, String outcome) {
		String[] tags = null;
		boolean tmp = false;
		if (deterministicSymbolsTagDictionary != null) {
			tags = deterministicSymbolsTagDictionary.getTags(outcome);
			if (tags != null) {
				// OK we are talking about deterministic POS (i.e $PUNCT)
				tmp = Arrays.asList(tags).contains(inputSequence[i].toString());
				if (!tmp) {
					// if det_tagDictionary (i.e $PUNCT) does not contain the
					// input sequence (i.e. ",")
					return false;
				}
			}
			// OK the det_tagDictionary contains the correct input sequence
			// (i.e. ",") or tags == null
			// check for normal tag dict
		} 
		if (tagDictionary == null) {
			System.out.println("tagDictionary = null");
			return true;
		} else {
			tags = tagDictionary.getTags(inputSequence[i].toString());
			if (tags == null) {
				// we are not talking about probabilistic POS (i.e adjective,
				// verbs
				// ... )
				return true;
			} else {
				// we are talking about about probabilistic POS (i.e adjective,
				// verbs ... )
				// return true if the outcome is OK with that
				return Arrays.asList(tags).contains(outcome);
			}
		}
	}

}



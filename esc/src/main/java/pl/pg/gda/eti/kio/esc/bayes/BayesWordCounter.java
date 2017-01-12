package pl.pg.gda.eti.kio.esc.bayes;

import pl.pg.gda.eti.kio.esc.data.WordFeature;

import java.util.List;

/**
 * @author Wojciech Stanisławski
 * @since 12.01.17
 */
public class BayesWordCounter {

	public int countWordsInDictionary(List<WordFeature> dictionary) {
		return dictionary.size();
	}
}

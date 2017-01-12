package pl.pg.gda.eti.kio.esc.bayes;

import java.util.Map;

/**
 * @author Wojciech Stanisławski
 * @since 12.01.17
 */
public class BayesWordInCategoryCounter {

	public /* kluczem jest nazwa kategorii */ Map<String, WordsInCategory> countWordsInCategories() {
		return null;
	}


	public class WordsInCategory {
		public String category;
		public /* kluczem jest nazwa słowa */ Map<String, Integer> wordCountInThisCategory;
		public int sumWordCountInThisCategory;
	}
}

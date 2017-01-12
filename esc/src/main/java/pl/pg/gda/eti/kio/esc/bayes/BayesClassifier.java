package pl.pg.gda.eti.kio.esc.bayes;

import pl.pg.gda.eti.kio.esc.WordDictionaryMerger;

import java.io.IOException;
import java.util.Map;

/**
 * @author Wojciech Stanisławski
 * @since 12.01.17
 */
public class BayesClassifier {
	
	public static void main(String[] args) throws IOException {
	//public void classify(/* coś tu będzie */) {
		WordDictionaryMerger merger = new WordDictionaryMerger();
		merger.mergeFiles("simple/temp-po_slowach-feature_dict-simple-20120104", "en/en-po_slowach-feature_dict-en-20111201", 1);

		BayesCategoryCounter bayesCategoryCounter = new BayesCategoryCounter();
		BayesCategoryCounter.CategoryStatistics categoryStatistics = bayesCategoryCounter.countCategories("simple/temp-po_slowach-categories-simple-20120104");

		BayesWordCounter bayesWordCounter = new BayesWordCounter();
		int wordsInDictionary = bayesWordCounter.countWordsInDictionary(merger.getChunks()[0]);

		BayesWordInCategoryCounter bayesWordInCategoryCounter = new BayesWordInCategoryCounter();
		Map<String, BayesWordInCategoryCounter.WordsInCategory> stringWordsInCategoryMap = bayesWordInCategoryCounter.countWordsInCategories("simple/temp-po_slowach-categories-simple-20120104", "simple/temp-po_slowach-lista-simple-20120104", merger.getChunks()[0]);
	}
}

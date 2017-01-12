package pl.pg.gda.eti.kio.esc.bayes;

import java.io.IOException;
import java.util.Map;

/**
 * @author Wojciech Stanisławski
 * @since 12.01.17
 */
public class BayesClassifier {
	
	public static void main(String[] args) throws IOException {
	//public void classify(/* coś tu będzie */) {
		BayesCategoryCounter bayesCategoryCounter = new BayesCategoryCounter();
		BayesCategoryCounter.CategoryStatistics categoryStatistics = bayesCategoryCounter.countCategories("simple/temp-po_slowach-categories-simple-20120104");

		BayesWordCounter bayesWordCounter = new BayesWordCounter();
		int wordsInDictionary = bayesWordCounter.countWordsInDictionary();
		
		BayesWordInCategoryCounter bayesWordInCategoryCounter = new BayesWordInCategoryCounter();
		BayesWordInCategoryCounter.WordsInCategory wordsInCategory = bayesWordInCategoryCounter.countWordsInCategories();
	}
}

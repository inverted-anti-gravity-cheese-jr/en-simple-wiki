package pl.pg.gda.eti.kio.esc;

import pl.pg.gda.eti.kio.esc.bayes.BayesWordInCategoryCounter;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Wojciech Stanisławski
 * @since 12.01.17
 */
public class TestingPurpouses {

	public static void main(String[] args) throws IOException {
		WordDictionaryMerger merger = new WordDictionaryMerger();
		merger.mergeFiles("simple/temp-po_slowach-feature_dict-simple-20120104", "en/en-po_slowach-feature_dict-en-20111201", 1);

		BayesWordInCategoryCounter bayesWordInCategoryCounter = new BayesWordInCategoryCounter();
		Map<String, BayesWordInCategoryCounter.WordsInCategory> stringWordsInCategoryMap = bayesWordInCategoryCounter.countWordsInCategories("simple/temp-po_slowach-categories-simple-20120104", "simple/temp-po_slowach-lista-simple-20120104", merger.getChunks()[0]);


		System.out.println(stringWordsInCategoryMap.size());

		/*

		stringWordsInCategoryMap.forEach((k, v) -> {
			System.out.println(k +": " + v.sumWordCountInThisCategory + " [");
			v.wordCountInThisCategory.forEach((k2, v2) -> {
				System.out.println("\t" + k2 + ": " + v2);
			});
			System.out.println("]");
		});

		// */
	}
}

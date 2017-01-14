package pl.pg.gda.eti.kio.esc.bayes;

import pl.pg.gda.eti.kio.esc.WordDictionaryMerger;
import pl.pg.gda.eti.kio.esc.data.WordFeature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Wojciech Stanisławski, Krzysztof Świeczkowski
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
			
		//artykuly do przypasowania	
		WordDictionaryMerger wordDictionaryMerger = new WordDictionaryMerger();
		wordDictionaryMerger.mergeFiles("simple/temp-po_slowach-feature_dict-simple-20120104", "en/en-po_slowach-feature_dict-en-20111201", 1);
		List<WordFeature> mergedDictionary = wordDictionaryMerger.getChunks()[0];
		
		//estymaty prawdopodobienstw warunkowych
			
		//Ładowanie pliku		
		File file = new File("en/en-po_slowach-lista-en-20111201");
		BufferedReader stream = new BufferedReader(new FileReader(file));
		String line;
		//foreach article
		while ((line = stream.readLine()) != null) {
			//geting article id
			String[] elements = line.split("#");
			String articleId = elements[0];
			//getting words in article
			List<WordFeature> wordFeaturesInArticle = new ArrayList<WordFeature>();
			String[] words = elements[1].split(" ");
			for(int  i=0; i < words.length; i++) {
				String wordId = words[i].split("-")[0];
				for (WordFeature feature : mergedDictionary) {
					if (wordId.equals(feature.getEnId())) {
						wordFeaturesInArticle.add(feature);
					}
			    }
			}
			//foreach class in simple wiki
			for (Map.Entry<String, BayesWordInCategoryCounter.WordsInCategory> entry : stringWordsInCategoryMap.entrySet()) {
				String key = entry.getKey();
				BayesWordInCategoryCounter.WordsInCategory value = entry.getValue();
				//foreach word in article
				for(WordFeature wordFeature : wordFeaturesInArticle) {
					if(value.wordCountInThisCategory.get(wordFeature) != null) {
						//System.out.println(value.wordCountInThisCategory.get(wordFeature));
						//liczba wystąpień słowa w dokumentach klasy + 1/liczba słów w klasie + rozmiar słownika
						double estymatPrawdopodobienstwaWarunkowego = (double)(value.wordCountInThisCategory.get(wordFeature) + 1) / (value.sumWordCountInThisCategory + wordsInDictionary);
						System.out.println(estymatPrawdopodobienstwaWarunkowego);
					}
				}
			}
		}
		stream.close();
		
	}
}

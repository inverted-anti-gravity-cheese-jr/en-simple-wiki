package pl.pg.gda.eti.kio.esc.bayes;

import pl.pg.gda.eti.kio.esc.WordDictionaryMerger;
import pl.pg.gda.eti.kio.esc.bayes.BayesConditionalProbability.ConditionalProbabilityForClass;
import pl.pg.gda.eti.kio.esc.data.WordFeature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Wojciech Stanisławski, Krzysztof Świeczkowski
 * @since 12.01.17
 */
public class BayesClassifier {
	
	public static void main(String[] args) throws IOException {
	//public void classify(/* coś tu będzie */) {
		BayesClassificationForArticles classification = new BayesClassificationForArticles();
		
		WordDictionaryMerger merger = new WordDictionaryMerger();
		merger.mergeFiles("simple/temp-po_slowach-feature_dict-simple-20120104", "en/en-po_slowach-feature_dict-en-20111201", 1);

		BayesCategoryCounter bayesCategoryCounter = new BayesCategoryCounter();
		BayesCategoryCounter.CategoryStatistics categoryStatistics = bayesCategoryCounter.countCategories("simple/temp-po_slowach-categories-simple-20120104");

		BayesWordCounter bayesWordCounter = new BayesWordCounter();
		int wordsInDictionary = bayesWordCounter.countWordsInDictionary(merger.getChunks()[0]);

		BayesWordInCategoryCounter bayesWordInCategoryCounter = new BayesWordInCategoryCounter();
		Map<String, BayesWordInCategoryCounter.WordsInCategory> stringWordsInCategoryMap = bayesWordInCategoryCounter.countWordsInCategories("simple/temp-po_slowach-categories-simple-20120104", "simple/temp-po_slowach-lista-simple-20120104", merger.getChunks()[0]);
		
		BayesConditionalProbability bayesConditionalProbability = new BayesConditionalProbability();
		Map<String, BayesConditionalProbability.ConditionalProbabilityForClass> conditionalProbability = bayesConditionalProbability.countConditionalProbability(stringWordsInCategoryMap, wordsInDictionary);
			
		//artykuly do przypasowania	
		List<WordFeature> mergedDictionary = merger.getChunks()[0];
		
		//Ładowanie pliku		
		File file = new File("en/en-po_slowach-lista-en-20111201");
		BufferedReader stream = new BufferedReader(new FileReader(file));
		String line;
		//foreach article
		while ((line = stream.readLine()) != null) {
			//geting article id
			String[] elements = line.split("#");
			if(elements.length < 2) {
				continue;
			}
			String articleId = elements[0];
			//getting words in article
			List<WordFeature> wordFeaturesInArticle = new ArrayList<WordFeature>();
			String[] words = elements[1].split(" ");
			//foreach word get wordFeatures
			for(int  i=0; i < words.length; i++) {
				String wordId = words[i].split("-")[0];
				for (WordFeature feature : mergedDictionary) {
					if (wordId.equals(feature.getEnId())) {
						wordFeaturesInArticle.add(feature);
					}
			    }
			}
			Map<Double, String> bayesClassificationForArticle = new TreeMap<Double, String>();
			//TODO: create return object
			//foreach class check if wordfeature exists and count probability
			for (Map.Entry<String, BayesConditionalProbability.ConditionalProbabilityForClass> conditionalProbabilityForClass : conditionalProbability.entrySet()) {
				Double bayesClassification = (double) categoryStatistics.articlesInCategoriesCount.get(conditionalProbabilityForClass.getKey()) / categoryStatistics.articlesCount; //TODO: podmienic na prawdopodobienstwo przynaleznosci dokumentu do kazdej z kategorii
				//liczba kategorii w klasie przez liczbe wszystkich art 
				for (WordFeature wordFeatureInArticle : wordFeaturesInArticle) {
					if(conditionalProbabilityForClass.getValue().conditionalProbabilityForWordInClass.containsKey(wordFeatureInArticle)) {
						bayesClassification *= conditionalProbabilityForClass.getValue().conditionalProbabilityForWordInClass.get(wordFeatureInArticle);
					}
				}
				bayesClassificationForArticle.put(bayesClassification, conditionalProbabilityForClass.getKey());			
			}
			classification.put(articleId, bayesClassificationForArticle);
		}
		stream.close();

		classification.getClassificationResults(2);
	}
	
	public static class BayesClassificationForArticles {
		Map<String, Map<Double, String>> classification;
		
		public BayesClassificationForArticles() {
			classification = new HashMap<String, Map<Double, String>>();
		}
				
		public void getClassificationResults(Integer noOfBestClasses) {
			for (Map.Entry<String, Map<Double, String>> classificationForArticle : classification.entrySet()) {
				String result = classificationForArticle.getKey() + "\t";
				int counter = noOfBestClasses;
				for (Map.Entry<Double, String> bayesClassification : classificationForArticle.getValue().entrySet()) {
					if(counter == 0) {
						break;
					}
					result += "(" + bayesClassification.getValue().toString() + "," + bayesClassification.getKey() + ")\t";
					counter--;
				}
				System.out.println(result);
			}
		}
		
		public void put(String articleId, Map<Double, String> bayesClassificationForArticle) {
			classification.put(articleId, bayesClassificationForArticle);
		}
	}
}
package pl.pg.gda.eti.kio.esc.bayes;

import pl.pg.gda.eti.kio.esc.DictionaryUtil;
import pl.pg.gda.eti.kio.esc.TestingPurpouses;
import pl.pg.gda.eti.kio.esc.TimeCounter;
import pl.pg.gda.eti.kio.esc.WordDictionaryMerger;
import pl.pg.gda.eti.kio.esc.bayes.BayesConditionalProbability.ConditionalProbabilityForClass;
import pl.pg.gda.eti.kio.esc.data.Tuple;
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

	public static void classify(BayesClassificationSettings settings) throws IOException {
		TimeCounter time = new TimeCounter();
		Map<String, String> predictedCategoriesMap = new TreeMap<>();

		DictionaryUtil.articleFinderInit(settings.enArticleDict);
		DictionaryUtil.categoryFinderInit(settings.simpleCategoryDict);

		WordDictionaryMerger merger = new WordDictionaryMerger();
		time.start();
		merger.mergeFiles(settings.enFeatureDict, settings.simpleFeatureDict, 1);
		time.end();
		time.printMessage("Reading dicts");


		BayesCategoryCounter bayesCategoryCounter = new BayesCategoryCounter();
		time.start();
		BayesCategoryCounter.CategoryStatistics categoryStatistics = bayesCategoryCounter.countCategories(settings.simpleArticleCategoryDict);
		time.end();
		time.printMessage("Calculating classes");

		BayesWordCounter bayesWordCounter = new BayesWordCounter();
		time.start();
		int wordsInDictionary = bayesWordCounter.countWordsInDictionary(merger.getChunks()[0]);
		time.end();
		time.printMessage("Calculating words");

		BayesWordInCategoryCounter bayesWordInCategoryCounter = new BayesWordInCategoryCounter();
		time.start();
		Map<String, BayesWordInCategoryCounter.WordsInCategory> stringWordsInCategoryMap = bayesWordInCategoryCounter.countWordsInCategories(settings.simpleArticleCategoryDict, settings.simpleWordArticleDict, merger.getChunks()[0]);
		time.end();
		time.printMessage("Counting words inside classes");

		BayesConditionalProbability bayesConditionalProbability = new BayesConditionalProbability();
		time.start();
		Map<String, BayesConditionalProbability.ConditionalProbabilityForClass> conditionalProbability = bayesConditionalProbability.countConditionalProbability(stringWordsInCategoryMap, merger.getChunks()[0], wordsInDictionary);
		time.end();
		time.printMessage("Calculating word in class probabilities");

		//artykuly do przypasowania	
		List<WordFeature> mergedDictionary = merger.getChunks()[0];
		
		//Ładowanie pliku
		time.start();
		File file = new File(settings.enWordArticleDict);
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

			BayesClassificationResultMap bayesClassificationForArticle = new BayesClassificationResultMap();
			Map.Entry<String, Double> predictedValue = null;

			//foreach class check if wordfeature exists and count probability
			for (Map.Entry<String, BayesConditionalProbability.ConditionalProbabilityForClass>
					conditionalProbabilityForClass : conditionalProbability.entrySet()) {
				Double bayesClassification = (double) categoryStatistics.articlesInCategoriesCount.get(conditionalProbabilityForClass.getKey()) / categoryStatistics.articlesCount;

				if(TestingPurpouses.DEBUG) {
					System.out.println("p(" + conditionalProbabilityForClass.getKey() + ")" + " = " + categoryStatistics.articlesInCategoriesCount.get(conditionalProbabilityForClass.getKey()) + " / " + categoryStatistics.articlesCount + " = " + bayesClassification);
				}

				//liczba kategorii w klasie przez liczbe wszystkich art 
				for (WordFeature wordFeatureInArticle : wordFeaturesInArticle) {
					if(conditionalProbabilityForClass.getValue().conditionalProbabilityForWordInClass.containsKey(wordFeatureInArticle)) {
						bayesClassification *= conditionalProbabilityForClass.getValue().conditionalProbabilityForWordInClass.get(wordFeatureInArticle);
					}
				}

				if(TestingPurpouses.DEBUG) {
					System.out.println("p = " + bayesClassification);
				}
				bayesClassificationForArticle.put(conditionalProbabilityForClass.getKey(), bayesClassification);
			}

			// get best prediction
			for(Map.Entry<String, Double> catPrediction: bayesClassificationForArticle.entrySet()) {
				if(predictedValue == null || catPrediction.getValue() > predictedValue.getValue()) {
					predictedValue = catPrediction;
				}
			}

			String articleName = DictionaryUtil.findArticleName(articleId);
			String categoryName = DictionaryUtil.findCategoryName(predictedValue.getKey());
			predictedCategoriesMap.put(articleName, categoryName);
		}
		stream.close();

		time.end();
		time.printMessage("Predicting classes");

		time.start();
		DictionaryUtil.saveDictionary(settings.outputFile, predictedCategoriesMap);
		time.end();
		time.printMessage("Saving results");
	}

}

class BayesClassificationResultMap extends TreeMap<String, Double> {}

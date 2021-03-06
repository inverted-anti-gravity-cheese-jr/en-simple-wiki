package pl.pg.gda.eti.kio.esc.bayes;

import pl.pg.gda.eti.kio.esc.DictionaryUtil;
import pl.pg.gda.eti.kio.esc.TestingPurposes;
import pl.pg.gda.eti.kio.esc.TimeCounter;
import pl.pg.gda.eti.kio.esc.WordDictionaryMerger;
import pl.pg.gda.eti.kio.esc.bayes.BayesConditionalProbability.ConditionalProbabilityForClass;
import pl.pg.gda.eti.kio.esc.data.Tuple;
import pl.pg.gda.eti.kio.esc.data.WordFeature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Wojciech Stanisławski, Krzysztof Świeczkowski
 * @since 12.01.17
 */
public class BayesClassifier {

	public static final boolean USE_TFIDF = false;
	public static final int RETURNED_CATEGORIES = 2;
	
	static <K,V extends Comparable<? super V>>
	SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
	    SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
	        new Comparator<Map.Entry<K,V>>() {
	            @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
	                int res = -1 * e1.getValue().compareTo(e2.getValue());
	                return res != 0 ? res : -1;
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}
	
	
	public static void classify(BayesClassificationSettings settings) throws IOException {
		TimeCounter time = new TimeCounter();
		PredictedCategoriesMap predictedCategoriesMap = new PredictedCategoriesMap();

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
		Map<String,WordFeature> mergedDictionary = merger.getChunksAsMapsWithKeyEnId()[0];
		
		//Ładowanie pliku
		File file = new File(settings.enWordArticleDict);
		int currentLineCounter = 0;
		BufferedReader reader = new BufferedReader(new FileReader(file));
		int totalLines = 0;
		while (reader.readLine() != null) totalLines++;
		reader.close();
		BufferedReader stream = new BufferedReader(new FileReader(file));
		String line;
		//foreach article
		time.start();
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
				WordFeature tempWordFeature = mergedDictionary.get(wordId);
				if(tempWordFeature != null) {
					wordFeaturesInArticle.add(tempWordFeature);
				}
			}
			BayesClassificationResultMap bayesClassificationForArticle = new BayesClassificationResultMap();
			Map.Entry<String, Double> predictedValue = null;
			//foreach class check if wordfeature exists and count probability
			for (Map.Entry<String, BayesConditionalProbability.ConditionalProbabilityForClass>
					conditionalProbabilityForClass : conditionalProbability.entrySet()) {
				double bayesClassification = 0;

				//String debugStr = "";
				if(TestingPurposes.DEBUG) {
					System.out.println("p(" + conditionalProbabilityForClass.getKey() + ")" + " = " + categoryStatistics.articlesInCategoriesCount.get(conditionalProbabilityForClass.getKey()) + " / " + categoryStatistics.articlesCount + " = " + bayesClassification);
					//debugStr += bayesClassification;
				}

				//liczba kategorii w klasie przez liczbe wszystkich art 
				for (WordFeature wordFeatureInArticle : wordFeaturesInArticle) {
					if(conditionalProbabilityForClass.getValue().conditionalProbabilityForWordInClass.containsKey(wordFeatureInArticle)) {
						bayesClassification += Math.log(conditionalProbabilityForClass.getValue().conditionalProbabilityForWordInClass.get(wordFeatureInArticle));
						//debugStr += " * " + conditionalProbabilityForClass.getValue().conditionalProbabilityForWordInClass.get(wordFeatureInArticle) + " [" + wordFeatureInArticle.getWord() + "]";
					}
				}			
				bayesClassification += Math.log((double) categoryStatistics.articlesInCategoriesCount.get(conditionalProbabilityForClass.getKey()) / categoryStatistics.articlesCount);
				if(TestingPurposes.DEBUG) {
					System.out.println("p = " + " = " + bayesClassification);
				}
				bayesClassificationForArticle.put(conditionalProbabilityForClass.getKey(), bayesClassification);
			}
			// get best prediction			
			List<String> predictedCategoriesList = new ArrayList<String>();
			int count = 0;
			//System.out.println("Start");
			for(Map.Entry<String, Double> catPrediction: entriesSortedByValues(bayesClassificationForArticle)) {
				if(count >= RETURNED_CATEGORIES) {
					break;
				}
				//System.out.println(catPrediction.getKey() + " " + catPrediction.getValue());
				predictedCategoriesList.add(DictionaryUtil.findCategoryName(catPrediction.getKey()));
				count++;
			}
			String articleName = DictionaryUtil.findArticleName(articleId);
			//String categoryName = DictionaryUtil.findCategoryName(catPrediction.getKey());
			predictedCategoriesMap.put(articleName, predictedCategoriesList);
			currentLineCounter++;
			if(currentLineCounter % 2000 == 0) {
			time.end();
			time.printMessage("Predicting 2000 classes. " + currentLineCounter + "/" + totalLines);
			time.start();
			}
		}
		time.end();
		stream.close();

		time.start();
		BufferedWriter writer = new BufferedWriter(new FileWriter(settings.outputFile));
		writer.write(predictedCategoriesMap.toString());
		writer.flush();
		writer.close();
		
		time.end();
		time.printMessage("Saving results");
	}
}

class BayesClassificationResultMap extends TreeMap<String, Double> {}
class PredictedCategoriesMap extends TreeMap<String, List<String>> {
	@Override
	public String toString() {
		String response = "";
		for(Map.Entry<String,List<String>> entry : this.entrySet()) {
			response += entry.getKey() + "\t";
			for(int i = 0; i < entry.getValue().size(); i++ ) {
				response += entry.getValue().get(i);
				if(i==entry.getValue().size()-1) {
					response += "\n";
				}
				else {
					response += "\t";
				}
			}
		}
		return response;
	}
}

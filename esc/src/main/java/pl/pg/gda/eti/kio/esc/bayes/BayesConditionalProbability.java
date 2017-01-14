package pl.pg.gda.eti.kio.esc.bayes;

import java.util.HashMap;
import java.util.Map;

import pl.pg.gda.eti.kio.esc.data.WordFeature;

/**
 * @author Krzysztof Świeczkowski
 * @since 14.01.17
 */

public class BayesConditionalProbability {
	public Map<String, ConditionalProbabilityForClass> countConditionalProbability(Map<String, BayesWordInCategoryCounter.WordsInCategory> stringWordsInCategoryMap, int wordsInDictionary) {
		//estymaty prawdopodobienstw warunkowych dla wszystkich słów
		Map<String, ConditionalProbabilityForClass> conditionalProbability = new HashMap<String, ConditionalProbabilityForClass>();
		//foreach class in simple wiki
		for (Map.Entry<String, BayesWordInCategoryCounter.WordsInCategory> entry : stringWordsInCategoryMap.entrySet()) {
			String categoryId = entry.getKey();
			BayesWordInCategoryCounter.WordsInCategory wordsInCategory = entry.getValue();
			ConditionalProbabilityForClass conditionalProbabilityForClass = new ConditionalProbabilityForClass(categoryId);
			//foreach word
			for (Map.Entry<WordFeature, Integer> wordData : wordsInCategory.wordCountInThisCategory.entrySet()) {
				//liczba wystąpień słowa w dokumentach klasy + 1/liczba słów w klasie + rozmiar słownika
				Double estymatPrawdopodobienstwaWarunkowego = (double)(wordData.getValue() + 1) / (double)(wordsInCategory.sumWordCountInThisCategory + wordsInDictionary);
				conditionalProbabilityForClass.conditionalProbabilityForWordInClass.put(wordData.getKey(), estymatPrawdopodobienstwaWarunkowego);
			}
			conditionalProbability.put(categoryId, conditionalProbabilityForClass);
		}
		return conditionalProbability;
	}
	
	public class ConditionalProbabilityForClass {
		public Map<WordFeature, Double> conditionalProbabilityForWordInClass; //key = id slowa, value = wartosc 
		public String classId;
		
		public ConditionalProbabilityForClass(String classId) {
			conditionalProbabilityForWordInClass = new HashMap<WordFeature, Double>();
			this.classId = classId;
		}
	}
}

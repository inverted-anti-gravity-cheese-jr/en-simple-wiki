package pl.pg.gda.eti.kio.esc.bayes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.pg.gda.eti.kio.esc.TestingPurposes;
import pl.pg.gda.eti.kio.esc.data.Tuple;
import pl.pg.gda.eti.kio.esc.data.WordFeature;

/**
 * @author Krzysztof Świeczkowski
 * @since 14.01.17
 */

public class BayesConditionalProbability {
	public Map<String, ConditionalProbabilityForClass> countConditionalProbability(Map<String, BayesWordInCategoryCounter.WordsInCategory> stringWordsInCategoryMap, List<WordFeature> words, int wordsInDictionary) {
		//estymaty prawdopodobienstw warunkowych dla wszystkich słów
		Map<String, ConditionalProbabilityForClass> conditionalProbability = new HashMap<String, ConditionalProbabilityForClass>();
		//foreach class in simple wiki
		for (Map.Entry<String, BayesWordInCategoryCounter.WordsInCategory> entry : stringWordsInCategoryMap.entrySet()) {
			String categoryId = entry.getKey();
			BayesWordInCategoryCounter.WordsInCategory wordsInCategory = entry.getValue();
			ConditionalProbabilityForClass conditionalProbabilityForClass = new ConditionalProbabilityForClass(categoryId);
			//foreach word
			for(WordFeature word: words) {
				double occurances = 0;
				if(wordsInCategory.wordCountInThisCategory.containsKey(word)) {
					occurances = wordsInCategory.wordCountInThisCategory.get(word);
				}
				Double estymatPrawdopodobienstwaWarunkowego = (double)(occurances + 1) / (double)(wordsInCategory.sumWordCountInThisCategory + wordsInDictionary);
				if(TestingPurposes.DEBUG) {
					System.out.println("p(" + word.getWord() + "/" + categoryId + ")" + " = (" + occurances + " + 1) / (" + wordsInCategory.sumWordCountInThisCategory + " + " + wordsInDictionary + ") = " + estymatPrawdopodobienstwaWarunkowego);
				}
				conditionalProbabilityForClass.conditionalProbabilityForWordInClass.put(word, estymatPrawdopodobienstwaWarunkowego);
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

package pl.pg.gda.eti.kio.esc.bayes;

import pl.pg.gda.eti.kio.esc.data.Tuple;
import pl.pg.gda.eti.kio.esc.data.WordFeature;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Wojciech Stanisławski
 * @since 12.01.17
 */
public class BayesWordInCategoryCounter {

	private BufferedReader simpleArtCatReader;
	private BufferedReader simpleFeatureReader;

	public /* kluczem jest nazwa kategorii */ Map<String, WordsInCategory> countWordsInCategories(String artCatFileName, String featureFileName, List<WordFeature> dictionary) throws IOException {

		Map<String, WordsInCategory> resultMap = new TreeMap<>();

		simpleFeatureReader = new BufferedReader(new FileReader(featureFileName));
		String line;
		while ((line = simpleFeatureReader.readLine()) != null) {
			if(line.isEmpty() || !line.contains("#")) {
				continue;
			}

			String artName = line.substring(0, line.indexOf('#'));

			// find words
			List<Tuple<String, Double>> foundWords = new ArrayList<>();
			double sum = findWordsInArticle(line, foundWords);

			// find category
			String catName = findCategory(artCatFileName, artName);

			if(catName != null) {
				WordsInCategory value;
				if(resultMap.containsKey(catName)) {
					value = resultMap.get(catName);
				}
				else {
					value = new WordsInCategory();
					value.wordCountInThisCategory = new TreeMap<>();
				}
				if(BayesClassifier.USE_TFIDF) {
					value.sumWordCountInThisCategory += sum;
				}
				else {
					value.sumWordCountInThisCategory += foundWords.size();
				}
				for(Tuple<String, Double> wordWithTfIdf : foundWords) {
					String word = wordWithTfIdf.getKey();
					Optional<WordFeature> feature = dictionary.stream().filter(f -> word.equals(f.getSimpleId())).findAny();
					if(feature.isPresent()) {
						double newValue = 1;
						if(BayesClassifier.USE_TFIDF) {
							newValue = wordWithTfIdf.getValue().doubleValue();
						}
						if (value.wordCountInThisCategory.containsKey(feature.get())) {
							Double oldCount = value.wordCountInThisCategory.get(feature.get());
							value.wordCountInThisCategory.put(feature.get(), oldCount + newValue);
						} else {
							value.wordCountInThisCategory.put(feature.get(), newValue);
						}
					}
				}

				resultMap.put(catName, value);
			}
		}
		simpleFeatureReader.close();
		return resultMap;
	}

	private double findWordsInArticle(String articleString, List<Tuple<String, Double>> foundWords) {
		String wordsStr = articleString.substring(articleString.indexOf('#') + 1);
		String[] words = wordsStr.split(" ");
		double wordCount = 0;
		for(String word: words) {
			double tfidf = Double.parseDouble(word.substring(word.indexOf('-') + 1));
			foundWords.add(new Tuple<String, Double>(word.substring(0, word.indexOf('-')), tfidf));
			wordCount += tfidf;
		}
		return wordCount;
	}

	private String findCategory(String artCatFileName, String artName) throws IOException {
		simpleArtCatReader = new BufferedReader(new FileReader(artCatFileName));
		String catName = null;
		String artLine;
		while ((artLine = simpleArtCatReader.readLine()) != null) {
			if(artLine.startsWith(artName)) {
				artLine = artLine.substring(artLine.indexOf('\t') + 1);
				if(artLine.contains("\t")) {
					catName = artLine.substring(0, artLine.indexOf('\t'));
				}
				else {
					catName = artLine.trim();
				}
			}
		}
		simpleArtCatReader.close();
		return catName;
	}


	public class WordsInCategory {
		public String category;
		public /* kluczem jest nazwa słowa */ Map<WordFeature, Double> wordCountInThisCategory;
		public double sumWordCountInThisCategory;

		@Override
		public String toString() {
			return "WordsInCategory{" +
					"category='" + category + '\'' +
					", wordCountInThisCategory=" + wordCountInThisCategory +
					", sumWordCountInThisCategory=" + sumWordCountInThisCategory +
					'}';
		}
	}
}

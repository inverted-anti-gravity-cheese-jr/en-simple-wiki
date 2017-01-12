package pl.pg.gda.eti.kio.esc.bayes;

import pl.pg.gda.eti.kio.esc.data.WordFeature;
import scala.util.parsing.combinator.testing.Str;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
			List<String> foundWords = findWordsInArticle(line);

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
				value.sumWordCountInThisCategory += foundWords.size();
				for(String word : foundWords) {
					Optional<WordFeature> feature = dictionary.stream().filter(f -> word.equals(f.getSimpleId())).findAny();
					if(feature.isPresent()) {
						if (value.wordCountInThisCategory.containsKey(feature.get())) {
							Integer oldCount = value.wordCountInThisCategory.get(feature.get());
							value.wordCountInThisCategory.put(feature.get(), oldCount + 1);
						} else {
							value.wordCountInThisCategory.put(feature.get(), 1);
						}
					}
				}

				resultMap.put(catName, value);
			}
		}
		simpleFeatureReader.close();
		return resultMap;
	}

	private List<String> findWordsInArticle(String articleString) {
		String wordsStr = articleString.substring(articleString.indexOf('#') + 1);
		String[] words = wordsStr.split(" ");
		List<String> foundWords = new ArrayList<>();
		for(String word: words) {
			foundWords.add(word.substring(0, word.indexOf('-')));
		}
		return foundWords;
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
		public /* kluczem jest nazwa słowa */ Map<WordFeature, Integer> wordCountInThisCategory;
		public int sumWordCountInThisCategory;

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

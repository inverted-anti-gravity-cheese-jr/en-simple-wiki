package pl.pg.gda.eti.kio.esc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;

/**
 * @author Krzysiek
 * @since 14.12.2016
 */

public class ResultParser {
    private BufferedReader cosineSimilarityReader;
    private BufferedReader articleDictionaryReader;
    private BufferedReader articleCategoryRelationReader;
    private BufferedReader categoryDictionaryReader;
    private BufferedWriter translatedResultWriter;
	
    private Dictionary<String, String> articleDictionary;
    private Dictionary<String, String> categoryDictionary;
    private Dictionary<String, List<String>> articleCategoryRelationListDictionary;
    
	public void parseResultFile(String cosineSimilarityFile, String articleDictionaryFile, String articleCategoryRelationFile, String categoryDictionaryFile, String outputFile) {
		try {
			loadFiles(cosineSimilarityFile, articleDictionaryFile, articleCategoryRelationFile, categoryDictionaryFile, outputFile);
			String resultForArticle;
			articleDictionary = createDictionary(articleDictionaryReader);
			categoryDictionary = createDictionary(categoryDictionaryReader);
			articleCategoryRelationListDictionary = createRelationListDictionary(articleCategoryRelationReader);
			
			while ((resultForArticle = cosineSimilarityReader.readLine()) != null) {
				Tuple<String, String, String> result = parseCosineSimilarityResult(resultForArticle);
				findArticleNameFromId(result._1);
				findCategoriesFromArticleId(result._2);
				translatedResultWriter.write(findArticleNameFromId(result._1) + "\t" + findCategoriesFromArticleId(result._2) + "\t" + result._3);
				translatedResultWriter.newLine();
		    }
			closeFiles();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
	
	private String findArticleNameFromId(String id) {
		return articleDictionary.get(id);
	}
	
	private String findCategoriesFromArticleId(String id) {
		List<String> categoryIds = articleCategoryRelationListDictionary.get(id);
		String categoriesString = ""; 
		for(String categoryId : categoryIds) {
			categoriesString = categoryDictionary.get(categoryId) + " ";
		}
		categoriesString = categoriesString.substring(0,categoriesString.length()-1);
		return categoriesString;
	}
	
	private Dictionary<String, String> createDictionary(BufferedReader reader) throws IOException {
		Dictionary<String, String> dictionary = new Hashtable<String, String>();
		String[] parts;
		String readLine;
		while ((readLine = reader.readLine()) != null) {
			parts = readLine.split("\t");
			dictionary.put(parts[1], parts[0]);
		}
		return dictionary;
	}
	
	private Dictionary<String, List<String>> createRelationListDictionary(BufferedReader reader) throws IOException {
		Dictionary<String, List<String>> dictionary = new Hashtable<String, List<String>>();
		String[] parts;
		String readLine;
		List<String> categories;
		while ((readLine = reader.readLine()) != null) {
			parts = readLine.split("\t");
			categories = new ArrayList<String>();
			for(int i=1;i< parts.length;i++) {
				categories.add(parts[i]);
			}
			dictionary.put(parts[0],categories);
		}
		return dictionary;
	}
	
	private void loadFiles(String cosineSimilarityFile, String articleDictionaryFile, String articleCategoryRelationFile, String categoryDictionaryFile, String outputFile) throws IOException {
		cosineSimilarityReader = new BufferedReader(new FileReader(cosineSimilarityFile));
		articleDictionaryReader = new BufferedReader(new FileReader(articleDictionaryFile));
		articleCategoryRelationReader = new BufferedReader(new FileReader(articleCategoryRelationFile));
		categoryDictionaryReader = new BufferedReader(new FileReader(categoryDictionaryFile));
		translatedResultWriter = new BufferedWriter(new FileWriter(outputFile));
    }

    private void closeFiles() throws IOException {
    	cosineSimilarityReader.close();
    	articleDictionaryReader.close();
    	articleCategoryRelationReader.close();
    	categoryDictionaryReader.close();
    	translatedResultWriter.close();
    }
    
    private Tuple<String, String, String> parseCosineSimilarityResult(String resultForArticle) {
		String articleEnId, articleSimpleId, cosineSimilarity;
		String[] resultParts = resultForArticle.split("#");
		articleEnId = resultParts[0];
		resultForArticle = resultParts[1];
		resultParts = resultForArticle.split("=");
		articleSimpleId = resultParts[0];
		cosineSimilarity = resultParts[1];
		Tuple<String, String, String> result = new Tuple<String, String, String>(articleEnId, articleSimpleId, cosineSimilarity);
		return result;
    }
    
    private class Tuple<T1, T2, T3> {
    	private T1 _1;
    	private T2 _2;
    	private T3 _3;

    	public Tuple(T1 _1, T2 _2, T3 _3) {
    	    this._1 = _1;
    	    this._2 = _2;
    	    this._3 = _3;
    	}
	}
}

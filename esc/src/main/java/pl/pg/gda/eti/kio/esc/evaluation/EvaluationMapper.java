package pl.pg.gda.eti.kio.esc.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EvaluationMapper {
	private static Map<String, String> getCategoryMapping(String mappingFile) throws IOException {
		Map<String, String> categoryMapping = new TreeMap<String, String>();
		File file = new File(mappingFile);
		BufferedReader stream = new BufferedReader(new FileReader(file));
		String line;
		while ((line = stream.readLine()) != null) {
			String[] elements = line.split("\t");
			if(elements.length == 3) {
				categoryMapping.put(elements[0], elements[2]);
			}
		}
		return categoryMapping;
	}
	
	private static Map<String, String> getArticleIdToNameMapping(String enArticleDict) throws IOException {
		Map<String, String> enArticleIdToNameMapping = new TreeMap<String, String>();
		File file = new File(enArticleDict);
		BufferedReader stream = new BufferedReader(new FileReader(file));
		String line;
		while ((line = stream.readLine()) != null) {
			String[] elements = line.split("\t");
			if(elements.length == 2) {
				enArticleIdToNameMapping.put(elements[1], elements[0]);
			}
		}
		return enArticleIdToNameMapping;
	}
	
	public static Map<String, List<String>> mapEnArticlesToSimpleCategories(String mappingFile, String enArticleDict, String enArticleCategoryDict) throws IOException {
		Map<String, List<String>> enArticleToSimpleCategory = new TreeMap<String, List<String>>();
		Map<String, String> categoryMapping = getCategoryMapping(mappingFile);
		Map<String, String> enArticleIdToNameMapping = getArticleIdToNameMapping(enArticleDict);

		File file = new File(enArticleCategoryDict);
		BufferedReader stream = new BufferedReader(new FileReader(file));
		String line;
		while ((line = stream.readLine()) != null) {
			String[] elements = line.split("\t");
			if(elements.length > 1) {
				List<String> categoryList = new ArrayList<String>();
				for(int i=1; i<elements.length; i++) {
					String categoryName = categoryMapping.get(elements[i]);
					if(categoryName != null) {
						categoryList.add(categoryName);
					}
				}
				String articleName = enArticleIdToNameMapping.get(elements[0]);
				if(articleName != null) {
					enArticleToSimpleCategory.put(articleName, categoryList);
				}
			}
		}
		stream.close();
		return enArticleToSimpleCategory;
	}
}

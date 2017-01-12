package pl.pg.gda.eti.kio.esc.bayes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * @author Krzysztof Świeczkowski
 * @since 12.01.17
 */
public class BayesCategoryCounter {

	public CategoryStatistics countCategories(String articleCategoryRelationFile) throws IOException {
		CategoryStatistics categoryStatistics = new CategoryStatistics();
		File file = new File(articleCategoryRelationFile);
		BufferedReader stream = new BufferedReader(new FileReader(file));
		String line;
		while ((line = stream.readLine()) != null) {
			String[] elements = line.split("\t");
			String articleId = elements[0]; //rest of the ids are for the categories
			for(int i = 0; i < elements.length; i++) {
				//check if exists
				
				if(categoryStatistics.articlesInCategoriesCount.containsKey(elements[i])) {
					//if exists
					categoryStatistics.articlesInCategoriesCount.put(Integer.parseInt(elements[i]), 
						categoryStatistics.articlesInCategoriesCount.get(elements[i])+1);
				}
				else {
					//if not exists
					categoryStatistics.articlesInCategoriesCount.put(Integer.parseInt(elements[i]), 1);
				}
			}
			categoryStatistics.articlesCount++;
		}
		stream.close();
		return categoryStatistics;
	}

	public class CategoryStatistics {
		public HashMap<Integer, Integer> articlesInCategoriesCount; //key = id kategorii, value = ilosc artykulow
		public Integer articlesCount;
		
		public CategoryStatistics() {
			articlesInCategoriesCount = new HashMap<Integer, Integer>(); 
		}
	}

}

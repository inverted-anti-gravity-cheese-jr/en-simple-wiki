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

public class ClassifierEvaluation {
	public static void evaluate(ClassifierEvaluationSettings settings) throws IOException {
		Map<String, List<String>> expectedResults = EvaluationMapper.mapEnArticlesToSimpleCategories(settings.mappingFile, settings.enArticleDict, settings.enArticleCategoryDict);
		Map<String, List<String>> bayesClassificationResults = loadClassificationResults(settings.bayesClassificationOutputFile);
		Map<String, List<String>> cosineClassificationResults = loadClassificationResults(settings.cosineClassificationOutputFile);
	}
	
	private static Map<String, List<String>> loadClassificationResults(String classificationResultsFile) throws IOException {
		Map<String, List<String>> classificationResults = new TreeMap<String, List<String>>();
		
		File file = new File(classificationResultsFile);
		BufferedReader stream = new BufferedReader(new FileReader(file));
		String line;
		while ((line = stream.readLine()) != null) {
			String[] elements = line.split("\t");
			if(elements.length > 1) {
				List<String> categoryList = new ArrayList<String>();
				for(int i=1; i<elements.length; i++) {
					categoryList.add(elements[i]);
				}
				classificationResults.put(elements[0], categoryList);
			}
		}
		stream.close();
		return classificationResults;
	}
}

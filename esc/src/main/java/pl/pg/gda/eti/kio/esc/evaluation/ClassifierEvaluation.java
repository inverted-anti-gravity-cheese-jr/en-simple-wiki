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

import pl.pg.gda.eti.kio.esc.bayes.BayesConditionalProbability;

public class ClassifierEvaluation {
	public static void evaluate(ClassifierEvaluationSettings settings) throws IOException {
		Map<String, List<String>> expectedResults = EvaluationMapper.mapEnArticlesToSimpleCategories(settings.mappingFile, settings.enArticleDict, settings.enArticleCategoryDict);
		Map<String, List<String>> bayesClassificationResults = loadClassificationResults(settings.bayesClassificationOutputFile);
		Map<String, List<String>> cosineClassificationResults = loadClassificationResults(settings.cosineClassificationOutputFile);
		
		System.out.println("Bayes: " + compareResultsToExpectedValues(bayesClassificationResults, expectedResults, settings.fMeasureBetaValue));
		System.out.println("Cosine: " + compareResultsToExpectedValues(cosineClassificationResults, expectedResults, settings.fMeasureBetaValue));
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
	
	public static ResultEvaluation compareResultsToExpectedValues(Map<String, List<String>> results,Map<String, List<String>> expectedValues, Double fMeasureBetaValue ) {
		int found = 0;
		int foundAndRelevant = 0;
		int relevant = 0;
		for (Map.Entry<String, List<String>> result : results.entrySet()) {
			List<String> expectedCategories = expectedValues.get(result.getKey());
			found += result.getValue().size();
			relevant += expectedCategories.size();
			for(String category: result.getValue()) {
				if(expectedCategories.contains(category)) {
					foundAndRelevant++;
				}
			}
		}
		return new ResultEvaluation(found, foundAndRelevant, relevant, fMeasureBetaValue);
	}
	
	public static class ResultEvaluation {
		public Double precision;
		public Double recall;
		public Double fMeasure;
		public Double beta;
		
		public ResultEvaluation(int found, int foundAndRelevant, int relevant, Double beta) {
			this.beta = beta; 
			precision = (double) foundAndRelevant / found;
			recall = (double) foundAndRelevant / relevant;
			fMeasure = (double) (beta * beta + 1) * precision * recall / (beta * beta * precision + recall);
		}
		
		@Override
		public String toString() {
			String returnString = "Precision: " + precision + "\n";
			returnString += "Recall: " + recall + "\n";
			returnString += "F Measure: " + fMeasure + "\n";
			return returnString;
		}
	}
}

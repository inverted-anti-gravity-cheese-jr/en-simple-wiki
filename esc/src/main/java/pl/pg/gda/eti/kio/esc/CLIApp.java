package pl.pg.gda.eti.kio.esc;


import pl.pg.gda.eti.kio.esc.bayes.BayesClassificationSettings;
import pl.pg.gda.eti.kio.esc.bayes.BayesClassifier;
import pl.pg.gda.eti.kio.esc.cosine.*;
import pl.pg.gda.eti.kio.esc.evaluation.ClassifierEvaluation;
import pl.pg.gda.eti.kio.esc.evaluation.ClassifierEvaluationSettings;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * @author Wojciech Stanisławski
 * @since 08.11.2016
 */
public class CLIApp {

	private static final boolean DISTRIBUTED = false;

	public static void main(String[] args) throws IOException, InterruptedException {
		if(args.length > 0 && Arrays.asList(args).contains("bayes")) {
			BayesClassificationSettings settings = new BayesClassificationSettings();

			settings.outputFile = "results/bayes-results";
			settings.enFeatureDict = "en/en-po_slowach-feature_dict-en-20111201";
			settings.simpleFeatureDict = "simple/temp-po_slowach-feature_dict-simple-20120104";
			settings.simpleArticleCategoryDict = "simple/temp-po_slowach-categories-simple-20120104";
			settings.simpleWordArticleDict = "simple/temp-po_slowach-lista-simple-20120104";
			settings.enWordArticleDict = "en/en-po_slowach-lista-en-20111201";
			settings.simpleCategoryDict = "simple/temp-po_slowach-cats_dict-simple-20120104";
			settings.enArticleDict = "en/en-po_slowach-articles_dict-en-20111201";

			BayesClassifier.classify(settings);
		}
		else if(args.length > 0 && Arrays.asList(args).contains("evaluation")) {
			ClassifierEvaluationSettings settings = new ClassifierEvaluationSettings();
			
			settings.outputFile = "results/evaluation-result";
			settings.mappingFile = "mapping/mapping-categories-en-simple";
			settings.enArticleDict = "en/en-po_slowach-articles_dict-en-20111201";
			settings.enArticleCategoryDict = "en/en-po_slowach-categories-en-20111201";
			settings.bayesClassificationOutputFile = "results/bayes-results";
			settings.cosineClassificationOutputFile = "results/cosine-results";
			settings.fMeasureBetaValue = 1.0;
			
			ClassifierEvaluation.evaluate(settings);
		}
		else {
			CosineClassificationSettings settings = new CosineClassificationSettings();

			settings.distributed = false;
			settings.cosineSimilarityResultFileName = "temp-data/cosine-similarity";
			settings.enFeatureDictAppliedFileName = "temp-data/en-applied";
			settings.simpleFeatureDictAppliedFileName = "temp-data/simple-applied";
			settings.enFeatureDict = "en/en-po_slowach-feature_dict-en-20111201";
			settings.simpleFeatureDict = "simple/temp-po_slowach-feature_dict-simple-20120104";
			settings.enWordArticleDict = "en/en-po_slowach-lista-en-20111201";
			settings.simpleWordArticleDict = "simple/temp-po_slowach-lista-simple-20120104";
			settings.enArticleDictionaryFile = "en/en-po_slowach-articles_dict-en-20111201";
			settings.simpleArticleCategoryRelationFile = "simple/temp-po_slowach-categories-simple-20120104";
			settings.simpleCategoryDictionaryFile = "simple/temp-po_slowach-cats_dict-simple-20120104";
			settings.outputFile = "results/cosine-results";

			CosineClassifier.classify(settings);
		}
	}
}

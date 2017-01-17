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
		EvaluationMapper.mapEnArticlesToSimpleCategories(settings.mappingFile, settings.enArticleDict, settings.enArticleCategoryDict);
	}
}

package pl.pg.gda.eti.kio.esc.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ClassifierEvaluation {
	public static void evaluate() throws IOException {
		mapCategories("en-po_slowach-categories-en-20111201");
	}
	
	private static void mapCategories(String enArticleCategoryRelationFile) throws IOException {
		
		
		
		File file = new File(enArticleCategoryRelationFile);
		BufferedReader stream = new BufferedReader(new FileReader(file));
		String line;
		while ((line = stream.readLine()) != null) {
			
			//map categories
			//
		}
		stream.close();
	}
}

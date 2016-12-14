package pl.pg.gda.eti.kio.esc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author wojtek
 */
public class CosineSimilarityCounter {

    private BufferedReader enStream;
    private BufferedWriter outputStream;
    private String simpleFileName;
    
    public void countSimilarityForAll(String enFileName, String simpleFilename, String outputFileName) {
	try {
	    loadFiles(enFileName, outputFileName);
	    simpleFileName = simpleFilename;
	    String enArticle;
	    while((enArticle = enStream.readLine()) != null) {
		double maxCos = findMaxCosineSimilarity(enArticle);
		String articleId = enArticle.substring(0, enArticle.indexOf('#'));
		outputStream.write(articleId + "#" + Double.toString(maxCos));
		outputStream.newLine();
	    }
	    closeFiles();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    private double countCosineSimilarity(String enArticle, String simpleArticle) {
	// TODO
	return 0;
    }
    
    private double findMaxCosineSimilarity(String enArticle) throws IOException {
	BufferedReader simpleStream = new BufferedReader(new FileReader(simpleFileName));
	String simpleArticle;
	double max = 0;
	while((simpleArticle = simpleStream.readLine()) != null) {
	    double sim = countCosineSimilarity(enArticle, simpleArticle);
	    if (sim > max) {
		max = sim;
	    }
	}
	simpleStream.close();
	return max;
    }
    
    private void loadFiles(String enFileName, String outputFileName) throws IOException {
	enStream = new BufferedReader(new FileReader(enFileName));
	outputStream = new BufferedWriter(new FileWriter(outputFileName));
    }
    
    private void closeFiles() throws IOException {
	enStream.close();
	outputStream.close();
    }
    
}

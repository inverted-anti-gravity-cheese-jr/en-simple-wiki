package pl.pg.gda.eti.kio.esc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

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
	    
	    int count = 0;
	    System.out.print("Processed articles: " + count + ".");
	    
	    while ((enArticle = enStream.readLine()) != null) {
		Tuple<Double, String> maxCos = findMaxCosineSimilarity(enArticle);
		String articleId = enArticle.substring(0, enArticle.indexOf('#'));
		outputStream.write(articleId + "#" + maxCos._2 + "=" + Double.toString(maxCos._1));
		outputStream.newLine();
		
		count++;
		System.out.print("\rProcessed articles: " + count + ".");
	    }
	    
	    System.out.println();
	    System.out.println("Done");
	    closeFiles();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private Tuple<Double, String> countCosineSimilarity(String enArticle, String simpleArticle) {
	String[] simpleWords = simpleArticle.substring(simpleArticle.indexOf('#') + 1).split(" ");
	String[] enWords = enArticle.substring(enArticle.indexOf('#') + 1).split(" ");
	
	String simpleId = simpleArticle.substring(0, simpleArticle.indexOf("#"));

	ArrayList<TermScoresHelper> scores = new ArrayList<TermScoresHelper>();
	for (String word : simpleWords) {
	    try {
		TermScoresHelper helper = new TermScoresHelper();
		if(!word.contains("-")) {
		    continue;
		}
		helper.word = word.substring(0, word.indexOf('-'));
		helper.simpleScore = Double.parseDouble(word.substring(word.indexOf('-') + 1));
		scores.add(helper);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	for (String word : enWords) {
	    try {
		if(!word.contains("-")) {
		    continue;
		}
		
		String wordId = word.substring(0, word.indexOf('-'));
		double enScore = Double.parseDouble(word.substring(word.indexOf('-') + 1));

		Optional<TermScoresHelper> score = scores.stream().filter(s -> s.word.equals(wordId)).findAny();
		if (score.isPresent()) {
		    score.get().enScore = enScore;
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	Stream<TermScoresHelper> helpersWithBothScoresStream = scores.stream().filter(s -> s.enScore != null);

	Stream<CosineScoresHelper> cosineHelpers = helpersWithBothScoresStream.map(score -> {
	    CosineScoresHelper helper = new CosineScoresHelper();

	    helper.dotProduct = score.enScore * score.simpleScore;
	    helper.english = score.enScore * score.enScore;
	    helper.simple = score.simpleScore * score.simpleScore;

	    return helper;
	});

	Optional<CosineScoresHelper> finalHelper = cosineHelpers.reduce((h1, h2) -> {
	    h1.dotProduct += h2.dotProduct;
	    h1.english += h2.english;
	    h1.simple += h2.simple;
	    return h1;
	});
	
	if(finalHelper.isPresent()) {
	    CosineScoresHelper helper = finalHelper.get();
	    return new Tuple<Double, String>(helper.dotProduct / (Math.sqrt(helper.english) + Math.sqrt(helper.simple)), simpleId);
	}

	return null;
    }

    private Tuple<Double, String> findMaxCosineSimilarity(String enArticle) throws IOException {
	BufferedReader simpleStream = new BufferedReader(new FileReader(simpleFileName));
	String simpleArticle;
	Tuple<Double, String> max = null;
	while ((simpleArticle = simpleStream.readLine()) != null) {
	    Tuple<Double, String> sim = countCosineSimilarity(enArticle, simpleArticle);
	    if (sim == null) {
		continue;
	    }
	    if (max == null || sim._1 > max._1) {
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
    
    private class Tuple<T1, T2> {
	private T1 _1;
	private T2 _2;

	public Tuple(T1 _1, T2 _2) {
	    this._1 = _1;
	    this._2 = _2;
	}
    }

    private class TermScoresHelper {

	private String word = null;
	private Double simpleScore = null;
	private Double enScore = null;
    }

    private class CosineScoresHelper {

	private double dotProduct;
	private double english;
	private double simple;
    }

}

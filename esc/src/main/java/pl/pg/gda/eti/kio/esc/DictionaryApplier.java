package pl.pg.gda.eti.kio.esc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.pg.gda.eti.kio.esc.data.WordFeature;

/**
 *
 * @author wojtek
 */
public class DictionaryApplier {

    private BufferedReader stream;
    private BufferedWriter outputStream;
    private List<WordFeature> dictionary;

    public void applyDictionary(String fileName, String outputFileName, List<WordFeature> words, boolean simple) {
	try {
	    loadFiles(fileName, outputFileName);
	    dictionary = words;
	    
	    int count = 0;
	    System.out.print("Processed articles: " + count + ".");

	    String line = null;
	    while ((line = stream.readLine()) != null) {
		String newLine = extractArticleData(line, simple).toString();
		outputStream.write(newLine);
		outputStream.newLine();
		count++;
		System.out.print("\rProcessed articles: " + count + ".");
	    }
	    System.out.println();
	    System.out.println("Done");

	    freeFiles();
	} catch (Exception ex) {
	    Logger.getLogger(DictionaryApplier.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    private ArticleData extractArticleData(String line, boolean simple) {
	String lineId = line.substring(0, line.indexOf('#'));
	String[] words = line.substring(line.indexOf('#') + 1).split(" ");

	ArticleData data = new ArticleData();
	data.lineId = lineId;

	for (int i = 0; i < words.length; i++) {
	    String word = words[i];
	    if(!word.contains("-")) {
		continue;
	    }
	    
	    String wordId = word.substring(0, word.indexOf('-'));

	    
	    for (WordFeature feature : dictionary) {
		if ((simple && wordId.equals(feature.getSimpleId())) || (!simple && wordId.equals(feature.getEnId()) )) {
		    words[i] = "\\" + feature.getWord() + word.substring(word.indexOf('-')) + " ";
		}
	    }
	    
	}

	data.words = words;
	return data;
    }

    private void loadFiles(String fileName, String outputFileName) throws IOException {
	File file = new File(fileName);
	stream = new BufferedReader(new FileReader(file));

	File outputFile = new File(outputFileName);
	outputStream = new BufferedWriter(new FileWriter(outputFile));
    }

    private void freeFiles() throws IOException {
	stream.close();
	outputStream.close();
    }

    private class ArticleData {

	public String lineId;
	public String[] words;

	@Override
	public String toString() {
	    StringBuilder builder = new StringBuilder();
	    builder.append(lineId);
	    builder.append("#");
	    for (String word : words) {
		if (word.startsWith("\\")) {
		    builder.append(word.substring(1));
		    builder.append(" ");
		}
	    }
	    return builder.toString().trim();
	}
    }

}

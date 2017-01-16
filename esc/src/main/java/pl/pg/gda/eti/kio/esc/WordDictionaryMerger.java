package pl.pg.gda.eti.kio.esc;

import pl.pg.gda.eti.kio.esc.data.WordFeature;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Wojciech Stanisławski
 * @since 08.11.2016
 */
public class WordDictionaryMerger {
    private List<WordFeature>[] chunks;
    private String comparatorForWordFeature = "word";
    
    public void setComparatorForWordFeature(String comparatorForWordFeature) {
    	this.comparatorForWordFeature = comparatorForWordFeature;
    }
    
    public List<WordFeature>[] getChunks() {
        return chunks;
    }

    public Map<String, WordFeature>[] getChunksAsMapsWithKeyEnId() {
    	Map<String,WordFeature>[] maps = new HashMap[chunks.length];
    	int count = 0;
    	for(List<WordFeature> chunk : chunks) {
    		maps[count] = new HashMap<String,WordFeature>();
    		for (WordFeature i : chunk) maps[count].put(i.getEnId(),i);
    		count++;
    	}
    	return maps;
    }
    
    public void mergeFiles(String simpleFileName, String enFileName, int numChunks) {
        int i = 0;
        int lineNum = 0;
        chunks = new List[numChunks];
        for(; i < numChunks; i++) {
            chunks[i] = new ArrayList<WordFeature>();
        }
        try {
            BufferedReader simpleReader = new BufferedReader(new FileReader(new File(simpleFileName)));
            String line;
            do {
                line = simpleReader.readLine();
                if(line != null) {
                    StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                    String word = tokenizer.nextToken();
                    String id = tokenizer.nextToken();
                    chunks[lineNum % numChunks].add(new WordFeature(word, id, null, comparatorForWordFeature));
                }
                lineNum++;
            } while(line != null);
            simpleReader.close();

            BufferedReader enReader = new BufferedReader(new FileReader(new File(enFileName)));
            do {
                line = enReader.readLine();
                if(line != null) {
                    StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                    String word = tokenizer.nextToken();
                    String id = tokenizer.nextToken();
                    for (i = 0; i < numChunks; i++) {
                        for(WordFeature feature : chunks[i]) {
                            if(feature.getWord().equals(word)) {
                                feature.setEnId(id);
                            }
                        }
                    }
                }
                lineNum++;
            } while(line != null);
            enReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(; i < numChunks; i++) {
            chunks[i].removeIf(x -> x.getEnId() == null);
        }
    }

    // przekombinowane
    @Deprecated
    public void mergeFilesConcurrent(String simpleFileName, String enFileName) {
        int i;
        File enFile = new File(enFileName);
        File simpleFile = new File(simpleFileName);
        Thread[] nodeThreads = null;

        int numCores = Runtime.getRuntime().availableProcessors();
        chunks = new List[numCores];
        WordDictionaryMergerNode[] workerNodes = new WordDictionaryMergerNode[numCores];

        if(numCores > 1) {
            nodeThreads = new Thread[numCores - 1];
        }

        workerNodes[0] = new WordDictionaryMergerNode(enFile, simpleFile, 0, numCores);

        if(numCores > 1) {
            for (i = 1; i < numCores; i++) {
                workerNodes[i] = new WordDictionaryMergerNode(enFile, simpleFile, i, numCores);
                nodeThreads[i - 1] = new Thread(workerNodes[i]);
                nodeThreads[i - 1].start();
            }
        }
        workerNodes[0].run();

        if(numCores > 1) {
            for (i = 0; i < numCores - 1; i++) {
                try {
                    nodeThreads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        for(i = 0; i < numCores; i++) {
            chunks[i] = workerNodes[i].getResults();
        }

    }

}

class WordDictionaryMergerNode implements Runnable {
    private File enFile;
    private File simpleFile;
    private int nodeNumber;
    private int numberOfNodes;
    private List<WordFeature> results;

    public WordDictionaryMergerNode(File enFile, File simpleFile, int nodeNumber, int numberOfNodes) {
        this.enFile = enFile;
        this.simpleFile = simpleFile;
        this.nodeNumber = nodeNumber;
        this.numberOfNodes = numberOfNodes;
    }

    @Override
    public void run() {
        try {
            results = new ArrayList<WordFeature>();
            readSimple();
            readEn();
            results.removeIf(x -> x.getEnId() == null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<WordFeature> getResults() {
        return results;
    }

    private void readSimple() throws IOException {
        String line;
        ChunkFileReader stream;

        long chunkSize = simpleFile.length() / numberOfNodes;
        long chunkStart = chunkSize * nodeNumber;

        stream = new ChunkFileReader(simpleFile, chunkStart, chunkSize);
        stream.open();

        do {
            line = stream.readLine();
            if(line != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                String word = tokenizer.nextToken();
                String id = tokenizer.nextToken();
                results.add(new WordFeature(word, id, null));
            }
        } while (line != null);

        stream.close();
    }

    private void readEn() throws IOException {
        String line;
        ChunkFileReader stream;

        long chunkSize = enFile.length() / numberOfNodes;
        long chunkStart = chunkSize * nodeNumber;

        stream = new ChunkFileReader(enFile, chunkStart, chunkSize);
        stream.open();

        do {
            line = stream.readLine();
            if(line != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                String word = tokenizer.nextToken();
                String id = tokenizer.nextToken();
                for (WordFeature feature: results) {
                    if(feature.getWord().equals(word)) {
                        feature.setEnId(id);
                    }
                }
            }
        } while (line != null);
        stream.close();
    }
}

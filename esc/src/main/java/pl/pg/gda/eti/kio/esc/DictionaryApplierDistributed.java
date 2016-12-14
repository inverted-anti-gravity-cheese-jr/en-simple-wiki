package pl.pg.gda.eti.kio.esc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import pl.pg.gda.eti.kio.esc.data.WordFeature;


/**
 * @author Wojciech Stanisławski
 * @since 08.11.2016
 */
public class DictionaryApplierDistributed {
    private static final int MAX_LOAD = 32;

    private BlockingQueue<String> linesToTake;
    private BlockingQueue<String> linesToSave;

    public void applyDictionary(String fileName, String outputFileName, List<WordFeature>[] chunks, boolean simple) throws IOException, InterruptedException {
        int numCores = chunks.length;
        int i, linesBalance = 0, saved = 0;

        linesToTake = new ArrayBlockingQueue<String>(MAX_LOAD);
        linesToSave = new ArrayBlockingQueue<String>(MAX_LOAD);

        // master

        File file = new File(fileName);
        BufferedReader stream = new BufferedReader(new FileReader(fileName));

        File outputFile = new File(outputFileName);
        BufferedWriter outputStream = new BufferedWriter(new FileWriter(outputFile));

        DictionaryApplierNode[] workerNodes = new DictionaryApplierNode[numCores - 1];
        ExecutorService executor = Executors.newFixedThreadPool(numCores - 1);


        for(i = 0; i < numCores - 1; i++) {
            workerNodes[i] = new DictionaryApplierNode(this, chunks[i], i, numCores - 1, simple);
        }

        for(i = 0; i < numCores - 1; i++) {
            workerNodes[i].setNextNode(workerNodes[(i + 1) % (numCores - 1)]);
            executor.submit(workerNodes[i]);
        }
        executor.shutdown();

        String line = "";

        do {
            if (!linesToSave.isEmpty()) {
                outputStream.write(linesToSave.poll() + "\n");
                linesBalance--;
                System.out.print("\rSaved " + (++saved) + " articles");
            }

            if(line == null || linesToTake.size() >= MAX_LOAD) {
                continue;
            }

            line = stream.readLine();

            if(line != null) {
                linesToTake.add(line);
                linesBalance++;
            }
        }while (!linesToSave.isEmpty() || !linesToTake.isEmpty() || linesBalance != 0);

        System.out.println();

        for(i = 0; i < numCores - 1; i++) {
            workerNodes[i].close();
        }
        while(!executor.isTerminated());

        stream.close();
        outputStream.close();

    }

    private synchronized void demandSave(String save) {
        try {
            linesToSave.put(save);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized boolean checkSaveLoad() {
        return linesToSave.size() >= MAX_LOAD;
    }

    private synchronized String demandLastResource() {
        if(linesToTake.isEmpty()) {
            return null;
        }
        try {
            return linesToTake.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public class ArticleFromLine {
        public int passes;
        public String articleId;
        public String[] words;

        @Override
        public String toString() {
            String line = articleId + "#";
            for(String word : words) {
                if(word.startsWith("\\")) {
                    line += word.substring(1) + " ";
                }
            }
            return line.trim();
        }
    }

    public class DictionaryApplierNode implements Runnable {
        private final boolean simple;
        private DictionaryApplierDistributed parentNode;
        private boolean finnish;
        private List<WordFeature> chunk;
        private DictionaryApplierNode nextNode;
        private int numNodes;
        private int nodeId;
        private BlockingQueue<ArticleFromLine> queue;

        public DictionaryApplierNode(DictionaryApplierDistributed parentNode, List<WordFeature> chunk, int nodeId, int numNodes, boolean simple) {
            this.parentNode = parentNode;
            this.chunk = chunk;
            this.nodeId = nodeId;
            this.numNodes = numNodes;
            this.simple = simple;
            queue = new ArrayBlockingQueue<ArticleFromLine>(numNodes * 5);
        }

        public void setNextNode(DictionaryApplierNode nextNode) {
            this.nextNode = nextNode;
        }

        public void close() {
            finnish = true;
        }


        @Override
        public void run() {

            // slave
            do {
                String line = null;
                ArticleFromLine article = null;
                int i;

                if(!queue.isEmpty()) {
                    try {
                        article = queue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(article == null) {
                    line = parentNode.demandLastResource();
                }

                if(article != null) {
                    for (i = 0; i < article.words.length; i++) {
                        String word = article.words[i];
                        if (word.startsWith("\\")) {
                            continue;
                        }
                        String wordId = word.substring(0, word.indexOf('-'));
                        for (WordFeature feature : chunk) {
                            if ((simple && wordId.equals(feature.getSimpleId())) || (!simple && wordId.equals(feature.getEnId()) )) {
                                article.words[i] = "\\" + feature.getWord() + word.substring(word.indexOf('-'));
                            }
                        }
                    }
                    article.passes++;
                    if(article.passes == numNodes) {
                        while(checkSaveLoad());
                        demandSave(article.toString());
                    }
                    else {
                        passFurther(article);
                    }
                }
                else if (line != null) {
                    String lineId = line.substring(0, line.indexOf('#'));
                    article = new ArticleFromLine();
                    article.articleId = lineId;
                    article.passes = 1;

                    String[] words = line.substring(line.indexOf('#') + 1).split(" ");

                    for (i = 0; i < words.length; i++) {
                        String word = words[i];
			if(!word.contains("-")) {
			    continue;
			}
			
                        String wordId = word.substring(0, word.indexOf('-'));
                        for (WordFeature feature : chunk) {
                            if ((simple && wordId.equals(feature.getSimpleId())) || (!simple && wordId.equals(feature.getEnId()) )) {
                                words[i] = "\\" + feature.getWord() + word.substring(word.indexOf('-')) + " ";
                            }
                        }
                    }
                    article.words = words;
                    passFurther(article);
                }
            } while(!queue.isEmpty() || !finnish);
        }

        private void passFurther(ArticleFromLine article) {
            try {
                nextNode.queue.put(article);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
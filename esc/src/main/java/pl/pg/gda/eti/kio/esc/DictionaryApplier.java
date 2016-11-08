package pl.pg.gda.eti.kio.esc;

import pl.pg.gda.eti.kio.esc.data.WordFeature;

import java.io.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Wojciech Stanisławski
 * @since 08.11.2016
 */
public class DictionaryApplier {
    private static final int MAX_LOAD = 32;

    private BlockingQueue<String> linesToTake;
    private BlockingQueue<String> linesToSave;

    public void applyDictionary(String fileName, String outputFileName, List<WordFeature>[] chunks, boolean simple) throws IOException, InterruptedException {
        int numCores = Runtime.getRuntime().availableProcessors();
        int i, linesBalance = 0;

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
            workerNodes[i] = new DictionaryApplierNode(this, chunks[i], numCores - 1, simple);
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
                System.out.println(linesBalance + ",sv " + linesToSave.size() + ",tk " + linesToTake.size());
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

        for(i = 0; i < numCores - 1; i++) {
            workerNodes[i].close();
        }
        while(!executor.isTerminated());

        stream.close();
        outputStream.close();

    }

    private synchronized void demandSave(String save) {
        linesToSave.add(save);
    }

    private synchronized String demandLastResource() {
        if(linesToTake.isEmpty()) {
            return null;
        }
        return linesToTake.poll();
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
        private DictionaryApplier parentNode;
        private boolean finnish;
        private List<WordFeature> chunk;
        private DictionaryApplierNode nextNode;
        private int numNodes;
        private long threadId;
        private BlockingQueue<ArticleFromLine> queue;

        public DictionaryApplierNode(DictionaryApplier parentNode, List<WordFeature> chunk, int numNodes, boolean simple) {
            this.parentNode = parentNode;
            this.chunk = chunk;
            this.numNodes = numNodes;
            this.simple = simple;
            queue = new ArrayBlockingQueue<ArticleFromLine>(numNodes);
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
            threadId = Thread.currentThread().getId();
            do {
                String line = null;
                ArticleFromLine article = null;
                int i;

                if(!queue.isEmpty()) {
                    article = queue.poll();
                }

                if(article == null) {
                    line = parentNode.demandLastResource();
                }

                if(article != null) {
                    System.out.println("Got passed article");
                    for (i = 0; i < article.words.length; i++) {
                        String word = article.words[i];
                        if (word.startsWith("\\")) {
                            continue;
                        }
                        String wordId = word.substring(0, word.indexOf('-'));
                        for (WordFeature feature : chunk) {
                            if ((simple && feature.getSimpleId().equals(wordId)) || (!simple && feature.getEnId().equals(wordId))) {
                                article.words[i] = "\\" + feature.getWord() + word.substring(word.indexOf('-'));
                            }
                        }
                    }
                    article.passes++;
                    if(article.passes == numNodes) {
                        System.out.println("Done article");
                        while(parentNode.linesToSave.size() >= MAX_LOAD);
                        System.out.println("Lock off");
                        demandSave(article.toString());
                    }
                    else {
                        passFurther(article);
                        System.out.println("Pass already passed");
                    }
                }
                else if (line != null) {
                    System.out.println("Took article");
                    String lineId = line.substring(0, line.indexOf('#'));
                    article = new ArticleFromLine();
                    article.articleId = lineId;
                    article.passes = 1;

                    String[] words = line.substring(line.indexOf('#') + 1).split(" ");

                    for (i = 0; i < words.length; i++) {
                        String word = words[i];
                        String wordId = word.substring(0, word.indexOf('-'));
                        for (WordFeature feature : chunk) {
                            if ((simple && feature.getSimpleId().equals(wordId)) || (!simple && feature.getEnId().equals(wordId))) {
                                words[i] = "\\" + feature.getWord() + word.substring(word.indexOf('-')) + " ";
                            }
                        }
                    }
                    article.words = words;
                    passFurther(article);
                    System.out.println("Pass tooked");
                }
            } while(!queue.isEmpty() || !finnish);
        }

        private void passFurther(ArticleFromLine article) {
            nextNode.queue.add(article);
        }
    }

}
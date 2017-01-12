package pl.pg.gda.eti.kio.esc;


import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
/**
 * @author Wojciech Stanisławski
 * @since 08.11.2016
 */
public class CLIApp {

    private static final boolean DISTRIBUTED = false;

    public static void main(String[] args) throws IOException, InterruptedException {
	long start, end;
	
	File simpleApplied = new File("temp-data/simple-applied");
	File enApplied = new File("temp-data/en-applied");
	
	if(!simpleApplied.exists() || !enApplied.exists()) {
	    WordDictionaryMerger merger = new WordDictionaryMerger();
	    start = System.currentTimeMillis();
	    if(DISTRIBUTED) {
		merger.mergeFiles("simple/temp-po_slowach-feature_dict-simple-20120104", "en/en-po_slowach-feature_dict-en-20111201", 4);
	    }
	    else {
		merger.mergeFiles("simple/temp-po_slowach-feature_dict-simple-20120104", "en/en-po_slowach-feature_dict-en-20111201", 1);
	    }
	    end = System.currentTimeMillis();
	    System.out.println("Reading dicts finnished, it took " + (end - start) + " ms");

	    if (DISTRIBUTED) {
		DictionaryApplierDistributed applier = new DictionaryApplierDistributed();
		start = System.currentTimeMillis();
		applier.applyDictionary("simple/temp-po_slowach-lista-simple-20120104", "temp-data/simple-applied", merger.getChunks(), true);
		end = System.currentTimeMillis();
		System.out.println("Appling dict to simple finnished, it took " + (end - start) + " ms");

		start = System.currentTimeMillis();
		applier.applyDictionary("en/en-po_slowach-lista-en-20111201", "temp-data/en-applied", merger.getChunks(), false);
		end = System.currentTimeMillis();
		System.out.println("Appling dict to en finnished, it took " + (end - start) + " ms");
	    } else {
		DictionaryApplier applier = new DictionaryApplier();
		start = System.currentTimeMillis();
		applier.applyDictionary("simple/temp-po_slowach-lista-simple-20120104", "temp-data/simple-applied", merger.getChunks()[0], true);
		end = System.currentTimeMillis();
		System.out.println("Appling dict to simple finnished, it took " + (end - start) + " ms");

		start = System.currentTimeMillis();
		applier.applyDictionary("en/en-po_slowach-lista-en-20111201", "temp-data/en-applied", merger.getChunks()[0], false);
		end = System.currentTimeMillis();
		System.out.println("Appling dict to en finnished, it took " + (end - start) + " ms");
	    }
	}
	if (Files.notExists(FileSystems.getDefault().getPath("temp-data/cosine-similarity"))) {
		CosineSimilarityCounter counter = new CosineSimilarityCounter();
		start = System.currentTimeMillis();
		counter.countSimilarityForAll("temp-data/en-applied", "temp-data/simple-applied", "temp-data/cosine-similarity");
		end = System.currentTimeMillis();
		System.out.println("Counting cosine finnished, it took " + (end - start) + " ms");
	}
	ResultParser resultParser = new ResultParser();
	resultParser.parseResultFile("temp-data/cosine-similarity", "en/en-po_slowach-articles_dict-en-20111201", "simple/temp-po_slowach-categories-simple-20120104", "simple/temp-po_slowach-cats_dict-simple-20120104", "temp-data/result");
    }
}

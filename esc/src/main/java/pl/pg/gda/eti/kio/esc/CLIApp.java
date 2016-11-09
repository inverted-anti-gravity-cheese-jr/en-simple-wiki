package pl.pg.gda.eti.kio.esc;

import pl.pg.gda.eti.kio.esc.data.WordFeature;
import sun.net.www.http.ChunkedInputStream;

import java.io.*;

/**
 * @author Wojciech Stanisławski
 * @since 08.11.2016
 */
public class CLIApp {

    public static void main(String[] args) throws IOException, InterruptedException {
        long start, end;

        WordDictionaryMerger merger = new WordDictionaryMerger();
        start = System.currentTimeMillis();
        merger.mergeFiles("simple/temp-po_slowach-feature_dict-simple-20120104", "en/en-po_slowach-feature_dict-en-20111201");
        end = System.currentTimeMillis();
        System.out.println("Reading dicts finnished, it took " + (end - start) + " ms");

        DictionaryApplier applier = new DictionaryApplier();
        start = System.currentTimeMillis();
        applier.applyDictionary("simple/temp-po_slowach-lista-simple-20120104", "temp-data/simple-applied", merger.getChunks(), true);
        end = System.currentTimeMillis();
        System.out.println("Appling dict to simple finnished, it took " + (end - start) + " ms");

        start = System.currentTimeMillis();
        applier.applyDictionary("en/en-po_slowach-lista-en-20111201", "temp-data/en-applied", merger.getChunks(), false);
        end = System.currentTimeMillis();
        System.out.println("Appling dict to en finnished, it took " + (end - start) + " ms");

    }
}

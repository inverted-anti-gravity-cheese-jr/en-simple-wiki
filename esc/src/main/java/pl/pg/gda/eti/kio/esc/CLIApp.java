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
        WordDictionaryMerger merger = new WordDictionaryMerger();
        merger.mergeFiles("simple/temp-po_slowach-feature_dict-simple-20120104", "en/en-po_slowach-feature_dict-en-20111201");

        DictionaryApplier applier = new DictionaryApplier();
        applier.applyDictionary("simple/temp-po_slowach-lista-simple-20120104-test", "temp-data/simple-applied", merger.getChunks(), true);

    }
}

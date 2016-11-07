package pl.pg.gda.eti.kio.esc;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.io.*;
import java.util.StringTokenizer;

public class MergeWordDictionaryAction implements SparkAction {

    private PairFunction<String, String, String> mappingFunction = new PairFunction<String, String, String>() {
        @Override
        public Tuple2<String, String> call(String s) throws Exception {
            StringTokenizer tokenizer = new StringTokenizer(s, "\t");

            String word = tokenizer.nextToken();
            String id = tokenizer.nextToken();

            return new Tuple2<String, String>(word, id);
        }
    };

    private JavaPairRDD<String, Tuple2<String, String>> unifiedWordDictionary;

    @Override
    public void doAction(JavaSparkContext sc) {
        JavaRDD<String> wordsSimple = sc.textFile("simple/temp-po_slowach-feature_dict-simple-20120104");
        JavaRDD<String> wordsEn = sc.textFile("en/en-po_slowach-feature_dict-en-20111201");

        JavaPairRDD<String, String> wordsWithKeySimple = wordsSimple.mapToPair(mappingFunction);
        JavaPairRDD<String, String> wordsWithKeyEn = wordsEn.mapToPair(mappingFunction);

        unifiedWordDictionary = wordsWithKeySimple.join(wordsWithKeyEn);
    }

    public JavaPairRDD<String, Tuple2<String, String>> getUnifiedWordDictionary() {
        return unifiedWordDictionary;
    }

    public void saveToTextFile(String tf) {
        unifiedWordDictionary.saveAsTextFile(tf);
    }

}

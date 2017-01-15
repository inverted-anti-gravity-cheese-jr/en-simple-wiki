package pl.pg.gda.eti.kio.esc.cosine;

import pl.pg.gda.eti.kio.esc.ResultParser;
import pl.pg.gda.eti.kio.esc.TimeCounter;
import pl.pg.gda.eti.kio.esc.WordDictionaryMerger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

/**
 * @author Wojciech Stanisławski
 * @since 15.01.17
 */
public class CosineClassifier {

	public static void classify(CosineClassificationSettings settings) throws IOException, InterruptedException {
		TimeCounter time = new TimeCounter();

		File simpleApplied = new File(settings.simpleFeatureDictAppliedFileName);
		File enApplied = new File(settings.enFeatureDictAppliedFileName);

		if (!simpleApplied.exists() || !enApplied.exists()) {
			WordDictionaryMerger merger = new WordDictionaryMerger();
			time.start();
			if (settings.distributed) {
				merger.mergeFiles(settings.simpleFeatureDict, settings.enFeatureDict, 4);
			} else {
				merger.mergeFiles(settings.simpleFeatureDict, settings.enFeatureDict, 1);
			}
			time.end();
			time.printMessage("Reading dicts");

			if (settings.distributed) {
				DictionaryApplierDistributed applier = new DictionaryApplierDistributed();
				time.start();
				applier.applyDictionary(settings.simpleWordArticleDict, settings.simpleFeatureDictAppliedFileName, merger.getChunks(), true);
				time.end();
				time.printMessage("Appling dict to simple (distributed)");

				time.start();
				applier.applyDictionary(settings.enWordArticleDict, settings.enFeatureDictAppliedFileName, merger.getChunks(), false);
				time.end();
				time.printMessage("Appling dict to en (distributed)");
			} else {
				DictionaryApplier applier = new DictionaryApplier();
				time.start();
				applier.applyDictionary(settings.simpleWordArticleDict, settings.simpleFeatureDictAppliedFileName, merger.getChunks()[0], true);
				time.end();
				time.printMessage("Appling dict to simple");

				time.start();
				applier.applyDictionary(settings.enWordArticleDict, settings.enFeatureDictAppliedFileName, merger.getChunks()[0], false);
				time.end();
				time.printMessage("Appling dict to en");
			}
		}
		if (Files.notExists(FileSystems.getDefault().getPath(settings.cosineSimilarityResultFileName))) {
			CosineSimilarityCounter counter = new CosineSimilarityCounter();
			time.start();
			counter.countSimilarityForAll(settings.enFeatureDictAppliedFileName, settings.simpleFeatureDictAppliedFileName, settings.cosineSimilarityResultFileName);
			time.end();
			time.printMessage("Counting cosine");
		}

		ResultParser resultParser = new ResultParser();
		time.start();
		resultParser.parseResultFile(settings.cosineSimilarityResultFileName, settings.enArticleDictionaryFile, settings.simpleArticleCategoryRelationFile, settings.simpleCategoryDictionaryFile, settings.outputFile);
		time.end();
		time.printMessage("Parsing results");
	}
}

package crfModel;

import Feature.FieldAppender;
import crfModel.Tool.CRFPPWrapper;
import crfModel.Tool.CRFsuiteWrapper;
import crfModel.Tool.CrfToolInterface;
import evaluate.Ner;
import evaluate.NewWordDetector;
import evaluate.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wan on 4/24/2017.
 */
abstract public class CRFModel implements NewWordDetector {
	static protected final String label_begin = "B", label_meddle = "M", label_end = "E", label_single = "S",
			label_true = "T", label_false = "F", label_inside = "I", label_other = "O";
	private static Logger logger = LoggerFactory.getLogger(CRFModel.class);
	private CrfToolInterface crfToolWrapper;

	public String model, template, trainData;

	{
		if (config.isCRFsuite) {
			crfToolWrapper = new CRFsuiteWrapper(this);
		} else
			crfToolWrapper = new CRFPPWrapper(this);
	}

	public static String getWord(String in) {
		return in.split("\t", 2)[0];
	}

	public static String getLabel(String in) {
		String[] tmp = in.split("\t");
		return tmp[tmp.length - 1];
	}

	public static Map<String, String> convertTestOuput2Res(String inputFile, String newWordFile, Ner ner) {
		logger.debug("converting label to ans file {}", newWordFile);
		HashMap<String, String> newWordList = new HashMap<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			PrintWriter writer = new PrintWriter(new FileWriter(newWordFile));
			String line = reader.readLine();

			while (true) {
				if (line == null) break;
				if (line.length() == 0) {
					line = reader.readLine();
					continue;
				}
				StringBuilder wordBuffer = new StringBuilder();
				FieldAppender fieldAppender = null;
				String label_head = getLabel(line);
				if (getLabel(line).contains(label_begin)) {
					while (!getLabel(line).contains(label_other) && !getLabel(line).contains(label_single)) {
						if (fieldAppender == null)
							fieldAppender = new FieldAppender(line);
						else
							fieldAppender.append(line);
						wordBuffer.append(getWord(line));
						line = reader.readLine();
						if (line.length() <= 0) break;
						if (getLabel(line).contains(label_begin)) break;
					}
				}
				else {
					fieldAppender = new FieldAppender(line);
					wordBuffer.append(getWord(line));
					line = reader.readLine();
				}
				String word = wordBuffer.toString();// 这是一个词
				if (ner == Ner.nw) {
					if (config.renmingribaoWord.isNewWord(word, null) && !newWordList.keySet().contains(word)) {
						newWordList.put(word, fieldAppender.toString());
						writer.println(word + "\t" + fieldAppender);
					}
				} // nw
				if (ner != Ner.nw) {
					if (label_head.equals(ner.label + label_begin) || label_head.equals(ner.label + label_single)) //单字名称 和 多字名称
						if (!newWordList.keySet().contains(word)) {
							newWordList.put(word, fieldAppender.toString());
							writer.println(word + "\t" + fieldAppender);
						}
				} // nr ,ns
			}

			writer.close();
		} catch (IOException e) {
			logger.error("err!");
			e.printStackTrace();
		}
		return newWordList;
	}

	public void train(String[] inputFiles, Ner ner) {
		model = "data/model/" + this.getClass().getSimpleName() + "." + ner.model+ ".model";
		template = "data/crf-template/" + this.getClass().getSimpleName() + "." + ner.template+ ".template";
		trainData = "tmp/crf/" + this.getClass().getSimpleName() + "." + ner.model+ ".crf";
		convert2TrainInput(inputFiles, ner);
		crfToolWrapper.train(template, model, trainData);
	}

	public Map<String, String> detectNewWord(String inputFile, String outputFile, Ner ner) {
		model = "data/model/" + this.getClass().getSimpleName() + "." + ner.model+ ".model";
		String crfppInput = String.join("", "tmp/crf/", inputFile.replaceAll(".*/", ""),
				".", this.getClass().getSimpleName(), ".", ner.label, ".crfin");
		String crfppOutput = String.join("", "tmp/crf/", inputFile.replaceAll(".*/", ""),
				".", this.getClass().getSimpleName(), ".", ner.label, ".crfout");
		convertSrc2TestInput(new String[]{inputFile}, crfppInput, ner);
		crfToolWrapper.decode("data/model/" + this.getClass().getSimpleName() + "." + ner.model + ".model", crfppInput,
				crfppOutput);
		return convertTestOuput2Res(crfppOutput, outputFile, ner);
	}

	abstract void convert2TrainInput(String[] inputFiles, Ner ner);

	abstract void convertSrc2TestInput(String[] inputFiles, String crfppInput, Ner ner);


}

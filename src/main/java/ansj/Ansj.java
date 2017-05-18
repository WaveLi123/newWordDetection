package ansj;

import dataProcess.Corpus;
import evaluate.NewWordDetector;
import evaluate.Test;
import evaluate.config;
import org.ansj.domain.Term;
import org.ansj.splitWord.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Created by wan on 4/28/2017.
 */
public class Ansj implements NewWordDetector {
	private static Logger logger = LoggerFactory.getLogger(Ansj.class);
	Analysis parser;

	public static void main(String... args) throws IOException {
		//segFileForWord2Vec(config.totalDataInput, "CRFPPWrapper/char.txt", "CRFPPWrapper/word.txt");
		Ansj ansj = new AnsjToAnalysis();
		for (String type: config.supportedType)
		Test.test(Test.readWordList(Test.getAnswerFile(config.testDataInput, type)), ansj.detectNewWord(config.testDataInput,
				"CRFPPWrapper/CRFPPWrapper." + type, type), ansj.getClass().getSimpleName() + "." + type);
	}

	static public void segFile(Analysis parser, String input, String output) {
		try {
			try (BufferedReader reader = new BufferedReader(new FileReader(input));
				 BufferedWriter writer = new BufferedWriter(new FileWriter(output))
			) {
				String line, tmp;
				while ((line = reader.readLine()) != null) {
					tmp = parser.parseStr(line).toString(" ");
					writer.append(tmp);
					writer.newLine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void segFileForWord2Vec(String input, String output1, String output2) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(input));
			 BufferedWriter writer1 = new BufferedWriter(new FileWriter(output1));
			 BufferedWriter writer2 = new BufferedWriter(new FileWriter(output2))
		) {
			String line, tmp;
			while ((line = reader.readLine()) != null) {
				if (line.length() == 0) continue;
				tmp = parser.parseStr(line).toStringWithOutNature(" ");
				for (int i = 0; i < line.length(); i++)
					writer1.append(line.charAt(i) + " ");
				writer1.newLine();
				writer2.append(tmp);
				writer2.newLine();
			}
		}

	}

	@Override
	public Map<String, String> detectNewWord(String inputFile, String outputFile, String pattern) {
		HashMap<String, String> newWordList = new HashMap<>();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.length() == 0) {
					continue;
				}
				List<Term> list = parser.parseStr(line).getTerms();
				for (Term term : list) {
					String word = term.getRealName(), pos = term.getNatureStr();
					if (pattern == config.nw) {
						if ((Corpus.isNewWord(word, pos)) && !newWordList.keySet().contains(word)
								) {
							newWordList.put(word, pos);
							writer.append(word + "\t" + pos);
							writer.newLine();
						}
					}// nw
					if (pattern == config.nr || pattern == config.ns) {
						if (pos.equals(pattern) && !newWordList.keySet().contains(word)) {
							newWordList.put(word, "nothing");
							writer.append(word);
							writer.newLine();
						}
					} // nr ns
				}
			}
			writer.close();
		} catch (java.io.IOException e) {
			logger.error("count word using ansj error");
			e.printStackTrace();
		}
		return newWordList;
	}

}

package dataProcess;

import evaluate.Ner;
import evaluate.RunSystemCommand;
import evaluate.Test;
import evaluate.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Created by wan on 4/7/2017.
 */
public class Corpus {
	private static final Logger logger = LoggerFactory.getLogger(Corpus.class);
	private static final Corpus renMinRiBao = new Corpus(config.renmingribao);
	public Set<String> wordList;

	public Corpus(String inputFile) {
		wordList = countSeg(inputFile);
	}

	static void clean() {
		//RunSystemCommand.run("rm data/corpus/*.words");
		RunSystemCommand.run("rm data/test/input/*");
		RunSystemCommand.run("rm data/test/ans/*");
		RunSystemCommand.run("rm data/test/*");
	}

	public static HashSet<String> extractWord(String inputFile, Ner nerType) {
		WordInfoInCorpus wordInfo = new WordInfoInCorpus(config.getInputFile(inputFile));
		HashSet<String> wordList = new HashSet<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(config.getAnswerFile(inputFile, nerType)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() == 0) continue;
				String[] strs = line.split(config.sepWordRegex);
				for (int i = 0; i < strs.length; i++) {
					String w = strs[i];
					String word = config.removePos(w);
					String pos = config.getPos(w);
					try {
						if (
								(nerType != nerType.nw && pos.contains(nerType.pattern) && !word.matches(config
										.newWordExcludeRegex)
										||
										(nerType == Ner.nw && renMinRiBao.isNewWord(word)
												//&&	!(inputFile == config.testData && !trainData.isNewWord(word))
										)
								)
										&& !wordList.contains(word)) {
							int lRE = 9, rLE = 9;
							if (i > 0)
								lRE = wordInfo.discreteWordInfo.getRE(config.removePos(strs[i - 1]));
							if (i < strs.length - 1)
								rLE = wordInfo.discreteWordInfo.getLE(config.removePos(strs[i + 1]));
							writer.append(
									wordInfo.addWordInfo(String.join("\t",
											word, config.category(word),
											Integer.toString(word.length()),
											pos
											//, Integer.toString(lRE)
											//,Integer.toString(rLE)
									)));
							wordList.add(word);
							writer.newLine();
							if (i > 0)
								if (renMinRiBao.isNewWord(word) && renMinRiBao.isNewWord(config.removePos(strs[i -
										1])))
									System.err.println(strs[i - 1] + "\t" + strs[i]);
						}
					} catch (IOException e) {
						logger.debug("untagged {}", line);
					}
				}
			}
			logger.info("{} {} in {}", wordList.size(), nerType.pattern, inputFile);
			writer.close();
		} catch (IOException e) {
			logger.error("err");
		}
		return wordList;
	}

	static void getInfoForAllWord(String inputFile) {
		WordInfoInCorpus wordInfo = new WordInfoInCorpus(config.getInputFile(inputFile));
		HashSet<String> wordList = new HashSet<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter("info/info.all"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() == 0) continue;
				String[] strs = line.split(config.sepWordRegex);
				for (int i = 0; i < strs.length; i++) {
					String w = strs[i];
					String word = config.removePos(w);
					String pos = config.getPos(w);
					try {
						if (!wordList.contains(word))
							if (!word.matches(config.newWordExcludeRegex)) {
								int lRE = 9, rLE = 9;
								if (i > 0)
									lRE = wordInfo.discreteWordInfo.getRE(config.removePos(strs[i - 1]));
								if (i < strs.length - 1)
									rLE = wordInfo.discreteWordInfo.getLE(config.removePos(strs[i + 1]));
								writer.append(
										wordInfo.addWordInfo(String.join("\t",
												word, config.category(word),
												Integer.toString(word.length()),
												pos,
												Integer.toString(lRE),
												Integer.toString(rLE),
												renMinRiBao.isNewWord(word) ? "True" : "False"
										)));
								wordList.add(word);
								writer.newLine();
								if (i > 0)
									if (renMinRiBao.isNewWord(word) && renMinRiBao.isNewWord(config.removePos(strs[i -
											1])))
										System.err.println(strs[i - 1] + "\t" + strs[i]);
							}
					} catch (IOException e) {
						logger.debug("untagged {}", line);
					}
				}
			}
			logger.info("{} word in {}", wordList.size(), inputFile);
			writer.close();
		} catch (IOException e) {
			logger.error("err");
		}
	}

	public static boolean isNewWord(String word, String pos) {
		return renMinRiBao.isNewWord(word);// && trainData.isNewWord(word);
	}

	/**
	 * 以行为单位打乱
	 *
	 * @param inputFile
	 * @param trainFile
	 * @param testFile
	 */
	public static void shuffleAndSplit(String inputFile, String trainFile, String testFile, String totalFile) {
		try {
			boolean last = false, curr;
			int totalSize = 0;
			List<String> article = new ArrayList<>();
			BufferedWriter writerTotal = new BufferedWriter(new FileWriter(totalFile));

			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			String line;
			StringBuilder buffer = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				if (line.length() == 0) continue;
				curr = line.substring(line.length() - 3).replaceAll("/.*", "").matches(".*[稿\\pP&&[^】]]");
				if (last && !curr) {
					article.add(buffer.toString());
					writerTotal.append(buffer.toString());
					totalSize += buffer.length();
					writerTotal.newLine();
					buffer = new StringBuilder();
				}
				buffer.append(line);
				buffer.append("\n");
				last = curr;
			}
			//没清空的
			article.add(buffer.toString());
			writerTotal.append(buffer.toString());
			writerTotal.close();

			if (config.isShuffle) {
				logger.info("article size {}", article.size());
				Collections.shuffle(article); // todo no shuffle
				RunSystemCommand.run("rm data/model/*.model");
			}
			Random random = new Random();
			int s = 300000;//random.nextInt(totalSize - totalSize / config.testSize);//todo 截取的起始位置
			int i = 0;
			int currentSize = 0;
			BufferedWriter writerTrain = new BufferedWriter(new FileWriter(trainFile));
			for (; currentSize < s; i++) {
				currentSize += article.get(i).length();
				writerTrain.append(article.get(i));
				writerTrain.newLine();
			}

			BufferedWriter writerTest = new BufferedWriter(new FileWriter(testFile));
			currentSize = 0;
			for (; currentSize < totalSize / config.testSize; i++) {
				writerTest.append(article.get(i));
				currentSize += article.get(i).length();
				writerTest.newLine();
			}
			writerTest.close();//测试文件

			for (; i < article.size(); i++) {
				writerTrain.append(article.get(i));
				writerTrain.newLine();
			}
			writerTrain.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String convertToSrc(String inputFile, String outputFile) {
		BufferedReader reader = null;
		int word = 0, article = 0;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			reader = new BufferedReader(new FileReader(inputFile));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.length() == 0) {
					article++;
					writer.newLine();
					continue;
				}
				line = line.replaceAll("/[^ ]+", "");
				line = line.replaceAll(" +", "");
				writer.append(line);
				writer.newLine();
				word += line.length();
			}
			writer.close();
			logger.info("{} article {} characters in {}", article, word, outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputFile;
	}

	/**
	 * 从数据集中提取新词，分割为训练集和测试集
	 *
	 * @param args
	 */
	public static void main(String... args) throws IOException {
		clean();
		config.testData = "data/test/test.txt";
		config.trainData = "data/test/train.txt";
		config.totalData = "data/test/total.txt";
		ConvertHalfWidthToFullWidth.convertFileToFulllKeepPos(config.news, config.newWordFile);
		shuffleAndSplit(config.newWordFile, config.trainData, config.testData, config.totalData);
		RunSystemCommand.run("rm data/corpus/wordlist/train.txt.wordlist");
		RunSystemCommand.run("rm data/corpus/*");

		convertToSrc(config.testData, config.getInputFile(config.testData));
		convertToSrc(config.trainData, config.getInputFile(config.trainData));
		convertToSrc(config.totalData, config.getInputFile(config.totalData));

		//config.wordInfoInCorpus_total = new WordInfoInCorpus(config.totalDataInput);
		for (Ner type : Ner.supported) {
			extractWord(config.trainData, type);
			extractWord(config.testData, type);
			extractWord(config.totalData, type);
		}
		getInfoForAllWord(config.totalData);
	}

	Set<String> countSeg(String inputFile) {
		Set<String> wordList;
		CounterMap wordCounter = new CounterMap();
		if (!new File(config.getWordListFile(inputFile)).exists()) {
			logger.debug("Scanning word list from {}...", inputFile);
			try {
				BufferedReader reader = new BufferedReader(new FileReader(ConvertHalfWidthToFullWidth
						.convertFileToFulllKeepPos(inputFile, "tmp/tmp")));
				String tmp;
				while ((tmp = reader.readLine()) != null) {
					String[] segs = tmp.split(config.sepWordRegex);
					for (String word : segs) {
						word = config.removePos(word).replaceAll("[\\[［]", "");
						if (!word.matches(config.newWordExcludeRegex))
							wordCounter.incr(word);
					}
				}
			} catch (java.io.IOException e) {
				e.printStackTrace();
				logger.error("Reading word list from {} err!", inputFile);
			}
			wordList = wordCounter.countAll().keySet();
			logger.info("[{}] word list size: {}", inputFile, wordList.size());
			wordCounter.output(config.getWordListFile(inputFile));
		} else {
			logger.info("Reading word lits from {} ...", config.getWordListFile(inputFile));
			wordList = Test.readWordList(config.getWordListFile(inputFile)).keySet();
			logger.info("[{}] word list size: {}", inputFile, wordList.size());
		}
		return wordList;
	}

	private boolean isNewWord(String word) {
		if (word.length() <= 1)
			return false;
		//标点符号，含字母和数字的不算
		//if (pos != null)
		//if (pos.matches("[tmq]")) return false;// todo 去除数量词 和 时间词

		word = config.newWordFileter(word);
		if (word.matches(config.newWordExcludeRegex))
			return false;
		if (!wordList.contains(word))
			return true;
		return false;
	}

}

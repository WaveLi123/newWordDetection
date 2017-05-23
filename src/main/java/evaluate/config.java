package evaluate;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import dataProcess.Corpus;
import dataProcess.WordInfoInCorpus;
import org.nlpcn.commons.lang.util.ObjConver;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import static evaluate.Test.readWordList;
import static evaluate.Test.test;

/**
 * Created by wan on 4/25/2017.
 */
public class config {


	final public static String sepSentenceRegex = "[，。？！：]";// 这样搞了之后就断不开了
	final public static String sepWordRegex = " +";

	final public static double thresholdMI = 10;
	final public static double thresholdTF = 3;
	final public static double thresholdNeighborEntropy = 1.5;
	final public static Integer testSize = 5;
	public static String comment;

	public static final String timeRegx =
			"([\\p{IsDigit}－～：兆亿万千百十九八七六五四三二一零○]+" +
			"((([年月日]|(世纪)|(年代))[前初中底末]?)|[号时分秒点]|(秒钟)|(点钟)|(月份)|(小时)))";
	public static final String pureLetterStringRegex = "([\\p{IsLatin}\\p{IsCyrillic}]+)";
	public static final String pureNumStringRegex = "(第?[兆亿万千百\\p{IsDigit}，．％∶：／×－＋·～]+)";
	public static final String letterWithNumStringRegex = "([\\p{IsDigit}\\p{IsCyrillic}\\p{IsLatin}．／－·～]+)";
	final public static String punctExcludeRegx = "([　°～｜■＋±\\pP&&[^·－／]]+)";
	final public static String newWordExcludeRegex = String.join("|", pureNumStringRegex, pureLetterStringRegex,
			letterWithNumStringRegex, timeRegx, punctExcludeRegx);

	public static final String pureChineseStringRegex = "([\\p{IsHan}]+)";
	public static final String chineseJoinedStringRegex = "([\\p{IsHan}·－／]+)";
	public static String newWordRemove;

	public static Integer levelNum = 10;
	public static Integer maxStringLength = 8;

	public static boolean isLoadCorpus = false;
	public static Boolean isShuffle = false;
	public static Boolean isNewWordFilter = true;
	public static Boolean isCRFsuite = true;
	public static String algorithm = " -p feature.minfreq=2";


	final public static String news = "data/raw/news.txt";
	final public static String renmingribao = "data/raw/renminribao.txt";
	public static String newWordFile = "tmp/newword.DBC";
	public static String corpusFile = "data/raw/renminribao.noPos.DBC";
	final public static String testData = "data/test/test.txt";
	final public static String trainData = "data/test/train.txt";
	final public static String totalData = "data/test/total.txt";

	public static Set<String> trainModel = new HashSet<>();
	public static Set<String> testModel = new HashSet<>();
	static {
		Properties prop = new Properties();
		try {
			//读取属性文件a.properties
			FileInputStream input = new FileInputStream(new File("config.properties"));
			prop.load(new InputStreamReader(input, Charset.forName("UTF-8")));     ///加载属性列表
			Iterator<String> it = prop.stringPropertyNames().iterator();
			while (it.hasNext()) {
				String key = it.next();
				if (key.equals("train")) {
					String[] tmp = prop.getProperty(key).split(",");
					for (String tmp0: tmp) {
						trainModel.add(tmp0);
					}
					continue;
				}
				if (key.equals("test")) {
					String[] tmp = prop.getProperty(key).split(",");
					for (String tmp0: tmp)
						testModel.add(tmp0);
					continue;
				}
				Field field = config.class.getField(key);
				field.set(null, ObjConver.conversion(prop.getProperty(key), field.getType()));
				System.out.println(key + "=" + prop.getProperty(key));
			}
			input.close();
		} catch (Exception e) {
			System.err.println(e);
		}
		RunSystemCommand.run("mkdir info");
		RunSystemCommand.run("mkdir tmp");
		RunSystemCommand.run("mkdir tmp/crf");
		RunSystemCommand.run("mkdir data");
		RunSystemCommand.run("mkdir data/corpus");
		RunSystemCommand.run("mkdir data/corpus/wordlist");
		RunSystemCommand.run("mkdir data/model");
		RunSystemCommand.run("mkdir data/test");
		RunSystemCommand.run("mkdir data/test/input");
		RunSystemCommand.run("mkdir data/test/ans");
	}

	//public static WordInfoInCorpus wordInfoInCorpus_total ;
	//public static WordInfoInCorpus wordInfoInCorpus_corpus = new WordInfoInCorpus(config.corpusFile);


	//static public final String basicWordListFile = "data/corpus/basicWordList.txt";

	public static String removePos(String in) {
		return in.replaceAll("/[^/]*$", "");
	}

	public static String getPos(String in) {
		return in.replaceAll("^.*/", "");
	}

	static public String newWordFileter(String word) {
		if (isNewWordFilter)
			return word.replaceAll(newWordRemove, "");
		return word;
	}

	public static void main(String... args) {
		for(String tmp: new String[]{"21", "20世纪末", "90年代", "5月","四月中", "三点钟","０３：２８"})
			System.err.println(tmp +"\t "+tmp.matches(timeRegx));
		for (Ner type : Ner.supported) {
			test(
					readWordList(config.getAnswerFile(config.trainData, type)),
					readWordList(config.getAnswerFile(config.testData, type)),
					type, "count", "count"
			);
		}
	}

	static public String category(String word) {
		if (word.matches(pureLetterStringRegex))
			return "纯字母";
		if (word.matches(pureNumStringRegex))
			return "纯数字";
		if (word.matches(letterWithNumStringRegex))
			return "字母和数字连字符组合";
		if (word.matches(pureChineseStringRegex))
			return "纯汉字";
		if (word.matches(chineseJoinedStringRegex))
			return "汉字加连字符斜杠分隔符";
		return "混合";
	}

	public static String getAnswerFile(String inputFile, Ner ner) {
		return "data/test/ans/" + inputFile.replaceAll(".*/", "") + "." + ner.name;
	}

	public static String getInputFile(String inputFile) {
		return "data/test/input/" + inputFile.replaceAll(".*/", "") + ".src";
	}
	public static String getWordListFile(String inputFile) {
		return "data/corpus/wordlist/" + inputFile.replaceAll(".*/", "") + ".wordlist";
	}
}

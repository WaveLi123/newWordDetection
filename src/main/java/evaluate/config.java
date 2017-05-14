package evaluate;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wan on 4/25/2017.
 */
public interface config {
	//final public static String sepSentenceRegex = "([【】°~～\\pP&&[^-－.%．·@／]]+)";
	final public static String sepSentenceRegex = "[，。？！：]";// 这样搞了之后就断不开了
	final public static String sepWordRegex = " +";
	//final public static String newWordExcludeRegex = "(.*[\\p{IsDigit}\\p{Lower}\\p{Upper}-[?]]+.*)" + "|" + ".*" +
	// sepSentenceRegex + ".*";
	String alphaNumExcludeRegx = "(第?[．％∶＋／×－·～\\p{IsDigit}亿万千百兆\\p{IsLatin}\\p{IsCyrillic}]+" +
			"([年月日号时分秒点]|(秒钟)|(点钟)|(月份)|(世纪)|(年代)|(小时))?" +
			"[型]?)";
	String punctExcludeRegx = "(.*[° ～｜■±+\\pP&&[^·－／]]+.*)";
	final public static String newWordExcludeRegex = punctExcludeRegx + "|" + alphaNumExcludeRegx;
	//final public static String newWordExcludeRegex = punctExcludeRegx;
	//标点符号和纯数字


	final public static String invalidSuffixRegex = "^(的|是|在|等|与|了)$";
	final public static double thresholdMI = 10;
	final public static double thresholdTF = 3;
	final public static double thresholdNeighborEntropy = 1.5;
	final public static double thresholdLeftEntropy = 1;
	final public static double thresholdRightEntropy = 1;
	final public static double thresholdLeftNumber = 1;
	final public static double thresholdLeftRightNumber = 1;
	final public static int testSize = 5;
	public static int levelNum = 10;
	public static int maxNagaoLength = 11;

	public static boolean isNagaoLoadedFromFile = false; //new File("data/model/nagao.corpus").exists();
	public static boolean isNagaoSavedIntoFile = false;
	public static boolean isLoadCorpus = false;
	public static boolean isTrain = true;
	public static boolean isShuffle = true;
	public static boolean isNewWordFilter = true;
	public static boolean isAnsjFeatureOpen = true;


	public static String renmingribao = "data/raw/renminribao.txt";
	//final public static String[] newWordFiles = {"data/raw/1_5000_1.segged.txt", "data/raw/1_5000_2.segged.txt",
	//		"data/raw/1_5000_3.segged.txt", "data/raw/1_5000_4.segged.txt", "data/raw/1_5000_5.segged.txt"};
	final public static String[] basicWordFiles = {renmingribao};
	public static String news = "data/raw/news.txt";
	public static String newWordFile = "tmp/input.txt";
	final public static String[] newWordFiles = {newWordFile};
	public static String testData = "data/test/test.txt";
	public static String trainData = "data/test/train.txt";
	public static String totalData = "data/test/total.txt";
	public static String testDataInput = "data/test/input/test.txt.src";
	public static String trainDataInput = "data/test/input/train.txt.src";
	public static String totalDataInput = "data/test/input/total.txt.src";
	public static String nw = "nw", nr = "nr", ns = "ns";
	public static String[] supportedType = new String[]{nw, nr, ns};
	String corpusInput = "data/raw/news.txt";
	String basicWordListFile = "data/corpus/basicWordList.txt";

	public static String removePos(String in) {
		return in.replaceAll("/[^/]*$", "");
	}

	public static String getPos(String in) {
		return in.replaceAll("^.*/", "");
	}

	static String newWordFileter(String word) {
		if (isNewWordFilter)
			return word.replaceAll("(型$)|(公司$)", "");
		return word;
	}

	public static void main(String... args) {
		System.out.println(removePos("a/b//l"));
		System.out.println(Double.parseDouble("-Infinity"));
		System.out.println(Double.NEGATIVE_INFINITY);
		System.out.println("７".matches(newWordExcludeRegex));
		System.out.println("Ｐ".matches(newWordExcludeRegex));
		System.out.println("１∶１００".matches(".*∶.*") + "1:100");
		System.out.println("Семёрка".matches(newWordExcludeRegex));
		System.out.println("你".matches("\\p{IsHan}"));
		if ("指令／秒".matches("[\\p{IsHan}·－／]+"))
		System.out.println(Test.getAnswerFile(testDataInput, nw));
		try {
			String tmp = PinyinHelper.convertToPinyinString("ak艾克", ",", PinyinFormat.WITH_TONE_NUMBER);
			System.out.println(tmp);
		} catch (PinyinException e) {
			e.printStackTrace();
		}
	}

	static void closeAnsj() {
		MyStaticValue.isNameRecognition = false;
		MyStaticValue.isNumRecognition = false;
		MyStaticValue.isQuantifierRecognition = false;
	}

	static void openAnsj() {
		MyStaticValue.isNameRecognition = true;
		MyStaticValue.isNumRecognition = true;
		MyStaticValue.isQuantifierRecognition = true;
	}

	static String category(String word) {
		if (word.matches("[\\p{IsLatin}\\p{IsCyrillic}]+"))
			return "纯字母";
		if (word.matches("[\\p{IsDigit}．％：／×—－·～]+"))
			return "纯数字";
		if (word.matches("[\\p{IsDigit}\\p{IsLatin}．％：／×—－·～]+"))
			return "字符和数字连字符组合";
		if (word.matches("[\\p{IsHan}]+"))
			return "纯汉字";
		if (word.matches("[\\p{IsHan}·－／]+"))
			return "汉字加连字符斜杠分隔符";
		return "混合";
	}
}

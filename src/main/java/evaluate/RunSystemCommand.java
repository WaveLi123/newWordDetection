package evaluate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 用来运行系统命令的，兼容linux和windows
 * Created by wan on 5/7/2017.
 */
public class RunSystemCommand {
	static String shell = "bash", option = "-c";
	static private Logger logger = LoggerFactory.getLogger("debug");

	static {
		if (System.getProperty("os.name").contains("Win")) {
			shell = "cmd";
			option = "/c";
		}
	}

	public static void run(String cmd) {
		try {
			logger.debug("Running command: [{}]", cmd);
			String[] cmds = new String[]{shell, option, cmd};
			Process pro = Runtime.getRuntime().exec(cmds);
			InputStream in = pro.getInputStream();
			InputStream err = pro.getErrorStream();
			BufferedReader read = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = read.readLine()) != null) {
				System.err.print("\r" + line);
				logger.trace(line);
			}
			in.close();
			pro.waitFor();
			in = pro.getErrorStream();
			read = new BufferedReader(new InputStreamReader(err));
			while ((line = read.readLine()) != null) {
				logger.error(line);
			}
			in.close();
			logger.trace("---------------------------------------------------------------------");
			logger.trace("---------------------------------------------------------------------");
		} catch (Exception e) {
			logger.error("Run command err! : [{}] ", cmd);
			e.printStackTrace();
		}
	}
}

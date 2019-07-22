package app.rtkafka;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import mars.app.config.ClodPort;

public class _RtKafka {
	static public String sm_rtKafkaResourcePath = "/res/";
	static public int sm_fileCacheSize = 100;

	static public int sm_rtKafkaPort = ClodPort.RtKafkaWeb.port();

	public static String VERSION = "v0.0.1 - RtKafka - 開發中版本";

	public static String sm_sDateFormat = "yyyy-MM-dd";
	public static String sm_sDBWriteDateTimeFormat = "yyyy-MM-dd HH:mm:ss";
	public static SimpleDateFormat dFormat = new SimpleDateFormat(_RtKafka.sm_sDateFormat);

	// Quote 最長要拉幾天 (根據盤後計算的所有 Rule 去算出一個最大天數，直接拉這個天數)
	public static final int MAX_QUOTE_DAYS = 240;
	

	
	// ------------------------------------------------------------------
	// API 要使用的
	public static final String AISTK_CODE = "ss";
	public static final ObjectMapper MAPPER = new ObjectMapper();
	public static final XmlMapper XML_MAPPER = new XmlMapper();
	public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
}

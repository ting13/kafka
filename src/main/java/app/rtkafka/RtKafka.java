package app.rtkafka;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.rtkafka.rapi.KafkaDBApi;
import excp.NoThrow;
import opt.Opt;
import time.Timing;
import timer.ITimer;
import webapp.WebAppObj;

/**
 * 策略相關的 Implementation
 * 
 * @author ericlin
 *
 */
public class RtKafka extends WebAppObj<RtKafkaApp, RtKafkaWeb, RtKafka> {

	// 用來處理 Honset DB API 的 Instance
	KafkaDBApi m_smartApi = new KafkaDBApi();
	
	public static ObjectMapper m_mapper = new ObjectMapper();

	protected RtKafka(RtKafkaApp app) throws SQLException {
		super(app, RtKafkaWeb.class, _RtKafka.sm_rtKafkaPort, "/res/icon/hat.ico", _RtKafka.sm_fileCacheSize,
				_RtKafka.sm_rtKafkaResourcePath);

		// 把 App 存起來，為了 reload config 使用
		m_app = app;
	}

	// 外面啟動 Service 的呼叫點
	public void start() {

		// 起來就 refresh 一次資料

		// 15 秒起來一次看是否要執行 job
		ITimer.getDefault().period("RtKafka2Run", Timing.ofSec(15), () -> NoThrow.act(() -> {
			checkNewDay();
			checkAndRun();
		}));
	}

	/**
	 * 檢查是否是早上七點 -> 要把歷史資料 Load 起來，更新盤中時間
	 */
	private void checkNewDay() {
		Calendar cal = Calendar.getInstance();

		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);

	}

	/**
	 * 執行真實的計算
	 */
	private void checkAndRun() {
		// 根據需要執行的時間區間，去叫起對應的 Module
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"));
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);

	}


	// -------------------------------------------------------------------------------------
	// 跟 Config 有關的相關 methods

	public void reloadConfig() {
		m_app.reloadConfig();
	}

	@Override
	public boolean authUser(String user, String pass) {
		return false;
	}

	public boolean isDemo() {
		Opt<Boolean> isDemo = m_app.toml("is_demo");
		return isDemo.hasValue() ? isDemo.get() : false;
	}



}

package app.rtkafka;

import excp.NoThrow;
import mars.app.AppDir;
import mars.app.TomlApp;
import mars.app.config.EApp;
import opt.Opt;

public class RtKafkaApp extends TomlApp<RtKafkaApp> {
	RtKafka m_rtKafka;

	// -----------------------------------
	protected RtKafkaApp(AppDir appDir) {
		super(EApp.rtkafka, appDir, _RtKafka.VERSION);
	}

	// -----------------------------------
	@Override
	protected boolean _isSelfAlive() {
		return true;
	}

	@Override
	protected void _extraCheck() {
	}

	@Override
	protected void _exec() {
		NoThrow.act(() -> {
			m_rtKafka = new RtKafka(this);

			// 確定有 toml config 裡面設定 backend 為 1 才開始 run backend (default 是不跑 -> 因為前端會持續上版測試)
			Opt<Integer> optBackend = tomlInt("backend");
			if (optBackend.hasValue() && optBackend.get() == 1) {
				m_rtKafka.start();
			} else {
				// front-end 版本要用的登入資訊
			}

			// 所有需要動態 reload 的 config 透過這個 method 來讀
			reloadConfig();
			
		});
	}

	/**
	 * 重新讀取一次 config -> 有需要更動的地方就呼叫對應的 method 進行更動
	 */
	public void reloadConfig() {
	}

	public static void main(String[] args) {
		new RtKafkaApp(AppDir.of(args)).startAndWait();
	}

}
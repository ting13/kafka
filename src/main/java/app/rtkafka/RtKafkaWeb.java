package app.rtkafka;

import cube.http.reqresp.ReqResp;
import cube.webber.IWebEnum;
import cube.webber.ReqUri;
import cube.webber.WebCache;
import lst.Lst;
import opt.Opt;
import resource.ResourceUtil;

public enum RtKafkaWeb implements IWebEnum<RtKafka, RtKafkaWeb> {

	root("首頁") {
		@Override
		public Object handle(RtKafka rtKafka, ReqResp rr) {
			rr.queryParam("msg");
			// ping
			// pong
			return  ResourceUtil.loadString("/res/index.html");
		}
	},
	
	notfound("not found", new ReqUri("/*")) {
		@Override
		public Object handle(RtKafka rtKafka, ReqResp rr) throws Exception {
			RtKafkaWeb w = longestMatch(RtKafkaWeb.class, rr);
			if (w == notfound) {
				return rr.redirect("");
			}
			return w.delegate(rtKafka, rr);
		}
	},

	;

	// --------------------------------------
	String m_sDesc;
	ReqUri m_uri;
	WebCache m_webCache = WebCache.No;

	// --------------------------------------
	RtKafkaWeb(String sDesc) {
		m_sDesc = sDesc;
	}

	RtKafkaWeb(String sDesc, ReqUri uri) {
		m_sDesc = sDesc;
		m_uri = uri;
	}

	RtKafkaWeb(String sDesc, WebCache webCache) {
		m_sDesc = sDesc;
		m_webCache = webCache;
	}

	RtKafkaWeb(String sDesc, ReqUri uri, WebCache webCache) {
		m_sDesc = sDesc;
		m_uri = uri;
		m_webCache = webCache;
	}
	// --------------------------------------

	@Override
	public boolean isCenter() {
		return false;
	}

	@Override
	public Opt<Lst<ReqUri>> uris() {
		return Opt.ofNullable(m_uri).map(uri -> Lst.of(uri));
	}

	@Override
	public WebCache shouldCache() { // 是否需要 cache。若是 true，會自動使用 Webber.set_noCache 設定為不 cache
		return m_webCache;
	}
	
}

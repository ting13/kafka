package app.rtkafka;

import java.text.SimpleDateFormat;

import cond.Skip;
import kafka.KafkaUtil;
import kafka.Producer;
import thread.ThreadUtil;
import tw.TwDecoder;
import tw.TwPort;
import util.intf.job.IJob3;
import util.intf.provider.IProv;
import valve.ValveDecoder;
import valve.decode.NtDecodeServer;
import valve.decode.TwDecode;
import valve.pkts.Pkt;

public class DupPurger2 {
	DupTwDecoder m_decoder = new DupTwDecoder(ValveDecoder::showDecodeTime);
	IProv<TwDecode> getDecodeProv() {
		return () -> new TwDecode(m_decoder);
	}
	
	// ------------------------------------------
	static class DupTwDecoder extends TwDecoder {
		Producer producer = new Producer();
		boolean newTransaction = true;
		DupTwDecoder(IJob3<Integer, Long, Long> onBeginIdleJob) {
			super(onBeginIdleJob);
		}
		// ------------------------------------------
		@Override
		protected Skip onNewPkt(String vvIP, String sClientId, long lReadTime, TwPort port, String sFormat, int nSeq, Pkt pkt) { // parseData 在 RtClients 的 thread 自己執行，不會影響 socket read
//			App.log("newPkt: " + vvIP + " " + sClientId + " " + port + " -> " + sFormat + ":" + nSeq);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String day = sdf.format(lReadTime);

			// 把producer 設定的BATCH_SIZE_CONFIG或LINGER_MS_CONFIG當作一次transaction
			// 如果producer還沒發送就不new 一個transaction
			
			KafkaUtil.produce(day+"-"+port.name(), port.port(), pkt.by().toBytes(), producer, true);
			if (producer.isDone()) {
				producer.producer.close();
				newTransaction = true;
				producer = new Producer();
System.out.println("new transsaction++++++++");
			// 這裡不produce 是因為 producer 設定的BATCH_SIZE_CONFIG或LINGER_MS_CONFIG
			} else {
				newTransaction = false;
//System.out.println("old transsaction--------");

			}

			return Skip.Yes;
		}

		/**
		 * 偵測到重複的 pkt
		 * @param vvIP
		 * @param sClientId
		 * @param lReadTime
		 * @param port
		 * @param pkt
		 * @return 是否跳掉這個 pkt。true 就不處理這個 pkt
		 */
		@Override
		protected Skip onDupPkt(String vvIP, String sClientId, long lReadTime, TwPort port, String sFormat, int nSeq, Pkt pkt) { // parseData 在 RtClients 的 thread 自己執行，不會影響 socket read
//			App.log("dupPkt: " + vvIP + " " + sClientId + " " + port + " -> " + sFormat + ":" + nSeq);
			
			return Skip.Yes;
		}

	}


	// ------------------------------------------

	public static void main(String... args) {
		DupPurger2 purger = new DupPurger2();
		NtDecodeServer s = new NtDecodeServer(7777, 0, purger.getDecodeProv());
		s.start();

		ThreadUtil.sleepForever();
	}
}

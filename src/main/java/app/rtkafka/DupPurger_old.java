package app.rtkafka;

import java.util.Set;
import java.util.TreeSet;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;

import bytes.ByStream;
import kafka.KafkaUtil;
import kafka.Producer;
import opt.Opt;
import tw.TwPort;
import valve.data.saver.UniObj;
import valve.decode.MDecodeFilter0_old;

public class DupPurger_old extends MDecodeFilter0_old {
	String m_sClientId = null;
	ByStream m_bs = new ByStream();
	Producer producer;
	boolean newTransaction = true;



	public DupPurger_old() {
	}

	@Override
	protected boolean isDup(String vvIP, int connId, long lReadTime, byte[] by) {
		Set<Integer> set = new TreeSet<>();
		m_bs.clear();
		m_bs.write(true, by);
//System.out.println(connId + "-> " + new String(by));		

		if (!readClientId_thrd()) // 先讀出 client id
			return false;

		while (true) {
			Opt<byte[]> opt = m_bs.readBlockBytes().map(bys -> bys.toBytes());
			if (opt.hasValue()) {
				byte[] byData = opt.get();

				UniObj<TwPort> uobj = UniObj.decode(TwPort.class, byData);	
				if(connId == 0) {
					//把producer 設定的BATCH_SIZE_CONFIG或LINGER_MS_CONFIG當作一次transaction
					if (newTransaction) {
						System.out.println("closed producer2222222222222222222222");
						producer = new Producer();
					}

					try {
						KafkaUtil.produce(uobj.m_port.toString(), 1, byData, producer, true);
						if(producer.isDone()) {
							System.out.println("time: " + uobj.m_lTime + ", port: " + uobj.m_port.toString()
							+ ", clientId: " + m_sClientId + ", connId: " + connId);
							System.out.println("is produce+++++++++++++++++++++");
							
							producer.producer.close();
							newTransaction = true;
						}else {
							newTransaction = false;
							//這裡不produce 是因為 producer 設定的BATCH_SIZE_CONFIG或LINGER_MS_CONFIG
							
							System.out.println("no produce");
						}

					}  catch (KafkaException e) {
					     // For all other exceptions, just abort the transaction and try again.
							producer.producer.close();

						 System.out.println("abort the transaction and try again");
					 }			
				}
			} else {
//				System.out.println("no value------------------------------");
				break;
			}
		}
		return false;
	}
	


	boolean readClientId_thrd() {
		if (m_sClientId == null) {
			Opt<String> opt = m_bs.readBlockString();
			
			if (opt.noValue()) {
				return false;
			}
			m_sClientId = opt.get();
			return true;
		}
		return true;
	}

	// 換成新的 DupPurge，因此移掉
//	// -------------------------------------------------------
//	public static void main(String... args) {
//		DupPurger_old purger = new DupPurger_old();
//		MNtDecodeServer_old s = new MNtDecodeServer_old(7777, 0, purger);
//		s.start();
//
//		ThreadUtil.sleepForever();
//	}
}

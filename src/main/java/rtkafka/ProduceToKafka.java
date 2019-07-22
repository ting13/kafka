package rtkafka;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import kafka.KafkaUtil;
import kafka.Producer;

public class ProduceToKafka{
	Producer pro;

	public ProduceToKafka() {
	}

	public void produce(List<RtPacket> temp) {
		System.out.println("-----kafka produce start-----");
		try {
			Iterator<RtPacket> it = temp.iterator();
			while (it.hasNext()) {
				RtPacket x = it.next();

//					System.out.println("生產到kafka, 交易所: " + x.getPort() + ", Msg: " + x.getMsg() + ", Time: "
//							+ x.getTime());
//				 KafkaUtil.produce(String.valueOf(x.getPort()), x.getPort(), x.getMsg(), pro,
//				 true);
			}

			// pro.producer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("-----kafka produce end-----");

	}

}

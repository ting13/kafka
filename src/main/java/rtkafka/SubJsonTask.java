package rtkafka;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import ind.tw.TwMajor;
import kafka.KafkaUtil;
import kafka.Producer;
import kafka.StringProducer;
import lst.Lst;
import netty.group.ClientGroup;
import redis.RClient;
import redis.RPubSub;
import thread.ThreadUtil;
import tw.quote.objs.TWSymbolQ;
import tw.record.value.RecValue;

public class SubJsonTask implements Runnable{
	TWQJson2Kafka twqJ;
	String sHost;
	int nPort;
	String sChannel;
	String thrdName;
	
	Producer producer;



	
	public SubJsonTask(TWQJson2Kafka twqJ, String sHost, int nPort, String sChannel, String thrdName) {
		this.twqJ = twqJ;
		this.sHost = sHost;
		this.nPort = nPort;
		this.sChannel = sChannel;
		this.thrdName = thrdName;
		producer = new StringProducer();
	}
	@Override
	public void run() {
		subscribe();
	}

	// 註冊所有的 Value Event
	public void subscribe() {
		RClient client = new RClient(ClientGroup.getDefault(), sHost, nPort, "clod", 0);
		RPubSub pubSub = client.getPubSub();

		String sChannelPattern = RecValue.TW_RV_PREFIX + sChannel;
		
		// 接上 Redis 來聽所有的 Value Object (因為是 t 開頭)
		pubSub.autoReconnect();
        
		//建一個sym對應產業的map
//        Map<String, String> twType = new HashMap<>();
//		
//        List<TwMajor> twMajorList = Arrays.asList(TwMajor.values());
//		Iterator<TwMajor> twMajorIt = twMajorList.iterator();	
//		while(twMajorIt.hasNext()) {
//			TwMajor next = twMajorIt.next();
//			Lst<String> syms = next.syms();
//			Iterator<String> symIt = syms.iterator();
//			while(symIt.hasNext()) {
//				String next2 = symIt.next();
//				twType.put(next2, next.name());
//			}
//		
//		}

		pubSub.setOnPMsg((channel, msg) -> {
			try {

				String s = new String(msg);


				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String day = sdf.format(System.currentTimeMillis());
				// 把producer 設定的BATCH_SIZE_CONFIG或LINGER_MS_CONFIG當作一次transaction
				// 如果producer還沒發送就不new 一個transaction
				
//				System.out.println("Text: "+s);
//				
//					TWSymbolQ twq = mapper.readValue(s, TWSymbolQ.class);
//					if(twType.containsKey(twq.bi.id)) {
//						System.out.println("+++++++++++++Í");
//					}

				
				KafkaUtil.produce(day+"-Json", 2, s, producer, true);


				ThreadUtil.sleep(1);
				if (producer.isDone()) {
					producer.producer.close();
					producer = new StringProducer();
//	System.out.println("new transsaction++++++++");
				// 這裡不produce 是因為 producer 設定的BATCH_SIZE_CONFIG或LINGER_MS_CONFIG
				} else {
//	System.out.println("old transsaction--------");
	
				}

			} catch (Exception e) {
//				e.printStackTrace();
			}
		});
		pubSub.psubscribe(sChannelPattern);
	}
}

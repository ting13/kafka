package kafka;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class KafkaUtil {
	
	private KafkaUtil() {
	}
	
	// value 是 ByteArray
	public static void produce(String topic, Integer key, byte[] msg,Producer producer,boolean isAsync) {
		producer.sendMsg(topic, key, msg , isAsync);
	}
	
	// value 是 String
	// value 是 ByteArray
	public static void produce(String topic, Integer key, String msg, Producer producer,boolean isAsync) {
		
		producer.sendMsg(topic, key, msg , isAsync);
	}
	
	
	//有指定producer時間
	public static void produce(String topic, Integer key, byte[] msg, Producer producer,boolean isAsync,long time) {
		producer.sendMsg(topic, key, msg , isAsync, time);
	}
	
	//有指定producer時間
	// value 是 String
	public static void produce(String topic, Integer key, String msg, Producer producer,boolean isAsync,long time) {
		producer.sendMsg(topic, key, msg , isAsync, time);
	}

	public static void consume(List<String> topic, String groupid, int consumer_num) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        //执行消费
        for (int i = 0; i < consumer_num; i++) {
            executor.execute(new Consumer(topic, groupid, "消费者" + (i + 1)));
        }   
	}




		
	
}

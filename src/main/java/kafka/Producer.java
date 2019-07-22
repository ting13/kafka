package kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class Producer {

	public KafkaProducer<Integer, byte[]> producer;
	boolean isDone = false;
	Properties properties;

	public Producer() {

		this.properties = new Properties();
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "pneko.prj.tw:9092");
		properties.put(ProducerConfig.CLIENT_ID_CONFIG, "MsgProducer");
		// properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");//
		// key 序列号方式
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.IntegerSerializer");// key 序列号方式
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.ByteArraySerializer");// value 序列号方式
		properties.put(ProducerConfig.ACKS_CONFIG, "all");

		/*
		 * 此設置使得之前的at least once成exactly once傳送 不再會導致重複消息。事務允许應用程序以原子方式將消息發送到多個分區（和主題！）
		 * 一旦這個屬性被設置，幂等也會自動開啟。然後使用事務API操作即可
		 */
		properties.put("enable.idempotence", true);
//		properties.put("transactional.id", "my-transactional-id");

//		使用默認16384Bytes
		properties.put(ProducerConfig.BATCH_SIZE_CONFIG, "30000");
//		默認為0,消息沒有到達batch.size,會延遲10ms再發送
		properties.put(ProducerConfig.LINGER_MS_CONFIG, "10");

		this.producer = new KafkaProducer<Integer, byte[]>(properties);

	}

	public void sendMsg(String topic, Integer key, byte[] msg, boolean isAsync) {

		if (isAsync) {// 異步
			producer.send(new ProducerRecord<Integer, byte[]>(topic, key, msg),
					new MsgProducerCallback(this, System.currentTimeMillis()));
		} else {// 同步
			producer.send(new ProducerRecord<Integer, byte[]>(topic, key, msg));
		}
	}

	public void sendMsg(String topic, Integer key, String msg, boolean isAsync) {
	}

	// 有指定producer時間
	public void sendMsg(String topic, Integer key, byte[] msg, boolean isAsync, long time) {
		if (isAsync) {// 異步
			producer.send(new ProducerRecord<Integer, byte[]>(topic, null, time, key, msg),
					new MsgProducerCallback(this, System.currentTimeMillis()));
		} else {// 同步
			producer.send(new ProducerRecord<Integer, byte[]>(topic, null, time, key, msg));
		}

	}

	public void sendMsg(String topic, Integer key, String msg, boolean isAsync, long time) {

	}

	public boolean isDone() {
		return this.isDone;
	}

}
	/**
	 * 消息發送後的回調函數
	 */
	class MsgProducerCallback implements Callback {
		private Producer producer;

		public MsgProducerCallback(Producer producer, long startTime) {
			this.producer = producer;

		}
		
		//消息發送後的處理邏輯
		@Override
		public void onCompletion(RecordMetadata recordMetadata, Exception e) {
			producer.isDone = true;
			if (recordMetadata != null) {				
//				System.out.println("消息的key->"+key+" ,消息內容->"+msg +" ,被傳送到的partition---> " + recordMetadata.partition() );
				
			}
			if (e != null) {
				e.printStackTrace();
			}
		}
		
	}
	


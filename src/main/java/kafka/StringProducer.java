package kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

public class StringProducer extends Producer {
	
	public  KafkaProducer<Integer, String> producer;	
	
	public StringProducer() {
		this.properties = new Properties();
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "pneko.prj.tw:9092");
		properties.put(ProducerConfig.CLIENT_ID_CONFIG, "MsgProducer");
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.IntegerSerializer");// key 序列号方式
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");// value 序列号方式
		properties.put(ProducerConfig.ACKS_CONFIG, "all");
		properties.put("enable.idempotence",true);
		properties.put(ProducerConfig.BATCH_SIZE_CONFIG, "20000");
		properties.put(ProducerConfig.LINGER_MS_CONFIG, "100");
		properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG,"524288");
	
		properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "20000");
		
		this.producer = new KafkaProducer<Integer, String>(properties);
	}
	@Override
	public void sendMsg(String topic, Integer key, String msg ,boolean isAsync) {
		
		if (isAsync) {// 異步
			producer.send(new ProducerRecord<Integer, String>(topic, key, msg),
					new MsgProducerCallback(this, System.currentTimeMillis()));
		} else {// 同步
			producer.send(new ProducerRecord<Integer, String>(topic, key, msg));
		}
	}
	
	//有指定producer時間
	@Override
	public void sendMsg(String topic, Integer key, String msg ,boolean isAsync,long time) {
		if (isAsync) {// 異步
			producer.send(new ProducerRecord<Integer, String>(topic,null,time, key, msg),
					new MsgProducerCallback(this, System.currentTimeMillis()));
		} else {// 同步
			producer.send(new ProducerRecord<Integer, String>(topic, null,time, key, msg));			
		}

	}
}

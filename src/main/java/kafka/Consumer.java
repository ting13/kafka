package kafka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.boon.di.In;

import app.twquotecore.ETWValueProc;
import bytes.ByteUtil;
import kafka.consumer.ConsumerConfig;
import tw.TwPort;
import tw.record.value.RecValue;
import valve.data.saver.UniObj;
import valve.redis.RVRedis;

public class Consumer implements Runnable {
	protected KafkaConsumer<Integer, byte[]> consumer;
	protected Properties props;
	protected List<String> topic;
	protected String groupid;
	// consumer name
	protected String name;
	private int count = 0;


	public Consumer(List<String> topic, String groupid, String name) {
		this.props = new Properties();
		this.topic = topic;
		this.groupid = groupid;
		this.name = name;
		// 定义kafka 服务的地址
		props.put("bootstrap.servers", "pneko.prj.tw:9092");
		// 制定consumer group
		props.put("group.id", groupid);
		// 是否自动确认offset
		// consumer消費一定量的消息後，會自動向zookeeper提交offset信息
		props.put("enable.auto.commit", "false");
		// key的序列化类
		props.put("key.deserializer", "org.apache.kafka.common.serialization.IntegerDeserializer");
		// value的序列化类
		props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
		props.setProperty("auto.offset.reset", "earliest");

		// 定义consumer
		this.consumer = new KafkaConsumer<>(props);
	}

	@Override
	public void run() {
		// 手動提交offset
		consumer.subscribe(topic);
		List<ConsumerRecord<Integer, byte[]>> buffer = new ArrayList<>();
		Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

		try {
			// 批量提交数量
			while (true) {
				// 读取数据，读取超时时间为100ms
				ConsumerRecords<Integer, byte[]> records = consumer.poll(300);
				for (ConsumerRecord<Integer, byte[]> record : records) {
					UniObj<TwPort> uobj = UniObj.decode(TwPort.class, record.value());	
					System.out.println("consume到的UniObj--->port: "+uobj.m_port+" ，time： "+uobj.m_lTime+" ，by: "+uobj.m_by);
					inc();
					currentOffsets.put(new TopicPartition(record.topic(), record.partition()),
							new OffsetAndMetadata(record.offset() + 1, "no metadata"));
					consumer.commitSync(currentOffsets);
//					buffer.add(record);
					
				}
//				checkbufferSize(buffer, currentOffsets);
			}

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			// 提交最後buffer內的數據
//			checkbufferSize(buffer,currentOffsets);
			consumer.close();
		}
	}
	void inc() {
		count++;
		if(count>10000) {
			System.out.println("consumer ----->"+count);
		}
	}
	
	public void checkbufferSize(List<ConsumerRecord<Integer, byte[]>> buffer, Map<TopicPartition, OffsetAndMetadata> currentOffsets) {
		if (!buffer.isEmpty()) {
			consumer.commitSync(currentOffsets);
			System.out.println("提交完畢");
			buffer.clear();
		}
	}
	
	public void checkbufferSize(List<ConsumerRecord<Integer, byte[]>> buffer) {
		if (!buffer.isEmpty()) {
			consumer.commitSync();
			System.out.println("提交完畢");
			buffer.clear();
		}
	}

}

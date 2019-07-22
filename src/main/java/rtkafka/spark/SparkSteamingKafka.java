package rtkafka.spark;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function3;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.StateSpec;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaMapWithStateDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import com.fasterxml.jackson.databind.ObjectMapper;

import ind.tw.TwMajor;
import lst.Lst;
import scala.Tuple2;
import tw.quote.objs.TWSymbolQ;

public class SparkSteamingKafka {
    public static void main(String[] args) throws InterruptedException {
        String checkpointDirectory = "./checkDir";

    	ObjectMapper mapper = new ObjectMapper();
        String brokers = "pneko.prj.tw:9092";
        String topics = "2018-12-17-Json";
       
        SparkConf sparkConf = new SparkConf()
        		.setMaster("local[2]")
        		.set("spark.io.compression.codec", "snappy")
        		.setAppName("SparkTest");

//        SparkSession spark = SparkSession.builder()
//        	    .config(sparkConf)
//        	    .getOrCreate();
       
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        
        JavaStreamingContext ssc = new JavaStreamingContext(sc, Durations.seconds(3));
        ssc.checkpoint(checkpointDirectory);
        ssc.sparkContext().setLogLevel("ERROR");
        
        Collection<String> topicsSet = new HashSet<>(Arrays.asList(topics.split(",")));
        //kafka相關參數
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", brokers);
        kafkaParams.put("group.id", "group1");
        kafkaParams.put("key.deserializer", "org.apache.kafka.common.serialization.IntegerDeserializer");
        kafkaParams.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        //Topic分區
//        Map<TopicPartition, Long> offsets = new HashMap<>();
//        offsets.put(new TopicPartition("2018-12-17-Json", 0), 3600000L); 
       
        //建一個sym對應產業的map
        Map<String, String> twType = new HashMap<>();
		List<TwMajor> twMajorList = Arrays.asList(TwMajor.values());
		Iterator<TwMajor> twMajorIt = twMajorList.iterator();	
		while(twMajorIt.hasNext()) {
			TwMajor next = twMajorIt.next();
			Lst<String> syms = next.syms();
			Iterator<String> symIt = syms.iterator();
			while(symIt.hasNext()) {
				String next2 = symIt.next();
				twType.put(next2, next.name());
			}
		}
		
        //通過KafkaUtils.createDirectStream(...)獲得kafka數據，kafka相關參數由kafkaParams指定
		JavaInputDStream<ConsumerRecord<Object, Object>> ds = KafkaUtils.createDirectStream(
				ssc,
				LocationStrategies.PreferConsistent(), 
				ConsumerStrategies.Subscribe(topicsSet, 
				kafkaParams));
 

        //可以打印數據，看下ConsumerRecord的结構
//      ds.foreachRDD(rdd -> {
//          rdd.foreach(x -> {
//            System.out.println(x);
//          });
//        });
		
		

		JavaPairDStream<String,String> pd =
				ds.filter(x->{		
					try {
						TWSymbolQ twq = mapper.readValue((String) x.value(), TWSymbolQ.class);
							//股號有在TwMajor.java裡的才取
							if(twType.containsKey(twq.bi.id)) {
								return true;
							}
						return false;
						
					} catch (Exception e) {
						return false;
					}})
				
				.mapToPair(new PairFunction<ConsumerRecord<Object, Object>, String, String>() {

		            private static final long serialVersionUID = 1L;

					@Override
					public Tuple2<String, String> call(ConsumerRecord<Object, Object> t) throws Exception {
						TWSymbolQ twq = mapper.readValue((String) t.value(), TWSymbolQ.class);
						String type = twType.get(twq.bi.id);
						
						return new Tuple2<String, String>(twq.bi.id, type+" "+twq.q.p+" "+twq.q.tv+" "+twq.q.udp);
					}
		        });
		
		//根據key更新數據
		Function3<String, Optional<String>, State<String>, Tuple2<String, String>> mappingFunc = (word, one, state) -> {
			String value = one.orElse("");
			Tuple2<String, String> output = new Tuple2<>(word, value);
			state.update(value);
			return output;
		};
		
		//當前狀態
//		pd.print(100);
	
		JavaMapWithStateDStream<String, String, String, Tuple2<String, String>> mapWithState = pd.mapWithState(StateSpec.function(mappingFunc));
			
		//全局狀態
		mapWithState.stateSnapshots().foreachRDD((rdd, time) -> {
			SparkSession spark = SparkSession.builder().config(rdd.context().getConf()).getOrCreate();
				
			  JavaRDD<JavaRow> rowRDD = rdd.map(word -> {
			    JavaRow record = new JavaRow();
			    String[] strE = word._2().split(" ");
			    record.setSym(word._1());
			    record.setType(strE[0]);;
			    record.setPrice(Double.parseDouble(strE[1]));;
			    record.setVol(Double.parseDouble(strE[2]));
			    record.setUdp(Double.parseDouble(strE[3]));

			    return record;
			  });
			
			  Dataset<Row> wordsDataFrame = spark.createDataFrame(rowRDD, JavaRow.class);

			  // Creates a temporary view using the DataFrame
			  wordsDataFrame.createOrReplaceTempView("frame");

			  // using SQL and print it
			  Dataset<Row> dataFrame =
			    spark.sql("select  type,"
//			    		+ " sum(price) as totalPrice,"
			    		+ " sum(udp) as totalUdp,"
			    		+ " count(*) as typeVol,"
			    		+ " sum(udp) / count(*) as avgUdp"
//			    		+ " sum(vol) as totalVol,"
//			    		+ " sum(price) * sum(vol) as totalPV"
//			    		+ " sum(price) * sum(vol) / sum(vol) as testPrice"
			    		+ " from frame group by type order by avgUdp desc");
			  
			  dataFrame.show(100);
			  
			  //打成json看看
//			dataFrame.toJSON().foreach(x -> {
//				System.out.println(x);
//			});

		});
//		mapWithState.stateSnapshots().count().print();
		
		ssc.start();
		ssc.awaitTermination();
//		ssc.close();
	}
}
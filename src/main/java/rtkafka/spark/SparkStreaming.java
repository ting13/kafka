package rtkafka.spark;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import com.fasterxml.jackson.databind.ObjectMapper;

import tw.quote.objs.TWSymbolQ;

public class SparkStreaming {
	public static void main(String[] args) {
		ObjectMapper mapper = new ObjectMapper();

        SparkConf sparkConf = new SparkConf().setMaster("local[2]").set("spark.io.compression.codec", "snappy").setAppName("SparkTest");
        SparkSession sparkSession = SparkSession.builder().config(sparkConf).getOrCreate();
//        List<StructField> structFields = new ArrayList<StructField>();
//        // 构建表结构
//        structFields.add(DataTypes.createStructField("sym", DataTypes.StringType, true));
//        structFields.add(DataTypes.createStructField("price", DataTypes.DoubleType, true));
//        structFields.add(DataTypes.createStructField("vol", DataTypes.DoubleType, true));		
//		StructType schema = DataTypes.createStructType(structFields);
		
        Dataset<Row> lines = sparkSession
        					.readStream()
        					.format("kafka")
        					.option("kafka.bootstrap.servers", "pneko.prj.tw:9092")
        					.option("subscribe", "2018-12-13-Json")
//        					.schema(schema)
        					.load();
        lines.col("value").cast(DataTypes.StringType);
        
        lines.as(Encoders.STRING()).map(
        		(MapFunction<String, String>) x -> 
        		{
        		TWSymbolQ twq = mapper.readValue(x, TWSymbolQ.class);
				return twq.bi.id+" "+twq.q.p+" "+twq.q.v;
				}

        		, Encoders.STRING());
        
        lines.show();
	
	}
}

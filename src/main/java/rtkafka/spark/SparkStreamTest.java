package rtkafka.spark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

public class SparkStreamTest {

    private static org.slf4j.Logger LOG;

    private static StructType structType;

    public SparkStreamTest() {
        LOG = LoggerFactory.getLogger(SparkStreamTest.class);

    }

    public static void main(String[] args) throws Exception {
        //初始化Spark SQL环境
        final SparkSession spark = SparkSession.builder().appName("SparkSession").enableHiveSupport().getOrCreate();
        List<StructField> structFields = new ArrayList<StructField>();

        // 构建表结构
        structFields.add(DataTypes.createStructField("sym", DataTypes.StringType, true));
        structFields.add(DataTypes.createStructField("price", DataTypes.DoubleType, true));
        structFields.add(DataTypes.createStructField("vol", DataTypes.DoubleType, true));

        //构建StructType，用于最后DataFrame元数据的描述
        structType = DataTypes.createStructType(structFields);
        queryUser(spark,args[0]);
    }

    private static void queryUser(SparkSession spark,final String mod) throws Exception {
        try {

            // 整个方法的为查出用户，调用外部接口查询用户信息，保存到一个新的hive表中，单条写入，或者批量写入性能较差，  这里的解决方案为内存中的临时表
            // 查询用户信息，为基础数据
            spark.sql("set mapred.output.compress=true");
            spark.sql("set hive.default.fileformat=Orc");
            Dataset<Row> data = spark.sql("SELECT count(*) price,vol from gdm.tabel_name ");
            List<Row> lst = data.collectAsList();

            // START 从这开始为处理数据过程，根据自己的业务来
            // 开20个线程调用金融接口 TP99 16ms
            final List<String> userInfoList = new ArrayList<>();
            ExecutorService es = Executors.newFixedThreadPool(10);
            final CountDownLatch cdl = new CountDownLatch(lst.size());
            for (final Row row : lst){
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (Long.parseLong(row.getString(1)) % 10 == Long.parseLong(mod)) {
                                // 处理数据，具体方法删掉了 以免暴露接口
//                                userInfoList.add(queryUserInfo(row.getString(0)));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            cdl.countDown();
                        }
                    }
                });
            }
            cdl.await();
            // end  到这结束

            // 构造数据及创建临时表操作
            List<Row> rowList = new ArrayList<>();
            for (String userInfo : userInfoList) {
                if (userInfo == null){
                    continue;
                }
                JSONObject jb = new JSONObject(userInfo);
                if ((Boolean)jb.get("success")){
                    // 字符串转json对象
                    JSONObject jsonObject = ((JSONObject)new JSONArray(jb.get("a").toString()).get(0));
                    // 把json对象转为 List<Row>  还多好多泛型也可以用，根据具体的数据来看用哪种比较方便
                    rowList.add(buildRow(jsonObject));
                }
            }
            // 这步创建临时表，并写入新的表中
            newInsertData(rowList, spark);
        
        } catch (Exception e) {
            throw e;
        }
    }

    private static void newInsertData(List<Row> rowList, SparkSession spark){
        // 创建表结构 Dataset<Row>  rowList 为数据，structType为最上面定义的表结构abcd那一堆
        Dataset<Row> dataset = spark.createDataFrame(rowList, structType);
        dataset = dataset.distinct();
        // createOrReplaceTempView 创建临时表，如果表存在了就覆盖掉，参数为表名，这步操作是在内存中进行的，根据内存大小调整每次保存的数量
        dataset.createOrReplaceTempView("linshibiao");
        spark.sql("set mapred.output.compress=true");
        spark.sql("set hive.default.fileformat=Orc");
        // sql 直接从临时表写到新表中，这种操作比 insert into values 这种快上N倍
        spark.sql("insert into dev.新表 select * from 临时表");
    }

    private static Row buildRow(JSONObject data){
        Row row = RowFactory.create(
        		data.getString("a"),
                data.getString("b"),
                data.getString("c"));
        return row;
    }
} 
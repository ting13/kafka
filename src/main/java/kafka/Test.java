package kafka;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import above.Above;
import above.db.AbDbRows;
import dbmgr.EDb;
import ind.tw.TwDetail;
import ind.tw.TwMajor;
import ind.tw.TwMicro;
import ind.tw.TwMinor;
import lst.Lst;
import rdb.CacheTime;
import rdb.Preps;
import rdb.intfs.ISql;
import twdb.entity.TdccEquity;

public class Test {
//	static ArrayDeque<byte[]> stack = new ArrayDeque<>();

	public static void main(String[] args) throws Exception {
//		TwMajor.all().forEach(x -> 
//					System.out.println(x.syms()));
		
//		List<TwMajor> list = Arrays.asList(TwMajor.values());
//		Iterator<TwMajor> it = list.iterator();
//		while(it.hasNext()) {
//			TwMajor next = it.next();
//			System.out.println(next.name());
//			Lst<String> syms = next.syms();
//			System.out.println(syms.hasExact("1233"));	//true		
//		}
//		System.out.println(TwMajor.valueOf("1262"));
		

//		KafkaUtil.produce();

		
//		List<String> topics = Arrays.asList("Tse");
//		KafkaUtil.consume(topics, "Tse_group", 1);
//		KafkaUtil.consume("7001", "test_rtkafka", 1);
//        Map<String, String> twType = new HashMap<>();
        
//		List<TwMajor> twMajorList = Arrays.asList(TwMajor.values());
//		System.out.println(TwMajor.電子.syms().size());
//		List<TwMinor> twMinorList = Arrays.asList(TwMinor.values());
//		List<TwDetail> twDetailList = Arrays.asList(TwDetail.values());
//		List<TwMicro> twMicroList = Arrays.asList(TwMicro.values());
//		
//		Iterator<TwMajor> twMajorIt = twMajorList.iterator();
//		Iterator<TwMinor> twMinorIt = twMinorList.iterator();
//		Iterator<TwDetail> twDetailIt = twDetailList.iterator();
//		Iterator<TwMicro> twMicroIt = twMicroList.iterator();
//
//		Iterator<String> symIt;
//		
//		while(twMajorIt.hasNext()) {
//			TwMajor next = twMajorIt.next();
//			Lst<String> syms = next.syms();
//			symIt = syms.iterator();
//			while(symIt.hasNext()) {
//				String next2 = symIt.next();
//				twType.put(next2, next.name());
//			}
//		}
//		
//		while(twMinorIt.hasNext()) {
//			TwMinor next = twMinorIt.next();
//			Lst<String> syms = next.syms();
//			symIt = syms.iterator();
//			while(symIt.hasNext()) {
//				String next2 = symIt.next();
//				twType.put(next2, next.name());
//			}
//		}
//		
//		while(twDetailIt.hasNext()) {
//			TwDetail next = twDetailIt.next();
//			Lst<String> syms = next.syms();
//			symIt = syms.iterator();
//			while(symIt.hasNext()) {
//				String next2 = symIt.next();
//				twType.put(next2, next.name());
//			}
//		}
//		
//		while(twMicroIt.hasNext()) {
//			TwMicro next = twMicroIt.next();
//			Lst<String> syms = next.syms();
//			symIt = syms.iterator();
//			while(symIt.hasNext()) {
//				String next2 = symIt.next();
//				twType.put(next2, next.name());
//			}
//		}
//		
//		System.out.println(twType.entrySet());
//		System.out.println(twType.get(""));
		
//		List<TwMajor> twMajorList = Arrays.asList(TwMajor.values());
//		List<TwMinor> twMinorList = Arrays.asList(TwMinor.values());
//		List<TwDetail> twDetailList = Arrays.asList(TwDetail.values());
//		List<TwMicro> twMicroList = Arrays.asList(TwMicro.values());
//
//		System.out.println(twMajorList.size());
//		System.out.println(twMinorList.size());
//		System.out.println(twDetailList.size());
//		System.out.println(twMicroList.size());
//		String str= "str ddd aaa bbb";
//		String[] split = str.split(" ");
//		System.out.println(split[0]);
		
		
		
		
		Lst<TdccEquity> lstRet = Lst.of();
		
		String sSql = "select Date,Symbol,cdesc from module1 where Date(Date)='2018/12/18' AND Symbol='2330'";

		AbDbRows rows;
		int colNum = 0;
		try {
			rows = Above.dbmgr().select(EDb.tw, ISql.of(sSql), CacheTime.ofHour(12));
			 colNum = rows.colNum();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println(colNum);

	}


}
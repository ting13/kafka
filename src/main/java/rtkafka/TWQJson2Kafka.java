package rtkafka;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TWQJson2Kafka{
	public static void main(String[] args) {
		TWQJson2Kafka twqJ = new TWQJson2Kafka();
		
		ExecutorService exec = Executors.newFixedThreadPool(1);
//		exec.execute(new SubJsonTask(twqJ, "pneko.prj.tw",3502,"*", "thrd-neko"));
//		exec.execute(new SubJsonTask(twqJ, "pway.prj.tw",3512,"*", "thrd-way"));
		exec.execute(new SubJsonTask(twqJ, "pneo.prj.tw",3512,"*", "thrd-neo"));
//		exec.execute(new SubJsonTask(twqJ, "pknuc.prj.tw",3512,"*", "thrd-knuc"));
	}
}

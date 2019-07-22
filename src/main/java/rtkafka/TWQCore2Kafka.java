package rtkafka;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import bytes.ByteUtil;
import cmd.thread.Daemon;
import cmd.thread.LambdaThread;
import tw.record.value.tse.TseOtcTickValue;
import valve.redis.RVRedis;

public class TWQCore2Kafka {
	volatile int countQue;
	volatile int countSet;
	
	public  LinkedBlockingQueue<byte[]> m_que;
	public  ConcurrentSkipListSet<RtPacket> m_set;
	public LinkedBlockingQueue<RtPacket> m_proQue;
	public LambdaThread m_ProThrd;
	public ProduceToKafka produce;

	
	

	public TWQCore2Kafka() {
		m_que = new LinkedBlockingQueue<>();
		m_set = new ConcurrentSkipListSet<>();
		m_proQue = new LinkedBlockingQueue<>();
		m_ProThrd = new LambdaThread("kafka produce", Daemon.no);
		produce = new ProduceToKafka();
	}

	public static void main(String[] args) throws InterruptedException {
		TWQCore2Kafka twq = new TWQCore2Kafka();
		ExecutorService exec = Executors.newFixedThreadPool(4);
		exec.execute(new SubscribeTask(twq, "pway.prj.tw",3511,"*", "thrd-way"));
		exec.execute(new SubscribeTask(twq, "pneko.prj.tw",3501,"*", "thrd-neko"));
		exec.execute(new SubscribeTask(twq, "pneo.prj.tw",3511,"*", "thrd-neo"));
		exec.execute(new SubscribeTask(twq, "pknuc.prj.tw",3511,"*", "thrd-knuc"));
		
		ExecutorService exec2 = Executors.newFixedThreadPool(5);
		byte[] value;
		while(!((value=twq.m_que.take()) == null)) {
			exec2.execute(new HandleValueTask(twq, value));
		}
		
	}
	
	 void firstAddToSet(byte[] x) {
//		byte classCode = x[0];
//		// 先取得所屬的 Processor
//		ETWValueProc proc = ETWValueProc.fromCode(classCode);
//		// 解出 Value Object 本體
//		Class<? extends RecValue<?>> clz = proc.getClz();
		byte[] bufObj = ByteUtil.sub(x, 5);

		TseOtcTickValue value = (TseOtcTickValue) RVRedis.getPacker().unpack(TseOtcTickValue.class, bufObj);
		RtPacket rtp = new RtPacket(value);
		m_set.add(rtp);

	}
	
	void addtoSet(byte[] x) throws InterruptedException {
//		byte classCode = x[0];
//		// 先取得所屬的 Processor
//		ETWValueProc proc = ETWValueProc.fromCode(classCode);
//		// 解出 Value Object 本體
//		Class<? extends RecValue<?>> clz = proc.getClz();
		byte[] bufObj = ByteUtil.sub(x, 5);
		TseOtcTickValue value = (TseOtcTickValue) RVRedis.getPacker().unpack(TseOtcTickValue.class, bufObj);
	
		RtPacket rtp = new RtPacket(value);

		boolean add = m_set.add(rtp);
		if (add) {
			m_proQue.add(m_set.pollFirst());
		}
		chk();
	}
	
	//檢查m_proQue > 10000 就發送到kafka
	synchronized public void chk() throws InterruptedException {
		List<RtPacket> templist = new ArrayList<>();
			
		if (m_proQue.size() > 10000) {	
System.out.println(Thread.currentThread().getName());
			// 將m_proQue的msg丟到templist, 並清空
			m_proQue.drainTo(templist);
System.out.println(m_proQue.size());
			m_ProThrd.queueAct(() -> {
				try {
					produce.produce(templist);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

		}
	}
	
	public synchronized void inc() {
		countQue++;
		if (countQue > 10000) {
System.out.println("setOnPMsg執行超過10000次");
			countQue = 0;
		}		
	}
	
	public synchronized void incSet() {
		countSet++;
		if (countSet > 10000) {
System.out.println("addToSet執行超過10000次");
			countSet = 0;
		}
	}

}


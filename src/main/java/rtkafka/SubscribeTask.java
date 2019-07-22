package rtkafka;

import java.util.HashSet;

import org.apache.poi.util.ArrayUtil;

import app.twquotecore.ETWValueProc;
import bytes.ByteUtil;
import netty.group.ClientGroup;
import redis.RClient;
import redis.RPubSub;
import tw.record.value.RecValue;
import tw.record.value.tse.TseOtcTickValue;
import valve.redis.RVRedis;

public class SubscribeTask implements Runnable{
	
	TWQCore2Kafka twq;
	String sHost;
	int nPort;
	String sChannel;
	String thrdName;
	
	public SubscribeTask(TWQCore2Kafka twq, String sHost, int nPort, String sChannel, String thrdName) {
		this.twq = twq;
		this.sHost = sHost;
		this.nPort = nPort;
		this.sChannel = sChannel;
		this.thrdName = thrdName;
	}

	@Override
	public void run() {
		subscribe();
	}

	// 註冊所有的 Value Event
	public void subscribe() {
		RClient client = new RClient(ClientGroup.getDefault(), sHost, nPort, "clod", 0);
		RPubSub pubSub = client.getPubSub();

		String sChannelPattern = RecValue.TW_RV_PREFIX + sChannel;
		// 接上 Redis 來聽所有的 Value Object (因為是 t 開頭)
		pubSub.autoReconnect();
		pubSub.setOnPMsg((channel, msg) -> {
			try {
				byte classCode = msg[0];
				// 先取得所屬的 Processor
				ETWValueProc proc = ETWValueProc.fromCode(classCode);
				// 解出 Value Object 本體
				Class<? extends RecValue<?>> clz = proc.getClz();
		
				//只取TseOtcTickValue.class
				if(clz.getSimpleName().equals("TseOtcTickValue")) {
					byte[] bufObj = ByteUtil.sub(msg, 5);
					TseOtcTickValue value = (TseOtcTickValue) RVRedis.getPacker().unpack(TseOtcTickValue.class, bufObj);
//System.out.println("value: "+value.tickTime+", thread name: "+thrdName);
					addToQue(msg);
					twq.inc();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		pubSub.psubscribe(sChannelPattern);
	}
	public void addToQue(byte[] msg) throws InterruptedException {
		twq.m_que.offer(msg);
	}
	

}

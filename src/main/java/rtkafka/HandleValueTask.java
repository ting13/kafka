package rtkafka;

public class HandleValueTask implements Runnable {
	public TWQCore2Kafka twq;
	public byte[] value;



	public HandleValueTask(TWQCore2Kafka twq, byte[] value) {
		this.twq = twq;
		this.value = value;
	}

	@Override
	public void run() {
		try {
			if ((twq.m_set.size() > 10000)) {
				twq.addtoSet(value);
			} else {
				//這裡會先執行
				twq.firstAddToSet(value);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//計算add了幾次
		twq.incSet();
	}
	
	public void setValue(byte[] value) {
		this.value = value;
	}


}

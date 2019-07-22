package rtkafka;

import tw.record.value.tse.TseOtcTickValue;

public class RtPacket implements Comparable<Object>{

	TseOtcTickValue value;
	
	public RtPacket() {
	}
	
	public RtPacket(TseOtcTickValue value) {
		this.value = value;
		
	}
	
	@Override
	public String toString() {
		return this.value.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof RtPacket))
			return false;
		RtPacket rtp = (RtPacket) obj;
		boolean ret = this.value.tickTime == rtp.value.tickTime;
//if(ret) {
//System.out.println("this="+this.value);
//}
		return ret;
	}

    @Override 
    public int hashCode() {
    	return (int)value.tickTime;
    }
    

	@Override
	public int compareTo(Object o) {
		if (!(o instanceof RtPacket))
			throw new RuntimeException();
		RtPacket p = (RtPacket) o;
		// 如果tickTime相同 表示兩個對象equals
		if ((this.value.tickTime == p.value.tickTime) ) {
			return 0;
		// 不相同就按照tickTime 排序
		} else {
			return (int) (this.value.tickTime - p.value.tickTime);
		}
	}

}

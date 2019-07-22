package rtkafka;

import java.util.LinkedHashSet;

import auto.AutoHash;

public class A1 {
	static public void main(String...args) {
		LinkedHashSet<RtPkt> s1 = new LinkedHashSet<>();
		

		D d =new D(1);
		D d1 =new D(1);
		D d2 =new D(2);
		
		s1.add(new RtPkt(d));
System.out.println("a1");		
		s1.add(new RtPkt(d1));
System.out.println("a1");		
		s1.add(new RtPkt(d2));
System.out.println("a1");		
		System.out.println(s1);
	}
	static class RtPkt {
		D m_d;
		RtPkt(D d) {
			this.m_d = d;
		}
		public boolean equals(Object obj) {
			boolean ret = m_d.equals(((RtPkt)obj).m_d);
			//System.out.println("ret="+ret);
			return ret;
		}
		public int hashCode() {
//System.out.println("ppp="+this);			
			return m_d.m_n;
		}
	}
	
	static class D extends C {
		
		D(int n) {
			super(n);
		}

		@Override
		public boolean equals(Object obj) {
			boolean ret = AutoHash.isEqual_onAllFields(this.m_n, ((D)obj).m_n);
			
			System.out.println("ret="+ret);
			return ret;
		}
	}
	
	static class C {
		int m_n;
		C(int n) {
			m_n = n;
		}
	}
	

}

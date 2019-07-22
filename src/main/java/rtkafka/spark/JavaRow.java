package rtkafka.spark;

public class JavaRow implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	private String sym;
	private String type;
	
	
	private double price;
	private double vol;
	private double udp;

	public String getSym() {
		return sym;
	}
	public void setSym(String sym) {
		this.sym = sym;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getVol() {
		return vol;
	}
	public void setVol(double vol) {
		this.vol = vol;
	}
	public double getUdp() {
		return udp;
	}
	public void setUdp(double udp) {
		this.udp = udp;
	}
	



}
import java.util.List;

public class Message implements java.io.Serializable {
	MessageType mt;
	String content;
	
	public Message(MessageType mt) {
		this.mt = mt;
	}
	
	public MessageType getMt() {
		return mt;
	}

	public void setMt(MessageType mt) {
		this.mt = mt;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int[] getDifferentialFromContent() {
		String[] a= content.split(",");
		int[] b = new int[3];
		b[0] = Integer.parseInt(a[0]);
		b[1] = Integer.parseInt(a[1]);
		b[2] = Integer.parseInt(a[2]);
		return b;
	}
	public void setContentFromDifferential(int[] a) {
		content = String.valueOf(a[0])+","+String.valueOf(a[1])+","+String.valueOf(a[2]);
	}
	


	public String[] getContentFromPrivateM(){
		String [] c = content.split(",");
		return c;
	}
	
	
}

enum MessageType{
	DIFFERENTIAL,MESSAGE,LOGIN,PRIVATEM
}
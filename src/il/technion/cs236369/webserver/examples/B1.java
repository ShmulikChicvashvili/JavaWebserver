package il.technion.cs236369.webserver.examples;

public class B1 implements B {

	private String word;
	
	public B1(String word) {
		this.word = word;
	}
	
	@Override
	public void speak() {
		System.out.println("B1: "+word);
	}

}

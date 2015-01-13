package il.technion.cs236369.webserver.examples;

public class B2 implements B {

	private String word;
	
	public B2(String word) {
		this.word = word;
	}
	
	@Override
	public void speak() {
		System.out.println("B2: "+word);
	}

}

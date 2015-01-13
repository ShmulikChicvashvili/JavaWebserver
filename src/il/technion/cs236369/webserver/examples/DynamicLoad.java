package il.technion.cs236369.webserver.examples;

public class DynamicLoad {

	public static void main(String[] args) throws Exception {
		
		for (int i : new int[]{1, 2}) {
			B b = (B)Class.forName("il.technion.cs236369.webserver.examples.B"+i).getConstructor(String.class).newInstance("Hi !!");
			b.speak();
		}
		// note that we can recv B1 and B2 class names as a string argument
	}
	
}

package il.technion.cs236369.webserver;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class TestMain {
	public static void main(String[] args) throws SQLException, IOException {
		JUnitCore junit = new JUnitCore();
		Result result = junit.run(WebServerTestSuite.class);
		System.out.println("You "
				+ (result.wasSuccessful() ? "passed " : "didn't pass ")
				+ "the basic test " + (result.wasSuccessful() ? ":)" : ":("));
		System.exit(0);
	}
}

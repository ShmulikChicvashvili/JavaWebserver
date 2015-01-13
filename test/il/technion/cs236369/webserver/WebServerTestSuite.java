package il.technion.cs236369.webserver;
import il.technion.cs236369.webserver.basic.BasicTest;
import junit.framework.TestCase;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ BasicTest.class })
public class WebServerTestSuite extends TestCase {
}

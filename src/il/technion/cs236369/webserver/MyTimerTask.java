/**
 *
 */

package il.technion.cs236369.webserver;


import java.util.TimerTask;




/**
 * @author Shmulik
 *
 */
public class MyTimerTask extends TimerTask
{
	public MyTimerTask(Thread thread)
	{
		t = thread;
	}
	
	
	/* (non-Javadoc) @see java.util.TimerTask#run() */
	@Override
	public void run()
	{
		if (t != null && t.isAlive())
		{
			t.interrupt();
		}
	}
	
	
	
	Thread t;

}

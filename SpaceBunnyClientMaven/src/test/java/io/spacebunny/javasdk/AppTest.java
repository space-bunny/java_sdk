package io.spacebunny.javasdk;

import io.spacebunny.SpaceBunny;
import io.spacebunny.device.SBDevice;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    public void testApp()
    {
        //SpaceBunny.Client spaceBunny = new SpaceBunny.Client("not_real_device_key");
        //assertEquals("Device Key must be correct", );
        assertTrue(true);
    }
}

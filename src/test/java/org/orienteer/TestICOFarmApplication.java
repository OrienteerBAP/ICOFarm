package org.orienteer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.orienteer.junit.OrienteerTestRunner;
import org.orienteer.junit.OrienteerTester;

import static org.junit.Assert.assertTrue;


@RunWith(OrienteerTestRunner.class)
@Singleton
public class TestICOFarmApplication
{
	@Inject
	private OrienteerTester tester;
    
	@Test
	public void testWebApplicationClass()
	{
	    assertTrue(tester.getApplication() instanceof ICOFarmApplication);
	}
}

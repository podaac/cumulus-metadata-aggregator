package gov.nasa.cumulus.metadata.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
 
@RunWith(Suite.class)
@Suite.SuiteClasses({
  AggregatorReleaseRegressionTest.class,
  AggregatorRelease_4_3_0_Test.class,
  gov.nasa.cumulus.metadata.test.MetadataFilesToEchoTest.class,
  gov.nasa.cumulus.metadata.test.UMMTest.class,
  gov.nasa.cumulus.metadata.test.ImageProcessorTest.class,
  gov.nasa.cumulus.metadata.test.FootprintProcessorTest.class,

})
public class AggregatorTestSuite {
    // the class remains completely empty, 
    // being used only as a holder for the above annotations


	public static void printTestInfo(String desc, int ticketNumber){
		System.out.println("---------------------------------------------------------");
		System.out.println("Running test: " + ticketNumber);
		System.out.println(desc);
		System.out.println("---------------------------------------------------------");
	}
}


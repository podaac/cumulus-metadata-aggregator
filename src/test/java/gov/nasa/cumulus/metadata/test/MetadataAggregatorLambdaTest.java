package gov.nasa.cumulus.metadata.test;

import gov.nasa.cumulus.metadata.aggregator.MetadataAggregatorLambda;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetadataAggregatorLambdaTest {
    String cmrString = "";
    String cmaString = null;

    @Before
    public void initialize() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();

            File inputCMAJsonFile = new File(classLoader.getResource("cumulus_message_input_example.json").getFile());
            cmaString = new String(Files.readAllBytes(inputCMAJsonFile.toPath()));

        } catch (IOException ioe) {
            System.out.println("Test initialization failed: " + ioe);
            ioe.printStackTrace();
        }
    }

    @Test
    public void testGetConceptId() throws ParseException {
        MetadataAggregatorLambda lambda = new MetadataAggregatorLambda();
        String conceptId = lambda.getConceptId(this.cmaString);
        assertEquals(conceptId, "G1238611022-POCUMULUS");
    }

}

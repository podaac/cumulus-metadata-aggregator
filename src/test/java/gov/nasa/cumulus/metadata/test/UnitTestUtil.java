package gov.nasa.cumulus.metadata.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class UnitTestUtil {
    public static boolean compareFileWithGranuleJson(String filePath, JSONObject granuleJson) throws
            IOException, ParseException {
        ClassLoader classLoader = UnitTestUtil.class.getClassLoader();
        File preSavedJsonFile = new File(classLoader.getResource(filePath).getFile());
        String readInJsonStr = FileUtils.readFileToString(preSavedJsonFile, StandardCharsets.UTF_8);
        JSONParser parser = new JSONParser();
        ObjectMapper mapper = new ObjectMapper();
        JSONObject readInJsonObj = (JSONObject) parser.parse(readInJsonStr);
        // remove ProviderDates structure because it always has most current datetime
        // the ProviderDates saved in file is different than the provider dates generated on the fly
        granuleJson.remove("ProviderDates");
        readInJsonObj.remove("ProviderDates");
        assertEquals(mapper.readTree(readInJsonObj.toJSONString()), mapper.readTree(granuleJson.toJSONString()));
        return true;  // if reached this point, return true
    }
}

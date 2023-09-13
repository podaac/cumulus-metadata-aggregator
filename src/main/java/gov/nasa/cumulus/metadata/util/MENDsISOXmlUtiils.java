package gov.nasa.cumulus.metadata.util;

import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import cumulus_message_adapter.message_parser.AdapterLogger;
import gov.nasa.cumulus.metadata.aggregator.IsoMendsXPath;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

public class MENDsISOXmlUtiils {
    /**
     * extract a string from xml document. swallow exception if there is any.
     * If exception is swallowed, return empty string.
     * Another extractXPathValueThrowsException shall be implemented whenever needed, which
     * in another case should throw exception instead of swallow.
     * @return
     */
    /**
     *
     * @param doc
     * @param xpath
     * @param pathStr   : the xml path in string format
     * @param pathTagStr :the tag form of the xml path string.  Ex IsoMendsXPath.ADDITIONAL_ATTRIBUTES_BLOCK.
     *                   This is for logging and support purpose so the developer can quickly identify what field(s)
     *                   is causing problem
     * @return : extracted string.  Or the extractedString default is "" which is empty string. Hence,
     *          any exception would cause this function to return an empty string
     */
    public static String extractXPathValueSwallowException(Document doc, XPath xpath, String pathStr, String pathTagStr) {
        String extractedStr = ""; //default to empty string.
        try {
            extractedStr = xpath.evaluate(pathStr, doc);
        } catch (XPathExpressionException xPathExpressionException) {
            AdapterLogger.LogError("extractXPathValueSwallowException error while extracting: " + pathTagStr
                    + " path string value:"+ pathStr
                    + " Exception:" +xPathExpressionException);
        } catch (Exception genericException) {
            AdapterLogger.LogError("extractXPathValueSwallowException error while extracting: "+ pathTagStr
                    + " path string value:"+ pathStr
                    + " Exception:" +genericException);
        }
        return extractedStr;
    }

    /**
     * extract a string from xml document. throws exception if there is any.
     * @param doc
     * @param xpath
     * @param pathStr
     * @param pathTagStr
     * @return
     * @throws Exception
     */
    public static String extractXPathValueThrowsException(Document doc, XPath xpath, String pathStr, String pathTagStr)
    throws Exception{
        String extractedStr = "";
        try {
            extractedStr = xpath.evaluate(pathStr, doc);
        } catch (XPathExpressionException xPathExpressionException) {
            AdapterLogger.LogError("extractXPathValueSwallowException error while extracting: " + pathTagStr
                    + " path string value:"+ pathStr
                    + " Exception:" +xPathExpressionException);
            throw xPathExpressionException;
        } catch (Exception genericException) {
            AdapterLogger.LogError("extractXPathValueSwallowException error while extracting: "+ pathTagStr
                    + " path string value:"+ pathStr
                    + " Exception:" +genericException);
            throw genericException;
        }
        return extractedStr;
    }

}

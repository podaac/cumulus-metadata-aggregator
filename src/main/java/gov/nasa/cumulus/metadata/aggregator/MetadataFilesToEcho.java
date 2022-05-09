package gov.nasa.cumulus.metadata.aggregator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import gov.nasa.podaac.inventory.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import gov.nasa.podaac.inventory.api.Constant.GranuleArchiveStatus;
import gov.nasa.podaac.inventory.api.Constant.GranuleArchiveType;
import gov.nasa.cumulus.metadata.umm.model.UMMGranuleArchive;
import org.xml.sax.SAXException;
import cumulus_message_adapter.message_parser.AdapterLogger;

enum Iso {
    MENDS,
    SMAP
}

public class MetadataFilesToEcho {
	String className = this.getClass().getName();
	private UMMGranule granule;
	Dataset dataset = new Dataset();
	boolean forceBB = false;
	boolean rangeIs360 = false;
    boolean isIsoFile = false;

	public MetadataFilesToEcho() {
		this(false);
	}

	public MetadataFilesToEcho(boolean isIso) {
        this.isIsoFile = isIso;
		if (isIsoFile)
			this.granule = new IsoGranule();
		else
			this.granule = new UMMGranule();
	}

	//this method reads the configuration file (per dataset) sent to this class (.cfg)
	public void readConfiguration(String file) throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		JSONObject metadata = (JSONObject) parser.parse(new FileReader(file));

		String shortName = (String)metadata.get("collection");
		setDatasetValues(shortName, metadata.get("version").toString(), (Boolean) metadata.get("rangeIs360"), null);
	}

	/**
	 * Sets shortName, version, rangeIs360 and north,east,south,west coordinates.
	 *
	 * @param shortName   dataset short name
	 * @param version     dataset version
	 * @param rangeIs360  true if dataset granule coordinates are 0 to 360, null/false if not
	 * @param boundingBox JSONObject with latMax, lonMax, latMin, lonMin
	 */
	public void setDatasetValues(String shortName, String version, Boolean rangeIs360, JSONObject boundingBox) {
		dataset.setShortName(shortName);

		DatasetCitation citation = new DatasetCitation();
		citation.setVersion(version);
		HashSet<DatasetCitation> citationSet = new HashSet<DatasetCitation>();
		citationSet.add(citation);
		dataset.setCitationSet(citationSet);

		if (rangeIs360 != null) {
			this.rangeIs360 = rangeIs360;
		}

		if (boundingBox != null) {
			setGranuleBoundingBox(
					(Double) boundingBox.get("latMax"),
					(Double) boundingBox.get("latMin"),
					(Double) boundingBox.get("lonMax"),
					(Double) boundingBox.get("lonMin"));
		}
	}
	
	//this method reads the output of a footprint command(.fp.xml)	
	/*
	 * {"EXTENT":"POLYGON ((97.20775 41.55055, 97.20775 63.62065, 139.74098 63.62065, 139.74098 41.55055, 97.20775 41.55055))",
	 * "FOOTPRINT":"POLYGON ((97.80899 63.62065, 120.18871 62.26886, 130.46156 60.30856, 139.74098 57.55454, 132.02788 50.46657, 125.67086 41.55055, 111.70353 44.57289, 97.20775 45.66548, 97.80899 63.62065))"}
	 */
	public void readFootprintFile(String file) throws FileNotFoundException, IOException, ParseException{
		JSONParser parser = new JSONParser();
		JSONObject metadata = (JSONObject) parser.parse(new FileReader(file));
	
		String extent = (String)metadata.get("EXTENT");
		String footprint = (String)metadata.get("FOOTPRINT");
		
		granule.getGranuleCharacterSet().add(createGranuleCharacter(footprint,"WKT_POLYGON"));
		granule.getGranuleCharacterSet().add(createGranuleCharacter(extent,"EXTENT"));
		
	}
	
	private GranuleCharacter createGranuleCharacter(String value, String name){
		
		GranuleCharacter gc = new GranuleCharacter();
		DatasetElement de = new DatasetElement();
		ElementDD edd = new ElementDD();
		
		gc.setValue(value);
		
		edd.setShortName(name);
		edd.setLongName(name);
		
		de.setElementDD(edd);
		gc.setDatasetElement(de);
		
		return gc;
	}
	
	
	//This method reads the output of the common metadata handler (.mp)
	
	/*
	 * {"granuleUR":"20170408033000-JPL-L2P_GHRSST-SSTskin-MODIS_T-N-v02.0-fv01.0.nc","localVersion":1,
	 * "boundingBox":{"SouthernLatitude":-84.1072,"NorthernLatitude":-59.4533,"EasternLongitude":-2.1755,"WesternLongitude":-99.8315},
	 * "productionDateTime":1496701426000,"dataFormat":"NetCDF","checksum":"300d760d9b362cf8b2f869c754ddf192","endingTime":1491622509000,"dataSize":24008128,
	 * "collection":{"name":"MODIS_T-JPL-L2P-v2014.0","version":1},"beginningTime":1491622211000,"dayNightFlag":"UNSPECIFIED"}
	 */
	public void readCommonMetadataFile(String file, String s3Location) throws FileNotFoundException, IOException, ParseException{
		//read JSON file
		JSONParser parser = new JSONParser();
		JSONObject metadata = (JSONObject) parser.parse(new FileReader(file));
		
		granule.setStartTimeLong((Long)metadata.get("beginningTime"));
		granule.setStopTimeLong((Long)metadata.get("endingTime"));
		granule.setCreateTimeLong((Long)metadata.get("productionDateTime"));
		
		//IS THIS THE NAME???
		//granule.setName((String)metadata.get("granuleUR"));
		granule.setVersion(((Long)metadata.get("localVersion")).intValue());
		granule.setDataFormat((String)metadata.get("dataFormat"));
		
		GranuleReference gr = new GranuleReference();
		gr.setDescription("S3 datafile.");
		gr.setPath(s3Location);
		gr.setStatus(GranuleArchiveStatus.ONLINE.toString());
		gr.setType(GranuleArchiveType.DATA.toString());
		granule.add(gr);
		//checksum and data size will be constructed by cumulus input granules by calling
		// setGranuleFileSizeAndChecksum function

		//lat/lon
		setGranuleBoundingBox(
				(Double)((JSONObject)metadata.get("boundingBox")).get("NorthernLatitude"),
				(Double)((JSONObject)metadata.get("boundingBox")).get("SouthernLatitude"),
				(Double)((JSONObject)metadata.get("boundingBox")).get("EasternLongitude"),
				(Double)((JSONObject)metadata.get("boundingBox")).get("WesternLongitude"));

		if (metadata.get("cycle") != null) {
			granule.setCycle(((Long) metadata.get("cycle")).intValue());
		}

		if (metadata.get("pass") != null) {
			granule.setPass(((Long) metadata.get("pass")).intValue());
		}

		if (metadata.get(Constants.Metadata.ORBIT) != null) {
			granule.setOrbitNumber(((Long) metadata.get(Constants.Metadata.ORBIT)).intValue());
		}

		if (metadata.get(Constants.Metadata.START_ORBIT) != null) {
			granule.setStartOrbit(((Long) metadata.get(Constants.Metadata.START_ORBIT)).intValue());
		}

		if (metadata.get(Constants.Metadata.END_ORBIT) != null) {
			granule.setEndOrbit(((Long) metadata.get(Constants.Metadata.END_ORBIT)).intValue());
		}
	}

	/**
	 * For a certain mission/collections, the workflow might not go through data handler step.
	 * Hence, we are using the input granule's files:[] as single point of truth for file size and checksum info
	 * file size and checksum goes into GranuleArchive POJO and then will be used to build UMM-G later
	 * as of 09/21/2020, following are the checksum algorithm acceptable by UMM JSON schema:  Be aware they are
	 * Case Sensitive
	 *
	 * "Algorithm": {
	 *           "description": "The algorithm name by which the checksum was calulated. This allows the user to re-calculate the checksum to verify the integrity of the downloaded data.",
	 *           "type": "string",
	 *           "enum": [
	 *             "Adler-32",
	 *             "BSD checksum",
	 *             "Fletcher-32",
	 *             "Fletcher-64",
	 *             "MD5",
	 *             "POSIX",
	 *             "SHA-1",
	 *             "SHA-2",
	 *             "SHA-256",
	 *             "SHA-384",
	 *             "SHA-512",
	 *             "SM3",
	 *             "SYSV"
	 *           ]
	 *         }
	 *
	 * @param input_granules : JSONArray presenting the input into MetadataAggregatorLambda function
	 *
	 */
	public void setGranuleFileSizeAndChecksum(JSONArray input_granules) {

		JSONArray files = (JSONArray)((JSONObject)input_granules.get(0)).get("files");

		for(Object f: files){
			JSONObject file = (JSONObject)f;
			AdapterLogger.LogDebug(this.className + " UMM-G GranuleArchive filename:" + (String)file.get("name"));
			AdapterLogger.LogDebug(this.className + " UMM-G GranuleArchive filesize:" + ((Double) file.get("size")).longValue());
			AdapterLogger.LogDebug(this.className + " UMM-G GranuleArchive checksum:" + (String)file.get("checksum"));
			AdapterLogger.LogDebug(this.className + " UMM-G GranuleArchive checksumType:" + (String)file.get("checksumType"));
			AdapterLogger.LogDebug(this.className + " UMM-G GranuleArchive type:" + (String)file.get("type"));
			UMMGranuleArchive uga = new UMMGranuleArchive();
			uga.setName((String)file.get("name"));
			uga.setFileSize(((Double) file.get("size")).longValue());
			uga.setChecksum((String)file.get("checksum"));
			uga.setChecksumAlgorithm(StringUtils.upperCase((String)file.get("checksumType")));
			String fileType = (String)file.get("type");
			if(StringUtils.equalsIgnoreCase(fileType, "data")) {
				uga.setType(GranuleArchiveType.DATA.toString());
			} else if (StringUtils.equalsIgnoreCase(fileType, "metadata")) {
				uga.setType(GranuleArchiveType.METADATA.toString());
			} else {
				uga.setType("");
			}
			granule.getGranuleArchiveSet().add(uga);
		}
		AdapterLogger.LogInfo(this.className + " GranuleArchive HashSet Size:" + granule.getGranuleArchiveSet().size());
		AdapterLogger.LogInfo(this.className + " GranuleArchive HashSet:" + granule.getGranuleArchiveSet());
	}

    /**
     * Entry point for translating ISO granule files to UMM-G
     *
     * @param file          the local path to the ISO file
     * @param s3Location    the s3 location to the granule file
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    public void readIsoMetadataFile(String file, String s3Location) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        Document doc = makeDoc(file);
        XPath xpath = makeXpath(doc);
        Iso isoType = getIsoType(doc, xpath);
        try {
            // parse the minimum required fields first
            parseRequiredFields(doc, xpath, isoType);
        } catch (XPathExpressionException e1) {
            // log a quick error message to help users narrow down the cause
            AdapterLogger.LogError("failed to parse required start, stop, and create times from: " + file);
            // now re-throw the error so we exit/stop the export
            throw e1;
        }
        // if we get here, we have the bare minimum fields already populated,
        // so try and parse the rest of the granule metadata...
        try {
            if (isoType == Iso.MENDS) {
                AdapterLogger.LogInfo("Found MENDS file");
                readIsoMendsMetadataFile(s3Location, doc, xpath);
            } else if (isoType == Iso.SMAP) {
                AdapterLogger.LogInfo("Found SMAP file");
                readIsoSmapMetadataFile(s3Location, doc, xpath);
            } else {
                throw new IOException(isoType.name() + " didn't match any expected ISO type.");
            }
        } catch (XPathExpressionException e2) {
            // ...but if we run into an issue, don't break out of the entire export,
            //  just log the warning, and continue
            AdapterLogger.LogWarning("Xpath error thrown when parsing optional metadata for: " + file);
        }
    }

    /**
     * Parses the ISO granule type from the file path
     *
     * @param doc       the Document object for the ISO File
     * @param xpath     the xpath object, used to parse the ISO type
     * @return           Iso.MENDS or Iso.SMAP based on xpath parsing
     *
     * @throws XPathExpressionException if there's an error while attempting to
     *  parse the ISO file type from the xpath object
     */
    private Iso getIsoType(Document doc, XPath xpath) throws XPathExpressionException {
        String ds_series_val = xpath.evaluate("/gmd:DS_Series", doc);
        return (ds_series_val == "") ? Iso.MENDS : Iso.SMAP;
    }

    private Document makeDoc(String file) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new File(file));
        return doc;
    }

    /**
     * Makes the xPath object for the provided Document object
     *
     * @param doc   the document object, from the ISO granule file
     * @return      the new xPath object
     */
    private XPath makeXpath(Document doc) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new NamespaceResolver(doc));
        return xpath;
    }

    /**
     * Parses the minimal required field for an ISO to UMM-G translation:
     *     i.e. start_time, stop_time and create_time
     *
     * @param doc   the document object of the iso file to parse
     * @param xpath the xpath object for use when parsing the doc
     * @param iso   flag to denote what type of ISO granule we have
     */
    private void parseRequiredFields(Document doc, XPath xpath, Iso iso) throws XPathExpressionException {
        if (iso == Iso.SMAP) {
            granule.setStartTime(DatatypeConverter.parseDateTime(xpath.evaluate(IsoSmapXPath.BEGINNING_DATE_TIME, doc)).getTime());
            granule.setStopTime(DatatypeConverter.parseDateTime(xpath.evaluate(IsoSmapXPath.ENDING_DATE_TIME, doc)).getTime());

            String productionDateTime = xpath.evaluate(IsoSmapXPath.PRODUCTION_DATE_TIME, doc);
            if (productionDateTime != "") {
                granule.setCreateTime(DatatypeConverter.parseDateTime(productionDateTime).getTime());
            } else {
                granule.setCreateTime(DatatypeConverter.parseDateTime(xpath.evaluate(IsoSmapXPath.CREATION_DATE_TIME, doc)).getTime());
            }
        } else if (iso == Iso.MENDS) {
            granule.setStartTime(DatatypeConverter.parseDateTime(xpath.evaluate(IsoMendsXPath.BEGINNING_DATE_TIME, doc)).getTime());
            granule.setStopTime(DatatypeConverter.parseDateTime(xpath.evaluate(IsoMendsXPath.ENDING_DATE_TIME, doc)).getTime());

            String productionDateTime = xpath.evaluate(IsoMendsXPath.PRODUCTION_DATE_TIME, doc);
            if (productionDateTime != "") {
                granule.setCreateTime(DatatypeConverter.parseDateTime(productionDateTime).getTime());
            } else {
                granule.setCreateTime(DatatypeConverter.parseDateTime(xpath.evaluate(IsoMendsXPath.CREATION_DATE_TIME, doc)).getTime());
            }
        } else {
            // throw an error, for now
        }
    }

    public void readIsoMendsMetadataFile(String s3Location, Document doc, XPath xpath) throws XPathExpressionException {

        if (xpath.evaluate(IsoMendsXPath.NORTH_BOUNDING_COORDINATE, doc) != "") {
            setGranuleBoundingBox(
                    Double.parseDouble(xpath.evaluate(IsoMendsXPath.NORTH_BOUNDING_COORDINATE, doc)),
                    Double.parseDouble(xpath.evaluate(IsoMendsXPath.SOUTH_BOUNDING_COORDINATE, doc)),
                    Double.parseDouble(xpath.evaluate(IsoMendsXPath.EAST_BOUNDING_COORDINATE, doc)),
                    Double.parseDouble(xpath.evaluate(IsoMendsXPath.WEST_BOUNDING_COORDINATE, doc)));
        }
        ((IsoGranule) granule).setPolygon(xpath.evaluate(IsoMendsXPath.POLYGON, doc));

        GranuleReference gr = new GranuleReference();
        gr.setDescription("S3 datafile.");
        gr.setPath(s3Location);
        gr.setStatus(GranuleArchiveStatus.ONLINE.toString());
        gr.setType(GranuleArchiveType.DATA.toString());
        granule.add(gr);

        NodeList nodes = (NodeList) xpath.evaluate(IsoMendsXPath.DATA_FILE, doc, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element dataFile = (Element) nodes.item(i);

            String description = xpath.evaluate(IsoMendsXPath.DATA_FILE_FILE_DESCRIPTION, dataFile);
            Pattern p = Pattern.compile("Size:\\s(.*)\\sSizeUnit:\\s(.*)\\sChecksumValue:\\s(.*)\\sChecksumAlgorithm:\\s(.*)\\sDescription:\\s(.*)");
            Matcher m = p.matcher(description);
            if (m.find()) {
                String type = m.group(5);
                if (type.equals("Science data file") || type.equals("ISO/Archive metadata file")
                        || type.equals("Quicklook Image of the Science data file")) {
                    String fileFormat = xpath.evaluate(IsoMendsXPath.DATA_FILE_FILE_FORMAT, dataFile);
                    if (type.equals("Science data file")) {
                        granule.setDataFormat(fileFormat);
                    }

                    IsoGranuleArchive ga = new IsoGranuleArchive();
                    ga.setType(fileFormat);
                    ga.setFileSize(Long.parseLong(m.group(1)));
                    ga.setSizeUnit(m.group(2));
                    ga.setName(xpath.evaluate(IsoMendsXPath.DATA_FILE_FILE_NAME, dataFile));
                    ga.setMimeType(xpath.evaluate(IsoMendsXPath.DATA_FILE_FILE_MIME_TYPE, dataFile));
                    ga.setChecksum(m.group(3));
                    ga.setChecksumAlgorithm(m.group(4));
                    granule.add(ga);
                }
            }
        }

        ((IsoGranule) granule).setProducerGranuleId(xpath.evaluate(IsoMendsXPath.PRODUCER_GRANULE_ID, doc));
        ((IsoGranule) granule).setCrid(xpath.evaluate(IsoMendsXPath.CRID, doc));

        NodeList identifiers = (NodeList) xpath.evaluate(IsoMendsXPath.IDENTIFIERS, doc, XPathConstants.NODESET);
        for (int i = 0; i < identifiers.getLength(); i++) {
            Element identifier = (Element) identifiers.item(i);
            String identifierDescription = xpath.evaluate(IsoMendsXPath.IDENTIFIER_DESCRIPTION, identifier);
            ((IsoGranule) granule).addIdentifier(identifierDescription.substring(identifierDescription.indexOf(" ") + 1), xpath.evaluate(IsoMendsXPath.IDENTIFIER_CODE, identifier));
        }

        String reprocessingPlanned = xpath.evaluate(IsoMendsXPath.REPROCESSING_PLANNED, doc);
        ((IsoGranule) granule).setReprocessingPlanned(reprocessingPlanned.substring(reprocessingPlanned.indexOf(" ") + 1));

        String reprocessingActual = xpath.evaluate(IsoMendsXPath.REPROCESSING_ACTUAL, doc);
        ((IsoGranule) granule).setReprocessingActual(reprocessingActual.substring(reprocessingActual.indexOf(" ") + 1));

        ((IsoGranule) granule).setParameterName(xpath.evaluate(IsoMendsXPath.PARAMETER_NAME, doc));
        String qaPercentMissingData = xpath.evaluate(IsoMendsXPath.QA_PERCENT_MISSING_DATA, doc);
        if (qaPercentMissingData != "") {
            ((IsoGranule) granule).setQAPercentMissingData(Double.parseDouble(qaPercentMissingData));
        }
        String qaPercentOutOfBoundsData = xpath.evaluate(IsoMendsXPath.QA_PERCENT_OUT_OF_BOUNDS_DATA, doc);
        if (qaPercentOutOfBoundsData != "") {
            ((IsoGranule) granule).setQAPercentOutOfBoundsData(Double.parseDouble(qaPercentOutOfBoundsData));
        }
        ((IsoGranule) granule).setOrbit(xpath.evaluate(IsoMendsXPath.ORBIT, doc));
        ((IsoGranule) granule).setSwotTrack(xpath.evaluate(IsoMendsXPath.SWOT_TRACK, doc));

        Source source = new Source();
        source.setSourceShortName(xpath.evaluate(IsoMendsXPath.PLATFORM, doc));

        Sensor sensor = new Sensor();
        sensor.setSensorShortName(xpath.evaluate(IsoMendsXPath.INSTRUMENT, doc));

        DatasetSource datasetSource = new DatasetSource();
        DatasetSource.DatasetSourcePK datasetSourcePK = new DatasetSource.DatasetSourcePK();
        datasetSourcePK.setSource(source);
        datasetSourcePK.setSensor(sensor);
        datasetSource.setDatasetSourcePK(datasetSourcePK);

        dataset.add(datasetSource);

        NodeList inputGranules = (NodeList) xpath.evaluate(IsoMendsXPath.GRANULE_INPUT, doc, XPathConstants.NODESET);
        for (int i = 0; i < inputGranules.getLength(); i++) {
            ((IsoGranule) granule).addInputGranule(inputGranules.item(i).getTextContent());
        }

        ((IsoGranule) granule).setPGEVersionClass(xpath.evaluate(IsoMendsXPath.PGE_VERSION_CLASS, doc));
    }

    private void readIsoSmapMetadataFile(String s3Location, Document doc, XPath xpath) throws XPathExpressionException {

        GranuleReference gr = new GranuleReference();
        gr.setDescription("S3 datafile.");
        gr.setPath(s3Location);
        gr.setStatus(GranuleArchiveStatus.ONLINE.toString());
        gr.setType(GranuleArchiveType.DATA.toString());
        granule.add(gr);

        String orbitInformation = xpath.evaluate(IsoSmapXPath.OrbitCalculatedSpatialDomains, doc);
        if (orbitInformation != null) {
            Pattern p = Pattern.compile("OrbitNumber:\\s*([^\\s]+)\\s*EquatorCrossingLongitude:\\s*([^\\s]+)\\s*EquatorCrossingDateTime:\\s*([^\\s]+)\\s*");
            Matcher m = p.matcher(orbitInformation);
            if (m.find()) {
                granule.setOrbitNumber(Integer.parseInt(m.group(1)));
                granule.setEquatorCrossingLongitude(new BigDecimal(m.group(2)));
                granule.setEquatorCrossingDateTime(m.group(3));
            } else {
                AdapterLogger.LogInfo("Couldn't match pattern with " + orbitInformation);
            }
        } else {
            AdapterLogger.LogInfo("Couldn't find orbit information from " + IsoSmapXPath.OrbitCalculatedSpatialDomains);
        }

        ((IsoGranule) granule).setProducerGranuleId(xpath.evaluate(IsoSmapXPath.PRODUCER_GRANULE_ID, doc));
        ((IsoGranule) granule).setCrid(xpath.evaluate(IsoXPath.CRID, doc));
        ((IsoGranule) granule).setParameterName("Parameter name placeholder");
        ((IsoGranule) granule).setReprocessingPlanned("No");

        ((IsoGranule) granule).setOrbit(xpath.evaluate(IsoSmapXPath.ORBIT, doc));

        ((IsoGranule) granule).setSwotTrack(xpath.evaluate(IsoSmapXPath.SWOT_TRACK, doc));

        Source source = new Source();
        source.setSourceShortName(xpath.evaluate(IsoXPath.PLATFORM, doc));
        Sensor sensor = new Sensor();
        sensor.setSensorShortName(xpath.evaluate(IsoXPath.INSTRUMENT, doc));

        DatasetSource datasetSource = new DatasetSource();
        DatasetSource.DatasetSourcePK datasetSourcePK = new DatasetSource.DatasetSourcePK();
        datasetSourcePK.setSource(source);
        datasetSourcePK.setSensor(sensor);
        datasetSource.setDatasetSourcePK(datasetSourcePK);

        dataset.add(datasetSource);

        NodeList inputGranules = (NodeList) xpath.evaluate(IsoSmapXPath.GRANULE_INPUT, doc, XPathConstants.NODESET);
        for (int i = 0; i < inputGranules.getLength(); i++) {
            ((IsoGranule) granule).addInputGranule(inputGranules.item(i).getTextContent());
        }

        ((IsoGranule) granule).setPGEVersionClass(xpath.evaluate(IsoSmapXPath.PGE_VERSION_CLASS, doc));
    }


	/**
	 * Parses SWOT archive.xml for metadata.
	 *
	 * @param file path to SWOT archive.xml
	 */
	public void readSwotArchiveXmlFile(String file) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(new File(file));

		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new NamespaceResolver(doc));

		granule.setStartTime(DatatypeConverter.parseDateTime(xpath.evaluate(SwotArchiveXmlXPath.BEGINNING_DATE_TIME, doc)).getTime());
		granule.setStopTime(DatatypeConverter.parseDateTime(xpath.evaluate(SwotArchiveXmlXPath.ENDING_DATE_TIME, doc)).getTime());
		granule.setCreateTime(DatatypeConverter.parseDateTime(xpath.evaluate(SwotArchiveXmlXPath.CREATION_DATE_TIME, doc)).getTime());

		// No spatial extent exists for SWOT L0 data so set as global
		setGranuleBoundingBox(90.0, -90.0, 180.0, -180.0);
	}

	/**
	 * Parses Sentinel-6 XFDU manifest for metadata.
	 *
	 * @param file path to XFDU manifest
	 */
	public void readSentinelManifest(String file) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, java.text.ParseException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(new File(file));

		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new NamespaceResolver(doc));

		String startTime = xpath.evaluate(ManifestXPath.BEGINNING_DATE_TIME, doc);
		if (startTime == "") {
			startTime = xpath.evaluate(ManifestXPath.AUX_BEGINNING_DATE_TIME, doc);
		}

		String stopTime = xpath.evaluate(ManifestXPath.ENDING_DATE_TIME, doc);
		if (stopTime == "") {
			stopTime = xpath.evaluate(ManifestXPath.AUX_ENDING_DATE_TIME, doc);
		}

		String createTime = null;
		try {
			createTime = xpath.evaluate(ManifestXPath.CREATION_DATE_TIME, doc);
		} catch (XPathExpressionException e) {
			createTime = xpath.evaluate(ManifestXPath.AUX_CREATION_DATE_TIME, doc);
		}

		granule.setStartTime(DatatypeConverter.parseDateTime(startTime).getTime());
		granule.setStopTime(DatatypeConverter.parseDateTime(stopTime).getTime());

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		granule.setCreateTime(simpleDateFormat.parse(createTime));

		try {
			String line = xpath.evaluate(ManifestXPath.LINE, doc);
			if (line != null && !line.isEmpty()) {
				granule.getGranuleCharacterSet().add(createGranuleCharacter(line,"line"));
			}
		} catch (XPathExpressionException e) {
			// Ignore if unable to parse for footprint since it isn't required for ingest
		}

		String cycle = xpath.evaluate(ManifestXPath.CYCLE, doc);
		if (!cycle.isEmpty())
			granule.setCycle(Integer.parseInt(cycle));

		String pass = xpath.evaluate(ManifestXPath.PASS, doc);
		if (!pass.isEmpty())
			granule.setPass(Integer.parseInt(pass));

		String productName = null;
		try {
		productName = xpath.evaluate(ManifestXPath.PRODUCT_NAME, doc);
		} catch (XPathExpressionException e1) {
			try {
				productName = xpath.evaluate(ManifestXPath.AUX_FILE_NAME, doc);
			} catch (XPathExpressionException e2) {
				// Ignore if we can't parse SAFE name
			}
		}
		if (productName != null && !productName.isEmpty())
			granule.getGranuleCharacterSet().add(createGranuleCharacter(productName, UMMGranuleFile.PROVIDER_DATA_SOURCE));
	}

    private void setGranuleBoundingBox(double north, double south, double east, double west) {
		AdapterLogger.LogInfo("set bounding box 4 coordinates for UMMGranule object");
		granule.setBbxNorthernLatitude(north);
		granule.setBbxSouthernLatitude(south);
		granule.setBbxEasternLongitude(east);
		granule.setBbxWesternLongitude(west);
	}

	public JSONObject createJson()
	throws ParseException, IOException, URISyntaxException {
		granule.setIngestTime(new Date());
		UMMGranuleFile granuleFile = new UMMGranuleFile(granule, dataset, rangeIs360);
		JSONObject granuleJson = granuleFile.defineGranule();
		return granuleJson;
	}

	public void writeJson(String outputLocation)
			throws IOException, ParseException, URISyntaxException{
		JSONObject granuleJson = createJson();
		FileUtils.writeStringToFile(new File(outputLocation), granuleJson.toJSONString());
	}
	
	public Dataset getDataset(){
		return this.dataset;
	}
	
	public Granule getGranule(){
		return this.granule;
	}
}

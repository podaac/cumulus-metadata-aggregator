package gov.nasa.cumulus.metadata.aggregator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import gov.nasa.cumulus.metadata.aggregator.factory.UmmgPojoFactory;
import gov.nasa.cumulus.metadata.umm.generated.AdditionalAttributeType;
import gov.nasa.cumulus.metadata.umm.generated.TrackPassTileType;
import gov.nasa.cumulus.metadata.umm.generated.TrackType;
import gov.nasa.cumulus.metadata.util.BoundingTools;
import gov.nasa.cumulus.metadata.util.JSONUtils;
import gov.nasa.podaac.inventory.model.Granule;
import gov.nasa.podaac.inventory.model.GranuleCharacter;
import gov.nasa.podaac.inventory.model.DatasetElement;
import gov.nasa.podaac.inventory.model.DatasetCitation;
import gov.nasa.podaac.inventory.model.DatasetSource;
import gov.nasa.podaac.inventory.model.GranuleReference;
import gov.nasa.podaac.inventory.model.Source;
import gov.nasa.podaac.inventory.model.Sensor;
import gov.nasa.podaac.inventory.model.ElementDD;
import gov.nasa.podaac.inventory.model.Dataset;
import org.apache.commons.collections.iterators.ObjectArrayIterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.podaac.inventory.api.Constant.GranuleArchiveStatus;
import gov.nasa.podaac.inventory.api.Constant.GranuleArchiveType;
import gov.nasa.cumulus.metadata.umm.model.UMMGranuleArchive;
import org.xml.sax.SAXException;
import cumulus_message_adapter.message_parser.AdapterLogger;

public class MetadataFilesToEcho {
	String className = this.getClass().getName();
	private UMMGranule granule;
	Dataset dataset = new Dataset();
	boolean forceBB = false;
	boolean rangeIs360 = false;
    boolean isIsoFile = false;
	JSONObject additionalAttributes = null;
	UmmgPojoFactory ummgPojoFactory = UmmgPojoFactory.getInstance();

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
		setDatasetValues(shortName, metadata.get("version").toString(), (Boolean) metadata.get("rangeIs360"), null, metadata);

	}

	/**
	 * Sets shortName, version, rangeIs360 and north,east,south,west coordinates.
	 *
	 * @param shortName   dataset short name
	 * @param version     dataset version
	 * @param rangeIs360  true if dataset granule coordinates are 0 to 360, null/false if not
	 * @param boundingBox JSONObject with latMax, lonMax, latMin, lonMin
	 */
	public void setDatasetValues(String shortName, String version, Boolean rangeIs360, JSONObject boundingBox, JSONObject additionalAttributes) {
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

		if (additionalAttributes != null) {
			setAdditionalAttributes(additionalAttributes);
		}
	}

	public void setAdditionalAttributes(JSONObject metadata){
		if(validateJSONObjectKeyExists(metadata, "additionalAttributes") && metadata.get("additionalAttributes") instanceof JSONObject){
			// contains additionalAttributes
			JSONObject additionalAttributes = (JSONObject) metadata.get("additionalAttributes");
			this.additionalAttributes = additionalAttributes;
		}
	}

	public boolean validateJSONObjectKeyExists(JSONObject jsonObject, String key){
		return (jsonObject.containsKey(key) && jsonObject.get(key) != null);
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
	public void readCommonMetadataFile(String file, String s3Location, JSONArray granule_files) throws IOException, ParseException{
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

		// appending granule references into granule object
		buildGranuleReferences(granule_files);

		//checksum and data size will be constructed by cumulus input granules by calling
		// setGranuleFileSizeAndChecksum function

		//lat/lon
		setGranuleBoundingBox(
				(Double)((JSONObject)metadata.get("boundingBox")).get("NorthernLatitude"),
				(Double)((JSONObject)metadata.get("boundingBox")).get("SouthernLatitude"),
				(Double)((JSONObject)metadata.get("boundingBox")).get("EasternLongitude"),
				(Double)((JSONObject)metadata.get("boundingBox")).get("WesternLongitude"));

		/**
		 * so far, universal-data-handler is not generating .mp file with tiles yet
		 * just cycle and passes.
		 * Passes are under cycle. If there is no cycle then there is no pass
		 * common metadata file does not include tiles info yet, so there is no need to convert TrackType
		 * 	 to AdditionalAttributeType Array
		 */
		try {
			TrackType trackType = createTrackType(NumberUtils.createInteger(UMMUtils.removeStrLeadingZeros(metadata.get("cycle").toString())),
					NumberUtils.createInteger(UMMUtils.removeStrLeadingZeros(metadata.get("pass").toString())));
			granule.setTrackType(trackType);
		} catch (Exception e) {
			AdapterLogger.LogWarning(this.className + " Continue execution after " +
					"common metadata .mp extracting cycle and pass failed with exception:" + UMMUtils.getStackTraceAsString(e));
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

	private void buildGranuleReferences(JSONArray granule_files) {
		if (granule_files != null) {
			for (Object f : granule_files) {
				JSONObject gf = (JSONObject) f;
				// This is wishing the "metadata" type doesn't needs further processing
				if (((String) gf.get("type")).equals("data") || ((String) gf.get("type")).equals("browse")) {
					AdapterLogger.LogDebug(this.className +
							"buildGranuleReferences - appending file as reference - " +
							gf.get("key"));
					GranuleReference gr = new GranuleReference();
					gr.setDescription("S3 datafile.");
					gr.setPath((String) gf.get("key"));
					gr.setStatus(GranuleArchiveStatus.ONLINE.toString());
					gr.setType(GranuleArchiveType.DATA.toString());
					granule.add(gr);
				}
			}
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
			AdapterLogger.LogDebug(this.className + " UMM-G GranuleArchive filename:" + (String)file.get("fileName"));
			AdapterLogger.LogDebug(this.className + " UMM-G GranuleArchive filesize:" + ((Double) file.get("size")).longValue());
			AdapterLogger.LogDebug(this.className + " UMM-G GranuleArchive checksum:" + (String)file.get("checksum"));
			AdapterLogger.LogDebug(this.className + " UMM-G GranuleArchive checksumType:" + (String)file.get("checksumType"));
			AdapterLogger.LogDebug(this.className + " UMM-G GranuleArchive type:" + (String)file.get("type"));
			UMMGranuleArchive uga = new UMMGranuleArchive();
			uga.setName((String)file.get("fileName"));
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
    public void readIsoMetadataFile(String file, String s3Location, JSONArray granule_files) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        Document doc = null;
        XPath xpath = null;
        IsoType isoType = null;
        try {
			doc = makeDoc(file);
			xpath = makeXpath(doc);
			isoType = getIsoType(doc, xpath);
            // parse the minimum required fields first
            parseRequiredFields(doc, xpath, isoType);
        } catch (ParserConfigurationException | IOException | SAXException | XPathExpressionException e1) {
            // log a quick error message to help users narrow down the cause
            AdapterLogger.LogError("failed to parse required start, stop, and create times from: " + file + " " + e1);
            // now re-throw the error so we exit/stop the export
            throw e1;
        }
        // if we get here, we have the bare minimum fields already populated,
        // so try and parse the rest of the granule metadata...
        try {
            if (isoType == IsoType.MENDS) {
                AdapterLogger.LogInfo("Found MENDS file");
                readIsoMendsMetadataFile(s3Location, doc, xpath);
            } else if (isoType == IsoType.SMAP) {
                AdapterLogger.LogInfo("Found SMAP file");
				((IsoGranule) this.granule).setIsoType(isoType);
                readIsoSmapMetadataFile(s3Location, doc, xpath);
            } else {
                AdapterLogger.LogWarning(isoType.name() + " didn't match any expected ISO type, skipping optional " +
                        "fields.");
            }
        }
		catch (XPathExpressionException e2) {
            AdapterLogger.LogWarning("Xpath error thrown when parsing optional metadata for: " + file + " " + e2);
			throw e2;
        }

		// appending granule references into granule object
		buildGranuleReferences(granule_files);

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
    public IsoType getIsoType(Document doc, XPath xpath) throws XPathExpressionException {
        String ds_series_val = xpath.evaluate("/gmd:DS_Series", doc);
        return (ds_series_val == "") ? IsoType.MENDS : IsoType.SMAP;
    }

    public Document makeDoc(String file) throws ParserConfigurationException, SAXException, IOException {
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
    public XPath makeXpath(Document doc) {
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
    private void parseRequiredFields(Document doc, XPath xpath, IsoType iso) throws XPathExpressionException {
        if (iso == IsoType.SMAP) {
            granule.setStartTime(DatatypeConverter.parseDateTime(xpath.evaluate(IsoSmapXPath.BEGINNING_DATE_TIME, doc)).getTime());
            granule.setStopTime(DatatypeConverter.parseDateTime(xpath.evaluate(IsoSmapXPath.ENDING_DATE_TIME, doc)).getTime());

            String productionDateTime = xpath.evaluate(IsoSmapXPath.PRODUCTION_DATE_TIME, doc);
            if (productionDateTime != "") {
                granule.setCreateTime(DatatypeConverter.parseDateTime(productionDateTime).getTime());
            } else {
                granule.setCreateTime(DatatypeConverter.parseDateTime(xpath.evaluate(IsoSmapXPath.CREATION_DATE_TIME, doc)).getTime());
            }
        } else if (iso == IsoType.MENDS) {
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
            throw new XPathExpressionException(iso.name() + " didn't match any expected ISO type, aborting.");
        }
    }

    public IsoGranule readIsoMendsMetadataFile(String s3Location, Document doc, XPath xpath) throws XPathExpressionException {

        if (xpath.evaluate(IsoMendsXPath.NORTH_BOUNDING_COORDINATE, doc) != "") {
            setGranuleBoundingBox(
                    Double.parseDouble(xpath.evaluate(IsoMendsXPath.NORTH_BOUNDING_COORDINATE, doc)),
                    Double.parseDouble(xpath.evaluate(IsoMendsXPath.SOUTH_BOUNDING_COORDINATE, doc)),
                    Double.parseDouble(xpath.evaluate(IsoMendsXPath.EAST_BOUNDING_COORDINATE, doc)),
                    Double.parseDouble(xpath.evaluate(IsoMendsXPath.WEST_BOUNDING_COORDINATE, doc)));
        }
        ((IsoGranule) granule).setPolygon(xpath.evaluate(IsoMendsXPath.POLYGON, doc));

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
        if (qaPercentMissingData != "" && BoundingTools.isParseable(qaPercentMissingData)) {
            ((IsoGranule) granule).setQAPercentMissingData(Double.parseDouble(qaPercentMissingData));
        }
        String qaPercentOutOfBoundsData = xpath.evaluate(IsoMendsXPath.QA_PERCENT_OUT_OF_BOUNDS_DATA, doc);
        if (qaPercentOutOfBoundsData != "" && BoundingTools.isParseable(qaPercentOutOfBoundsData)) {
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
            ((IsoGranule) granule).addInputGranule(inputGranules.item(i).getTextContent().trim());
        }

        ((IsoGranule) granule).setPGEVersionClass(xpath.evaluate(IsoMendsXPath.PGE_VERSION_CLASS, doc));
		// Process ISO cycle, pass and tile
		String  cyclePassTileSceneStr =StringUtils.trim(xpath.evaluate(IsoMendsXPath.CYCLE_PASS_TILE_SCENE, doc));
		if(!StringUtils.isBlank(cyclePassTileSceneStr)) {
			try {
				createIsoCyclePassTile(cyclePassTileSceneStr);
			} catch (Exception e) {
				// Since TrackType which contains Cycle Pass Tile and Scenes is not a required field
				// we catch exception with printStackTrace to know the exact line throwing error
				// then continue processing.
				AdapterLogger.LogError("Iso MENDs cyclePassTileScene processing failed :" + UMMUtils.getStackTraceAsString(e));
				throw e;
			}
		}

		if(additionalAttributes != null) {
			// Gets the XML contents of the additional attributes
			NodeList additionalAttributesBlock = (NodeList) xpath.evaluate(IsoMendsXPath.ADDITIONAL_ATTRIBUTES_BLOCK, doc, XPathConstants.NODESET);

			// Builds full list of additional attributes as the AdditionalAttributeType
			List<AdditionalAttributeType> additionalAttributeTypes = appendAdditionalAttributes(additionalAttributes, additionalAttributesBlock);
			((IsoGranule) granule).setAdditionalAttributeTypes(additionalAttributeTypes);

			// Save anything that isn't in `publishAll` and `publish` into dynamicAttributeNameMapping so can be placed into JSON
			// TODO: should we make a copy instead?
			// Also JSONOBJECT.remove might cause memory leak if within loop
			// (https://arduinojson.org/v6/api/jsonobject/remove/)
			additionalAttributes.remove("publish");
			additionalAttributes.remove("publishAll");
			((IsoGranule) granule).setDynamicAttributeNameMapping(additionalAttributes);
		}
		
					
        String mgrsId = xpath.evaluate(IsoMendsXPath.MGRS_ID, doc);
        if (mgrsId != null && !mgrsId.equals("")) {
            // If MGRS_ID field is not null, set as additional attribute
            AdditionalAttributeType mgrsAttr = new AdditionalAttributeType("MGRS_TILE_ID", Collections.singletonList(mgrsId));
            
            List<AdditionalAttributeType> additionalAttributeTypes = ((IsoGranule) granule).getAdditionalAttributeTypes();
            if (additionalAttributeTypes == null) {
                additionalAttributeTypes = Collections.singletonList(mgrsAttr);
            } else {
                additionalAttributeTypes.add(mgrsAttr);
            }
            
            JSONObject dynamicAttributeNameMapping = ((IsoGranule) granule).getDynamicAttributeNameMapping();
            if (dynamicAttributeNameMapping == null) {
                ((IsoGranule) granule).setDynamicAttributeNameMapping(additionalAttributes);
            } else {
                dynamicAttributeNameMapping.put("MGRS_TILE_ID", Collections.singletonList(mgrsId));
            }
            ((IsoGranule) granule).setAdditionalAttributeTypes(additionalAttributeTypes);
            ((IsoGranule) granule).setDynamicAttributeNameMapping(dynamicAttributeNameMapping);
        }

		return  ((IsoGranule) granule);
	}

	public List<AdditionalAttributeType> appendAdditionalAttributes(JSONObject metaAdditionalAttributes, NodeList additionalAttributesBlock){
		/*
		Scan through meta.additionalAttributes
		if `publishAll`, just publish everything mapped
		if not, see which fields we want to get from the `publish` field

		To see this code in action, run any unit test with `testReadIsoMendsMetadataFileAdditionalFields` as part of its name
		 */

		/*
		Pre-check publishAll and publish list (if any)
		 */
		if(!metaAdditionalAttributes.containsKey("publishAll")){
			//publish all key isn't in additionalAttributes block, assume null
			throw new MissingResourceException("publishAll key is missing from additionalAttribute",
					"MetadataFilesToEcho.appendAdditionalAttributes()",
					"publishAll");
		}else if(metaAdditionalAttributes.get("publishAll") == null
				|| metaAdditionalAttributes.get("publishAll").toString().isEmpty()){
			// Somehow publishAll exists but is null or empty...
			throw new MissingResourceException("publishAll key is empty or null from additionalAttribute",
					"MetadataFilesToEcho.appendAdditionalAttributes()",
					"publishAll");
		}

		// Make simple List of Additional Attributes
		List<AdditionalAttributeType> additionalAttributeTypes = new ArrayList<>();
		List<AdditionalAttributeType> subAdditionalAttributeTypes = new ArrayList<>();
		List<String> publishList = metaAdditionalAttributes.get("publish") == null ? null : (List<String>) metaAdditionalAttributes.get("publish");
		// Check to ensure additional attributes are in pairs (key/pair)
		if (additionalAttributesBlock.getLength() % 2 == 0) {
			for (int i = 0; i < additionalAttributesBlock.getLength(); i++) {
				// We only want to look at address 0, 2, 4..., which should be the key
				if (i % 2 == 0) {
					Node key = additionalAttributesBlock.item(i);
					Node val = additionalAttributesBlock.item(i + 1);

					AdditionalAttributeType aat = new AdditionalAttributeType();
					aat.setName(key.getTextContent());
					aat.setValues(Arrays.asList(val.getTextContent()));
					additionalAttributeTypes.add(aat);

					if(publishList != null && publishList.contains(key.getTextContent())){
						subAdditionalAttributeTypes.add(aat);
					}
				}
			}
		}

		// if publish all, just return the list
		if((Boolean) metaAdditionalAttributes.get("publishAll")){
			return additionalAttributeTypes;
		}else{
			return subAdditionalAttributeTypes;
		}

	}

	/**
	 * Marshell the cycle pass tile string from ISO xml into UMMG POJO.
	 * Example input :"Cycle: 5 Pass: [40, Tiles: 4-5L 4-8R] [41, Tiles: 6R 6L] [42, Tiles: 7F]"
	 *"Cycle: 5 Pass: [40, Tiles: 4-5L 4-5R] [41, Tiles: 6R 6L] [42, Tiles: 7F] Cycle: 6 Pass: [50, Tiles: 4-5L 4-5R] [51, Tiles: 6R 6L] [52, Tiles: 7F]";
	 * @param cyclePassTileStr
	 * @return
	 */
	public IsoGranule createIsoCyclePassTile(String cyclePassTileStr) {
		cyclePassTileStr = UMMUtils.removeLineFeedCarriageReturn(cyclePassTileStr);
		AdapterLogger.LogInfo(this.className + " iso cycle pass tile string:" + cyclePassTileStr);
		String toBeProcessedStr = StringUtils.upperCase(StringUtils.trim(cyclePassTileStr));
		Pattern p_cycle = Pattern.compile("CYCLE\\s*:\\s*\\d+\\s*?");
		Matcher m_cycle = p_cycle.matcher(toBeProcessedStr);
		String cycleStr = "";
		ArrayList<String> cycleStrs = new ArrayList<>();
		while(m_cycle.find()) {
			cycleStr = m_cycle.group();
			AdapterLogger.LogInfo("Cycle:" + cycleStr);
			cycleStrs.add(cycleStr.trim());
		}
		int numberOfCycles = cycleStrs.size();
		int i = 0;
		// Current UMMG schema 1.6.3 supports only one cycle. Although it is possible that
		//  one granule contains 2 cycles as the edge case.
		ArrayList<String> cyclePassStrs = new ArrayList<>();
		while(i +1 < numberOfCycles) {
			String cyclePassStr = StringUtils.substring(toBeProcessedStr,
					StringUtils.indexOf(toBeProcessedStr, cycleStrs.get(i)),
					StringUtils.indexOf(toBeProcessedStr, cycleStrs.get(i+1)));
			AdapterLogger.LogInfo("cyclePass processing candidate:" + cyclePassStr);
			cyclePassStrs.add(cyclePassStr);
			toBeProcessedStr = StringUtils.replace(toBeProcessedStr, cyclePassStr, "");
			i++;
		}
		toBeProcessedStr = StringUtils.trim(toBeProcessedStr);
		// Processed the last Cycle OR if there is only one cycle, we are processing the first==last cycle here
		if(StringUtils.isNotEmpty(toBeProcessedStr) && StringUtils.startsWithIgnoreCase(toBeProcessedStr, "CYCLE")) {
			AdapterLogger.LogInfo("Processing the last or first cycle" + toBeProcessedStr);
			cyclePassStrs.add(toBeProcessedStr);
		}
		TrackType trackType = null;
		List<AdditionalAttributeType> additionalAttributeTypes = new ArrayList<>();
		/**
		 * This block of code supports multiple cycles.  In theory, during cycle transition, it is possible
		 * a granule consists 2 cycles.  However, UMMG json schema does support one at the time.
		 */
		for(String cps : cyclePassStrs) {
			try {
				trackType = createTrackType(cps, p_cycle);
			} catch (Exception e) {
				AdapterLogger.LogError(this.className + " Creating TrackType with exception: " + UMMUtils.getStackTraceAsString(e));
				throw e;
			}
			UmmgPojoFactory ummgPojoFactory = UmmgPojoFactory.getInstance();
			additionalAttributeTypes=
					ummgPojoFactory.trackTypeToAdditionalAttributeTypes(trackType);
		}
		// It is possible after all the above processing, cycle is present but passes is not (no pass in passes array)
		// That is, we shall NOT create trackType at all.  Otherwise, CMR will throw validation error
		if (trackType.getCycle()!=null && trackType.getPasses()!=null && trackType.getPasses().size() >0) {
			((IsoGranule) granule).setTrackType(trackType);
			((IsoGranule) granule).setAdditionalAttributeTypes(additionalAttributeTypes);
		}
		return (IsoGranule)granule;
	}

	public TrackType createTrackType(String cyclePassTileStr, Pattern p_cycle) {
		Matcher m_cycle = p_cycle.matcher(cyclePassTileStr);
		String cycleStr=null;
		while(m_cycle.find()) {
			cycleStr = m_cycle.group();
		}
		String cycleNoStr = StringUtils.trim(
				StringUtils.replace(
						StringUtils.replaceIgnoreCase(cycleStr, "CYCLE","")
						,":",""));
		AdapterLogger.LogInfo("Cycle:" + cycleNoStr);
		TrackType trackType  = new TrackType();
		trackType.setCycle(NumberUtils.createInteger(UMMUtils.removeStrLeadingZeros(cycleNoStr)));

		Pattern p_pass = Pattern.compile("\\[.*?\\]");
		Matcher m_pass = p_pass.matcher(cyclePassTileStr);
		/**
		 * The matcher will resolve to the following tokens:
		 * [40, TILES: 4-5L 4-5R]
		 * [41, TILES: 6R 6L]
		 * [42, TILES: 7F]
		 * The following loop process each token by extracting the passes and tilles
		 */
		ArrayList<TrackPassTileType> trackPassTileTypes = new ArrayList<>();
		while(m_pass.find()) {
			TrackPassTileType trackPassTileType = new TrackPassTileType();
			String passTilesStr = m_pass.group();
			passTilesStr =StringUtils.replace(
					StringUtils.replace(passTilesStr,"[",""),"]","");
			passTilesStr = passTilesStr.replaceAll("TILES\\s*:\\s*?", "");
			String[] passTiles = StringUtils.split(passTilesStr, ",");
			String passStr = StringUtils.trim(passTiles[0]);
			trackPassTileType.setPass(NumberUtils.createInteger(UMMUtils.removeStrLeadingZeros(passStr)));
			try {
				List<String> tiles = getTiles(StringUtils.trim(passTiles[1]));
				trackPassTileType.setTiles(tiles);
			} catch (Exception e) {
				AdapterLogger.LogWarning(this.className + " Continue processing after tile processing failed with " +
						"exception: " + UMMUtils.getStackTraceAsString(e));
			}
			trackPassTileTypes.add(trackPassTileType);
		}
		trackType.setPasses(trackPassTileTypes);
		return trackType;
	}

	/**
	 *  5-8L 4K means, tile 5L, 6L, 7L, 8L and 4K.  because dash means a range separator
	 *
	 * @param tilesStr  ex 5-6L 4K
	 * @return
	 */
	public List<String> getTiles(String tilesStr) {
		AdapterLogger.LogInfo(this.className + " decoding tiles string:" + tilesStr);
		ArrayList<String> tiles = new ArrayList<>();
		String[] tileRanges = StringUtils.split(tilesStr, " ");
		// Using Apache Commons Collections
		ObjectArrayIterator iterator = new ObjectArrayIterator(tileRanges);

		while (iterator.hasNext()) {
			String tileRangStr=(String) iterator.next();
			if(StringUtils.containsIgnoreCase(tileRangStr,"-")){
				String[] tileRangeTokens = StringUtils.split(tileRangStr, "-");
				String endTileStr = tileRangeTokens[tileRangeTokens.length -1];
				// find the letter for the last token
				String tileMarkChar = endTileStr.substring(endTileStr.length() - 1);
				Integer startTileNum = NumberUtils.createInteger(UMMUtils.removeStrLeadingZeros(
						StringUtils.trim(tileRangeTokens[0])));
				Integer endTileNumStr = NumberUtils.createInteger(
						UMMUtils.removeStrLeadingZeros(
						StringUtils.trim(
								StringUtils.substring(endTileStr, 0, endTileStr.length()-1))));
				for(Integer i = startTileNum; i<=endTileNumStr; i++ ) {
					tiles.add(i + tileMarkChar);
				}
			} else { // if it is not a range of tiles like : 5-8L
				tiles.add(tileRangStr);
			}
		}
		return tiles;
	}

    private void readIsoSmapMetadataFile(String s3Location, Document doc, XPath xpath) throws XPathExpressionException {
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
        ((IsoGranule) granule).setCrid(xpath.evaluate(IsoSmapXPath.CRID, doc));
        ((IsoGranule) granule).setParameterName("Parameter name placeholder");
        ((IsoGranule) granule).setReprocessingPlanned("No");

        ((IsoGranule) granule).setOrbit(xpath.evaluate(IsoSmapXPath.ORBIT, doc));

        ((IsoGranule) granule).setSwotTrack(xpath.evaluate(IsoSmapXPath.SWOT_TRACK, doc));
		((IsoGranule) granule).setPolygon(xpath.evaluate(IsoSmapXPath.POLYGON, doc));

        Source source = new Source();
        source.setSourceShortName(xpath.evaluate(IsoSmapXPath.PLATFORM, doc));
        Sensor sensor = new Sensor();
        sensor.setSensorShortName(xpath.evaluate(IsoSmapXPath.INSTRUMENT, doc));

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

		granule = createSwotArchiveGranule(doc, xpath);
		// No spatial extent exists for SWOT L0 data so set as global
		setGranuleBoundingBox(90.0, -90.0, 180.0, -180.0);
	}
    
    /**
     * Parse metadata from SWOT Cal/Val XML file
     * @param file path to SWOT Cal/Val XML file on local file system
     */
    public void readSwotCalValXmlFile(String file) throws ParserConfigurationException, IOException, SAXException,
            XPathExpressionException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new File(file));
    
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new NamespaceResolver(doc));
    
        String startTime = xpath.evaluate(SwotCalValXmlPath.BEGINNING_DATE_TIME, doc);
        String stopTime = xpath.evaluate(SwotCalValXmlPath.ENDING_DATE_TIME, doc);
        String createTime = xpath.evaluate(SwotCalValXmlPath.CREATION_DATE_TIME, doc);
    
        String north = xpath.evaluate(SwotCalValXmlPath.NORTH_BOUNDING_COORDINATE, doc);
        String south = xpath.evaluate(SwotCalValXmlPath.SOUTH_BOUNDING_COORDINATE, doc);
        String east = xpath.evaluate(SwotCalValXmlPath.EAST_BOUNDING_COORDINATE, doc);
        String west = xpath.evaluate(SwotCalValXmlPath.WEST_BOUNDING_COORDINATE, doc);
    
        try {
            granule.setStartTime(DatatypeConverter.parseDateTime(startTime).getTime());
            granule.setStopTime(DatatypeConverter.parseDateTime(stopTime).getTime());
            granule.setCreateTime(DatatypeConverter.parseDateTime(createTime).getTime());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(String.format("Failed to parse datetime start=%s stop=%s create=%s",
                    startTime, stopTime, createTime), exception);
        }
    
        try {
            setGranuleBoundingBox(Double.parseDouble(north),
                    Double.parseDouble(south),
                    Double.parseDouble(east),
                    Double.parseDouble(west));
        } catch (NullPointerException | NumberFormatException exception) {
            throw new IllegalArgumentException(String.format("Failed to parse bbox N=%s S=%s E=%s W=%s", north, south,
                    east, west), exception);
        }
    }

	UMMGranule createSwotArchiveGranule(Document doc, XPath xpath)
	throws XPathExpressionException{
		granule.setStartTime(DatatypeConverter.parseDateTime(xpath.evaluate(SwotArchiveXmlXPath.BEGINNING_DATE_TIME, doc)).getTime());
		granule.setStopTime(DatatypeConverter.parseDateTime(xpath.evaluate(SwotArchiveXmlXPath.ENDING_DATE_TIME, doc)).getTime());
		granule.setCreateTime(DatatypeConverter.parseDateTime(xpath.evaluate(SwotArchiveXmlXPath.CREATION_DATE_TIME, doc)).getTime());
		String  cycleStr =StringUtils.trim(xpath.evaluate(SwotArchiveXmlXPath.ARCHIVE_CYCLE, doc));
		String  passStr  =StringUtils.trim(xpath.evaluate(SwotArchiveXmlXPath.ARCHIVE_PASS, doc));
		String  tileStr  =StringUtils.trim(xpath.evaluate(SwotArchiveXmlXPath.ARCHIVE_TILE, doc));

		// TrackType is not a required field.  Hence, if thrown exception during process, we will swallow exception
		// log and continue
		TrackType trackType;
		try {
			trackType = createTrackType(cycleStr, passStr, tileStr);
			granule.setTrackType(trackType);
			granule.setAdditionalAttributeTypes(UmmgPojoFactory.getInstance().trackTypeToAdditionalAttributeTypes(trackType));
		} catch (Exception e) {
			AdapterLogger.LogWarning(this.className + " Exception while creating TrackType: " + UMMUtils.getStackTraceAsString(e));
		}
		return granule;
	}

	/**
	 * to create TrackType, cycle and passes are both required.
	 * However, within each passes (TrackPassTileType), only pass is required.
	 * i.e., it is possible we can have TrackPassTileType with pass but no tile
	 *
	 * However, the Track (TrackType) under HorizontalSpatialDomainType, is not required.
	 * https://git.earthdata.nasa.gov/projects/EMFD/repos/unified-metadata-model/browse/granule/v1.6.3/umm-g-json-schema.json#514
	 * @param cycleStr
	 * @param passStr
	 * @param tileStr
	 * @return
	 */
	public TrackType createTrackType(String cycleStr, String passStr, String tileStr) {
		/*
		based on the "do as much we can" theory.  the code shall only allow the situation where
		title can not be processed, since cycle and passes are both required.
		According to UMMG schema 1.6.3, cycle and pass are both integer.  tile is String

		on the other hand, any exceptions caused by cycle or pass should be thrown all the way up and break the ingestion
		 */
		try {
			if (NumberUtils.createInteger(StringUtils.trim(cycleStr)) == null ||
					NumberUtils.createInteger(StringUtils.trim(passStr)) == null) {
				return null;
			}
		} catch(NumberFormatException nfe) { // if either cycle or pass are un-processable, then return null
			AdapterLogger.LogWarning(this.className+ " cycle or pass string can not be converted to Integer :" + UMMUtils.getStackTraceAsString(nfe));
			throw nfe;
		}

		ArrayList<TrackPassTileType>trackPassTileTypes = new ArrayList<>();
		TrackType trackType = null;
		if (StringUtils.isNotEmpty(passStr)) {
			ArrayList<String>tiles = null;
			if(StringUtils.isNotEmpty(tileStr)) {
				tiles = new ArrayList<>();
				tiles.add(tileStr);
			}
			TrackPassTileType trackPassTileType =
					ummgPojoFactory.createTrackPassTileType(NumberUtils.createInteger(UMMUtils.removeStrLeadingZeros(passStr)), tiles);
			trackPassTileTypes.add(trackPassTileType);
		}
		if (StringUtils.isNotEmpty(cycleStr)) {
			trackType = ummgPojoFactory.createTrackType(NumberUtils.createInteger(UMMUtils.removeStrLeadingZeros(cycleStr)), trackPassTileTypes);
		}
		return trackType;
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

		String cycle = StringUtils.trim(xpath.evaluate(ManifestXPath.CYCLE, doc));
		String pass = StringUtils.trim(xpath.evaluate(ManifestXPath.PASS, doc));

		try {  // TrackType is not a required field hence continue execution if any exception happened.
			TrackType trackType = createTrackType(NumberUtils.createInteger(UMMUtils.removeStrLeadingZeros(cycle)),
					NumberUtils.createInteger(UMMUtils.removeStrLeadingZeros(pass)));
			granule.setTrackType(trackType); // No tile so we don't need to convert trackType to AdditionalAttributeType array
		} catch (Exception e) {
			AdapterLogger.LogWarning(this.className + " Continue execution after s6 creating TrackType failed with exception:" + e);
		}

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

	/**
	 * create a TrackType when there is only one cycle and one pass.
	 * no representation of tiles.
	 * @param iCycle
	 * @param iPass
	 */
	private TrackType createTrackType(Integer iCycle, Integer iPass) {
		ArrayList<TrackPassTileType> trackPassTileTypes = new ArrayList<>();
		TrackPassTileType trackPassTileType =
				ummgPojoFactory.createTrackPassTileType(iPass, null);
		trackPassTileTypes.add(trackPassTileType);
		TrackType trackType = ummgPojoFactory.createTrackType(iCycle, trackPassTileTypes);
		AdapterLogger.LogDebug("When only one cycle/pass, created TrackType: " + trackType);
		return trackType;
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
        JSONUtils.cleanJSON(granuleJson);
        FileUtils.writeStringToFile(new File(outputLocation), granuleJson.toJSONString());
	}

	public Dataset getDataset(){
		return this.dataset;
	}

	public Granule getGranule(){
		return this.granule;
	}
}

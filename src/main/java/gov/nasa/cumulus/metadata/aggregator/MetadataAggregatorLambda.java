package gov.nasa.cumulus.metadata.aggregator;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import gov.nasa.cumulus.metadata.aggregator.bo.TaskConfigBO;
import gov.nasa.cumulus.metadata.aggregator.factory.TaskConfigFactory;
import gov.nasa.cumulus.metadata.aggregator.processor.DMRPPProcessor;
import gov.nasa.cumulus.metadata.aggregator.processor.FootprintProcessor;
import gov.nasa.cumulus.metadata.aggregator.processor.ImageProcessor;
import gov.nasa.cumulus.metadata.aggregator.processor.RelatedUrlsProcessor;
import gov.nasa.cumulus.metadata.state.MENDsIsoXMLSpatialTypeEnum;
import gov.nasa.cumulus.metadata.state.WorkflowTypeEnum;
import gov.nasa.cumulus.metadata.util.S3Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.lambda.runtime.Context;

import cumulus_message_adapter.message_parser.ITask;
import cumulus_message_adapter.message_parser.AdapterLogger;

public class MetadataAggregatorLambda implements ITask {
	String className = this.getClass().getName();
	private final String region = System.getenv("region");
	private WorkflowTypeEnum workflowType;

	@Override
	public String PerformFunction(String input, Context context) throws Exception {
		S3Utils s3Utils = new S3Utils();
		AdapterLogger.LogDebug(this.className + " PerformFunction Processing:" + input);

		JSONParser jp = new JSONParser();
		JSONObject jo = (JSONObject) jp.parse(input);

		JSONObject config = (JSONObject) jo.get("config");

		String collectionName = (String) config.get("collection");
		String collectionVersion = (String) config.get("version");
		Boolean rangeIs360 = (Boolean) config.get("rangeIs360");
		JSONObject boundingBox = (JSONObject) config.get("boundingBox");
		JSONObject additionalAttributes = (JSONObject) config.get("additionalAttributes");
		/** call the setWorkFlowType by passing in the stateMachine name to set a WorkflowTypeEnum
		 * this will help the logic in postIngestProcess function.
		 */
		this.setWorkFlowType((String) config.get("stateMachine"));
		TaskConfigBO taskConfigBO = TaskConfigFactory.createTaskConfigBO(config);

		String isoRegex = (String) config.get("isoRegex");
		String archiveXmlRegex = (String) config.get("archiveXmlRegex");
		String calValXmlRegex = (String) config.get("calValXmlRegex");
		String granuleId = (String) config.get("granuleId");
		context.getLogger().log("Started processing: " + granuleId);
		String internalBucket = (String) config.get("internalBucket");
		String executionId = (String) config.get("executionId");
		AdapterLogger.LogInfo(this.className + " current execution id:" + executionId);

		JSONArray granules = (JSONArray) jo.get("input"); //payload object
		JSONArray files = (JSONArray) ((JSONObject) granules.get(0)).get("files");

		String postIngestOutputStr = postIngestProcess(input);
		if (StringUtils.isNotEmpty(postIngestOutputStr)) {
			return postIngestOutputStr;
		}

		/*
		Typically should only have 1 file tagged as "data"
		*/
		//data location
		String s3Location = null;
		String stagingDirectory = null;
		for (Object f : files) {
			JSONObject file = (JSONObject) f;
			if (((String) file.get("type")).equals("data")) {
				s3Location = (String) file.get("key");
				stagingDirectory = getStagingDirFromKey((String) file.get("key"));
			}
		}

		/*
		 * go through files and process if it's an .FP or an .MP
		 */
		//for(int i = 0; i< files.size(); i++){

		List<Object> objectList = new ArrayList<Object>();
		String meta = null;
		String footprint = null;
		String iso = null;
		String xfdumanifest = null;
		String archiveXml = null;
		String calValXml = null;
		// the bucket and key for .mp file
		String mpFileBucket = null;
		String mpFileKey = null;
		for (Object f : files) {
			JSONObject file = (JSONObject) f;
			String filename = (String) file.get("fileName");
			String key = (String) file.get("key");

			//String filename = file.getName();
			if (filename.endsWith(".mp")) {
				mpFileBucket = (String) file.get("bucket");
				mpFileKey = (String) file.get("key");
				meta = s3Utils.download(region, mpFileBucket,
						mpFileKey,
						Paths.get("/tmp", filename).toString());
				objectList.add(f);
			} else if (filename.endsWith(".fp")) {
				footprint = s3Utils.download(region, (String) file.get("bucket"), key,
						Paths.get("/tmp", filename).toString());
				objectList.add(f);
			} else if (isoRegex != null && filename.matches(isoRegex)) {
				AdapterLogger.LogDebug(this.className + " download isoRegrex from bucket:" + file.get("bucket") +
						"  key" + file.get("key") + " to:" + Paths.get("/tmp", filename));
				iso = s3Utils.download(region, (String) file.get("bucket"), key,
						Paths.get("/tmp", filename).toString());
			} else if (filename.endsWith(".xfdumanifest.xml")) {
				xfdumanifest = s3Utils.download(region, (String) file.get("bucket"), key,
						Paths.get("/tmp", filename).toString());
			} else if (archiveXmlRegex != null && filename.matches(archiveXmlRegex)) {
				archiveXml = s3Utils.download(region, (String) file.get("bucket"), key,
						Paths.get("/tmp", filename).toString());
			} else if (calValXmlRegex != null && filename.matches(calValXmlRegex)) {
                AdapterLogger.LogDebug(this.className + " download CalVal XML from bucket:" +
                        file.get("bucket") + "  key" + file.get("key") + " to:" + Paths.get("/tmp", filename));
                calValXml = s3Utils.download(region, (String) file.get("bucket"), key,
                        Paths.get("/tmp", filename).toString());
            }
		}

		//remove the fp and mp files from the array
		for (Object o : objectList) {
			files.remove(o);
			//TODO Delete the file from it's place in S3.
			//TODO remove mp,fp files from CMR
		}

		MetadataFilesToEcho mtfe;
        boolean isIsoFile = (iso != null);
		mtfe = new MetadataFilesToEcho(isIsoFile, taskConfigBO.getIsoXMLSpatialTypeHashSet());
		//set the name/granuleId
		mtfe.getGranule().setName(granuleId);
        mtfe.setDatasetValues(collectionName, collectionVersion, rangeIs360, boundingBox, additionalAttributes);
        if (granules != null && granules.size() > 0) {
            mtfe.setGranuleFileSizeAndChecksum(granules);
        }

		if (isIsoFile) {
			try {
                mtfe.readIsoMetadataFile(iso, s3Location);
			} catch (IOException e) {
				AdapterLogger.LogError(this.className + " MetadataFilesToEcho input TRUE read error:" + e.getMessage());
				e.printStackTrace();
				throw e;
			}
		} else {
			try {
				AdapterLogger.LogInfo(this.className + " Creating UMM-G data structure");
				// Reason for giving a list of files: to check which ones are PNG's so we can add in extra metadata
				// Just the s3Location isn't enough
				if (meta != null) mtfe.readCommonMetadataFile(meta, s3Location);
				if (granules != null && granules.size() > 0) {
					mtfe.setGranuleFileSizeAndChecksum(granules);
				}
				if (footprint != null) mtfe.readFootprintFile(footprint);
				if (xfdumanifest != null) mtfe.readSentinelManifest(xfdumanifest);
				if (archiveXml != null) mtfe.readSwotArchiveXmlFile(archiveXml);
				if (calValXml != null) mtfe.readSwotCalValXmlFile(calValXml);
			} catch (IOException | ParseException e) {
				AdapterLogger.LogError(this.className + " MetadataFilesToEcho input FALSE read error:" + e.getMessage());
				e.printStackTrace();
			}
		}

		/**
		 * generate the ummg json object.  Use RelatedUrlsProcessor to append more items to RelateUrls.
		 * See RelatedUrls java doc to know detailed logic to process payload.granules[0].files[]
		 */
		String cmrFilePath = "/tmp/" + granuleId + ".cmr.json";
		try {
			JSONObject granuleJson = mtfe.createJson();
			RelatedUrlsProcessor relatedUrlsProcessor = new RelatedUrlsProcessor();
			granuleJson = relatedUrlsProcessor.appendSubTypes(granuleJson, taskConfigBO, files);
			FileUtils.writeStringToFile(new File(cmrFilePath), granuleJson.toJSONString(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			AdapterLogger.LogError(this.className + " mtfe.writeJson error:" + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		/*
		 * Upload cmr.json to s3 bucket
		 * Add UMM-G file to payload/file objects
		 */
		File cmrFile = new File(cmrFilePath);
		s3Utils.upload(region, internalBucket,
				Paths.get(stagingDirectory, "/" + granuleId + ".cmr.json").toString(),
				cmrFile);

		JSONObject ummFile = new JSONObject();
		ummFile.put("bucket", internalBucket);
		ummFile.put("fileName", granuleId + ".cmr.json");
		ummFile.put("type", "metadata");
		ummFile.put("size", cmrFile.length());
		ummFile.put("key", Paths.get(stagingDirectory, granuleId + ".cmr.json").toString());

		files.add(ummFile);

		AdapterLogger.LogInfo("Cleaning tmp for granule: " + granuleId);
		FileUtils.cleanDirectory(new File("/tmp"));
		//jo = input,config...
		//need to output {granules:[]}

		/**
		 * combining output objects
		 */
		JSONObject returnable = new JSONObject();
		returnable.put("granules", granules);
		AdapterLogger.LogInfo(this.className + " Finished processing with granuleId:" + granuleId);
		/**
		 * remove the .mp file
		 */
		if (StringUtils.isNotEmpty(mpFileBucket) && StringUtils.isNotEmpty(mpFileKey)) {
			AdapterLogger.LogInfo(this.className + " cleaning up .mp file");
			s3Utils.delete(region, mpFileBucket, mpFileKey);
		}
		return returnable.toJSONString();
	}
	/**
	 * get S3 fileStaging direction from S3 full key
	 *
	 * @param key : the s3 key to identify the file location : ex  my-folder-level1/my-folder-level2/fileName.json
	 * @return
	 */
	public String getStagingDirFromKey(String key) {
		String returnS = "";
		String[] tokens = StringUtils.split(key, "/");
		int length = tokens.length;
		String path = StringUtils.replace(key, tokens[length - 1], "");
		if (StringUtils.length(path) == 0 || StringUtils.equals(path, "/")) {
			returnS = "";
		} else {
			if (StringUtils.startsWith(path, "/")) {
				returnS = StringUtils.replaceOnce(path, "/", "");
			} else {
				returnS = path;
			}
		}
		return returnS;
	}

	private String postIngestProcess(String input)
			throws com.vividsolutions.jts.io.ParseException, ParseException,
			NoSuchAlgorithmException, KeyStoreException,
			CertificateException, UnrecoverableKeyException, KeyManagementException, IOException,
			URISyntaxException {
		String output = "";
		// Set CMR related metadata into ECHOResetClientProvider
		CMRLambdaRestClient elrc = buildLambdaRestClient(input);
		setCMRMetadataToProvider(input);
		// From this point, determine if we are going to process dmrpp, footprint or thumbnail image
		if(workflowType == WorkflowTypeEnum.DMRPPWorkflow) {
			Hashtable<String, String> returnVars = getMetaDataHash(elrc, input);
			DMRPPProcessor processor = new DMRPPProcessor();
			output = processor.process(input, returnVars.get("ummgStr"), region,
					(new BigInteger(returnVars.get("revisionId"))).add(new BigInteger("1")).toString());
			return output;
		}
		// From this point, determine if we are going to process Forge workflow
		if(workflowType == WorkflowTypeEnum.ForgeWorkflow) {
			Hashtable<String, String> returnVars = getMetaDataHash(elrc, input);
			FootprintProcessor processor = new FootprintProcessor();
			output = processor.process(input, returnVars.get("ummgStr"), region,
					(new BigInteger(returnVars.get("revisionId"))).add(new BigInteger("1")).toString());
			return output;
		}
		// From this point, determine if we are going to process TIG workflow
		if(workflowType == WorkflowTypeEnum.ThumbnailImageWorkflow) {
			Hashtable<String, String> returnVars = getMetaDataHash(elrc, input);
			ImageProcessor processor = new ImageProcessor();
			output = processor.process(input, returnVars.get("ummgStr"), region,
					(new BigInteger(returnVars.get("revisionId"))).add(new BigInteger("1")).toString());
			return output;
		}
		return output;
	}

	/**
	 * This function initialize the CMRLambdaRestClient to get ready for token production
	 * as well as obtain the useful cmr UMMG str, revision-id
	 *
	 * @param input
	 * @return hashtable : key/value pair of ummG, revision-id and concept-id
	 * @throws ParseException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private Hashtable<String, String> getMetaDataHash(CMRLambdaRestClient elrc, String input)
			throws ParseException, IOException,
			URISyntaxException {
		Hashtable<String, String> returnHash = new Hashtable<>();
		String tkn = elrc.getToken();
		elrc.cleanTempDir();  // clean up the temp working directory where token.json was stored.
		AdapterLogger.LogDebug(this.className + " Launchpad token length:" + StringUtils.length(tkn));
		// Now query the CMR through concept-id (cmrConceptId)
		// cumulus CMR expected input : const revisionId = event.input.cmrRevisionId
		JSONParser jp = new JSONParser();
		JSONObject jo = (JSONObject) jp.parse(input);
		JSONObject config = (JSONObject) jo.get("config");
		String collectionName = (String) config.get("collection");
		String granuleId = (String) config.get("granuleId");
		String provider = (String) config.get("provider");
		String cmrConceptId = this.getConceptId(input);
		String ummgStr = elrc.getGranuleByNativeConceptId(cmrConceptId);
		BigInteger revisionId = elrc.getGranuleRevisionId(provider, collectionName, granuleId);
		returnHash.put("cmrConceptId", cmrConceptId);
		AdapterLogger.LogInfo(this.className + " retrieved CMR concept-id:" + cmrConceptId);
		AdapterLogger.LogInfo(this.className + " retrieved CMR revision-id:" + revisionId);
		returnHash.put("ummgStr", ummgStr);
		returnHash.put("revisionId", revisionId.toString());
		return returnHash;
	}

	/**
	 * To get the cmr concept ID from the input message to subworkflows (ex. forge, tig, dmrpp)
	 * the input could be
	 * 1. coming from main IngestWorkflow, where a field call cmrConceptId will be presented : ex : "cmrConceptId": "G1239806838-POCUMULUS",
	 * 2. coming from bulk ingest, the conceptId will be embedded in cmrLink field within payload.  example below
	 * "payload": {
	 *     "granules": [
	 *       {
	 *         "beginningDateTime": "2021-05-01T06:50:01.000Z",
	 *         "cmrLink": "https://cmr.uat.earthdata.nasa.gov/search/concepts/G1243703485-POCUMULUS.umm_json",
	 *         "collectionId": "MODIS_A-JPL-L2P-v2019.0___2019.0",
	 *         "createdAt": 1656570595208,
	 *         "duration": 36267.1,
	 *         "endingDateTime": "2021-05-01T06:55:00.000Z",
	 *         "error": {
	 *           "Cause": "None",
	 *           "Error": "Unknown Error"
	 *         },
	 * @param input
	 * @return
	 * @throws ParseException
	 */
	public String getConceptId(String input) throws ParseException{
		String conceptId="";
		JSONParser jp = new JSONParser();
		JSONObject jo = (JSONObject) jp.parse(input);
		JSONArray granules = (JSONArray) jo.get("input"); //payload object
		JSONObject granule = (JSONObject) granules.get(0);
		if(granule.containsKey("cmrConceptId")){
			conceptId = granule.get("cmrConceptId").toString();
		} else if (granule.containsKey("cmrLink")){
			String cmrLink = granule.get("cmrLink").toString();
			String[] tokens = StringUtils.split(cmrLink, '/');
			String lastToken = tokens[tokens.length -1]; //last token ex. G1243703485-POCUMULUS.umm_json
			String[] conceptIdTokens= StringUtils.split(StringUtils.trim(lastToken), '.');
			conceptId = conceptIdTokens[0];
		}
		return conceptId;
	}

	private CMRLambdaRestClient buildLambdaRestClient(String input)
			throws NoSuchAlgorithmException, KeyStoreException,
			CertificateException, UnrecoverableKeyException, KeyManagementException, IOException,
			URISyntaxException {
		CMRLambdaRestClient elrc = CMRRestClientProvider.getClient(
				input, System.getenv().getOrDefault("LAUNCHPAD_CRYPTO_DIR", ""),
				System.getenv().getOrDefault("CMR_URL", ""),
				this.region,
				System.getenv().getOrDefault("LAUNCHPAD_TOKEN_BUCKET", ""),
				System.getenv().getOrDefault("LAUNCHPAD_TOKEN_FILE", ""));
		return elrc;
	}

	/**
	 * Take cumulus adapter input message as input and decode the meta.CMR object to set
	 * CMR related metadata into CMRRestClientProvider
	 * <p>
	 * In the future, all CMR related metadata should be "contained" by CMRRestClientProvider
	 *
	 * @param input Message adapter input string.  Mapped through step function workflow
	 * @throws ParseException
	 */
	public void setCMRMetadataToProvider(String input)
			throws ParseException {
		JSONParser jp = new JSONParser();
		JSONObject jo = (JSONObject) jp.parse(input);
		JSONObject config = (JSONObject) jo.get("config");
		String provider = (String) config.get("provider");
		CMRRestClientProvider.setProvider(provider);
	}

	public void setWorkFlowType(String stateMachine) throws ParseException {
		AdapterLogger.LogInfo("current state machine:" + stateMachine);
		if(StringUtils.endsWithIgnoreCase(stateMachine, "IngestWorkflow")) {
			workflowType = WorkflowTypeEnum.IngestWorkflow;
		} else if(StringUtils.endsWithIgnoreCase(stateMachine, "DMRPPWorkflow")) {
			workflowType = WorkflowTypeEnum.DMRPPWorkflow;
		} else if (StringUtils.endsWithIgnoreCase(stateMachine, "ThumbnailImageWorkflow")) {
			workflowType = WorkflowTypeEnum.ThumbnailImageWorkflow;
		} else if (StringUtils.endsWithIgnoreCase(stateMachine, "ForgeWorkflow")) {
			workflowType = WorkflowTypeEnum.ForgeWorkflow;
		} else  {
			workflowType = WorkflowTypeEnum.NONE;
		}
		AdapterLogger.LogInfo("Current workflow type: " + workflowType);
	}

}

package gov.nasa.cumulus.metadata.aggregator;

import com.google.gson.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import cumulus_message_adapter.message_parser.AdapterLogger;
import gov.nasa.cumulus.metadata.umm.adapter.UMMGCollectionAdapter;
import gov.nasa.cumulus.metadata.umm.adapter.UMMGListAdapter;
import gov.nasa.cumulus.metadata.umm.adapter.UMMGMapAdapter;
import gov.nasa.cumulus.metadata.umm.generated.AdditionalAttributeType;
import gov.nasa.cumulus.metadata.util.BoundingTools;
import gov.nasa.cumulus.metadata.util.JSONUtils;
import gov.nasa.cumulus.metadata.util.TimeConversion;
import gov.nasa.cumulus.metadata.exception.GEOProcessException;
import gov.nasa.cumulus.metadata.umm.model.UMMGranuleArchive;
import gov.nasa.podaac.inventory.api.Constant;

import gov.nasa.podaac.inventory.model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains UMM granule object.
 *
 * @author nchung
 */
public class UMMGranuleFile {
    String className = this.getClass().getName();

    public static final String PROVIDER_DATA_SOURCE = "ProviderDataSource";

    private static final String UMM_VERSION = Constants.UMMG_VERSION;
    private static final String UMM_URL = "https://cdn.earthdata.nasa.gov/umm/granule/v"+Constants.UMMG_VERSION;

    private static Log log = LogFactory.getLog(UMMGranuleFile.class);
    private Granule granule;
    private Dataset dataset;
    private boolean rangeIs360;
    private String nativeId;
    private JSONObject granuleJson;
    /**
     * if processed the footprint through S6 manifest xml's <lilne></lilne> tag
     */
    boolean isLineFormattedPolygon = false;

    public UMMGranuleFile(Granule granule, Dataset dataset, boolean rangeIs360) {
        this.granule = granule;
        this.dataset = dataset;
        this.rangeIs360 = rangeIs360;
    }

    public JSONObject defineGranule()
    throws URISyntaxException, IOException, ParseException{
        JSONObject granuleJson = new JSONObject();
        granuleJson.put("GranuleUR", granule.getName());
        this.nativeId = granule.getName();

        JSONArray providerDates = new JSONArray();
        granuleJson.put("ProviderDates", providerDates);

        JSONObject insertDate = new JSONObject();
        insertDate.put("Date", TimeConversion.convertDate(granule.getIngestTime()).toString());
        insertDate.put("Type", "Insert");
        providerDates.add(insertDate);

        // Populate revision dates
        // set this to current date
        JSONObject updateDate = new JSONObject();
        updateDate.put("Date", TimeConversion.convertDate(new Date()).toString());
        updateDate.put("Type", "Update");
        providerDates.add(updateDate);

        // Populate Collection metadata
        JSONObject collectionReference = new JSONObject();
        granuleJson.put("CollectionReference", collectionReference);

        collectionReference.put("ShortName", dataset.getShortName());
        // PODAAC-1440: moved duplicate code as part of UMM conversion
        collectionReference.put("Version", UMMUtils.getDatasetVersion(dataset));

        // Populate Data Granule
        granuleJson.put("DataGranule", exportDataGranule());

        // Populate the temporal metadata
        granuleJson.put("TemporalExtent", exportTemporal());

        // Populate the Spatial metadata
        granuleJson.put("SpatialExtent", exportSpatial());

        /**
         * AdditionalAttributes
         */
        if(((UMMGranule) granule).getAdditionalAttributeTypes() != null &&
                ((UMMGranule) granule).getAdditionalAttributeTypes().size() >0
        ) {
            granuleJson.put("AdditionalAttributes",
                    createAdditionalAttributes((UMMGranule) granule));
        }

        /**
         * Populate the Orbital Metadata
         * exportOrbitCalculatedSpatialDomain() was designed in a way if no orbit information is found, then,
         * return a JSONArray with zero size.  In case the zero size, OrbitCalculatedSpatialDomains should not be
         * constructed within UMM-G to prevent JSON schema validation error.
         *
         * The following code also remove OrbitCalculatedSpatialDomains before appending.
         */
        JSONArray orbitArray = exportOrbitCalculatedSpatialDomain();
        if(orbitArray.size() >0) {
            granuleJson.remove("OrbitCalculatedSpatialDomains");
            granuleJson.put("OrbitCalculatedSpatialDomains", orbitArray);
        }

        // Populate the resource information.
        granuleJson.put("RelatedUrls", exportAccessURL(granule.getGranuleReferenceSet()));

        JSONObject metadataSpecification = new JSONObject();
        metadataSpecification.put("URL", UMM_URL);
        metadataSpecification.put("Name", "UMM-G");
        metadataSpecification.put("Version", UMM_VERSION);
        granuleJson.put("MetadataSpecification", metadataSpecification);

        // populate AdditionalAttributes
        JSONArray additionalAttribs = exportAdditional();
        if (!additionalAttribs.isEmpty())
            granuleJson.put("AdditionalAttributes", additionalAttribs);

        // directly populate fields if in special additional field mapper
        // i.e. no need to convert to field in object, just take whatever user inputs and add to JSON
        // Refer to test testReadIsoMendsMetadataFileAdditionalFields_appendFieldToJSON in MetadataFilesToEchoTest.java to see this code in action
        JSONObject attributeMapping = ((UMMGranule) granule).getDynamicAttributeNameMapping();
        if(((UMMGranule) granule).getDynamicAttributeNameMapping() != null
                && !((UMMGranule) granule).getDynamicAttributeNameMapping().isEmpty()){
            //key to save into granuleJson
            for (String key : (Iterable<String>) attributeMapping.keySet()) {
                String mappedKey = attributeMapping.get(key) instanceof String ? (String) attributeMapping.get(key) : null; //this is the name we're looking for in additionalAttributes
                Optional<AdditionalAttributeType> filteredAdditionalAttributeType = ((UMMGranule) granule).getAdditionalAttributeTypes()
                        .stream()
                        .filter(e -> e.getName().equals(mappedKey))
                        .findFirst();
                AdditionalAttributeType foundFilteredAdditionalAttributeType = filteredAdditionalAttributeType.orElse(null);
                if (foundFilteredAdditionalAttributeType != null) {
                    // Cast value as Int or String, determine it
                    if(UMMUtils.isNumeric(foundFilteredAdditionalAttributeType.getValues().get(0))){
                        double value = Double.parseDouble(foundFilteredAdditionalAttributeType.getValues().get(0));
                        if(value % 1 == 0)
                            // Convert to flat int (no 45.0, just 45 in JSON)
                            granuleJson.put(key, (int) value);
                        else
                            // Keep as double (445.323)
                            granuleJson.put(key, value);
                    }else{
                        // Save as String
                        granuleJson.put(key, foundFilteredAdditionalAttributeType.getValues().get(0));
                    }
                }
            }
        }

        if (granule instanceof IsoGranule) {
            // Populate MeasuredParameters
            JSONArray measuredParameters = new JSONArray();
            if (((IsoGranule) granule).getParameterName() != "") {
                measuredParameters.add(exportQAStats((IsoGranule) granule));
                granuleJson.put("MeasuredParameters", measuredParameters);
            }

            // Populate Platforms
            granuleJson.put("Platforms", exportPlatform());

            // Export input granules
            if (!((IsoGranule) granule).getInputGranules().isEmpty()) {
                JSONArray inputGranules = new JSONArray();
                for (String inputGranule : ((IsoGranule) granule).getInputGranules()) {
                    inputGranules.add(inputGranule);
                }
                granuleJson.put("InputGranules", inputGranules);
            }

            // Export PGEVersionClass
            if (((IsoGranule) granule).getPGEVersionClass() != "") {
                granuleJson.put("PGEVersionClass", exportPGEVersionClass());
            }
        }
        /**
         * Only when having gone through S6A Line to Polygon processing, then call UMMGPostProcessing.
         */
        if(this.isLineFormattedPolygon) {
            granuleJson = UMMGPostProcessing(granuleJson);
        }

        return granuleJson;
    }

    /**
     * In this function, UMMG is POST to CMR /validate endpoint.  Only when responding Spatial error, then
     * remove GPolygons structure and add global bounding box.
     *
     * Please be aware that for some S6A (especially Level 0) collections are configured to NOT going through
     * universal-data-hander (meta.workflowChoice.readDataFileForMetadata = false) , will have empty RelatedUrls
     * array structure.  In this case, we append a fake item RelatedUrls.
     * @param granuleJson
     * @return
     * @throws ParseException
     * @throws IOException
     * @throws URISyntaxException
     */
    private JSONObject UMMGPostProcessing(JSONObject granuleJson)
    throws ParseException, IOException, URISyntaxException{
        // remove GPolygon in case the polygon came from line formatted xml and spatial validation failed
        AdapterLogger.LogDebug(this.className + " final UMMG json:" + granuleJson.toJSONString());
        boolean shouldAppendRelatedUrl = isMissingRelatedUrls(granuleJson);
        if(shouldAppendRelatedUrl) {
            granuleJson = appendFakeRelatedUrl(granuleJson);
        }
        if(!isSpatialValid(granuleJson)) {
            AdapterLogger.LogDebug(this.className + " Spatial validation error.  Putting in global bounding box");
            JSONObject spatialJSON = (JSONObject) granuleJson.get("SpatialExtent");
            //In case this gets more complicated, we could introduce JSONPath library to find object
            if(spatialJSON!=null) {
                JSONObject horizontalSpatialDomain = (JSONObject)spatialJSON.get("HorizontalSpatialDomain");
                if(horizontalSpatialDomain !=null) {
                    JSONObject geometry = (JSONObject)horizontalSpatialDomain.get("Geometry");
                    if(geometry != null) {
                        geometry.remove("GPolygons");
                        geometry = addGlobalBoundingBox2Geometry(geometry);
                    }
                }
            }
        }
        // if we appended fake relatedUrl, then remove it.
        granuleJson = shouldAppendRelatedUrl? removeAllRelatedUrls(granuleJson):granuleJson;

        return granuleJson;
    }

    private boolean isMissingRelatedUrls(JSONObject granuleJson) {
        JSONArray relatedUrls = (JSONArray) granuleJson.get("RelatedUrls");
        if(relatedUrls == null || relatedUrls.size() ==0) {
            return true;
        }
        else {
            return false;
        }
    }

    private JSONObject appendFakeRelatedUrl(JSONObject granuleJson) {
        JSONArray relatedUrls = (JSONArray) granuleJson.get("RelatedUrls");
        JSONObject urlObj = new JSONObject();
        urlObj.put("URL", "https://xxx/xxx");
        urlObj.put("Description", "fake url");
        urlObj.put("Type", "GET DATA");
        relatedUrls = relatedUrls==null ? new JSONArray():relatedUrls;
        relatedUrls.add(urlObj);
        return granuleJson;
    }

    private JSONObject removeAllRelatedUrls(JSONObject granuleJson) {
        granuleJson.put("RelatedUrls", new JSONArray());
        return granuleJson;
    }

    private JSONArray exportOrbitCalculatedSpatialDomain() {
        JSONArray orbitDomains = new JSONArray();
        JSONObject orbitJsonObject = new JSONObject();
        UMMGranule ummGranule = null;
        if(this.granule instanceof gov.nasa.cumulus.metadata.aggregator.UMMGranule) {
            ummGranule = (UMMGranule) granule;
            Integer orbitNumber = ummGranule.getOrbitNumber();

            if (orbitNumber != null) {
                orbitJsonObject.put("OrbitNumber", orbitNumber);
            }

            String equatorCrossingDateTime = ummGranule.getEquatorCrossingDateTime();
            if (equatorCrossingDateTime != null) {
                orbitJsonObject.put("EquatorCrossingDateTime", equatorCrossingDateTime);
            }

            BigDecimal equatorCrossingLongitude = ummGranule.getEquatorCrossingLongitude();
            if (equatorCrossingLongitude != null) {
                orbitJsonObject.put("EquatorCrossingLongitude", equatorCrossingLongitude);
            }

            if (!orbitJsonObject.isEmpty()) {
                orbitDomains.add(orbitJsonObject);
            }
            if (ummGranule.getStartOrbit() != null && ummGranule.getEndOrbit() != null) {
                orbitJsonObject.put("BeginOrbitNumber", ummGranule.getStartOrbit());
                orbitJsonObject.put("EndOrbitNumber", ummGranule.getEndOrbit());
                orbitDomains.add(orbitJsonObject);
            }
        }
        return orbitDomains;
    }
    // Populate Data Granule
    private JSONObject exportDataGranule() {
        JSONObject dataGranule = new JSONObject();
        //echoDataGranule.setLocalVersionId(String.valueOf(granule.getVersion()));
        dataGranule.put("ProductionDateTime", TimeConversion.convertDate(granule.getCreateTime()).toString());
        dataGranule.put("DayNightFlag", "Unspecified");

        // TODO determine DayNightFlag from metadata

        JSONArray archiveArray = new JSONArray();
        dataGranule.put("ArchiveAndDistributionInformation", archiveArray);

        Set<GranuleArchive> archiveSet = granule.getGranuleArchiveSet();
        for (GranuleArchive archive : archiveSet) {
            JSONObject archiveJson = new JSONObject();
            archiveJson.put("Name", archive.getName());
            AdapterLogger.LogInfo(this.className + " Granule Archive Name: " + archive.getName());
            JSONObject checksum = new JSONObject();
            if (archive instanceof IsoGranuleArchive) {
                checksum.put("Value", archive.getChecksum());
                checksum.put("Algorithm", getUmmChecksumAlgorithm(((IsoGranuleArchive) archive).getChecksumAlgorithm()));
                archiveJson.put("Size", archive.getFileSize());
                archiveJson.put("SizeUnit", ((IsoGranuleArchive) archive).getSizeUnit());
                archiveJson.put("Checksum", checksum);
                archiveJson.put("MimeType", ((IsoGranuleArchive) archive).getMimeType());
                archiveJson.put("Format", archive.getType());
            }
            else if(archive instanceof UMMGranuleArchive){ // UMM-G format
                UMMGranuleArchive ummArchive = (UMMGranuleArchive)archive;
                archiveJson.put("Size", archive.getFileSize() / (1024. * 1024));
                archiveJson.put("SizeUnit", "MB");
                archiveJson.put("SizeInBytes", archive.getFileSize());
                if (ummArchive.getChecksum() != null) {
                    checksum.put("Value", ummArchive.getChecksum());
                    checksum.put("Algorithm", getUmmChecksumAlgorithm(ummArchive.getChecksumAlgorithm()));
                    archiveJson.put("Checksum", checksum);
                }
            } else {
                archiveJson.put("Size", archive.getFileSize() / (1024. * 1024));
                archiveJson.put("SizeUnit", "MB");
                archiveJson.put("SizeInBytes", archive.getFileSize());
            }
            archiveArray.add(archiveJson);
        }
        AdapterLogger.LogInfo(this.className + " Granule ArchiveAndDistributionInformation array: " + archiveArray);
        if (granule instanceof IsoGranule) {
            // Export Identifiers
            JSONArray identifiers = new JSONArray();

            JSONObject producerGranuleId = new JSONObject();
            producerGranuleId.put("Identifier", ((IsoGranule) granule).getProducerGranuleId());
            producerGranuleId.put("IdentifierType", "ProducerGranuleId");
            identifiers.add(producerGranuleId);

            String cridVal = ((IsoGranule) granule).getCrid();
            if (cridVal != "") {
                JSONObject crid = new JSONObject();
                crid.put("Identifier", cridVal);
                crid.put("IdentifierType", "CRID");
                identifiers.add(crid);
            }

            HashMap<String, String> identifiersMap = ((IsoGranule) granule).getIdentifiers();
            for (String key : identifiersMap.keySet()) {
                JSONObject identifier = new JSONObject();
                identifier.put("Identifier", identifiersMap.get(key));
                identifier.put("IdentifierType", "Other");
                identifier.put("IdentifierName", key);
                identifiers.add(identifier);
            }

            dataGranule.put("Identifiers", identifiers);

            // Export reprocessing planned
            dataGranule.put("ReprocessingPlanned", ((IsoGranule) granule).getReprocessingPlanned());

            // Export reprocessing actual
            // dataGranule.put("ReprocessingActual", ((IsoGranule) granule).getReprocessingActual());
        }

        return dataGranule;
    }

    // Populate the temporal metadata
    private JSONObject exportTemporal() {
        JSONObject temporal = new JSONObject();
        JSONObject range = new JSONObject();
        range.put("BeginningDateTime", TimeConversion.convertDate(granule.getStartTime()).toString());
        range.put("EndingDateTime", TimeConversion.convertDate(granule.getStopTime()).toString());
        temporal.put("RangeDateTime", range);
        return temporal;
    }

    private boolean shouldAddBBx(Granule granule) {
        boolean shouldAddBBx = false;
        if(granule !=null && granule instanceof  gov.nasa.cumulus.metadata.aggregator.UMMGranule) {
            shouldAddBBx = true;
        }
        // if the granule object is IsoGranule type and it is SMAP mission, then we check if polygon was added.
        // if polygon was part of geometry (added to SpatialExtent), then bounding box should not be included in
        // SpacialExtend
        if(granule instanceof  gov.nasa.cumulus.metadata.aggregator.IsoGranule && ((IsoGranule)granule).getIsoType() == IsoType.SMAP
        &&  StringUtils.isNotEmpty(((IsoGranule) granule).getPolygon())) {
                shouldAddBBx = false;
        }
        return shouldAddBBx;
    }

    private JSONObject exportSpatial() throws ParseException{
        JSONObject spatialExtent = new JSONObject();
        JSONObject geometry = new JSONObject();
        JSONObject horizontalSpatialDomain = new JSONObject();
        Boolean foundOrbitalData = false;
        spatialExtent.put("HorizontalSpatialDomain", horizontalSpatialDomain);

        if (granule instanceof IsoGranule) {
            String polygon = ((IsoGranule) granule).getPolygon();
            if (polygon != "" && polygon != null) {
                // Export Polygon
                addPolygon(geometry, polygon);
            }
            // Export Orbit
            // Commented out for now since UMM v1.5 only allows for either Geometry or Orbit not both
            JSONObject orbit = new JSONObject();
            horizontalSpatialDomain.put("Orbit", orbit);
            Pattern p = Pattern.compile("AscendingCrossing:\\s?(.*)\\s?StartLatitude:\\s?(.*)\\s?StartDirection:\\s?(.*)\\s?EndLatitude:\\s?(.*)\\s?EndDirection:\\s?(.*)");
            Matcher m = p.matcher(((IsoGranule) granule).getOrbit());
            foundOrbitalData = m.find();
            if (foundOrbitalData && BoundingTools.allParsable(m.group(1), m.group(2), m.group(4))) {
                orbit.put("AscendingCrossing", UMMUtils.longitudeTypeNormalizer(Double.parseDouble(m.group(1))));
                orbit.put("StartLatitude", Double.parseDouble(m.group(2)));
                orbit.put("StartDirection", m.group(3).trim());
                orbit.put("EndLatitude", Double.parseDouble(m.group(4)));
                orbit.put("EndDirection", m.group(5).trim());
            }

            // Export track
            if (((IsoGranule) granule).getSwotTrack() != "") {
                JSONObject track = new JSONObject();
                horizontalSpatialDomain.put("Track", track);
                Pattern trackPattern = Pattern.compile("Cycle:\\s(.*)\\sPass:\\s(.*)\\sTile:\\s(.*)");
                Matcher trackMatcher = trackPattern.matcher(((IsoGranule) granule).getSwotTrack());
                if (trackMatcher.find()) {
                    track.put("Cycle", Integer.parseInt(trackMatcher.group(1)));
                    JSONArray passes = new JSONArray();
                    track.put("Passes", passes);

                    String passString = trackMatcher.group(2);
                    if (passString.contains("-")) {
                        String[] passNumbers = passString.split("-");
                        int beginPass = Integer.parseInt(passNumbers[0]);
                        int endPass = Integer.parseInt(passNumbers[1]);
                        for (int i = beginPass; i <= endPass; i++) {
                            JSONObject pass = new JSONObject();
                            pass.put("Pass", i);
                            pass.put("Tiles", parseTrackTiles(trackMatcher.group(3)));
                            passes.add(pass);
                        }
                    } else {
                        JSONObject pass = new JSONObject();
                        pass.put("Pass", Integer.parseInt(trackMatcher.group(2)));
                        pass.put("Tiles", parseTrackTiles(trackMatcher.group(3)));
                        passes.add(pass);
                    }
                }
            }
        }

        // We can only include orbital or bounding-box data, not both
        if (foundOrbitalData == false) {

            horizontalSpatialDomain.put("Geometry", geometry);

            JSONArray boundingRectangles = new JSONArray();
            geometry.put("BoundingRectangles", boundingRectangles);

            double north = 0, south = 0, east = 0, west = 0;
            if(granule !=null && granule instanceof  gov.nasa.cumulus.metadata.aggregator.UMMGranule) {
                east = ((UMMGranule) granule).getBbxEasternLongitude() != null ?
                        ((UMMGranule) granule).getBbxEasternLongitude() : 0;
                west = ((UMMGranule) granule).getBbxWesternLongitude() != null?
                        ((UMMGranule) granule).getBbxWesternLongitude() : 0;
                north = ((UMMGranule) granule).getBbxNorthernLatitude() != null?
                        ((UMMGranule) granule).getBbxNorthernLatitude() : 0;
                south = ((UMMGranule) granule).getBbxSouthernLatitude() != null?
                        ((UMMGranule) granule).getBbxSouthernLatitude() : 0;
            } else {
                Set<GranuleReal> grs = granule.getGranuleRealSet();

                for (GranuleReal gr : grs) {
                    switch (gr.getDatasetElement().getElementDD().getElementId()) {
                        case 28:
                            west = gr.getValue();
                            break;
                        case 15:
                            north = gr.getValue();
                            break;
                        case 8:
                            east = gr.getValue();
                            break;
                        case 23:
                            south = gr.getValue();
                            break;
                    }
                }
            }

            // first, check to see if any of the spatial values are bad/invalid
            if (BoundingTools.coordsInvalid(north, south, east, west)) {
                log.warn("Bounding coordinates invalid: \'North: " + north +
                        ", \'South: " + south +
                        ", \'West: " + west +
                        ", \'East: " + east +
                        "\' using placeholder values for bounding box.");
                // If so, use the placeholder values over the south pole
                west = -180.0;
                east = -179.0;
                north = -89.0;
                south = -90.0;
                // and make sure we turn off the rangeIs360 flag
                this.rangeIs360 = false;
            }

            BigDecimal nrth = new BigDecimal(north);
            BigDecimal sth = new BigDecimal(south);
            nrth = nrth.setScale(3, RoundingMode.HALF_UP);
            sth = sth.setScale(3, RoundingMode.HALF_UP);

            BigDecimal wst = BigDecimal.valueOf(west);
            BigDecimal est = BigDecimal.valueOf(east);
            wst = wst.setScale(3, RoundingMode.HALF_UP);
            est = est.setScale(3, RoundingMode.HALF_UP);


            // If we get here, it means we have valid values for spatial data, so continue
            // with the spatial extent export.
            if (rangeIs360 && shouldAddBBx(granule)) {
                BigDecimal bdeast = BoundingTools.convertBoundingVal(est, true);
                BigDecimal bdwest = BoundingTools.convertBoundingVal(wst, true);

                if (bdeast.compareTo(bdwest) == 1) {
                    boundingRectangles.add(createBoundingBoxJson(nrth, sth, BoundingTools.convertBoundingVal(est, true), BoundingTools.convertBoundingVal(wst, true)));
                } else {
                    log.debug("East Longitude is less than West Longitude: separating bounding boxes.");

                    if (bdwest.compareTo(BigDecimal.valueOf(180)) != 0) {
                        boundingRectangles.add(createBoundingBoxJson(nrth, sth, BigDecimal.valueOf(180), BoundingTools.convertBoundingVal(wst, true)));
                    }

                    boundingRectangles.add(createBoundingBoxJson(nrth, sth, BoundingTools.convertBoundingVal(est, true), BigDecimal.valueOf(-180)));
                }
            } else if (shouldAddBBx(granule)){
                if (est.doubleValue() >= wst.doubleValue()) {
                    boundingRectangles.add(createBoundingBoxJson(nrth, sth, est, wst));
                } else {
                    log.debug("East Longitude is less than West Longitude: separating bounding boxes.");

                    if (wst.doubleValue() != 180d) {
                        boundingRectangles.add(createBoundingBoxJson(nrth, sth, BigDecimal.valueOf(180), wst));
                    }

                    if (est.doubleValue() != -180d) {
                        boundingRectangles.add(createBoundingBoxJson(nrth, sth, est, BigDecimal.valueOf(-180)));
                    }
                }
            }
        }

        // Export track if cycle and pass exists
        if (granule instanceof UMMGranule) {
            /**
             * Track include cycle and passes(array).
             */
            if(((UMMGranule) granule).getTrackType() != null ) {
                horizontalSpatialDomain.put("Track", createUMMGTrack((UMMGranule) granule));
            }
        }

        // Export footprint if it exists

        Set<GranuleCharacter> granuleCharacters = granule.getGranuleCharacterSet();
        for (GranuleCharacter granuleCharacter : granuleCharacters) {
            if (granuleCharacter.getDatasetElement().getElementDD().getShortName().equals("line")) {
                geometry = line2Polygons(geometry,granuleCharacter.getValue());
                break;
            }
        }
        return spatialExtent;
    }

    public JSONObject createUMMGTrack(UMMGranule ummGranule) throws ParseException {
        Gson gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeHierarchyAdapter(Collection.class, new UMMGCollectionAdapter())
                .registerTypeHierarchyAdapter(List.class, new UMMGListAdapter())
                .registerTypeHierarchyAdapter(Map.class, new UMMGMapAdapter())
                .create();
        JsonObject trackJsonObj = gsonBuilder.toJsonTree(ummGranule.getTrackType()).getAsJsonObject();
        JSONObject track = JSONUtils.GsonToJSONObj(trackJsonObj);
        AdapterLogger.LogInfo("TrackType:" + track.toString());
        return track;
    }

    public JSONArray createAdditionalAttributes(UMMGranule ummGranule) throws ParseException {
        Gson gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeHierarchyAdapter(Collection.class, new UMMGCollectionAdapter())
                .registerTypeHierarchyAdapter(List.class, new UMMGListAdapter())
                .registerTypeHierarchyAdapter(Map.class, new UMMGMapAdapter())
                .create();
        JsonArray additionalAttributes = gsonBuilder.toJsonTree(ummGranule.getAdditionalAttributeTypes()).getAsJsonArray();
        JSONArray jarray = JSONUtils.GsonArrayToJSONArray(additionalAttributes);
        AdapterLogger.LogInfo("AdditionalAttributes: " + jarray.toString());
        return jarray;
    }

    /**
     * This function will first translate the line string to a List<Coordinates>
     *     and add BoundingRectangles to geometry object when
     *     * split function returns 2, or >3 group of coordinates
     *     * The raw polygon given by the line string is NOT valid
     * @param geometry  : A Geometry Json object following UMM-G json schema
     * @param line : A string represents a geometry line type. long lat lon lat .... . (All divided by space)
     */
    public JSONObject line2Polygons(JSONObject geometry, String line) {
        geometry.remove("BoundingRectangles");
        GeometryFactory geometryFactory = new GeometryFactory();
        try {
            ArrayList<Coordinate> coordinates = UMMUtils.lineString2Coordinates(line);
            AdapterLogger.LogDebug("coordinate array size before eliminating:" + coordinates.size());
            coordinates = UMMUtils.eliminateDuplicates(coordinates);
            AdapterLogger.LogDebug("coordinate array size after eliminating:" + coordinates.size());
            // deal with a Line object
            AdapterLogger.LogInfo(this.className + " Original array size:" + coordinates.size());
            Polygon originalPolygon = geometryFactory.createPolygon(coordinates.stream().toArray(Coordinate[]::new));
            AdapterLogger.LogInfo(this.className + " original polygon:" + UMMUtils.getWKT(originalPolygon));
            AdapterLogger.LogInfo(this.className + " original polygon valid? " + originalPolygon.isValid());
            List<List<Coordinate>> splittedGeos = UMMUtils.split(coordinates);
            int dividedSize = splittedGeos.size();
            AdapterLogger.LogInfo(this.className + " original polygon divided to no of geos:" + dividedSize);
            if (UMMUtils.isGlobalBoundingBox(coordinates)) {
                AdapterLogger.LogError(this.className + " Original polygon representing a global bounding box ....");
                geometry = addGlobalBoundingBox2Geometry(geometry);
            } else if (dividedSize == 1 && !originalPolygon.isValid()) {
                // check the original polygon is valid or not ONLY when it is NOT cross dateline.
                AdapterLogger.LogError(this.className + " Original polygon is not valid. Creating global bounding box ....");
                geometry = addGlobalBoundingBox2Geometry(geometry);
            } else if (coordinates.size() >= 3) {
                // 3 coordinate is a triangle - minimum number to construct a polygon
                // This is the block of code where the logic resides to split polygon over dateline
                if (dividedSize == 1) {
                    // not over IDL, create one polygon here
                    // if there is only one polygon and it does not pass the isValid test, then set to global bounding box
                    AdapterLogger.LogInfo(this.className + " Divided to 1 polygon, not over IDL");
                    ArrayList<ArrayList<Coordinate>> polygons = new ArrayList<>();
                    // use the original coordinate array instead of splittedGeos.get(0)
                    // for the original coordinate array is "un-damaged
                    polygons.add(coordinates);
                    geometry = addPolygons(geometry, polygons);
                } else if (dividedSize == 2) {
                    // dont know how to process. Create global bounding box
                    AdapterLogger.LogError(this.className + " split divided to more than 2 geos. Creating global bounding box");
                    geometry = addGlobalBoundingBox2Geometry(geometry);
                }
                if (dividedSize == 3) {
                    // reconstruct 2 polygons divided over IDL
                    AdapterLogger.LogInfo(this.className + " Divided to 3 GEOs, connect 1st and 3rd to polygon");
                    ArrayList<Coordinate> polygon1 = UMMUtils.reconstructPolygonsOver2Lines(
                            (ArrayList<Coordinate>) splittedGeos.get(0), (ArrayList<Coordinate>) splittedGeos.get(2));
                    ArrayList<ArrayList<Coordinate>> polygons = new ArrayList<>();
                    polygons.add(polygon1);
                    polygons.add(UMMUtils.closeUp((ArrayList<Coordinate>) splittedGeos.get(1)));
                    geometry = addPolygons(geometry, polygons);
                } else if (dividedSize > 3) {
                    // donot know how to process, Perhaps throw exception
                    AdapterLogger.LogError(this.className + " split divided to more than 3 geos, ");
                    geometry = addGlobalBoundingBox2Geometry(geometry);
                }
            } else {
                AdapterLogger.LogError("Something is seriously wrong. the line string has less than 3 coordinates");
                throw new GEOProcessException("Something is seriously wrong. the line string has less than 3 coordinates");
            }
        } catch(Exception e)  {
            AdapterLogger.LogFatal("Fatal Error, Line2Polygon Unhandlable error and set to Global BoundingBox:" + e);
            geometry.remove("GPolygons");
            geometry = addGlobalBoundingBox2Geometry(geometry);
        }
        AdapterLogger.LogInfo(this.className + " geometry string:" + geometry);
        return geometry;
    }

    public JSONObject addGlobalBoundingBox2Geometry(JSONObject geometry) {
        JSONArray boundingRectangles = new JSONArray();
        boundingRectangles.add(createBoundingBoxJson(BigDecimal.valueOf(90.0), BigDecimal.valueOf(-90.0), BigDecimal.valueOf(180.0), BigDecimal.valueOf(-180.0)));
        geometry.put("BoundingRectangles", boundingRectangles);
        return geometry;
    }

    public JSONObject addPolygons(JSONObject geometry, ArrayList<ArrayList<Coordinate>> inputPolygons) {
        JSONArray polygons = new JSONArray();
        geometry.put("GPolygons", polygons);
        GeometryFactory geometryFactory = new GeometryFactory();

        for(int i =0; i<inputPolygons.size(); i++) {
            ArrayList<Coordinate> geo = inputPolygons.get(i);

            AdapterLogger.LogInfo(this.className + " Polygon coordinates:" + geo);
            Polygon polygon = geometryFactory.createPolygon(geo.stream().toArray(Coordinate[]::new));
            AdapterLogger.LogInfo(this.className + " Polygon is valid: " + polygon.isValid());
            AdapterLogger.LogInfo(this.className + " Polygon WKT: " + UMMUtils.getWKT(polygon));
            AdapterLogger.LogInfo(this.className + " ------------------------------------------");
            List<Coordinate> counterClockwiseCoordinates = Arrays.asList(
                    UMMUtils.ensureOrientation(CGAlgorithms.COUNTERCLOCKWISE, geo.toArray(new Coordinate[geo.size()]))
                    );

            // valid polygon by vividsolution again
            if(polygon.isValid()) {
                JSONObject gPolygon = new JSONObject();
                JSONObject boundary = new JSONObject();
                gPolygon.put("Boundary", boundary);
                polygons.add(gPolygon);
                JSONArray points = new JSONArray();
                for(int j=0; j<counterClockwiseCoordinates.size(); j++) {
                    JSONObject point = new JSONObject();
                    Coordinate c = counterClockwiseCoordinates.get(j);
                    point.put("Longitude", c.x);
                    point.put("Latitude", c.y);
                    points.add(point);
                }
                boundary.put("Points", points);
            } else {
                // If any polygon is not valid, create a global bounding box and exit
                AdapterLogger.LogInfo(this.className + " Polygon is NOT valid: " + polygon);
                AdapterLogger.LogInfo(this.className + " Polygon WKT: " + UMMUtils.getWKT(polygon));
                AdapterLogger.LogInfo(this.className + " ------------------------------------------");
                geometry.remove("GPolygons");
                geometry = addGlobalBoundingBox2Geometry(geometry);
                break;
            }
        }
        return geometry;
    }

    /**
     * calling the CMRLambdaRestClient function to validate ONLY the spatial data of geometry
     * return true is spatial data valid
     * return false if spatial data not valid
     * throws exception if other data (for instance, other part then spatial data or the UMMG schema) is not valid
     * @param geometry
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public boolean isSpatialValid(JSONObject geometry)
            throws URISyntaxException, IOException, ParseException {
        boolean result =  CMRRestClientProvider.getLambdaRestClient().isUMMGSpatialValid(CMRRestClientProvider.getProvider(),
                granule.getName(),
                geometry.toJSONString());
        return result;
    }

    private JSONArray parseTrackTiles(String tiles) {
        JSONArray tilesJson = new JSONArray();
        String[] tileRanges = tiles.split(" ");
        for (String tileRange: tileRanges) {
            if (tileRange.contains("-")) {
                String tileCharacter = tileRange.substring(tileRange.length() - 1);
                String[] tileNumbers = tileRange.substring(0, tileRange.length() - 1).split("-");
                int beginTile = Integer.parseInt(tileNumbers[0]);
                int endTile = Integer.parseInt(tileNumbers[1]);
                for (int i = beginTile; i <= endTile; i++) {
                    tilesJson.add(i + tileCharacter);
                }
            } else {
                tilesJson.add(tileRange);
            }
        }
        return tilesJson;
    }

    private JSONArray exportPlatform() {
        JSONArray platforms = new JSONArray();

        for (DatasetSource datasetSource: dataset.getSourceSet()) {
            JSONObject platform = new JSONObject();
            platform.put("ShortName", datasetSource.getDatasetSourcePK().getSource().getSourceShortName());

            JSONArray instruments = new JSONArray();
            JSONObject instrument = new JSONObject();
            instrument.put("ShortName", datasetSource.getDatasetSourcePK().getSensor().getSensorShortName());
            instruments.add(instrument);
            platform.put("Instruments", instruments);

            platforms.add(platform);
        }
        return platforms;
    }

    private JSONObject exportPGEVersionClass() {
        JSONObject PGEVersionClass = new JSONObject();
        Pattern p = Pattern.compile("PGEName:\\s(.*)\\sPGEVersion:\\s(.*)");
        Matcher m = p.matcher(((IsoGranule) granule).getPGEVersionClass());
        if (m.find()) {
            PGEVersionClass.put("PGEName", m.group(1));
            PGEVersionClass.put("PGEVersion", m.group(2));
        }
        return PGEVersionClass;
    }

    private JSONObject createBoundingBoxJson(BigDecimal north, BigDecimal south, BigDecimal east, BigDecimal west) {
        JSONObject boundingBox = new JSONObject();
        boundingBox.put("WestBoundingCoordinate", west);
        boundingBox.put("EastBoundingCoordinate", east);
        boundingBox.put("NorthBoundingCoordinate", north);
        boundingBox.put("SouthBoundingCoordinate", south);
        return boundingBox;
    }

    private static JSONArray exportAccessURL(Set<GranuleReference> referenceSet) {
        JSONArray relatedUrls = new JSONArray();

        HashSet<String> accessURLs = new HashSet<String>();
        for (GranuleReference reference : referenceSet) {

            String type = reference.getType();
            JSONObject relatedUrl = new JSONObject();
            if (!type.startsWith(Constant.LocationPolicyType.ARCHIVE.toString())) {

                if (accessURLs.add(reference.getPath())) {
                    relatedUrl.put("URL", reference.getPath());
                    relatedUrl.put("Type", "GET DATA");
                    log.info("Access URL \"" + reference.getPath() + "\" will be added to granule export information");

                    if (type.endsWith("FTP"))
                        relatedUrl.put("Description", "The FTP location for the granule.");
                    else if (type.endsWith("OPENDAP"))
                        relatedUrl.put("Description", "The OPENDAP location for the granule.");
                    else
                        relatedUrl.put("Description", "The base directory location for the granule.");
                    relatedUrls.add(relatedUrl);
                } else {
                    log.info("Reference/Access URL has already been added: " + reference.getPath());
                    log.info("Access URL will not be added to granule export information");
                }
            }
        }
        return relatedUrls;
    }

    private JSONObject exportQAStats(IsoGranule granule) {
        JSONObject qaStats = new JSONObject();
        qaStats.put("QAPercentMissingData", granule.getQAPercentMissingData());
        qaStats.put("QAPercentOutOfBoundsData", granule.getQAPercentOutOfBoundsData());

        JSONObject parameter = new JSONObject();
        parameter.put("ParameterName", granule.getParameterName());
        parameter.put("QAStats", qaStats);

        return parameter;
    }

    private void addPolygon(JSONObject geometry, String polygon) {
        JSONArray polygons = new JSONArray();
        geometry.put("GPolygons", polygons);

        JSONObject gPolygon = new JSONObject();
        JSONObject boundary = new JSONObject();
        gPolygon.put("Boundary", boundary);
        polygons.add(gPolygon);

        JSONArray points = new JSONArray();
        String[] polygonArray = polygon.split(" ");

        for (int i = 0; i < polygonArray.length; i += 2) {
            JSONObject point = new JSONObject();
            String lon = polygonArray[i + 1];
            String lat = polygonArray[i];
            if (BoundingTools.allParsable(lon, lat)) {
                point.put("Longitude", Double.parseDouble(lon));
                point.put("Latitude", Double.parseDouble(lat));
                points.add(point);
            } else {
                AdapterLogger.LogWarning("Unable to parse polygon, lon: " + lon + ", lat: " + lat);
            }
        }
        boundary.put("Points", points);
    }

    /**
     * Parses line string and adds it to lines array in geometry JSON object.
     * Points are sorted by longitude. Duplicate points are removed.
     *
     * @param geometry JSON Object
     * @param line     String of latitude longitude pairs
     */
    public static void addLine(JSONObject geometry, String line) {
        JSONArray lines = new JSONArray();
        geometry.put("Lines", lines);

        JSONObject boundary = new JSONObject();
        lines.add(boundary);

        JSONArray points = new JSONArray();
        String[] lineArray = line.split(" ");

        Double currLon = null;
        Double currLat = null;
        for (int i = 0; i < lineArray.length - 2; i += 2) {
            Double lon = Double.parseDouble(lineArray[i + 1]);
            if (lon > 180.0)
                lon = lon - 360.0;
            Double lat = Double.parseDouble(lineArray[i]);

            if (!(lon.equals(currLon) && lat.equals(currLat))) {
                JSONObject point = new JSONObject();
                point.put("Longitude", lon);
                point.put("Latitude", lat);
                points.add(point);
            }
            currLon = lon;
            currLat = lat;
        }
        boundary.put("Points", points);
    }

    private JSONArray exportAdditional() {
        JSONArray additionalArray = new JSONArray();
        Set<GranuleCharacter> granuleCharacters = granule.getGranuleCharacterSet();
        for (GranuleCharacter granuleCharacter : granuleCharacters) {
            if (granuleCharacter.getDatasetElement().getElementDD().getShortName().equals(PROVIDER_DATA_SOURCE)) {
                additionalArray.add(createAdditionalAttribute(PROVIDER_DATA_SOURCE, granuleCharacter.getValue()));
                break;
            }
        }
        return additionalArray;
    }

    private JSONObject createAdditionalAttribute(String name, String value) {
        JSONObject attribute = new JSONObject();
        JSONArray values = new JSONArray();
        values.add(value);

        attribute.put("Name", name);
        attribute.put("Values", values);

        return attribute;
    }

    /**
     * Adds hyphen to SHA family checksum if hyphen doesn't exist.
     *
     * @param algorithm checksum algorithm e.g. SHA512
     * @return original algorithm value or algorithm value with hyphen added
     */
    public static String getUmmChecksumAlgorithm(String algorithm) {
        if (algorithm.startsWith("SHA") && !algorithm.contains("-")) {
            return "SHA-" + algorithm.substring(3);
        } else {
            return algorithm;
        }
    }
    /*
        DMAS 5.5 changes/additions
     */

    public void initGranuleJson()
    throws  ParseException, IOException, URISyntaxException {
        this.granuleJson = defineGranule();
    }

    public JSONObject getGranuleJson() {
        return this.granuleJson;
    }

    public String toJSONString() {
        return this.granuleJson.toJSONString();
    }

    public String getGranuleId() {
        return String.valueOf(this.granule.getGranuleId());
    }

    public String getNativeId() {
        return this.nativeId;
    }

    public String getUmmVersion() {
        return this.UMM_VERSION;
    }
}

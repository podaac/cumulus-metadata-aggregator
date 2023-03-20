# Metadata Aggregator Lambda
Table of Content
- [Metadata Aggregator Lambda function](#metadata-aggregator-lambda-function)
  * [Overview](#overview)
  * [Build Manually](#build-manually)
  * [Technical Information](#technical-information)
    + [UMM-G Json to model](#umm-g-json-to-model)

## Overview
* MetadataAggregator is a lambda/ECS function which will aggregate information from different sources
Ex. .xfdumanifest.xml, *.fp
into UMM-G Json and upload UMM-G Json file up to CMR
* This labmda has ability to obtain CMR token through launchpad (CMRLambdaRestClient.java). The high level view of the 
  process is
copy the certificate key to lambda local directory, find certificate passcode through cloud system secret key
and finally use the certificate to request CMR AUTHORIZATION token
* If MetadataAggregator sees the input granule including the .fp (footprint) file. It will read the fp file, use
the CMR AUTHORIZATION token to obtain the latest UMMG json, append footprint information and upload the .cmr.json to internal
bucket
* If MetadataAggregator sees the input granule including the .png (image) files. It will  use
  the CMR AUTHORIZATION token to obtain the latest UMMG json, append image download url and upload the .cmr.json to internal
  bucket
## Build Manually
mvn dependency:copy-dependencies
gradle -x test build

## Technical Information

### UMM-G Json to model
 * As of 2020/12/22, this module(MetadataUpdate) is adopting UMM-G version 1.6 (https://git.earthdata.nasa.gov/projects/EMFD/repos/unified-metadata-model/browse/granule/v1.6/umm-g-json-schema.json)
   However, version 1.6.1 is available.
 * Footprint can be in the form of Line, MultiLine, Polygon, and Multipolygon. Depending on the geometry types of footprint. It should be add to
   either Line of GPolygon object under UMM-G. Please refer to UMM-G json schema at : https://git.earthdata.nasa.gov/projects/EMFD/repos/unified-metadata-model/browse/granule/v1.6/umm-g-json-schema.json
 * Due to UMM-G specify SpatialExtent is one of the top optional components and some of SpatialExtent's sub json components 
   are also optional, it becomes a tedious job to code many Java object!==null conditions before appending Line and GPolygon
   objects under Geometry object and then append Geometry object until SpatialExtent. Not to mention such way of programming is error prone. 
   Hence, adopted Json data  serialization/de-serialization model.  That is, de-serialize json string to Java POJOs and serialize
   POJOs to json string.  The model objects are generated using jsonschema2pojo utility.
 * jsonschema2pojo git project : https://github.com/joelittlejohn/jsonschema2pojo/wiki/Getting-Started#the-maven-plugin  
 * Preparation On Mac machine
   ```aidl
    brew install jsonschema2pojo
    brew install coreutils  // this is to prepare greadlink utility 
    vi jsonschema2pojo  and change readlink to greadlink
``` 

  * Example command go generate models:
    ** -s  source json schema file
    ** -t, --target   target generation directory
    ** -p  target package name
    ** -a  Gson annotation
    ** -c  --generate-constructors
    ** -fdt  --formate-date-time : formate date time to default yyyy-MM-dd'T'HH:mm:ss.SSSZ with UTC
    ** -r  --constructors-required-only : generate constructor for required field only
    ** -R   remove old output 
 ```aidl
  jsonschema2pojo -s ./UMM-G1.6.3.json --target java-gen -p gov.nasa.cumulus.metadata.umm.model  -a GSON -r -fdt true -R
  jsonschema2pojo -s ./UMM-G1.6.3.json --target java-gen -p gov.nasa.cumulus.metadata.umm.model  -a GSON -fdt 
  
  jsonshcema2pojo maven plugin is also configured within the pom.xml file
  mvn compile    // call plugin goal to generate pojo classes
```

 * jsonschema2pojo utility supports ACKSON, JACKSON1, JACKSON2, GSON, and MOSHI1
 * Code maintenance due to UMM-G schema upgrade is also benefit from jsonschema2pojo.  Regenerate the model objects, fix all compilation errors and test.
 * There is no need to run json schema validation after building the new UMM-G string
 * jsonschema2pojo maven plugin is also configured in pom.xml.   Executing mvn compile command will generate the UMM-G schema POJOs


### Lambda Environment Variables

| field name | type | default | values | description
| ---------- | ---- | ------- | ------ | -----------
| CMR_ENVIRONMENT | string | (required) | | CMR envionment which this lambda is connected to: ex. SNDBOX, SIT, UAT, OPS
| stackName | string | (required) | | The prefix of lambda
| CUMULUS_MESSAGE_ADAPTER_DIR | string | (required) | | set to "/opt"
| region | string | (required) | | AWS region where forge lambda is running upon.  Ex. us-west-2
| LAUNCHPAD_CRYPTO_DIR | string | (required) | | directory where certificate file is located under system bucket.
| LAUNCHPAD_TOKEN_BUCKET | string | (required) | | Bucket name where launchpad token is stored
| LAUNCHPAD_TOKEN_FILE | string | (required) | | directory path and full file name of token file under LAUNCHPAD_TOKEN_BUCKET
| CMR_URL | string | (required) | |CMR API Base URL
| layers | list(string) | (required) | | list of layers' arn where forge runs upon.
| INTERNAL_BUCKET | list(string) | (required) | | The bucket name where .cmr.json file will be stored
| DIR | list(string) | (required) | | Base dir of .cmr.json file.  generated ummg file will be stored at s3://INTERNAL_BUCKET/CMR_DIR/collectionName/granuleId.cmr.json
    
* MetadataAggregator lambda environment configuration example

```aidl
CMR_ENVIRONMENT             = var.cmr_environment
stackName                   = var.prefix
CUMULUS_MESSAGE_ADAPTER_DIR = "/opt/"
region                      = var.region
LAUNCHPAD_CRYPTO_DIR        = "${var.prefix}/crypto"
LAUNCHPAD_TOKEN_BUCKET      = var.system_bucket
LAUNCHPAD_TOKEN_FILE        = "${var.prefix}/crypto/token.json"
CMR_URL                     = var.cmr_url
INTERNAL_BUCKET             = var.buckets.internal.name
CMR_DIR                     = "CMR"
```

### Additional Attribute usage and configuration
A special field `additionalAttributes` can be added to the `meta` level inside a collection (`$.meta.collection.meta` from step functions perspective). Once added it'll enable the user to append an ISO.XML's `eos:AdditionalAttribute` data into the CMR.JSON's "AdditionalAttributes" JSON root block

Example Collection Config - Meta section: [cumulus dashboard image](documentation_image/additionalAttributeImage.png)
```json
{
  "meta": {
    "additionalAttributes": {
      "publishAll": false,
      "publish": [
        "PercentCloudCover"
      ],
      "CloudCover": "PercentCloudCover"
    },
    "glacier-bucket": "hryeung-ia-podaac-glacier",
    "granuleMetadataFileExtension": "cmr.json",
    "granuleRecoveryWorkflow": "OrcaRecoveryWorkflow",
    "iso-regex": "^OPERA_L3_DSWx-HLS_.*v([0-9]*)\\.([0-9]*).*\\.iso\\.xml$",
    "response-endpoint": "arn:aws:sns:us-west-2:065089468788:hryeung-ia-podaac-provider-response-sns",
    "workflowChoice": {
      "compressed": false,
      "convertNetCDF": false,
      "dmrpp": false,
      "glacier": false,
      "readDataFileForMetadata": false
    }
  }
}
```
The configuration above has 3 parts to consider
- `"publishAll": true/false` - whether or not to publish EVERYTHING found in the AdditionalAttribute XML block; this field is **required** if an `additionalAttributes` block is added in `meta`; `true` means publish everything
- `"publish": ["NAME_OF_FIELD_IN_XML_ADDITIONAL_ATTRIBUTES"]` - Publish the field from the XML into JSON `additionalAttributes`
- `"CloudCover": "PercentCloudCover"` - Append a key `CloudCover` with the value from `PercentCloudCover` inside the CMR.JSON output

#### Example Collection Configurations
Assume this is the XML
```xml
<?xml version="1.0" encoding="UTF-8"?>
<gmi:MI_Metadata xmlns:gmi="http://www.isotc211.org/2005/gmi" xmlns:eos="http://earthdata.nasa.gov/schema/eos" xmlns:gco="http://www.isotc211.org/2005/gco">
   <eos:AdditionalAttributes>
      <eos:AdditionalAttribute>
         <eos:reference>
            <eos:EOS_AdditionalAttributeDescription>
               <eos:type>
                  <eos:EOS_AdditionalAttributeTypeCode codeList="https://cdn.earthdata.nasa.gov/iso/resources/Codelist/eosCodelists.xml#EOS_AdditionalAttributeTypeCode" codeListValue="qualityInformation">qualityInformation</eos:EOS_AdditionalAttributeTypeCode>
               </eos:type>
               <eos:name>
                  <gco:CharacterString>PercentCloudCover</gco:CharacterString>
               </eos:name>
               <eos:description>
                  <gco:CharacterString>The percentage of cloud and cloud shadow in the L3_DSWx_HLS product based on the HLS QA mask</gco:CharacterString>
               </eos:description>
               <eos:dataType>
                  <eos:EOS_AdditionalAttributeDataTypeCode codeList="https://cdn.earthdata.nasa.gov/iso/resources/Codelist/eosCodelists.xml#EOS_AdditionalAttributeDataTypeCode" codeListValue="int">int</eos:EOS_AdditionalAttributeDataTypeCode>
               </eos:dataType>
            </eos:EOS_AdditionalAttributeDescription>
         </eos:reference>
         <eos:value>
            <gco:CharacterString>76</gco:CharacterString>
         </eos:value>
      </eos:AdditionalAttribute>
      <eos:AdditionalAttribute>
         <eos:reference>
            <eos:EOS_AdditionalAttributeDescription>
               <eos:type>
                  <eos:EOS_AdditionalAttributeTypeCode codeList="https://cdn.earthdata.nasa.gov/iso/resources/Codelist/eosCodelists.xml#EOS_AdditionalAttributeTypeCode" codeListValue="platformInformation">platformInformation</eos:EOS_AdditionalAttributeTypeCode>
               </eos:type>
               <eos:name>
                  <gco:CharacterString>SensorProductID</gco:CharacterString>
               </eos:name>
               <eos:description>
                  <gco:CharacterString>The Landsat product ID or Sentinel L1C granule URI</gco:CharacterString>
               </eos:description>
               <eos:dataType>
                  <eos:EOS_AdditionalAttributeDataTypeCode codeList="https://cdn.earthdata.nasa.gov/iso/resources/Codelist/eosCodelists.xml#EOS_AdditionalAttributeDataTypeCode" codeListValue="string">string</eos:EOS_AdditionalAttributeDataTypeCode>
               </eos:dataType>
            </eos:EOS_AdditionalAttributeDescription>
         </eos:reference>
         <eos:value>
            <gco:CharacterString>LC08_L1TP_027038_20210906_20210915_02_T1; LC08_L1TP_027039_20210906_20210915_02_T1</gco:CharacterString>
         </eos:value>
      </eos:AdditionalAttribute>
   </eos:AdditionalAttributes>
</gmi:MI_Metadata>
```
##### with this collection config
```json
{
  "additionalAttributes": {
    "publishAll": true
  }
}
```
the output CMR.JSON would look like follows
```json
{
  "AdditionalAttributes": [
    {
      "Values": [
        "76"
      ],
      "Name": "PercentCloudCover"
    },
    {
      "Values": [
        "LC08_L1TP_027038_20210906_20210915_02_T1; LC08_L1TP_027039_20210906_20210915_02_T1"
      ],
      "Name": "SensorProductID"
    }
  ]
}
```
##### with this collection config
```json
{
  "additionalAttributes": {
    "publishAll": false,
    "publish": ["PercentCloudCover"]
  }
}
```
the output CMR.JSON would look like follows
```json
{
  "AdditionalAttributes": [
    {
      "Values": [
        "76"
      ],
      "Name": "PercentCloudCover"
    }
  ]
}
```
##### with this collection config
```json
{
  "additionalAttributes": {
    "publishAll": true,
    "CloudCover": "PercentCloudCover"
  }
}
```
the output CMR.JSON would look like follows
```json
{
  "AdditionalAttributes": [
    {
      "Values": [
        "76"
      ],
      "Name": "PercentCloudCover"
    },
    {
      "Values": [
        "LC08_L1TP_027038_20210906_20210915_02_T1; LC08_L1TP_027039_20210906_20210915_02_T1"
      ],
      "Name": "SensorProductID"
    }
  ],
  "CloudCover": 76
}
```
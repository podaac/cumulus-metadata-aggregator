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
* This labmda has ability to obtain CMR token through launchpad (ECHOLambdaRestClient.java). The high level view of the process is
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
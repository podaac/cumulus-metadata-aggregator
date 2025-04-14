# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
### Deprecated
### Changed
- **PODAAC-6729**
   - Added a check coordinates length when checking if footprint points are counter clockwise
### Removed
### Fixed
### Security

## [8.9.0]
### Added
### Deprecated
### Changed
   - Update CHANGELOG.md
### Removed
### Fixed
### Security

## [8.7.0]
### Added
- **PODAAC-6181**
  - add relatedUrl subType BROWSE IMAGE SOURCE
- **PODAAC-6537**
  - New code to work with Multi polygon
### Deprecated
### Changed
- **PODAAC-6185**
  - java 11->17
### Removed
### Fixed
### Security

## [8.6.0]
### Added
- **PODAAC-5876**
  - Update to use CMA 2.0.0, thus allowing 2.0.3 layer for lambda
  - Update build to use java 11
  - To generate java 11 compatible UMMG schema POJOs, jsonschema2pojo shall make use of command line parameter: --target-version 1.11 
- **PODAAC-6169**
  - SWOT iso.xml
  - OPERA iso.xml
  - SWOT archive.xml
  - .mp
  - SWOT CalVal XML
  - Does/does not cross the IDL
### Deprecated
### Removed
### Fixed
- **PODAAC-5857**
  - Fixed the issue so footprint and bbx do not always appear together while isoXmlSpatial is configured to footprint only
  - remove request.close() statement in CMRLambdaRestClient so the response could be pulled out and logged properly instead of always logging apache chunk read error due to the http channel was closed.
  - adding more parameters to Jsonschema2pojo plugin to generate proper pojo from ummg schema
### Security
- Snyk: Security upgrade com.amazonaws:aws-java-sdk-s3 from 1.12.544 to 1.12.641

## [8.5.0]
### Added
- **PODAAC-5594**
  - Support BasinID
- **PODAAC-5770**
  - use meta.isoXMLSpatialType to configure the collection should process the combination of footprint, orbit and bbox
- **PODAAC-5717**
  - Upgrade to UMMG 1.6.5
  - support empty Pass in Cycle/Pass/Tile string
### Deprecated
### Removed
### Fixed
- **PODAAC-5708**
  - .nc.iso.xml Polygon divided over IDL
### Security

- 
## [8.4.0]
### Added
- Update metadata aggregator to add description to image variables from image processor and test tig forge processor
### Deprecated
### Removed
### Fixed
- **PODAAC-5614**
  - Strip leading zeros from cycle and pass in validity check
### Security
- Snyk: Security upgrade com.amazonaws:aws-java-sdk-s3 from 1.12.378 to 1.12.386


## [8.3.0]
### Added
- **PODAAC-4748**
  - Enable metadata aggregator to append ISO.XML -> AdditionalAttribute fields (full set or subset) into CMR.JSON
  - Enable metadata aggregator to append Additional Attribute(s) into CMR.JSON as its own root key
- **PODAAC-5201**
  - Support SWOT Cal/Val XML format
- **PODAAC-5053**
  - Extract MGRS_TILE_ID metadata from iso.xml. Used for OPERA processing
### Deprecated
### Removed
### Fixed
- **PODAAC-4273**
  - Added exclusiive zones for polygon holes and interior rings.
### Security
- Snyk: Security upgrade com.amazonaws:aws-java-sdk-s3 from 1.12.378 to 1.12.386

## [8.2.1]
### Added
### Added
### Deprecated
### Removed
### Fixed
- **PODAAC-5219**
  - NumberUtils.createInteger(String:s) while s has leading zero, the NumberUtils will treat the string as Octal
  - implemented a utility function to remove leading zero.
### Security

## [8.2.0]
### Added
### Deprecated
### Removed
### Fixed
- **PODAAC-5078**
  - Enhanced MetataAggregator to handle incorrectly formatted cycle, pass or tiles
  - fixes maily for SWOT acrhive.xml and SWOT iso.xml
### Security

## [8.1.0]
### Added
- **PODAAC-4721**
  - Enhanced MetataAggregator by adding DMRPP Processor which supports bulk operation
- **PODAAC-4713**
  - fixed metadataAggregator can not extract posList to construct GPolygon issue
  - added logic to exclude bounding box if SMAP GPolygon is appearing under SpatialExtent
  - Upgrade amazon libraries
- **PODAAC-4831**
  - Enhanced metadataAggregator so the swot iso.xml cycle, pass information can be
    marshaled into UMMG's Track object and AdditionalAttributes object
  
### Deprecated
### Removed
### Fixed
- **PODAAC-2796**
  - remove all OPeNDAP URL object from RelatedUrls before doing dmrpp file generator processing
- **PODAAC-4832**
  - remove BoundingRectangles structure from SpatialExtent if GPolygon appears
- **PODAAC-5012**
  - Fixed issue where SWOT provides a PNG and triggers the `postIngestProcess`
### Security

## [8.0.0] - 2022-06-06
### Added
- **PODAAC-4328**
  - Modify the code to be compliant with cumulus 11 input/output new schema
### Deprecated
### Removed
### Fixed
### Security

## [7.0.1] - 2022-05-27
### Added
### Changed
### Deprecated
### Removed
### Fixed
- **PODAAC-4248**
  - Fixed handling of empty DataGranule.Identifiers values in ISO to UMM-G translation
### Security

## [7.0.0] - 2022-05-16
### Added
- **PODAAC-4521**
    - Copied necessary inventory data model classes into project.
### Changed
- **PODAAC-2667**
    - Renamed 'EchoRestClient' to 'CMRRestClient' and related classes accordingly
    - Cleanup legacy distribute code, removed old echo-10 related methods from CMRRestClient
- **PODAAC-4327**
    - Incorporated NSIDC changes to support ISO granule files.
- **PODAAC-4248**
    - When parsing ISO files, only start, stop, and creation time are required. All other fields are optional.
- **PODAAC-4521**
  - Refactored project structure, package names, etc. to better match other cumulus lambdas.
### Deprecated
### Removed
- **PODAAC-2667**
    - Cleanup of legacy distribute code to speed up builds and reduce size of lambda, the following have been removed:
      - distribute-gen module
      - distribute-subscriber module
      - distribute-gcmd module
      - GCMDExport shell script/tool
      - EchoExport shell script/tool
      - All related files to EchoExport and GCMDExport (e.g. EchoExport.java, EchoGranuleFile.java, GCMDExport.java, 
        etc.)
      - All test cases pertaining to the above removed tools (e.g. GCMDExport, ECHOExport, etc.)
- **PODAAC-4521**
    - Refactored project into a single maven project, and removed leftover misc files and classes from 2667.
    - Removed dependencies on inventory-ws and inventory-api.
    - Removed UMM-C related classes, tests, and tools.
### Fixed
### Security
- **PODAAC-4521**
    - Addressed known SNYK vulnerabilities.

## [v6.2.0] - 2022-03-09
### Added
### Changed
- **PODAAC-3555**
    - Switch from 'Echo-Token' http headers to new 'Authorization' header for sending credentials to CMR
### Deprecated
### Removed
### Fixed
### Security
- **Snyk**
  - Upgrade aws-java-sdk-s3@1.12.96 to com.amazonaws:aws-java-sdk-s3@1.12.175

## [v6.1.2] - 2022-01-21
### Added
### Changed
### Deprecated
### Removed
### Fixed
### Security
- **PODAAC-4095**
  - Upgrade to cumulus-message-adapter 1.3.9 to overcome log4j vulnerability

## [v6.1.1] - 2021-12-15
### Added
### Changed
### Deprecated
### Removed
### Fixed
### Security
- **PODAAC-4046**
  - Upgrade to cumulus-message-adapter 1.3.5 to overcome log4j vulnerability

## [v6.1.0] - 2021-11-10
### Added
### Changed
### Deprecated
### Removed
### Fixed
- **PODAAC-3700**
  - fix issue about while east bound == west bound then both are set to zero under boundingRectangle within UMMG
- **PODAAC-4002**
  - fix Metadata Aggregator Lambda fails on Sentinel-6 collections that have workflowChoice.readDataFileForMetadata set to false
### Fixed
- **PODAAC-3970**
  - fix CMR validation error by validate UMMG through CMR /validate endpoint. If spatial error 
    then construct global bounding box
### Security
- **Snyk**
  - Upgrade aws-java-sdk-s3@1.12.14 to com.amazonaws:aws-java-sdk-s3@1.12.96

## [v6.0.1] - 2021-11-09
### Added
### Changed
### Deprecated
### Removed
### Fixed
- **PODAAC-3700**
  - fix issue about while east bound == west bound then both are set to zero under boundingRectangle within UMMG

### Security
- **Snyk**
  - Upgrade aws-java-sdk-s3@1.12.14 to com.amazonaws:aws-java-sdk-s3@1.12.96

## [v6.0.0] - 2021-08-26
### Added
- **PODAAC-3680**
  - Hint UMM-G schema 1.6.3 in the title to enable cumulus to insert "Type": "GET DATA VIA DIRECT ACCESS" for s3 path
  - Upgrade UMM-G mapped pojo to 1.6.3
- **PODAAC-3327**
  - Add BulkUpdate tool for adding s3 links to existing granules.
  - Moved CSV related and other shared code to a new BulkTools library class.
### Changed
### Deprecated
### Removed
### Fixed
- **PODAAC-3726**
    - Workaround stale/duplicate granuleUr's in bulkUpdate tool. Use a hash set to track all GranuleUR's we send an 
      update for, and ignore duplicates.
- **PODAAC-3736**
  - Fix for wrong file being linked in the s3 update by switching to only checking 'GET DATA' urls
### Security

## [v5.11.0] - 2021-07-26
### Added
- **PODAAC-3518**
   - Divide S6 footprint over DateLine
   - Logic to detect if Polygon coordinate is counterclockwise sequence. If not reverse polygon sequence
### Changed
### Deprecated
### Removed
### Fixed
- **PODAAC-3532**
   - Fixed S6 L2P spatial validation error
   - Further fix polygon creation error by using origianl coordinate array if GEO object not cross IDL
   - Implement coordinate array sanitizig function to remove some coordinates when they are too close to each other.
- **PODAAC-3904**
     Add try...catch block for  s6 posList coordinate processing block and set global bounding box for 
     unhandl-able exceptions.  In details, while processing L0 data, the eliminate function cut number coordinates < 4 
     which cause vividsolution to throw creating polygon exception.
  
### Security

## [v5.10.0] - 2021-07-15
### Added
### Changed
- **PODAAC-3484**
  - Only export the PODAAC user services contact information for collections.
- **PODAAC-3157**
    - Hard code placeholder spatial coordinates (over the south pole) for granules with invalid values, e.g. -999, -995, etc.
### Deprecated
### Removed
- **PODAAC-3484**
  - Removed old code to add all contacts associated with a collection, since we are now hard coding values for the 
    PODAAC user services contact
### Fixed
- **PODAAC-3489**
  - Fix CMR Polygon boundary error by using only outer ring 
### Security
- **Snyk**
  - Upgrade aws-java-sdk-s3@1.11.1025 to com.amazonaws:aws-java-sdk-s3@1.12.14

## [v5.9.0] - 2021-05-25
### Added
### Changed
- **PODAAC-3334**
  - read .mp file from internal bucket instead of public bucket
  - simplified clean /tmp process.  Add logic to only delete .mp file if it exists
- **PODAAC-3463**
  - append orbit information to UMM-G json
- **PODAAC-3325**
    - Add DOI export for all datasets, including those wihtout a DOI, using 'MissingReason' and relevant explanation.
### Changed
### Deprecated
### Removed
### Fixed
- **PODAAC-3497**
    - Fixed to not sort on longitude when parsing footprint from S6 xfdumanifest.xml
### Security
- **Snyk**
  - Upgrade aws-java-sdk-s3@1.11.951 to com.amazonaws:aws-java-sdk-s3@1.11.1025

## [v5.8.1] - 2021-05-28
### Added
### Changed
### Deprecated
### Removed
### Fixed
- **PODAAC-3035**
  - Fixed url for launchpad token validation so that tokens are reused. 
### Security

## [v5.8.0] - 2021-04-24
### Added
- **PODAAC-3035**
  - Obtain CMR ECHO token through launchpad
- **PODAAC-3015**
  - read revision-id from CMR
- **PODAAC-3009**
  - add image download URLs to UMMG
- **PODAAC-3174**
  - Query latest UMM-G through CMR, append FP and Image download data then create a new .cmr.json
- **PODAAC-3296**
    - Switch to using dataset short name instead of persistent id when generating EMSReport
### Changed
### Deprecated
### Removed
### Fixed
- **PODAAC-3394**
  - Code enhancement to fix download imageURL case sensitive issue and avoid duplication. 
  - output cmrRevisionId = retrieved cmr revisionId + 1
  - while quering CMR with shortname and granule_ur, provider must be provided, too. 
### Security

## [v5.7.3] - 2021-04-21
### Added
### Changed
- **PODAAC-3296**
    - Switch to using dataset short name instead of persistent id when generating EMSReport
### Deprecated
### Removed
### Fixed
### Security

## [v5.7.2] - 2021-03-16
### Added
### Changed
### Deprecated
### Removed
### Fixed
- **PODAAC-3200**
  - Fixed handling of cycle and pass when exporting to UMM-G. 
### Security

## [v5.7.1] - 2021-02-04
### Added
### Changed
### Deprecated
### Removed
### Fixed
- **PODAAC-3083**
  - Removed duplicate points from line when adding to UMM-G
### Security

## [v5.7.0] - 2021-01-14
### Added
- **PODAAC-2844**
  - Add capability to read footprint file and append to UMM-G output which is .cmr.json
### Changed
- **PODAAC-2637**
    - Ensure the batchExport.sh tool works with ecco 0.5 degree, and Sentinel 6 collections.
    - Updated batchExport to find and update collections that have previously been ingested.
### Deprecated
### Removed
### Fixed
### Security

## [v5.6.7] - 2021-02-04
### Added
### Changed
### Deprecated
### Removed
### Fixed
- **PODAAC-3083**
  - Removed duplicate points from line when adding to UMM-G
### Security

## [v5.6.6] - 2020-12-14
### Added
### Changed
- **PODAAC-2495**
  - Convert SHA512 to SHA-512 in UMM-G.
### Deprecated
### Removed
### Fixed
### Security

## [v5.6.5] - 2020-11-25
### Added
- **PODAAC-2603**
  - Added reading of bounding box values from Cumulus collection meta.
### Changed
- **PODAAC-2781**
  - Updated lambda to get collection short name, version, rangeIs360 from Cumulus collection configuration instead of from meta.cfg file.
### Deprecated
### Removed
### Fixed
### Security
- **Snyk**
  - Upgrade aws-java-sdk-s3@1.11.893 to com.amazonaws:aws-java-sdk-s3@1.11.903

## [v5.6.4] - 2020-11-19
### Added
### Changed
### Deprecated
### Removed
### Fixed
- **PODAAC-2775**
  - Upgrade to CMA-java v1.3.2 to fix timeout on large messages.
### Security

## [v5.6.3] - 2020-11-02

### Added
### Changed
### Deprecated
### Removed
### Fixed
- **PODAAC-2664**
  - Removed duplicate data file from UMM-G DataGranule.ArchiveAndDistributionInformation

- **PODAAC-2546**
  - to build product object in CNMResponse, we need to retrieve data from granule.files.
    Each file's size should be tagged by size instead of fileSize.
### Security
- **PODAAC-2711**
  - Fixed Snyk high severity findings.
  - Upgrade com.amazonaws:aws-java-sdk-s3@1.11.229 to com.amazonaws:aws-java-sdk-s3@1.11.893
  - Upgrade org.apache.httpcomponents:httpclient@4.5.2 to org.apache.httpcomponents:httpclient@4.5.13
  - Upgrade commons-collections:commons-collections@3.2 to commons-collections:commons-collections@3.2.2

## [v5.6.2] - 2020-10-22

### Added

- **PODAAC-2357**
  - Export Level-1/Level-2 dataset spatial resolution metadata in UMM-C 

### Changed
### Deprecated
### Removed
### Fixed

- **PODAAC-2589**
  - Converted dataset.swath_width to Kilometers and updated Instrument SwathWidth units to Kilometers.

- **PODAAC-2654**
  - Fixed null check for dataset.region_detail causing UMM-C export to fail.

### Security

## [v5.6.1] - 2020-10-13

### Added
### Changed
### Deprecated
### Removed
### Fixed
- **PODAAC-2595**
  - Fix to remove empty ProviderDataSource value from UMM-G when sentinel6:productName is missing from XFDU manifest which caused CMR validation failure. 

### Security

## [v5.6.0] - 2020-10-05

Note: This is the first release where we are consolidating DMAS changes with Cumulus changes.

### Added
- **PCESA-2265**
  - Added ProviderDataSource additional attribute to UMM-G whose value is Sentinel-6 SAFE package name

- **PODAAC-2149**
  - Added bulk UMM-C too based on MMT template and CSV collection metadata.

### Changed
- **PCESA-2305**
  - ArchiveAndDistributionInformation structure should be built upon the data from cumulus input granule

- **PODAAC-2399**
  - Added persistent_id and series_name to UMM-C additional attributes.

- **PODAAC-2360**
  - Export data provider resource URL in UMM-C.

- **PODAAC-2359**
  - Added dataset.projection_type and dataset.projection_detail to UMM-C.
  
- **PODAAC-2358**
  - Added Spatial Keywords to UMM-C.

- **PODAAC-2354**
  - Added dataset latency to UMM-C additional attribute.

- **PODAAC-2351**
  - Added Reference to UMM-C.

### Deprecated
### Removed
### Fixed
### Security

## [v5.5.3] - 2020-10-02

### Added
### Changed
### Deprecated
### Removed
### Fixed

- **PODAAC-2416**
  - Not all of the sensors are getting exported in UMM-C

### Security

## [5.2.9] - 2020-07-06

### Added

- **PCESA-2205**
  - Added parsing of SWOT archive.xml metadata file into UMM-G.

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [5.2.8] - 2020-06-29

### Added

### Changed

- **PCESA-2166**
  - Updated lambda to use CMA AdapterLogger for logging to Elasticsearch.

### Deprecated

### Removed

### Fixed

### Security

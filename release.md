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
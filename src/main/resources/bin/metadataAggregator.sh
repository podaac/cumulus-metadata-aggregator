#!/bin/sh

# Make the classpath:

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"


java -Xmx512m -cp "$DIR/../lib/*" -Dlog4j.configuration=file://$DIR/../config/metadataaggregator.log.properties \
        gov.nasa.cumulus.metadataaggregator.aggregator.MetadataAggregatorCLI $*

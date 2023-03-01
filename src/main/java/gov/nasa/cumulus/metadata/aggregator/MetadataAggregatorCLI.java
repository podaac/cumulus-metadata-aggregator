package gov.nasa.cumulus.metadata.aggregator;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.simple.parser.ParseException;

/**
 * TODO: CLI might not be working! (or not up to date compared to lambda version)
 * Command-line application for generating CMR metadata file
 */
public class MetadataAggregatorCLI {

    private static Log log = null;
    private static Options options = init();

    private static Options init() {
        Options options = new Options();
        options.addOption(OptionBuilder.withArgName("file")
                .hasArg(true)
                .withLongOpt("config")
                .withDescription("Path to collection config file")
                .create("c"));
        options.addOption(OptionBuilder.withArgName("file")
                .hasArg(true)
                .withLongOpt("metadata")
                .withDescription("Path to metadata .mp file")
                .create("m"));
        options.addOption(OptionBuilder.withArgName("file")
                .hasArg(true)
                .withLongOpt("iso")
                .withDescription("Path to ISO-MENDS metadata file")
                .create("i"));
        options.addOption(OptionBuilder.withArgName("file")
                .hasArg(true)
                .withLongOpt("archive-xml")
                .withDescription("Path to SWOT archive.xml metadata file")
                .create("a"));
        options.addOption(OptionBuilder.withArgName("id")
                .hasArg(true)
                .withLongOpt("granule")
                .withDescription("Granule id")
                .create("g"));
        options.addOption(OptionBuilder.withArgName("s3")
                .hasArg(true)
                .withLongOpt("s3")
                .withDescription("Path of s3 location")
                .create("s"));
        options.addOption(OptionBuilder.withArgName("path")
                .hasArg(true)
                .withLongOpt("output")
                .withDescription("Path of directory to write output metadata file")
                .create("o"));
        return options;
    }

    private static void processCmdLine(String[] args)
     throws URISyntaxException, ParseException {
        MetadataAggregatorCLI aggregator = new MetadataAggregatorCLI();
        CommandLineParser parser = new BasicParser();

        String configFile = null;
        String metadataFile = null;
        String isoFile = null;
        String archiveXmlFile = null;
        String granuleId = null;
        String s3Location = null;
        String outputDir = null;
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("c")) {
                configFile = cmd.getOptionValue("c");
            } else {
                printUsage();
            }

            if (cmd.hasOption("m")) {
                metadataFile = cmd.getOptionValue("m");
            } else if (cmd.hasOption("i")) {
                isoFile = cmd.getOptionValue("i");
            } else if (cmd.hasOption("a")) {
                archiveXmlFile = cmd.getOptionValue("a");
            } else {
                printUsage();
            }

            if (cmd.hasOption("g")) {
                granuleId = cmd.getOptionValue("g");
            } else {
                printUsage();
            }

            if (cmd.hasOption("s")) {
                s3Location = cmd.getOptionValue("s");
            } else {
                printUsage();
            }

            if (cmd.hasOption("o")) {
                outputDir = cmd.getOptionValue("o");
            } else {
                outputDir = "/tmp";
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e.getCause());
            e.printStackTrace();
            printUsage();
        }
        if (metadataFile != null) {
            processMetadata(granuleId, s3Location, configFile, metadataFile, outputDir);
        } else if (isoFile != null) {
            processIsoMetadata(granuleId, s3Location, configFile, isoFile, outputDir);
        } else if (archiveXmlFile != null) {
            processArchiveXmlMetadata(granuleId, s3Location, configFile, archiveXmlFile, outputDir);
        }
    }

    private static void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("MetadataAggregatorCLI", options, true);
        System.exit(0);
    }

    public static void processIsoMetadata(String granuleId, String s3Location, String configFile, String isoFile,
                                       String outputDir)
    throws  ParseException, URISyntaxException{
        MetadataFilesToEcho mtfe = new MetadataFilesToEcho(true);
        try {
            mtfe.readConfiguration(configFile);
            mtfe.readIsoMetadataFile(isoFile, s3Location);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

        writeOutputMetadata(mtfe, granuleId, outputDir);
    }

    public static void processMetadata(String granuleId, String s3Location, String configFile, String metadataFile,
                                       String outputDir)
    throws  URISyntaxException, ParseException{
        MetadataFilesToEcho mtfe = new MetadataFilesToEcho();
        try {
            mtfe.readConfiguration(configFile);
            mtfe.readCommonMetadataFile(metadataFile, s3Location);

        } catch (IOException | ParseException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

        writeOutputMetadata(mtfe, granuleId, outputDir);
    }

    /**
     * Parses SWOT archive.xml metadata file.
     *
     * @param granuleId    unique granule identifier
     * @param s3Location   S3 path to data file
     * @param configFile   path to metadata aggregator config file
     * @param metadataFile path to archive.xml metadata file
     * @param outputDir    directory to write output CMR metadata file
     */
    public static void processArchiveXmlMetadata(String granuleId, String s3Location, String configFile, String metadataFile,
                                       String outputDir)
    throws  ParseException, URISyntaxException{
        MetadataFilesToEcho mtfe = new MetadataFilesToEcho();
        try {
            mtfe.readConfiguration(configFile);
            mtfe.readSwotArchiveXmlFile(metadataFile);

        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

        writeOutputMetadata(mtfe, granuleId, outputDir);
    }

    private static void writeOutputMetadata(MetadataFilesToEcho mtfe, String granuleId, String outputDir)
    throws ParseException, URISyntaxException {
        //set the name
        mtfe.getGranule().setName(granuleId);

        //write UMM-G to file
        try {
            mtfe.writeJson(outputDir + File.separator + granuleId + ".cmr.json");
        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws ParseException, URISyntaxException{
        log = LogFactory.getLog(MetadataAggregatorCLI.class);
        processCmdLine(args);
    }
}

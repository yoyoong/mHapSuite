package com;

import com.args.*;
import com.common.Annotation;
import org.apache.commons.cli.*;
import org.apache.commons.io.Charsets;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.*;

public class Main {
    static Convert convert = new Convert();
    static Merge merge = new Merge();
    static Tanghulu tanghulu = new Tanghulu();
    static MHapView mHapView = new MHapView();
    static Stat stat = new Stat();
    static GenomeWide genomeWide = new GenomeWide();
    static MHBDiscovery mhbDiscovery = new MHBDiscovery();
    static ScatterPlot scatterPlot = new ScatterPlot();
    static BoxPlot boxPlot = new BoxPlot();
    static HeatMapPlot heatMapPlot = new HeatMapPlot();
    static ProfilePlot profilePlot = new ProfilePlot();
    static EnrichmentPlot enrichmentPlot = new EnrichmentPlot();

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");

        if (args != null && args[0] != null && !"".equals(args[0])) {
            if (args[0].equals("help") || args[0].equals("h") || args[0].equals("-help") || args[0].equals("-h")) {
                printHelp();
            } else if (args[0].equals("convert")) {
                ConvertArgs convertArgs = parseConvert(args);
                if (convertArgs != null) {
                    convert.convert(convertArgs);
                }
            } else if (args[0].equals("merge")) {
                MergeArgs mergeArgs = parseMerge(args);
                if (mergeArgs != null) {
                    merge.merge(mergeArgs);
                }
            } else if (args[0].equals("tanghulu")) {
                TanghuluArgs tanghuluArgs = parseTanghulu(args);
                if (tanghuluArgs != null) {
                    tanghulu.tanghulu(tanghuluArgs);
                }
            } else if (args[0].equals("mHapView")) {
                MHapViewArgs mHapViewArgs = parseMHapView(args);
                if (mHapViewArgs != null) {
                    mHapView.mHapView(mHapViewArgs);
                }
            } else if (args[0].equals("stat")) {
                StatArgs statArgs = parseStat(args);
                if (statArgs != null) {
                    stat.stat(statArgs);
                }
            } else if (args[0].equals("genomeWide")) {
                GenomeWideArgs genomeWideArgs = parseGenomeWide(args);
                if (genomeWideArgs != null) {
                    genomeWide.genomeWide(genomeWideArgs);
                }
            } else if (args[0].equals("MHBDiscovery")) {
                MHBDiscoveryArgs mhbDiscoveryArgs = parseMHBDiscovery(args);
                if (mhbDiscoveryArgs != null) {
                    mhbDiscovery.MHBDiscovery(mhbDiscoveryArgs);
                }
            } else if (args[0].equals("scatterPlot")) {
                ScatterPlotArgs scatterPlotArgs = parseScatterPlot(args);
                if (scatterPlotArgs != null) {
                    scatterPlot.scatterPlot(scatterPlotArgs);
                }
            } else if (args[0].equals("boxPlot")) {
                BoxPlotArgs boxPlotArgs = parseBoxPlot(args);
                if (boxPlotArgs != null) {
                    boxPlot.boxPlot(boxPlotArgs);
                }
            } else if (args[0].equals("heatMapPlot")) {
                HeatMapPlotArgs heatMapPlotArgs = parseHeatMapPlot(args);
                if (heatMapPlotArgs != null) {
                    heatMapPlot.heatMapPlot(heatMapPlotArgs);
                }
            } else if (args[0].equals("profilePlot")) {
                ProfilePlotArgs profilePlotArgs = parseProfilePlot(args);
                if (profilePlotArgs != null) {
                    profilePlot.profilePlot(profilePlotArgs);
                }
            } else if (args[0].equals("enrichmentPlot")) {
                EnrichmentPlotArgs enrichmentPlotArgs = parseEnrichmentPlot(args);
                if (enrichmentPlotArgs != null) {
                    enrichmentPlot.enrichmentPlot(enrichmentPlotArgs);
                }
            } else {
                System.out.println("unrecognized command:" + args[0]);
            }
        } else { // show the help message
            printHelp();
        }
    }

    private static void printHelp() {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out, Charsets.UTF_8), true);
        pw.println("mHapSuite: a tool kit for analysis of DNA methylation haplotypes.");
        pw.println("Usage: java -jar mHapSuite <command> [options]");
        pw.println("\nCommands:");

        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(110);
//        hf.setSyntaxPrefix("");
        pw.println("\tconvert\t\t\tconvert BAM files to mHap files");
        pw.println("\tmerge\t\t\tplot the DNA methylation status for mHaps in a region");
        pw.println("\ttanghulu\t\tshow linkage disequilibrium (LD) scores of CpGs in a region");
        pw.println("\tMHBDiscovery\tidentification of methylation haplotype blocks within a region or genome-wide");
        pw.println("\tstat\t\t\tcalculate DNA methylation metrics for mHaps that cover predefined regions");
        pw.println("\tgenomeWide\t\tcalculate DNA methylation metrics for mHaps that cover each CpG site across the genome");
        pw.println("\tscatterPlot\t\tshow a scatter plot for two DNA methylation metrics");
        pw.println("\tboxPlot\t\t\tshow the distribution of metric values from multiple samples as a boxplot");
        pw.println("\theatMapPlot\t\tshow signals around the center of given regions as a heatmap");
        pw.println("\tprofilePlot\t\tshow the average profiles of signals in the predefined intervals and their flanking regions");
        pw.println("\tenrichmentPlot\tshow the perventage of genomic features that overlap with predefined open chromatin regions");
    }

    private static Options getOptions(Field[] declaredFields) {
        Options options = new Options();
        Option helpOption = OptionBuilder.withLongOpt("help").withDescription("help").create("h");
        options.addOption(helpOption);
        Field[] fields = declaredFields;
        for(Field field : fields) {
            String annotation = field.getAnnotation(Annotation.class).value();
            Option option = null;
            if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                option = OptionBuilder.withLongOpt(field.getName()).withDescription(annotation).create(field.getName());
            } else {
                option = OptionBuilder.withLongOpt(field.getName()).hasArg().withDescription(annotation).create(field.getName());
            }
            options.addOption(option);
        }
        return options;
    }

    public static String getStringFromMultiValueParameter(CommandLine commandLine, String args) {
        String value = commandLine.getOptionValue(args);
        if (commandLine.getArgs().length > 1) {
            for (int i = 1; i < commandLine.getArgs().length; i++) {
                value += " " + commandLine.getArgs()[i];
            }
        }
        // 去除重复的值
        String[] valueList = value.split(" ");
        Set<Object> haoma = new LinkedHashSet<Object>();
        for (int i = 0; i < valueList.length; i++) {
            haoma.add(valueList[i]);
        }

        String realValue = "";
        for (int i = 0; i < haoma.size(); i++) {
            realValue += " " + haoma.toArray()[i];
        }

        return realValue.trim();
    }

    private static ConvertArgs parseConvert(String[] args) throws ParseException {
        Options options = getOptions(ConvertArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        ConvertArgs convertArgs = new ConvertArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.setWidth(110);
                hf.setSyntaxPrefix("");
                hf.printHelp("Options:", options);
                return null;
            } else {
                convertArgs.setInputFile(commandLine.getOptionValue("inputFile"));
                convertArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                if (commandLine.hasOption("region")) {
                    convertArgs.setRegion(commandLine.getOptionValue("region"));
                }
                if (commandLine.hasOption("bedPath")) {
                    convertArgs.setBedPath(commandLine.getOptionValue("bedPath"));
                }
                if (commandLine.hasOption("nonDirectional")) {
                    convertArgs.setNonDirectional(true);
                }
                if (commandLine.hasOption("outPutFile")) {
                    convertArgs.setOutPutFile(commandLine.getOptionValue("outPutFile"));
                }
                if (commandLine.hasOption("mode")) {
                    convertArgs.setMode(commandLine.getOptionValue("mode"));
                }
                if (commandLine.hasOption("pat")) {
                    convertArgs.setPat(true);
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return convertArgs;
    }

    private static MergeArgs parseMerge(String[] args) throws ParseException {
        Options options = getOptions(MergeArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        MergeArgs mergeArgs = new MergeArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.setWidth(110);
                helpFormatter.setSyntaxPrefix("");
                helpFormatter.printHelp("Options", options);
                return null;
            } else {
                if (commandLine.hasOption("inputFile")) {
                    String inputFile = commandLine.getOptionValue("inputFile");
                    if (commandLine.getArgs().length > 1) {
                        for (int i = 1; i < commandLine.getArgs().length; i++) {
                            inputFile += " " + commandLine.getArgs()[i];
                        }
                    }
                    // 去除重复的metrics
                    String[] inputFileList = inputFile.split(" ");
                    Set<Object> haoma = new LinkedHashSet<Object>();
                    for (int i = 0; i < inputFileList.length; i++) {
                        haoma.add(inputFileList[i]);
                    }

                    String realInputFile = "";
                    for (int i = 0; i < haoma.size(); i++) {
                        realInputFile += " " + haoma.toArray()[i];
                    }
                    mergeArgs.setInputFile(realInputFile);
                }
                mergeArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                if (commandLine.hasOption("outPutFile")) {
                    mergeArgs.setOutPutFile(commandLine.getOptionValue("outPutFile"));
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return mergeArgs;
    }

    private static TanghuluArgs parseTanghulu(String[] args) throws ParseException {
        Options options = getOptions(TanghuluArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        TanghuluArgs tanghuluArgs = new TanghuluArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.setWidth(110);
                helpFormatter.setSyntaxPrefix("");
                helpFormatter.printHelp("Options", options);
                return null;
            } else {
                tanghuluArgs.setMhapPath(commandLine.getOptionValue("mhapPath"));
                tanghuluArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                tanghuluArgs.setRegion(commandLine.getOptionValue("region"));
                tanghuluArgs.setTag(commandLine.getOptionValue("tag"));
                if (commandLine.hasOption("outFormat")) {
                    tanghuluArgs.setOutFormat(commandLine.getOptionValue("outFormat"));
                }
                if (commandLine.hasOption("strand")) {
                    tanghuluArgs.setStrand(commandLine.getOptionValue("strand"));
                }
                if (commandLine.hasOption("maxReads")) {
                    tanghuluArgs.setMaxReads(Integer.valueOf(commandLine.getOptionValue("maxReads")));
                }
                if (commandLine.hasOption("maxLength")) {
                    tanghuluArgs.setMaxLength(Integer.valueOf(commandLine.getOptionValue("maxLength")));
                }
                if (commandLine.hasOption("merge")) {
                    tanghuluArgs.setMerge(true);
                }
                if (commandLine.hasOption("simulation")) {
                    tanghuluArgs.setSimulation(true);
                }
                if (commandLine.hasOption("cutReads")) {
                    tanghuluArgs.setCutReads(true);
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return tanghuluArgs;
    }

    private static MHapViewArgs parseMHapView(String[] args) throws ParseException {
        Options options = getOptions(MHapViewArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        MHapViewArgs mHapViewArgs = new MHapViewArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.setWidth(110);
                helpFormatter.setSyntaxPrefix("");
                helpFormatter.printHelp("Options", options);
                return null;
            } else {
                mHapViewArgs.setMhapPath(commandLine.getOptionValue("mhapPath"));
                mHapViewArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                mHapViewArgs.setRegion(commandLine.getOptionValue("region"));
                if (commandLine.hasOption("bedPath")) {
                    mHapViewArgs.setBedPath(commandLine.getOptionValue("bedPath"));
                }
                mHapViewArgs.setTag(commandLine.getOptionValue("tag"));
                if (commandLine.hasOption("outFormat")) {
                    mHapViewArgs.setOutFormat(commandLine.getOptionValue("outFormat"));
                }
                if (commandLine.hasOption("strand")) {
                    mHapViewArgs.setStrand(commandLine.getOptionValue("strand"));
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return mHapViewArgs;
    }

    private static StatArgs parseStat(String[] args) throws ParseException {
        Options options = getOptions(StatArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        StatArgs statArgs = new StatArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.setWidth(110);
                helpFormatter.setSyntaxPrefix("");
                helpFormatter.printHelp("Options", options);
                return null;
            } else {
                if (commandLine.hasOption("metrics")) {
                    String metrics = commandLine.getOptionValue("metrics");
                    if (commandLine.getArgs().length > 1) {
                        for (int i = 1; i < commandLine.getArgs().length; i++) {
                            metrics += " " + commandLine.getArgs()[i];
                        }
                    }
                    // 去除重复的metrics
                    String[] metricsList = metrics.split(" ");
                    Set<Object> haoma = new LinkedHashSet<Object>();
                    for (int i = 0; i < metricsList.length; i++) {
                        haoma.add(metricsList[i]);
                    }

                    String realMetrics = "";
                    for (int i = 0; i < haoma.size(); i++) {
                        realMetrics += " " + haoma.toArray()[i];
                    }

                    statArgs.setMetrics(realMetrics);
                }
                statArgs.setMhapPath(commandLine.getOptionValue("mhapPath"));
                statArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                if (commandLine.hasOption("region")) {
                    statArgs.setRegion(commandLine.getOptionValue("region"));
                }
                if (commandLine.hasOption("bedPath")) {
                    statArgs.setBedPath(commandLine.getOptionValue("bedPath"));
                }
                statArgs.setOutputFile(commandLine.getOptionValue("outputFile"));
                if (commandLine.hasOption("minK")) {
                    statArgs.setMinK(Integer.valueOf(commandLine.getOptionValue("minK")));
                }
                if (commandLine.hasOption("maxK")) {
                    statArgs.setMaxK(Integer.valueOf(commandLine.getOptionValue("maxK")));
                }
                if (commandLine.hasOption("K")) {
                    statArgs.setK(Integer.valueOf(commandLine.getOptionValue("K")));
                }
                if (commandLine.hasOption("strand")) {
                    statArgs.setStrand(commandLine.getOptionValue("strand"));
                }
                if (commandLine.hasOption("cutReads")) {
                    statArgs.setCutReads(true);
                }
                if (commandLine.hasOption("r2Cov")) {
                    statArgs.setR2Cov(Integer.valueOf(commandLine.getOptionValue("r2Cov")));
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return statArgs;
    }

    private static GenomeWideArgs parseGenomeWide(String[] args) throws ParseException {
        Options options = getOptions(GenomeWideArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        GenomeWideArgs genomeWideArgs = new GenomeWideArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.setWidth(110);
                helpFormatter.setSyntaxPrefix("");
                helpFormatter.printHelp("Options:", options);
                return null;
            } else {
                genomeWideArgs.setTag(commandLine.getOptionValue("tag"));
                genomeWideArgs.setMhapPath(commandLine.getOptionValue("mhapPath"));
                genomeWideArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                if (commandLine.hasOption("metrics")) {
                    String metrics = commandLine.getOptionValue("metrics");
                    if (commandLine.getArgs().length > 1) {
                        for (int i = 1; i < commandLine.getArgs().length; i++) {
                            metrics += " " + commandLine.getArgs()[i];
                        }
                    }
                    // 去除重复的metrics
                    String[] metricsList = metrics.split(" ");
                    Set<Object> haoma = new LinkedHashSet<Object>();
                    for (int i = 0; i < metricsList.length; i++) {
                        haoma.add(metricsList[i]);
                    }

                    String realMetrics = "";
                    for (int i = 0; i < haoma.size(); i++) {
                        realMetrics += " " + haoma.toArray()[i];
                    }

                    genomeWideArgs.setMetrics(realMetrics);
                }
                genomeWideArgs.setOutputDir(commandLine.getOptionValue("outputDir"));
                if (commandLine.hasOption("minK")) {
                    genomeWideArgs.setMinK(Integer.valueOf(commandLine.getOptionValue("minK")));
                }
                if (commandLine.hasOption("maxK")) {
                    genomeWideArgs.setMaxK(Integer.valueOf(commandLine.getOptionValue("maxK")));
                }
                if (commandLine.hasOption("K")) {
                    genomeWideArgs.setK(Integer.valueOf(commandLine.getOptionValue("K")));
                }
                if (commandLine.hasOption("strand")) {
                    genomeWideArgs.setStrand(commandLine.getOptionValue("strand"));
                }
                if (commandLine.hasOption("region")) {
                    genomeWideArgs.setRegion(commandLine.getOptionValue("region"));
                }
                if (commandLine.hasOption("bedPath")) {
                    genomeWideArgs.setBedPath(commandLine.getOptionValue("bedPath"));
                }
                if (commandLine.hasOption("cpgCov")) {
                    genomeWideArgs.setCpgCov(Integer.valueOf(commandLine.getOptionValue("cpgCov")));
                }
                if (commandLine.hasOption("r2Cov")) {
                    genomeWideArgs.setR2Cov(Integer.valueOf(commandLine.getOptionValue("r2Cov")));
                }
                if (commandLine.hasOption("k4Plus")) {
                    genomeWideArgs.setK4Plus(Integer.valueOf(commandLine.getOptionValue("k4Plus")));
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return genomeWideArgs;
    }

    private static MHBDiscoveryArgs parseMHBDiscovery(String[] args) throws ParseException {
        Options options = getOptions(MHBDiscoveryArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        MHBDiscoveryArgs mhbDiscoveryArgs = new MHBDiscoveryArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.setWidth(110);
                helpFormatter.setSyntaxPrefix("");
                helpFormatter.printHelp("Options", options);
                return null;
            } else {
                mhbDiscoveryArgs.setMhapPath(commandLine.getOptionValue("mhapPath"));
                mhbDiscoveryArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                if (commandLine.hasOption("region")) {
                    mhbDiscoveryArgs.setRegion(commandLine.getOptionValue("region"));
                }
                if (commandLine.hasOption("bedPath")) {
                    mhbDiscoveryArgs.setBedPath(commandLine.getOptionValue("bedPath"));
                }
                if (commandLine.hasOption("window")) {
                    mhbDiscoveryArgs.setWindow(Integer.valueOf(commandLine.getOptionValue("window")));
                }
                if (commandLine.hasOption("r2")) {
                    mhbDiscoveryArgs.setR2(Double.valueOf(commandLine.getOptionValue("r2")));
                }
                if (commandLine.hasOption("pvalue")) {
                    mhbDiscoveryArgs.setPvalue(Double.valueOf(commandLine.getOptionValue("pvalue")));
                }
                mhbDiscoveryArgs.setOutputDir(commandLine.getOptionValue("outputDir"));
                mhbDiscoveryArgs.setTag(commandLine.getOptionValue("tag"));
//                if (commandLine.hasOption("qcFlag")) {
//                    mhbDiscoveryArgs.setQcFlag(true);
//                }

            }
        } else {
            System.out.println("The paramter is null");
        }

        return mhbDiscoveryArgs;
    }

    private static ScatterPlotArgs parseScatterPlot(String[] args) throws ParseException {
        Options options = getOptions(ScatterPlotArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        ScatterPlotArgs scatterPlotArgs = new ScatterPlotArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.setWidth(110);
                helpFormatter.setSyntaxPrefix("");
                helpFormatter.printHelp("Options: ", options);
                return null;
            } else {
                scatterPlotArgs.setBedPath(commandLine.getOptionValue("bedPath"));
                scatterPlotArgs.setBigwig1(commandLine.getOptionValue("bigwig1"));
                scatterPlotArgs.setBigwig2(commandLine.getOptionValue("bigwig2"));
                scatterPlotArgs.setTag(commandLine.getOptionValue("tag"));
                if (commandLine.hasOption("outFormat")) {
                    scatterPlotArgs.setOutFormat(commandLine.getOptionValue("outFormat"));
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return scatterPlotArgs;
    }

    private static BoxPlotArgs parseBoxPlot(String[] args) throws ParseException {
        Options options = getOptions(BoxPlotArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        BoxPlotArgs boxPlotArgs = new BoxPlotArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.setWidth(110);
                helpFormatter.setSyntaxPrefix("");
                helpFormatter.printHelp("Options: ", options);
                return null;
            } else {
                boxPlotArgs.setBedPath(commandLine.getOptionValue("bedPath"));
                boxPlotArgs.setBigwigs(getStringFromMultiValueParameter(commandLine, "bigwigs"));
                boxPlotArgs.setTag(commandLine.getOptionValue("tag"));
                if (commandLine.hasOption("outFormat")) {
                    boxPlotArgs.setOutFormat(commandLine.getOptionValue("outFormat"));
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return boxPlotArgs;
    }

    private static HeatMapPlotArgs parseHeatMapPlot(String[] args) throws ParseException {
        Options options = getOptions(HeatMapPlotArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        HeatMapPlotArgs heatMapPlotArgs = new HeatMapPlotArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.setWidth(110);
                helpFormatter.setSyntaxPrefix("");
                helpFormatter.printHelp("Options:", options);
                return null;
            } else {
                heatMapPlotArgs.setBedPaths(getStringFromMultiValueParameter(commandLine, "bedPaths"));
                heatMapPlotArgs.setBigwig(commandLine.getOptionValue("bigwig"));
                if (commandLine.hasOption("upLength")) {
                    heatMapPlotArgs.setUpLength(Integer.valueOf(commandLine.getOptionValue("upLength")));
                }
                if (commandLine.hasOption("downLength")) {
                    heatMapPlotArgs.setDownLength(Integer.valueOf(commandLine.getOptionValue("downLength")));
                }
                if (commandLine.hasOption("window")) {
                    heatMapPlotArgs.setWindow(Integer.valueOf(commandLine.getOptionValue("window")));
                }
                if (commandLine.hasOption("sortRegions")) {
                    heatMapPlotArgs.setSortRegions(commandLine.getOptionValue("sortRegions"));
                }
                heatMapPlotArgs.setTag(commandLine.getOptionValue("tag"));
                if (commandLine.hasOption("outFormat")) {
                    heatMapPlotArgs.setOutFormat(commandLine.getOptionValue("outFormat"));
                }
                if (commandLine.hasOption("matrixFlag")) {
                    heatMapPlotArgs.setMatrixFlag(true);
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return heatMapPlotArgs;
    }

    private static ProfilePlotArgs parseProfilePlot(String[] args) throws ParseException {
        Options options = getOptions(ProfilePlotArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        ProfilePlotArgs profilePlotArgs = new ProfilePlotArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.setWidth(110);
                helpFormatter.setSyntaxPrefix("");
                helpFormatter.printHelp("Options:", options);
                return null;
            } else {
                profilePlotArgs.setBedPaths(getStringFromMultiValueParameter(commandLine, "bedPaths"));
                profilePlotArgs.setBigwig(commandLine.getOptionValue("bigwig"));
                if (commandLine.hasOption("upLength")) {
                    profilePlotArgs.setUpLength(Integer.valueOf(commandLine.getOptionValue("upLength")));
                }
                if (commandLine.hasOption("downLength")) {
                    profilePlotArgs.setDownLength(Integer.valueOf(commandLine.getOptionValue("downLength")));
                }
                if (commandLine.hasOption("windowNum")) {
                    profilePlotArgs.setWindowNum(Integer.valueOf(commandLine.getOptionValue("windowNum")));
                }
                profilePlotArgs.setTag(commandLine.getOptionValue("tag"));
                if (commandLine.hasOption("outFormat")) {
                    profilePlotArgs.setOutFormat(commandLine.getOptionValue("outFormat"));
                }
                if (commandLine.hasOption("matrixFlag")) {
                    profilePlotArgs.setMatrixFlag(true);
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return profilePlotArgs;
    }

    private static EnrichmentPlotArgs parseEnrichmentPlot(String[] args) throws ParseException {
        Options options = getOptions(EnrichmentPlotArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        EnrichmentPlotArgs enrichmentPlotArgs = new EnrichmentPlotArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.setWidth(110);
                helpFormatter.setSyntaxPrefix("");
                helpFormatter.printHelp("Options:", options);
                return null;
            } else {
                enrichmentPlotArgs.setBedPaths(getStringFromMultiValueParameter(commandLine, "bedPaths"));
                enrichmentPlotArgs.setBigwig(commandLine.getOptionValue("bigwig"));
                enrichmentPlotArgs.setOpenChromatin(commandLine.getOptionValue("openChromatin"));
                if (commandLine.hasOption("groupNum")) {
                    enrichmentPlotArgs.setGroupNum(Integer.valueOf(commandLine.getOptionValue("groupNum")));
                }
                if (commandLine.hasOption("groupCutoff")) {
                    enrichmentPlotArgs.setGroupCutoff(Integer.valueOf(commandLine.getOptionValue("groupCutoff")));
                }
                enrichmentPlotArgs.setTag(commandLine.getOptionValue("tag"));
                if (commandLine.hasOption("outFormat")) {
                    enrichmentPlotArgs.setOutFormat(commandLine.getOptionValue("outFormat"));
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return enrichmentPlotArgs;
    }
}

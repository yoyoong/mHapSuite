package com;

import com.args.*;
import com.common.Annotation;
import org.apache.commons.cli.*;

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
    static ScatterView scatterView = new ScatterView();

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");

        if (args != null && args[0] != null && !"".equals(args[0])) {
            if (args[0].equals("convert")) {
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
            } else if (args[0].equals("scatterView")) {
                ScatterViewArgs scatterViewArgs = parseScatterView(args);
                if (scatterViewArgs != null) {
                    scatterView.scatterView(scatterViewArgs);
                }
            } else {
                System.out.println("unrecognized command:" + args[0]);
            }
        } else { // show the help message

        }
    }

    private static Options getOptions(Field[] declaredFields) {
        Options options = new Options();
        Option helpOption = OptionBuilder.withLongOpt("help").withDescription("help").create("h");
        options.addOption(helpOption);
        Field[] fields = declaredFields;
        for(Field field : fields) {
            String annotation = field.getAnnotation(Annotation.class).value();
            Option option = null;
            if (field.getType().equals(boolean.class)) {
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
                hf.printHelp("Options:", options);
                return null;
            } else {
                convertArgs.setInputFile(commandLine.getOptionValue("inputFile"));
                convertArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                if (commandLine.hasOption("region")) {
                    convertArgs.setRegion(commandLine.getOptionValue("region"));
                }
                if (commandLine.hasOption("bedPath")) {
                    convertArgs.setBedFile(commandLine.getOptionValue("bedPath"));
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
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options:", options);
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
                helpFormatter.printHelp("Options", options);
                return null;
            } else {
                tanghuluArgs.setMhapPath(commandLine.getOptionValue("mhapPath"));
                tanghuluArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                tanghuluArgs.setRegion(commandLine.getOptionValue("region"));
                tanghuluArgs.setOutputFile(commandLine.getOptionValue("outputFile"));
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
                helpFormatter.printHelp("Options", options);
                return null;
            } else {
                mhbDiscoveryArgs.setmHapPath(commandLine.getOptionValue("mhapPath"));
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
                if (commandLine.hasOption("qcFlag")) {
                    mhbDiscoveryArgs.setQcFlag(true);
                }

            }
        } else {
            System.out.println("The paramter is null");
        }

        return mhbDiscoveryArgs;
    }

    private static ScatterViewArgs parseScatterView(String[] args) throws ParseException {
        Options options = getOptions(ScatterViewArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        ScatterViewArgs scatterViewArgs = new ScatterViewArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Options", options);
                return null;
            } else {
                scatterViewArgs.setRegion(commandLine.getOptionValue("region"));
                scatterViewArgs.setBigwig1(commandLine.getOptionValue("bigwig1"));
                scatterViewArgs.setBigwig2(commandLine.getOptionValue("bigwig2"));
                scatterViewArgs.setTag(commandLine.getOptionValue("tag"));
                scatterViewArgs.setOutFormat(commandLine.getOptionValue("outFormat"));
            }
        } else {
            System.out.println("The paramter is null");
        }

        return scatterViewArgs;
    }
}

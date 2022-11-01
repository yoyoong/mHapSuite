package com;

import com.args.*;
import org.apache.commons.cli.*;

import java.util.*;

public class Main {
    static Convert convert = new Convert();
    static Merge merge = new Merge();
    static Tanghulu tanghulu = new Tanghulu();
    static MHapView mHapView = new MHapView();
    static Stat stat = new Stat();
    static GenomeWide genomeWide = new GenomeWide();
    static MHBDiscovery mhbDiscovery = new MHBDiscovery();
    static LinkM linkM = new LinkM();

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
            } else if (args[0].equals("linkM")) {
                LinkMArgs linkMyArgs = parseLinkM(args);
                if (linkMyArgs != null) {
                    linkM.linkM(linkMyArgs);
                }
            } else {
                System.out.println("unrecognized command:" + args[0]);
            }
        } else { // show the help message

        }
    }

    private static ConvertArgs parseConvert(String[] args) throws ParseException {
        String inputFile_Description = "input file, SAM/BAM format, should be sorted by samtools";
        String cpgPath_Description = "genomic CpG file, gz format and indexed";
        String region_Description = "one region, in the format of chr:start-end";
        String bedPath_Description = "bed file, one query region per line";
        String nonDirectional_Description = "non-directional, do not group results by the direction of reads.";
        String outPutFile_Description = "output filename. (default: out.mhap.gz)";
        String mode_Description = "sequencing mode. ( TAPS | BS (default) )";

        Options options = new Options();
        Option option0 = OptionBuilder.withLongOpt("help").withDescription("help").create("h");
        Option option1 = OptionBuilder.withArgName("args").withLongOpt("inputFile").hasArg().withDescription(inputFile_Description).create("inputFile");
        Option option2 = OptionBuilder.withArgName("args").withLongOpt("cpgPath").hasArg().withDescription(cpgPath_Description).create("cpgPath");
        Option option3 = OptionBuilder.withArgName("args").withLongOpt("region").hasArg().withDescription(region_Description).create("region");
        Option option4 = OptionBuilder.withArgName("args").withLongOpt("bedPath").hasArg().withDescription(bedPath_Description).create("bedPath");
        Option option5 = OptionBuilder.withArgName("args").withLongOpt("nonDirectional").withDescription(nonDirectional_Description).create("nonDirectional");
        Option option6 = OptionBuilder.withArgName("args").withLongOpt("outPutFile").hasArg().withDescription(outPutFile_Description).create("outPutFile");
        Option option7 = OptionBuilder.withArgName("args").withLongOpt("mode").hasArg().withDescription(mode_Description).create("mode");
        options.addOption(option0).addOption(option1).addOption(option2).addOption(option3).addOption(option4)
                .addOption(option5).addOption(option6).addOption(option7);

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
            }
        } else {
            System.out.println("The paramter is null");
        }

        return convertArgs;
    }

    private static MergeArgs parseMerge(String[] args) throws ParseException {
        String inputFile_Description = "input files, multiple .mhap.gz files to merge";
        String cpgPath_Description = "genomic CpG file, gz format and indexed";
        String outPutFile_Description = "output filename. (default: out.mhap.gz)";

        Options options = new Options();
        Option option0 = OptionBuilder.withLongOpt("help").withDescription("help").create("h");
        Option option1 = OptionBuilder.withArgName("args").withLongOpt("inputFile").hasArg().withDescription(inputFile_Description).create("inputFile");
        Option option2 = OptionBuilder.withArgName("args").withLongOpt("cpgPath").hasArg().withDescription(cpgPath_Description).create("cpgPath");
        Option option3 = OptionBuilder.withArgName("args").withLongOpt("outPutFile").hasArg().withDescription(outPutFile_Description).create("outPutFile");
        options.addOption(option0).addOption(option1).addOption(option2).addOption(option3);

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
        String mhapPath_Description = "input file,mhap.gz format,generated by mHapTools and indexed";
        String cpgPath_Description = "genomic CpG file, gz format and indexed";
        String region_Description = "one region, in the format of chr:start-end";
        String outputFile_Description = "output file";
        String outFormat_Description = "output format,pdf or png [pdf]";
        String strand_Description = "plus,minus,both [both]";
        String maxReads_Description = "the max number of reads to plot [50]";
        String maxLength_Description = "the max length of region to plot [2000]";
        String merge_Description = "indicates whether identical mHaps should be merged";
        String simulation_Description = "indicates whether mHaps should be simulated";
        String cutReads_Description = "indicates whether only keep CpGs in the defined region";

        Options options = new Options();
        Option option0 = OptionBuilder.withLongOpt("help").withDescription("help").create("h");
        Option option1 = OptionBuilder.withLongOpt("mhapPath").hasArg().withDescription(mhapPath_Description).create("mhapPath");
        Option option2 = OptionBuilder.withLongOpt("cpgPath").hasArg().withDescription(cpgPath_Description).create("cpgPath");
        Option option3 = OptionBuilder.withLongOpt("region").hasArg().withDescription(region_Description).create("region");
        Option option4 = OptionBuilder.withLongOpt("outputFile").hasArg().withDescription(outputFile_Description).create("outputFile");
        Option option5 = OptionBuilder.withLongOpt("outFormat").hasArg().withDescription(outFormat_Description).create("outFormat");
        Option option6 = OptionBuilder.withLongOpt("strand").hasArg().withDescription(strand_Description).create("strand");
        Option option7 = OptionBuilder.withLongOpt("maxReads").hasArg().withDescription(maxReads_Description).create("maxReads");
        Option option8 = OptionBuilder.withLongOpt("maxLength").hasArg().withDescription(maxLength_Description).create("maxLength");
        Option option9 = OptionBuilder.withLongOpt("merge").withDescription(merge_Description).create("merge");
        Option option10 = OptionBuilder.withLongOpt("simulation").withDescription(simulation_Description).create("simulation");
        Option option11 = OptionBuilder.withLongOpt("cutReads").withDescription(cutReads_Description).create("cutReads");
        options.addOption(option0).addOption(option1).addOption(option2).addOption(option3).addOption(option4).addOption(option5).addOption(option6)
                .addOption(option7).addOption(option8).addOption(option9).addOption(option10).addOption(option11);

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
        String mhapPath_Description = "input file,mhap.gz format,generated by mHapTools and indexed";
        String cpgPath_Description = "genomic CpG file, gz format and indexed";
        String region_Description = "one region, in the format of chr:start-end";
        String bedPath_Description = "a bed file";
        String tag_Description = "prefix of the output file(s)";
        String outFormat_Description = "output format,pdf or png [pdf]";
        String strand_Description = "plus,minus,both [both]";

        Options options = new Options();
        Option option0 = OptionBuilder.withLongOpt("help").withDescription("help").create("h");
        Option option1 = OptionBuilder.withLongOpt("mhapPath").hasArg().withDescription(mhapPath_Description).create("mhapPath");
        Option option2 = OptionBuilder.withLongOpt("cpgPath").hasArg().withDescription(cpgPath_Description).create("cpgPath");
        Option option3 = OptionBuilder.withLongOpt("region").hasArg().withDescription(region_Description).create("region");
        Option option4 = OptionBuilder.withLongOpt("bedPath").hasArg().withDescription(bedPath_Description).create("bedPath");
        Option option5 = OptionBuilder.withLongOpt("tag").hasArg().withDescription(tag_Description).create("tag");
        Option option6 = OptionBuilder.withLongOpt("outFormat").hasArg().withDescription(outFormat_Description).create("outFormat");
        Option option7 = OptionBuilder.withLongOpt("strand").hasArg().withDescription(strand_Description).create("strand");
        options.addOption(option0).addOption(option1).addOption(option2).addOption(option3).addOption(option4).addOption(option5).
                addOption(option6).addOption(option7);

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
        String metrics_Description = "mHap-level metrics, including MM, PDR, CHALM, MHL, MCR, MBS, Entropy and R2 [None]";
        String mhapPath_Description = "input file,mhap.gz format,generated by mHapTools and indexed";
        String cpgPath_Description = "genomic CpG file, gz format and indexed";
        String region_Description = "one region, in the format of chr:start-end";
        String bedPath_Description = "input BED file";
        String outputFile_Description = "output file";
        String minK_Description = "minimum k-mer length for MHL [1]";
        String maxK_Description = "maximum k-mer length for MHL [10]";
        String K_Description = "k-mer length for entropy, PDR, and CHALM, can be 3, 4, or 5 [4]";
        String strand_Description = "plus,minus,both [both]";
        String cutReads_Description = "indicates whether only keep CpGs in the defined region";
        String r2Cov_Description = "minimal number of reads that cover two CpGs for R2 calculation [20]";

        Options options = new Options();
        Option option0 = OptionBuilder.withLongOpt("help").withDescription("help").create("h");
        Option option1 = OptionBuilder.withLongOpt("metrics").hasArg().withDescription(metrics_Description).create("metrics");
        Option option2 = OptionBuilder.withLongOpt("mhapPath").hasArg().withDescription(mhapPath_Description).create("mhapPath");
        Option option3 = OptionBuilder.withLongOpt("cpgPath").hasArg().withDescription(cpgPath_Description).create("cpgPath");
        Option option4 = OptionBuilder.withLongOpt("region").hasArg().withDescription(region_Description).create("region");
        Option option5 = OptionBuilder.withLongOpt("bedPath").hasArg().withDescription(bedPath_Description).create("bedPath");
        Option option6 = OptionBuilder.withLongOpt("outputFile").hasArg().withDescription(outputFile_Description).create("outputFile");
        Option option7 = OptionBuilder.withLongOpt("minK").hasArg().withDescription(minK_Description).create("minK");
        Option option8 = OptionBuilder.withLongOpt("maxK").hasArg().withDescription(maxK_Description).create("maxK");
        Option option9 = OptionBuilder.withLongOpt("K").hasArg().withDescription(K_Description).create("K");
        Option option10 = OptionBuilder.withLongOpt("strand").hasArg().withDescription(strand_Description).create("strand");
        Option option11 = OptionBuilder.withLongOpt("cutReads").withDescription(cutReads_Description).create("cutReads");
        Option option12 = OptionBuilder.withLongOpt("r2Cov").hasArg().withDescription(r2Cov_Description).create("r2Cov");
        options.addOption(option0).addOption(option1).addOption(option2).addOption(option3).addOption(option4).addOption(option5).addOption(option6).
                addOption(option7).addOption(option8).addOption(option9).addOption(option10).addOption(option11).addOption(option12);

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
        String tag_Description = "prefix of the output file(s)";
        String mhapPath_Description = "input file,mhap.gz format,generated by mHapTools and indexed";
        String cpgPath_Description = "genomic CpG file, gz format and indexed";
        String metrics_Description = "mHap-level metrics,including M,PDR,CHALM,MHL,MCR,MBS,Entropy,and R2";
        String outputDir_Description = "output directory, created in advance";
        String minK_Description = "minimum k-mer length for MHL [1]";
        String maxK_Description = "maximum k-mer length for MHL [10]";
        String K_Description = "k-mer length for entropy, PDR, and CHALM, can be 3, 4, or 5 [4]";
        String strand_Description = "plus,minus,both [both]";
        String region_Description = "one region, in the format of chr:start-end";
        String bedPath_Description = "input BED file";
        String cpgCov_Description = "minimal number of CpG coverage for MM calculation [5]";
        String r2Cov_Description = "minimal number of reads that cover two CpGs for R2 calculation [20]";
        String k4Plus_Description = "minimal number of reads that cover 4 or more CpGs for PDR, CHALM, MHL, MCR, MBS and Entropy [5]";

        Options options = new Options();
        Option option0 = OptionBuilder.withLongOpt("help").withDescription("help").create("h");
        Option option1 = OptionBuilder.withLongOpt("tag").hasArg().withDescription(tag_Description).create("tag");
        Option option2 = OptionBuilder.withLongOpt("mhapPath").hasArg().withDescription(mhapPath_Description).create("mhapPath");
        Option option3 = OptionBuilder.withLongOpt("cpgPath").hasArg().withDescription(cpgPath_Description).create("cpgPath");
        Option option4 = OptionBuilder.withLongOpt("metrics").hasArg().withDescription(metrics_Description).create("metrics");
        Option option5 = OptionBuilder.withLongOpt("outputDir").hasArg().withDescription(outputDir_Description).create("outputDir");
        Option option6 = OptionBuilder.withLongOpt("minK").hasArg().withDescription(minK_Description).create("minK");
        Option option7 = OptionBuilder.withLongOpt("maxK").hasArg().withDescription(maxK_Description).create("maxK");
        Option option8 = OptionBuilder.withLongOpt("K").hasArg().withDescription(K_Description).create("K");
        Option option9 = OptionBuilder.withLongOpt("strand").hasArg().withDescription(strand_Description).create("strand");
        Option option10 = OptionBuilder.withLongOpt("region").hasArg().withDescription(region_Description).create("region");
        Option option11 = OptionBuilder.withLongOpt("bedPath").hasArg().withDescription(bedPath_Description).create("bedPath");
        Option option12 = OptionBuilder.withLongOpt("cpgCov").hasArg().withDescription(cpgCov_Description).create("cpgCov");
        Option option13 = OptionBuilder.withLongOpt("r2Cov").hasArg().withDescription(r2Cov_Description).create("r2Cov");
        Option option14 = OptionBuilder.withLongOpt("k4Plus").hasArg().withDescription(k4Plus_Description).create("k4Plus");
        options.addOption(option0).addOption(option1).addOption(option2).addOption(option3).addOption(option4).addOption(option5).addOption(option6).
                addOption(option7).addOption(option8).addOption(option9).addOption(option10).addOption(option11).addOption(option12).addOption(option13).addOption(option14);

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
        String mhapPath_Description = "input file,mhap.gz format,generated by mHapTools and indexed";
        String cpgPath_Description = "genomic CpG file, gz format and indexed";
        String region_Description = "one region, in the format of chr:start-end";
        String bedPath_Description = "input BED file";
        String window_Description = "Size of core window";
        String r2_Description = "R square cutoff";
        String pvalue_Description = "P value cutoff";
        String outputDir_Description = "output directory, created in advance";
        String tag_Description = "prefix of the output file(s)";
        String qcFlag_Description = "whether output matrics for QC";

        Options options = new Options();
        Option option0 = OptionBuilder.withLongOpt("help").withDescription("help").create("h");
        Option option1 = OptionBuilder.withLongOpt("mhapPath").hasArg().withDescription(mhapPath_Description).create("mhapPath");
        Option option2 = OptionBuilder.withLongOpt("cpgPath").hasArg().withDescription(cpgPath_Description).create("cpgPath");
        Option option3 = OptionBuilder.withLongOpt("region").hasArg().withDescription(region_Description).create("region");
        Option option4 = OptionBuilder.withLongOpt("bedPath").hasArg().withDescription(bedPath_Description).create("bedPath");
        Option option5 = OptionBuilder.withLongOpt("window").hasArg().withDescription(window_Description).create("window");
        Option option6 = OptionBuilder.withLongOpt("r2").hasArg().withDescription(r2_Description).create("r2");
        Option option7 = OptionBuilder.withLongOpt("pvalue").hasArg().withDescription(pvalue_Description).create("pvalue");
        Option option8 = OptionBuilder.withLongOpt("outputDir").hasArg().withDescription(outputDir_Description).create("outputDir");
        Option option9 = OptionBuilder.withLongOpt("tag").hasArg().withDescription(tag_Description).create("tag");
        Option option10 = OptionBuilder.withLongOpt("qcFlag").withDescription(qcFlag_Description).create("qcFlag");
        options.addOption(option0).addOption(option1).addOption(option2).addOption(option3).addOption(option4).addOption(option5)
                .addOption(option6).addOption(option7).addOption(option8).addOption(option9).addOption(option10);

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

    private static LinkMArgs parseLinkM(String[] args) throws ParseException {
        String mhapPathT_Description = "input tumor file,mhap.gz format,generated by mHapTools and indexed";
        String mhapPathN_Description = "input normal file,mhap.gz format,generated by mHapTools and indexed";
        String cpgPath_Description = "genomic CpG file, gz format and indexed";
        String region_Description = "one region, in the format of chr:start-end";
        //String bedPath_Description = "input BED file";
        String outputDir_Description = "output directory, created in advance";
        String tag_Description = "prefix of the output file(s)";
        String fLength_Description = "fLength";
        String rLength_Description = "rLength";
        String minT_Description = "minT";
        String maxN_Description = "maxN";
        String minFC_Description = "minFC";
        String minInsertSize_Description = "minInsertSize";
        String maxInsertSize_Description = "maxInsertSize";
        String minCov_Description = "minCov";

        Options options = new Options();
        Option option0 = OptionBuilder.withLongOpt("help").withDescription("help").create("h");
        Option option1 = OptionBuilder.withLongOpt("mhapPathT").hasArg().withDescription(mhapPathT_Description).create("mhapPathT");
        Option option2 = OptionBuilder.withLongOpt("mhapPathN").hasArg().withDescription(mhapPathN_Description).create("mhapPathN");
        Option option3 = OptionBuilder.withLongOpt("cpgPath").hasArg().withDescription(cpgPath_Description).create("cpgPath");
        Option option4 = OptionBuilder.withLongOpt("region").hasArg().withDescription(region_Description).create("region");
        Option option5 = OptionBuilder.withLongOpt("outputDir").hasArg().withDescription(outputDir_Description).create("outputDir");
        Option option6 = OptionBuilder.withLongOpt("tag").hasArg().withDescription(tag_Description).create("tag");
        Option option7 = OptionBuilder.withLongOpt("fLength").hasArg().withDescription(fLength_Description).create("fLength");
        Option option8 = OptionBuilder.withLongOpt("rLength").hasArg().withDescription(rLength_Description).create("rLength");
        Option option9 = OptionBuilder.withLongOpt("minT").hasArg().withDescription(minT_Description).create("minT");
        Option option10 = OptionBuilder.withLongOpt("maxN").hasArg().withDescription(maxN_Description).create("maxN");
        Option option11 = OptionBuilder.withLongOpt("minFC").hasArg().withDescription(minFC_Description).create("minFC");
        Option option12 = OptionBuilder.withLongOpt("minInsertSize").hasArg().withDescription(minInsertSize_Description).create("minInsertSize");
        Option option13 = OptionBuilder.withLongOpt("maxInsertSize").hasArg().withDescription(maxInsertSize_Description).create("maxInsertSize");
        Option option14 = OptionBuilder.withLongOpt("minCov").hasArg().withDescription(minCov_Description).create("minCov");
        options.addOption(option0).addOption(option1).addOption(option2).addOption(option3).addOption(option4).addOption(option5)
                .addOption(option6).addOption(option7).addOption(option8).addOption(option9).addOption(option10).addOption(option11)
                .addOption(option12).addOption(option13).addOption(option14);

        BasicParser parser = new BasicParser();
        LinkMArgs linkMArgs = new LinkMArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Options", options);
                return null;
            } else {
                linkMArgs.setMhapPathT(commandLine.getOptionValue("mhapPathT"));
                linkMArgs.setMhapPathN(commandLine.getOptionValue("mhapPathN"));
                linkMArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                linkMArgs.setRegion(commandLine.getOptionValue("region"));
                if (commandLine.hasOption("outputDir")) {
                    linkMArgs.setOutputDir(commandLine.getOptionValue("outputDir"));
                }
                if (commandLine.hasOption("tag")) {
                    linkMArgs.setTag(commandLine.getOptionValue("tag"));
                }
                if (commandLine.hasOption("fLength")) {
                    linkMArgs.setfLength(Integer.valueOf(commandLine.getOptionValue("fLength")));
                }
                if (commandLine.hasOption("rLength")) {
                    linkMArgs.setrLength(Integer.valueOf(commandLine.getOptionValue("rLength")));
                }
                if (commandLine.hasOption("minT")) {
                    linkMArgs.setMinT(Double.valueOf(commandLine.getOptionValue("minT")));
                }
                if (commandLine.hasOption("maxN")) {
                    linkMArgs.setMaxN(Double.valueOf(commandLine.getOptionValue("maxN")));
                }
                if (commandLine.hasOption("minFC")) {
                    linkMArgs.setMinFC(Double.valueOf(commandLine.getOptionValue("minFC")));
                }
                if (commandLine.hasOption("minInsertSize")) {
                    linkMArgs.setMinInsertSize(Integer.valueOf(commandLine.getOptionValue("minInsertSize")));
                }
                if (commandLine.hasOption("maxInsertSize")) {
                    linkMArgs.setMaxInsertSize(Integer.valueOf(commandLine.getOptionValue("maxInsertSize")));
                }
                if (commandLine.hasOption("minCov")) {
                    linkMArgs.setMinCov(Integer.valueOf(commandLine.getOptionValue("minCov")));
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return linkMArgs;
    }
}

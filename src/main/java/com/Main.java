package com;

import com.args.*;
import org.apache.commons.cli.*;

import java.util.*;

public class Main {
    static Tanghulu tanghulu = new Tanghulu();
    static MHapView mHapView = new MHapView();
    static Stat stat = new Stat();

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");

        if (args != null && args[0] != null && !"".equals(args[0])) {
            if (args[0].equals("tanghulu")) {
                TanghuluArgs tanghuluArgs = parseTanghulu(args);
                tanghulu.tanghulu(tanghuluArgs);
            } else if (args[0].equals("mHapView")) {
                MHapViewArgs mHapViewArgs = parseMHapView(args);
                mHapView.mHapView(mHapViewArgs);
            } else if (args[0].equals("stat")) {
                StatArgs statArgs = parseStat(args);
                stat.stat(statArgs);
            } else{
                System.out.println("unrecognized command:" + args[0]);
            }
        } else { // show the help message

        }
    }

    private static TanghuluArgs parseTanghulu(String[] args) throws ParseException {
        Options options = new Options();
        Option option1 = OptionBuilder.withArgName("com/args").withLongOpt("mhapPath").isRequired().hasArg().withDescription("mhapPath").create("mhapPath");
        Option option2 = OptionBuilder.withArgName("com/args").withLongOpt("cpgPath").isRequired().hasArg().withDescription("cpgPath").create("cpgPath");
        Option option3 = OptionBuilder.withArgName("com/args").withLongOpt("region").isRequired().hasArg().withDescription("region").create("region");
        Option option4 = OptionBuilder.withArgName("com/args").withLongOpt("outputFile").isRequired().hasArg().withDescription("outputFile").create("outputFile");
        Option option5 = OptionBuilder.withArgName("com/args").withLongOpt("outFormat").hasArg().withDescription("outFormat").create("outFormat");
        Option option6 = OptionBuilder.withArgName("com/args").withLongOpt("strand").hasArg().withDescription("strand").create("strand");
        Option option7 = OptionBuilder.withArgName("com/args").withLongOpt("maxReads").hasArg().withDescription("maxReads").create("maxReads");
        Option option8 = OptionBuilder.withArgName("com/args").withLongOpt("maxLength").hasArg().withDescription("maxLength").create("maxLength");
        Option option9 = OptionBuilder.withArgName("com/args").withLongOpt("merge").withDescription("merge").create("merge");
        Option option10 = OptionBuilder.withArgName("com/args").withLongOpt("simulation").withDescription("simulation").create("simulation");
        Option option11 = OptionBuilder.withArgName("com/args").withLongOpt("cutReads").withDescription("cutReads").create("cutReads");
        options.addOption(option1).addOption(option2).addOption(option3).addOption(option4).addOption(option5).addOption(option6).addOption(option7)
                .addOption(option8).addOption(option9).addOption(option10).addOption(option11);

        BasicParser parser = new BasicParser();
        TanghuluArgs tanghuluArgs = new TanghuluArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
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
        Options options = new Options();
        Option option1 = OptionBuilder.withArgName("com/args").withLongOpt("mhapPath").isRequired().hasArg().withDescription("mhapPath").create("mhapPath");
        Option option2 = OptionBuilder.withArgName("com/args").withLongOpt("cpgPath").isRequired().hasArg().withDescription("cpgPath").create("cpgPath");
        Option option3 = OptionBuilder.withArgName("com/args").withLongOpt("region").isRequired().hasArg().withDescription("region").create("region");
        Option option4 = OptionBuilder.withArgName("com/args").withLongOpt("bed").hasArg().withDescription("bed").create("bed");
        Option option5 = OptionBuilder.withArgName("com/args").withLongOpt("outputFile").isRequired().hasArg().withDescription("outputFile").create("outputFile");
        Option option6 = OptionBuilder.withArgName("com/args").withLongOpt("outFormat").hasArg().withDescription("outFormat").create("outFormat");
        Option option7 = OptionBuilder.withArgName("com/args").withLongOpt("strand").hasArg().withDescription("strand").create("strand");
        options.addOption(option1).addOption(option2).addOption(option3).addOption(option4).addOption(option5).
                addOption(option6).addOption(option7);

        BasicParser parser = new BasicParser();
        MHapViewArgs mHapViewArgs = new MHapViewArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
            } else {
                mHapViewArgs.setMhapPath(commandLine.getOptionValue("mhapPath"));
                mHapViewArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                mHapViewArgs.setRegion(commandLine.getOptionValue("region"));
                if (commandLine.hasOption("bed")) {
                    mHapViewArgs.setBed(commandLine.getOptionValue("bed"));
                }
                mHapViewArgs.setOutputFile(commandLine.getOptionValue("outputFile"));
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
        Options options = new Options();
        Option option1 = OptionBuilder.withArgName("com/args").withLongOpt("metrics").hasArg().withDescription("metrics").create("metrics");
        Option option2 = OptionBuilder.withArgName("com/args").withLongOpt("mhapPath").isRequired().hasArg().withDescription("mhapPath").create("mhapPath");
        Option option3 = OptionBuilder.withArgName("com/args").withLongOpt("cpgPath").isRequired().hasArg().withDescription("cpgPath").create("cpgPath");
        Option option4 = OptionBuilder.withArgName("com/args").withLongOpt("region").hasArg().withDescription("region").create("region");
        Option option5 = OptionBuilder.withArgName("com/args").withLongOpt("bedPath").hasArg().withDescription("bedPath").create("bedPath");
        Option option6 = OptionBuilder.withArgName("com/args").withLongOpt("outputFile").isRequired().hasArg().withDescription("outputFile").create("outputFile");
        Option option7 = OptionBuilder.withArgName("com/args").withLongOpt("minK").hasArg().withDescription("minK").create("minK");
        Option option8 = OptionBuilder.withArgName("com/args").withLongOpt("maxK").hasArg().withDescription("maxK").create("maxK");
        Option option9 = OptionBuilder.withArgName("com/args").withLongOpt("K").hasArg().withDescription("K").create("K");
        Option option10 = OptionBuilder.withArgName("com/args").withLongOpt("strand").hasArg().withDescription("strand").create("strand");
        Option option11 = OptionBuilder.withArgName("com/args").withLongOpt("cutReads").withDescription("cutReads").create("cutReads");
        Option option12 = OptionBuilder.withArgName("com/args").withLongOpt("cutOff").hasArg().withDescription("cutOff").create("cutOff");
        options.addOption(option1).addOption(option2).addOption(option3).addOption(option4).addOption(option5).addOption(option6).addOption(option7)
                .addOption(option8).addOption(option9).addOption(option10).addOption(option11).addOption(option12);

        BasicParser parser = new BasicParser();
        StatArgs statArgs = new StatArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
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
}

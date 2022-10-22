package com;

import com.args.LinkMArgs;
import com.bean.MHapInfo;
import com.bean.Region;
import com.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.util.*;
import java.util.stream.Collectors;

public class LinkM {
    public static final Logger log = LoggerFactory.getLogger(LinkM.class);

    LinkMArgs args = new LinkMArgs();
    Util util = new Util();

    public void linkM(LinkMArgs linkMArgs) throws Exception {
        log.info("command.linkM start!");
        args = linkMArgs;

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // parse the region
        Region region = util.parseRegion(args.getRegion());

        // parse the cpg file
        List<Integer> cpgPosList = util.parseCpgFile(args.getCpgPath(), region);

        // parse the mhap file
        List<MHapInfo> tumorMHapList = util.parseMhapFile(args.getMhapPathT(), region, "both", true);
        List<MHapInfo> normalMHapList = util.parseMhapFile(args.getMhapPathN(), region, "both", true);

        BufferedWriter bufferedWriter = util.createOutputFile("", args.getTag() + ".linkM.txt");
        bufferedWriter.write("Fpos" + "\t" + "Rpos" + "\t" + "Fpattern" + "\t" + "Rpattern" + "\t" + "T_RC" + "\t" + "T" + "\t" + "N_RC" + "\t" +"N" + "\t" + "FC" + "\n");

        Region fWindow = new Region(); // forward window region
        fWindow.setChrom(region.getChrom());
        Integer fWindowStart = region.getStart(); // forward window start position
        long totalPosCnt = region.getEnd() - region.getStart();
        long readPosCnt = 0;
        for (; fWindowStart < region.getEnd() - args.getMinInsertSize() - args.getrLength() + 1 && fWindow.getEnd() <= region.getEnd(); fWindowStart++) {
            fWindow.setStart(fWindowStart); // forward window start position
            fWindow.setEnd(fWindowStart + args.getfLength() - 1); // forward window end position
            readPosCnt++;
            if (readPosCnt % (totalPosCnt / 100) == 0) {
                int percent = (int) Math.round(Double.valueOf(readPosCnt) * 100 / totalPosCnt);
                log.info("Read complete " + percent + "%.");
            }

            // get the cpg position list in forward window
            List<Integer> cpgPosListInFWindow = getCpgPosListInWindow(cpgPosList, fWindow);
            if (cpgPosListInFWindow.size() < 1) {
                continue;
            }

            Region rWindow = new Region(); // forward window region
            rWindow.setChrom(region.getChrom());
            Integer rWindowStart = fWindow.getEnd() + args.getMinInsertSize() + 1; // reverse window start position
            for (; rWindowStart < fWindow.getEnd() + args.getMaxInsertSize() + 1 && rWindow.getEnd() <= region.getEnd(); rWindowStart++) {
                rWindow.setStart(rWindowStart); // reverse window start position
                rWindow.setEnd(rWindowStart + args.getrLength() - 1); // reverse window end position
                //log.info("Reverse window: " + rWindow.toHeadString() + " read start!");

                // get the region include forward and reverse window
                Region f2rWindow = new Region();
                f2rWindow.setChrom(region.getChrom());
                f2rWindow.setStart(fWindow.getStart());
                f2rWindow.setEnd(rWindow.getEnd());

                // get the cpg position list in both forward and reverse window
                List<Integer> cpgPosListInWindow = getCpgPosListInWindow(cpgPosList, f2rWindow);
                if (cpgPosListInWindow.size() < 1) {
                    continue;
                }
                // get the cpg position list in reverse window
                List<Integer> cpgPosListInRWindow = getCpgPosListInWindow(cpgPosList, rWindow);
                if (cpgPosListInRWindow.size() < 1) {
                    continue;
                }
                Integer fWindowCpgStartIndex = util.indexOfList(cpgPosListInWindow, 0, cpgPosListInWindow.size() - 1, cpgPosListInFWindow.get(0));
                Integer fWindowCpgEndIndex = util.indexOfList(cpgPosListInWindow, 0, cpgPosListInWindow.size() - 1, cpgPosListInFWindow.get(cpgPosListInFWindow.size() - 1));
                Integer rWindowCpgStartIndex = util.indexOfList(cpgPosListInWindow, 0, cpgPosListInWindow.size() - 1, cpgPosListInRWindow.get(0));
                Integer rWindowCpgEndIndex = util.indexOfList(cpgPosListInWindow, 0, cpgPosListInWindow.size() - 1, cpgPosListInRWindow.get(cpgPosListInRWindow.size() - 1));

                // get the tumor and normal mhap list in window
//                List<MHapInfo> tumorMHapListInWindow = getMHapListInWindow(tumorMHapList, f2rWindow);
//                if (tumorMHapListInWindow.size() < args.getMinCov()) {
//                    continue;
//                }
                List<MHapInfo> tumorMHapListInWindow = getMHapListInWindow(tumorMHapList, f2rWindow);
                if (tumorMHapListInWindow.size() < args.getMinCov()) {
                    continue;
                }
//                List<MHapInfo> normalMHapListInWindow = getMHapListInWindow(normalMHapList, f2rWindow);
//                if (normalMHapListInWindow.size() < args.getMinCov()) {
//                    continue;
//                }
                List<MHapInfo> normalMHapListInWindow = getMHapListInWindow(normalMHapList, f2rWindow);
                if (normalMHapListInWindow.size() < args.getMinCov()) {
                    continue;
                }

                // get the tumor pattern in window
                Map<String, Integer> tumorPatternMap = getPatternInWindow(tumorMHapListInWindow, cpgPosList, cpgPosListInWindow, f2rWindow);
                Map<String, Integer> newTumorPatternMap = new HashMap<>();
                Iterator<String> tumorPatternMapIterator = tumorPatternMap.keySet().iterator();
                while (tumorPatternMapIterator.hasNext()) {
                    String key = tumorPatternMapIterator.next();
                    String newKey = key.substring(fWindowCpgStartIndex, fWindowCpgEndIndex + 1) + key.substring(rWindowCpgStartIndex);
                    if (newTumorPatternMap.containsKey(newKey)) {
                        newTumorPatternMap.put(newKey, newTumorPatternMap.get(newKey) + tumorPatternMap.get(key));
                    } else {
                        newTumorPatternMap.put(newKey, tumorPatternMap.get(key));
                    }

                }

                // get the normal pattern in window
                Map<String, Integer> normalPatternMap = getPatternInWindow(normalMHapListInWindow, cpgPosList, cpgPosListInWindow, f2rWindow);
                Map<String, Integer> newNormalPatternMap = new HashMap<>();
                Iterator<String> normalPatternMapIterator = normalPatternMap.keySet().iterator();
                while (normalPatternMapIterator.hasNext()) {
                    String key = normalPatternMapIterator.next();
                    String newKey = key.substring(fWindowCpgStartIndex, fWindowCpgEndIndex + 1) + key.substring(rWindowCpgStartIndex);
                    if (newNormalPatternMap.containsKey(newKey)) {
                        newNormalPatternMap.put(newKey, newNormalPatternMap.get(newKey) + normalPatternMap.get(key));
                    } else {
                        newNormalPatternMap.put(newKey, normalPatternMap.get(key));
                    }
                }

                // filter the different pattern of 2 map
                tumorPatternMapIterator = newTumorPatternMap.keySet().iterator();
                while (tumorPatternMapIterator.hasNext()) {
                    String key = tumorPatternMapIterator.next();
                    if (!newNormalPatternMap.containsKey(key)) {
                        tumorPatternMapIterator.remove();
                    }
                }
                normalPatternMapIterator = newNormalPatternMap.keySet().iterator();
                while (normalPatternMapIterator.hasNext()) {
                    String key = normalPatternMapIterator.next();
                    if (!newTumorPatternMap.containsKey(key)) {
                        normalPatternMapIterator.remove();
                    }
                }

                // get the tumor and normal total pattern count
                Integer tumarTotalPatternCount = newTumorPatternMap.entrySet().stream().mapToInt(t->t.getValue()).sum();
                Integer normalTotalPatternCount = newNormalPatternMap.entrySet().stream().mapToInt(t->t.getValue()).sum();
                tumorPatternMapIterator = newTumorPatternMap.keySet().iterator();
                while (tumorPatternMapIterator.hasNext()) {
                    String key = tumorPatternMapIterator.next();
                    Double tumorRate = newTumorPatternMap.get(key).doubleValue() / tumarTotalPatternCount.doubleValue();
                    Double normalRate = newNormalPatternMap.get(key).doubleValue() / normalTotalPatternCount.doubleValue();
                    Double foldChange = tumorRate / normalRate;
                    if (tumorRate > args.getMinT() && normalRate < args.getMaxN() && foldChange > args.getMinFC()) {
                        bufferedWriter.write(fWindow.toHeadString() + "\t" + rWindow.toHeadString() + "\t" +
                                key.substring(fWindowCpgStartIndex, fWindowCpgEndIndex + 1) + "\t" + key.substring(fWindowCpgEndIndex + 1) + "\t"
                                + tumarTotalPatternCount + "\t" + tumorRate.floatValue() + "\t" +  normalTotalPatternCount + "\t" + normalRate.floatValue() + "\t"
                                + foldChange.floatValue() + "\n");
                    }
                }
                //log.info("Reverse window: " + rWindow.toHeadString() + " read end!");
            }
            //log.info("Forward window: " + fWindow.toHeadString() + " read end!");
        }
        bufferedWriter.close();

        log.info("command.linkM end! ");
    }

    private boolean checkArgs() {

        return true;
    }

    private List<Integer> getCpgPosListInWindow(List<Integer> cpgPosList, Region window) {
        List<Integer> cpgPosListInWindow = new ArrayList<>();

        Integer cpgIndex;
        Integer startPos = window.getStart();
        while (startPos <= window.getEnd() && util.indexOfList(cpgPosList, 0, cpgPosList.size() - 1, startPos) < 0) {
            startPos++;
        }
        if (startPos >= window.getEnd()) {
            return cpgPosListInWindow;
        }
        cpgIndex = util.indexOfList(cpgPosList, 0, cpgPosList.size() - 1, startPos);

        while (cpgIndex < cpgPosList.size() && cpgPosList.get(cpgIndex) <= window.getEnd()) {
            cpgPosListInWindow.add(cpgPosList.get(cpgIndex));
            cpgIndex++;
        }

        return cpgPosListInWindow;
    }

//    private List<MHapInfo> getMHapListInWindow(List<MHapInfo> mHapList, Region window) {
//        List<MHapInfo> mHapListNew = new ArrayList<>();
//        for (MHapInfo mHapInfo : mHapList) {
//            if (mHapInfo.getStart() <= window.getStart() && mHapInfo.getEnd() >= window.getEnd()) {
//                mHapListNew.add(mHapInfo);
//            }
//            if (mHapInfo.getStart() > window.getStart()) {
//                break;
//            }
//        }
//        return mHapListNew;
//    }

    private List<MHapInfo> getMHapListInWindow(List<MHapInfo> mHapList, Region f2rWindow) {
        List<MHapInfo> mHapListFiltered = util.filterMHapListInRegion(mHapList, f2rWindow);
        List<MHapInfo> mHapListNew = new ArrayList<>();
        for (MHapInfo mHapInfo : mHapListFiltered) {
            if (mHapInfo.getStart() <= f2rWindow.getStart() && mHapInfo.getEnd() >= f2rWindow.getEnd()) {
                mHapListNew.add(mHapInfo);
            }
            if (mHapInfo.getStart() > f2rWindow.getStart()) {
                break;
            }
        }
        return mHapListNew;
    }

    private Map<String, Integer> getPatternInWindow(List<MHapInfo> mHapList, List<Integer> cpgPosList, List<Integer> cpgPosListInWindow, Region window) {
        Map<String, Integer> tumorPatternMap = new HashMap<>();

        for (MHapInfo mHapInfo : mHapList) {
            if (mHapInfo.getStart() <= window.getStart() && mHapInfo.getEnd() >= window.getEnd()) {
                Integer startCpgIndex = util.indexOfList(cpgPosList, 0, cpgPosList.size() - 1, cpgPosListInWindow.get(0)) -
                        util.indexOfList(cpgPosList, 0, cpgPosList.size() - 1, mHapInfo.getStart());
                Integer endCpgIndex = util.indexOfList(cpgPosList, 0, cpgPosList.size() - 1, cpgPosListInWindow.get(cpgPosListInWindow.size() - 1)) -
                        util.indexOfList(cpgPosList, 0, cpgPosList.size() - 1, mHapInfo.getStart());
                String pattern = mHapInfo.getCpg().substring(startCpgIndex, endCpgIndex + 1);
                if (tumorPatternMap.containsKey(pattern)) {
                    tumorPatternMap.put(pattern, tumorPatternMap.get(pattern) + mHapInfo.getCnt());
                } else {
                    tumorPatternMap.put(pattern, mHapInfo.getCnt());
                }
            }
            if (mHapInfo.getStart() > window.getStart()) {
                break;
            }
        }
        return tumorPatternMap;
    }

}

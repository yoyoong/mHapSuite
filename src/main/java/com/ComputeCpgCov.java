package com;

import com.args.ComputeCpgCovArgs;
import com.bean.Region;
import com.common.Util;
import com.common.bigwigTool.BBFileReader;
import com.common.bigwigTool.BigWigIterator;
import com.common.bigwigTool.WigItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComputeCpgCov {
    public static final Logger log = LoggerFactory.getLogger(ComputeCpgCov.class);
    ComputeCpgCovArgs args = new ComputeCpgCovArgs();
    Util util = new Util();

    public void computeCpgCov(ComputeCpgCovArgs ComputeCpgCovArgs) throws Exception {
        log.info("ComputeCpgCov start!");
        args = ComputeCpgCovArgs;

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        List<Region> openChromatinRegionList = util.getBedRegionList(args.getOpenChromatin());
        if (openChromatinRegionList.size() < 1) {
            log.info("The openChromatin is null, please check.");
            return;
        }
        Map<String, List<Region>> openChromatinRegionMap = openChromatinRegionList.stream().collect(Collectors.groupingBy(Region::getChrom));

        List<Region> bedRegionList = util.getBedRegionList(args.getBedPath());
        if (bedRegionList.size() < 1) {
            log.info("The bed file is null, please check.");
            return;
        }

        BBFileReader reader = new BBFileReader(args.getBigwig());
        BufferedWriter bufferedWriter = util.createOutputFile("", args.getTag() + ".txt");
        long totalCnt = bedRegionList.size(); // get the total lines of file
        long completeCnt= 0l;
        for (Region region : bedRegionList) {
            completeCnt++;
            if (completeCnt % (totalCnt / 100) == 0) {
                int percent = (int) Math.round(Double.valueOf(completeCnt) * 100 / totalCnt );
                log.info("Complete " + percent + "%.");
            }
            // parse the cpg file
            List<Integer> cpgPosListInRegion = util.parseCpgFile(args.getCpgPath(), region);
            if (cpgPosListInRegion.size() < 1) {
                continue;
            }

            // get the openChromatinRegion list of this chrom
            List<Region> openChromatinRegionOfChrom = openChromatinRegionMap.get(region.getChrom());
            if (openChromatinRegionOfChrom == null || openChromatinRegionOfChrom.size() < 1) {
                log.info("openChromatinRegion in " + region.getChrom() + " is null.");
                continue;
            }

            for (Integer cpgPos : cpgPosListInRegion) {
                Integer coverFlag = whetherRegionListCoverCpg(openChromatinRegionOfChrom, 0,
                        openChromatinRegionOfChrom.size(), cpgPos);

                BigWigIterator iter = reader.getBigWigIterator(region.getChrom(), cpgPos - 1, region.getChrom(), cpgPos, true);
                Double value = 0.0;
                if (iter.hasNext()) {
                    WigItem wigItem = iter.next();
                    value = Double.valueOf(wigItem.getWigValue());
                } else {
                    if (!args.getMissingDataAsZero()) {
                        value = Double.NaN;
                    }
                }

                String regionStr = region.getChrom() + ":" + (cpgPos - 1) + "-" + cpgPos;
                bufferedWriter.write(regionStr + "\t" + value + "\t" + coverFlag + "\n");
            }
        }

        bufferedWriter.close();
        log.info("ComputeCpgCov end!");
    }

    private boolean checkArgs() {
        if (args.getBigwig() == null || args.getBigwig().equals("")) {
            log.error("The bigwig file can not be null.");
            return false;
        }
        if (args.getCpgPath() == null || args.getCpgPath().equals("")) {
            log.error("cpgPath can not be null.");
            return false;
        }
        if (args.getBedPath() == null || args.getBedPath().equals("")) {
            log.error("The bed file can not be null.");
            return false;
        }
        if (args.getOpenChromatin() == null || args.getOpenChromatin().equals("")) {
            log.error("The open chromatin file can not be null.");
            return false;
        }
        if (args.getTag() == null || args.getTag().equals("")) {
            log.error("The tag can not be null.");
            return false;
        }

        return true;
    }

    private Integer whetherRegionListCoverCpg(List<Region> regionList, Integer start, Integer end, Integer target) {
        if(start <= end){
            Integer middle = (start + end) / 2;
            Region middleRegion = regionList.get(middle);//中间值
            if (target < middleRegion.getStart()) {
                if (middle < 0) {
                    return 0;
                }
                return whetherRegionListCoverCpg(regionList, start, middle - 1, target);
            } else if (target >= middleRegion.getStart()) {
                if (target <= middleRegion.getEnd()) {
                    return 1;
                } else {
                    if (middle >= regionList.size() - 1) {
                        return 0;
                    }
                    return whetherRegionListCoverCpg(regionList, middle + 1, end, target);
                }
            }
        }
        return 0;
    }
}

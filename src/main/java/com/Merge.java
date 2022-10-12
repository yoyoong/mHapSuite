package com;

import com.args.MergeArgs;
import com.bean.MHapInfo;
import com.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class Merge {
    public static final Logger log = LoggerFactory.getLogger(Merge.class);

    MergeArgs args = new MergeArgs();
    Util util = new Util();

    public void merge(MergeArgs mergeArgs) throws Exception {
        log.info("command.Merge start!");
        args = mergeArgs;

        // 校验命令正确性
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        String mhapFileName = "";
        if (args.getOutPutFile() == null || args.getOutPutFile().equals("")) {
            mhapFileName = "out.mhap";
        } else {
            mhapFileName = args.getOutPutFile().substring(0, args.getOutPutFile().length() - 3);
        }
        BufferedWriter outputWriter = util.createOutputFile("", mhapFileName);

        List<BufferedReader> readerList = new ArrayList<>();
        List<MHapInfo> newLineList = new ArrayList<>();
        String[] fileList = args.getInputFile().split(" ");
        for (String fileName : fileList) {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
            InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String mHapLine = bufferedReader.readLine();
            MHapInfo mHapInfo = new MHapInfo(mHapLine.split("\t")[0], Integer.valueOf(mHapLine.split("\t")[1]),
                    Integer.valueOf(mHapLine.split("\t")[2]), mHapLine.split("\t")[3],
                    Integer.valueOf(mHapLine.split("\t")[4]), mHapLine.split("\t")[5]);
            newLineList.add(mHapInfo);
            readerList.add(bufferedReader);
        }

        long lineCnt = 0;
        while (readerList.size() > 0) {
            lineCnt++;
            if (lineCnt % 1000000 == 0) {
                log.info("read complete "  + lineCnt + " lines");
            }

            MHapInfo minLine = newLineList.get(0);
            List<Integer> minIndex = new ArrayList<>();
            for (int i = 0; i < newLineList.size(); i++) {
                if (newLineList.get(i).indexByReadAndStrand().compareTo(minLine.indexByReadAndStrand()) < 0) {
                    minIndex.clear();
                    minIndex.add(i);
                } else if (newLineList.get(i).indexByReadAndStrand().compareTo(minLine.indexByReadAndStrand()) == 0) {
                    minIndex.add(i);
                }
            }

            MHapInfo writeLine = newLineList.get(minIndex.get(0));
            String firstNewLine = readerList.get(minIndex.get(0)).readLine();
            if (firstNewLine == null || firstNewLine.equals("")) {
                List<BufferedReader> newReadList = new ArrayList<>();
                List<MHapInfo> newNewLineList = new ArrayList<>();
                for(int i = 0; i < readerList.size(); i++){
                    if (i != minIndex.get(0)) {
                        newReadList.add(readerList.get(i));
                        newNewLineList.add(newLineList.get(i));
                    } else {
                        for (int j = 0; j < minIndex.size(); j++) {
                            minIndex.set(j, minIndex.get(j) - 1);
                        }
                    }
                }
                readerList = newReadList;
                newLineList = newNewLineList;
            } else {
                MHapInfo firstNewMhap = new MHapInfo(firstNewLine.split("\t")[0], Integer.valueOf(firstNewLine.split("\t")[1]),
                        Integer.valueOf(firstNewLine.split("\t")[2]), firstNewLine.split("\t")[3],
                        Integer.valueOf(firstNewLine.split("\t")[4]), firstNewLine.split("\t")[5]);
                newLineList.set(minIndex.get(0), firstNewMhap);
            }

            for (int i = 1; i < minIndex.size(); i++) {
                writeLine.setCnt(writeLine.getCnt() + newLineList.get(minIndex.get(i)).getCnt());
                String nextNewLine = readerList.get(minIndex.get(i)).readLine();
                if (nextNewLine == null || nextNewLine.equals("")) {
                    List<BufferedReader> newReadList = new ArrayList<>();
                    List<MHapInfo> newNewLineList = new ArrayList<>();
                    for(int j = 0; j < readerList.size(); j++){
                        if (j != minIndex.get(i)) {
                            newReadList.add(readerList.get(j));
                            newNewLineList.add(newLineList.get(j));
                        } else {
                            for (int k = 0; k < minIndex.size(); k++) {
                                minIndex.set(k, minIndex.get(k) - 1);
                            }
                        }
                    }
                    readerList = newReadList;
                    newLineList = newNewLineList;
                } else {
                    MHapInfo nextNewMhap = new MHapInfo(nextNewLine.split("\t")[0], Integer.valueOf(nextNewLine.split("\t")[1]),
                            Integer.valueOf(nextNewLine.split("\t")[2]), nextNewLine.split("\t")[3],
                            Integer.valueOf(nextNewLine.split("\t")[4]), nextNewLine.split("\t")[5]);
                    newLineList.set(minIndex.get(i), nextNewMhap);
                }
            }
            //log.info(writeLine.print());
            outputWriter.write(writeLine.print() + "\n");
        }
        log.info("read complete "  + lineCnt + " lines");
        outputWriter.close();

        log.info("command.Merge end! ");
    }

    private boolean checkArgs() {

        return true;
    }

}

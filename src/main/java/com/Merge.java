package com;

import com.args.MergeArgs;
import com.bean.MHapInfo;
import com.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.remote.rmi._RMIConnection_Stub;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

        ArrayList<BufferedReader> readerList = new ArrayList<>();
        ArrayList<MHapInfo> newLineList = new ArrayList<>();
        String[] fileList = args.getInputFile().trim().split(" ");
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
        String thisChrom = newLineList.get(0).getChrom();
        String nextChrom = newLineList.get(0).getChrom();
        while (readerList.size() > 0) {
            lineCnt++;
            if (lineCnt % 1000000 == 0) {
                log.info("read complete "  + lineCnt + " lines. Now in " + newLineList.get(0).getChrom());
            }

            List<MHapInfo> newLineListSorted = new ArrayList<>();
            for (MHapInfo mHapInfo : newLineList) {
                if (mHapInfo.getChrom().equals(thisChrom)) {
                    newLineListSorted.add(mHapInfo);
                }
            }
            newLineListSorted.sort(Comparator.comparing(MHapInfo::sort));

            MHapInfo writeLine = newLineListSorted.get(0);
            ArrayList<Integer> moveIndexs = new ArrayList<>();
            moveIndexs.add(newLineList.indexOf(newLineListSorted.get(0)));
            for (int i = 1; i < newLineListSorted.size(); i++) {
                if (newLineListSorted.get(i).index().equals(writeLine.index())) {
                    writeLine.setCnt(writeLine.getCnt() + newLineListSorted.get(i).getCnt());
                    moveIndexs.add(newLineList.indexOf(newLineListSorted.get(i)));
                }
            }

            for (int i = 0; i < moveIndexs.size(); i++) {
                String newLine = readerList.get(moveIndexs.get(i)).readLine();
                if (newLine == null || newLine.equals("")) {
                    readerList.get(moveIndexs.get(i)).close();
                    readerList.remove((int) moveIndexs.get(i));
                    newLineList.remove((int) moveIndexs.get(i));
                    moveIndexs.remove(i);
                    for (int j = i; j < moveIndexs.size(); j++) {
                        moveIndexs.set(j, moveIndexs.get(j) - 1);
                    }
                    i--;
                } else {
                    MHapInfo mHapInfo = new MHapInfo(newLine.split("\t")[0], Integer.valueOf(newLine.split("\t")[1]),
                            Integer.valueOf(newLine.split("\t")[2]), newLine.split("\t")[3],
                            Integer.valueOf(newLine.split("\t")[4]), newLine.split("\t")[5]);
                    if (!mHapInfo.getChrom().equals(thisChrom)) {
                        nextChrom = mHapInfo.getChrom();
                    }
                    newLineList.set(moveIndexs.get(i), mHapInfo);
                }
            }

            boolean changeChrFlag = true;
            for (MHapInfo mHapInfo : newLineList) {
                if (!mHapInfo.getChrom().equals(nextChrom)) {
                    changeChrFlag = false;
                }
            }
            if (changeChrFlag) {
                thisChrom = nextChrom;
            }

            //log.info("writeLine: " + writeLine.print());
            outputWriter.write(writeLine.print() + "\n");
        }
        log.info("read complete "  + lineCnt + " lines");
        outputWriter.close();

        // generate the .gz file
        String gzFileName = mhapFileName + ".gz";
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(gzFileName));
        FileInputStream fileInputStream = new FileInputStream(mhapFileName);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = fileInputStream.read(buffer)) > 0) {
            gzipOutputStream.write(buffer, 0, len);
        }
        fileInputStream.close();
        gzipOutputStream.finish();
        gzipOutputStream.close();
        new File(mhapFileName).delete();

        log.info("command.Merge end! ");
    }

    private boolean checkArgs() {
        if (args.getInputFile().equals("")) {
            log.error("The input file cannot be empty.");
        }

        return true;
    }

}

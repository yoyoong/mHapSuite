package com;

import com.args.MergeArgs;
import com.bean.MHapInfo;
import com.common.Util;
import htsjdk.samtools.cram.compression.BZIP2ExternalCompressor;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
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

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // create the output file bufferWriter
        String mhapFileName = "";
        if (args.getOutputFile() == null || args.getOutputFile().equals("")) {
            mhapFileName = "out.mhap";
        } else {
            mhapFileName = args.getOutputFile().substring(0, args.getOutputFile().length() - 3);
        }
        BufferedWriter outputWriter = util.createOutputFile("", mhapFileName);

        // get the file and bufferReader list
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
                log.info("Merge complete "  + lineCnt + " lines. Now in " + thisChrom);
            }

            List<MHapInfo> newLineListFiltered = new ArrayList<>();
            for (MHapInfo mHapInfo : newLineList) {
                if (mHapInfo.getChrom().equals(thisChrom)) {
                    newLineListFiltered.add(mHapInfo);
                }
            }

            // find the minimum line index
            int minIndex = 0; // first minimum line index
            for (int i = 0; i < newLineListFiltered.size(); i++) {
                if (newLineListFiltered.get(minIndex).compareTo(newLineListFiltered.get(i)) > 0) {
                    minIndex = i;
                }
            }

            // summarize the cnt same minimum line and the bufferReader index should be move
            Integer minLineCnt = newLineListFiltered.get(minIndex).getCnt();
            MHapInfo writeLine = newLineListFiltered.get(minIndex);
            ArrayList<Integer> moveIndexs = new ArrayList<>();
            for (int i = 0; i < newLineListFiltered.size(); i++) {
                MHapInfo mHapInfo = newLineListFiltered.get(i);
                if (mHapInfo != null && newLineListFiltered.get(minIndex).compareTo(mHapInfo) == 0) {
                    writeLine.setCnt(writeLine.getCnt() + newLineListFiltered.get(i).getCnt());
                    moveIndexs.add(newLineList.indexOf(mHapInfo));
                }
            }
            writeLine.setCnt(writeLine.getCnt() - minLineCnt);

            for (int i = 0; i < moveIndexs.size(); i++) {
                String newLine = readerList.get(moveIndexs.get(i)).readLine();
                if (newLine == null || newLine.equals("")) { // if file move to end, remove this from list
                    readerList.get(moveIndexs.get(i)).close();
                    readerList.remove((int) moveIndexs.get(i));
                    newLineList.remove((int) moveIndexs.get(i));
                    moveIndexs.remove(i);
                    for (int j = i; j < moveIndexs.size(); j++) {
                        moveIndexs.set(j, moveIndexs.get(j) - 1);
                    }
                    i--;
                } else { // get the next line
                    MHapInfo mHapInfo = new MHapInfo(newLine.split("\t")[0], Integer.valueOf(newLine.split("\t")[1]),
                            Integer.valueOf(newLine.split("\t")[2]), newLine.split("\t")[3],
                            Integer.valueOf(newLine.split("\t")[4]), newLine.split("\t")[5]);
                    if (!mHapInfo.getChrom().equals(thisChrom)) {
                        nextChrom = mHapInfo.getChrom();
                    }
                    newLineList.set(moveIndexs.get(i), mHapInfo);
                }
            }

            // if all new line's chrom have change, change the thisChrom
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
        log.info("Merge succeed! Total "  + lineCnt + " lines");
        outputWriter.close();

        // convert mhap file to .gz file
        String gzFileName = mhapFileName + ".gz";
        InputStream inputStream = new FileInputStream(mhapFileName);
        OutputStream outputStream = new BlockCompressedOutputStream(new File(gzFileName));
        byte[] b = new byte[1024];
        int len = inputStream.read(b);
        while (len > 0) {
            outputStream.write(b, 0, len);
            len = inputStream.read(b);
        }
        inputStream.close();
        outputStream.close();
        new File(mhapFileName).delete();

        log.info("command.Merge end! ");
    }

    private boolean checkArgs() {
        if (args.getInputFile().equals("")) {
            log.error("The input file cannot be empty.");
            return false;
        }

        return true;
    }

}

package com.bean;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MHapInfo implements Serializable {
    public String chrom;
    public Integer start;
    public Integer end;
    public String cpg;
    public Integer cnt;
    public String strand;
    public List<Integer> cpgPosList;
    public String haploString;
    public List<Integer> qualityList;
    public String readName;

    public MHapInfo(String chrom, Integer start, Integer end, String cpg, Integer cnt, String strand) {
        this.chrom = chrom;
        this.start = start;
        this.end = end;
        this.cpg = cpg;
        this.cnt = cnt;
        this.strand = strand;
    }

    public MHapInfo(String chrom, Integer start, Integer end, String cpg, Integer cnt, String strand,
                    List<Integer> cpgPosList, String haploString, List<Integer> qualityList, String readName) {
        this.chrom = chrom;
        this.start = start;
        this.end = end;
        this.cpg = cpg;
        this.cnt = cnt;
        this.strand = strand;
        this.cpgPosList = cpgPosList;
        this.haploString = haploString;
        this.qualityList = qualityList;
        this.readName = readName;
    }

    public String getChrom() {
        return chrom;
    }

    public void setChrom(String chrom) {
        this.chrom = chrom;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public String getHaploString() {
        return haploString;
    }

    public void setHaploString(String haploString) {
        this.haploString = haploString;
    }

    public String getCpg() {
        return cpg;
    }

    public void setCpg(String cpg) {
        this.cpg = cpg;
    }

    public Integer getCnt() {
        return cnt;
    }

    public void setCnt(Integer cnt) {
        this.cnt = cnt;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public List<Integer> getCpgPosList() {
        return cpgPosList;
    }

    public void setCpgPosList(List<Integer> cpgPosList) {
        this.cpgPosList = cpgPosList;
    }

    public List<Integer> getQualityList() {
        return qualityList;
    }

    public void setQualityList(List<Integer> qualityList) {
        this.qualityList = qualityList;
    }

    public String getReadName() {
        return readName;
    }

    public void setReadName(String readName) {
        this.readName = readName;
    }

    public String indexByPos() {
        return this.chrom + this.start;
    }

    public String indexByRead() {
        return this.chrom + this.start+ this.end + this.cpg;
    }

    public String indexByReadAndStrand() {
        return this.chrom + "_" + this.start + "_" + this.end + "_" + this.cpg + "_" + this.strand;
    }

    public String index() {
        return this.chrom + this.start+ this.end + this.cpg + this.strand;
    }

    public String sort() {
        DecimalFormat decimalFormat = new DecimalFormat("0000000000");
        String startFormat = decimalFormat.format(this.start);
        String endFormat = decimalFormat.format(this.end);
        return this.chrom + startFormat + endFormat + this.cpg + this.strand;
    }

    public String print() {
        return this.chrom + "\t" + this.start + "\t"+ this.end + "\t"
                + this.cpg + "\t" + this.cnt + "\t" + this.strand;
    }

    public int compareTo(MHapInfo mHapInfo) {
        DecimalFormat decimalFormat = new DecimalFormat("0000000000");
        String start1 = decimalFormat.format(this.start);
        String end1 = decimalFormat.format(this.end);
        String start2 = decimalFormat.format(mHapInfo.getStart());
        String end2 = decimalFormat.format(mHapInfo.getEnd());

        String mhapString1 = this.chrom + start1 + end1 + this.cpg + this.strand;
        String mhapString2 = mHapInfo.getChrom() + start2 + end2 + mHapInfo.getCpg() + mHapInfo.getStrand();

        return mhapString1.compareTo(mhapString2);
    }
}

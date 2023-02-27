package com.args;

import com.common.Annotation;

import java.io.Serializable;

public class ConvertArgs implements Serializable {
    @Annotation("input file, SAM/BAM format, should be sorted by samtools")
    public String inputFile = "";
    @Annotation("genomic CpG file, gz format and indexed")
    public String cpgPath = "";
    @Annotation("one region, in the format of chr:start-end")
    public String region = "";
    @Annotation("bed file, one query region per line")
    public String bedFile = "";
    @Annotation("non-directional, do not group results by the direction of reads")
    public boolean nonDirectional = false;
    @Annotation("output filename. (default: out.mhap.gz)")
    public String outPutFile = "";
    @Annotation("sequencing mode. ( TAPS | BS (default) )")
    public String mode = "BS";
    @Annotation("whether inputPath is pat file")
    public boolean pat = false;

    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public String getCpgPath() {
        return cpgPath;
    }

    public void setCpgPath(String cpgPath) {
        this.cpgPath = cpgPath;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBedFile() {
        return bedFile;
    }

    public void setBedFile(String bedFile) {
        this.bedFile = bedFile;
    }

    public boolean isNonDirectional() {
        return nonDirectional;
    }

    public void setNonDirectional(boolean nonDirectional) {
        this.nonDirectional = nonDirectional;
    }

    public String getOutPutFile() {
        return outPutFile;
    }

    public void setOutPutFile(String outPutFile) {
        this.outPutFile = outPutFile;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isPat() {
        return pat;
    }

    public void setPat(boolean pat) {
        this.pat = pat;
    }
}

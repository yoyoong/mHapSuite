package com.args;

import com.common.Annotation;

import java.io.Serializable;

public class MergeArgs implements Serializable {
    @Annotation("input files, multiple .mhap.gz files to merge")
    public String inputFile = "";
    @Annotation("genomic CpG file, gz format and Indexed")
    public String cpgPath = "";
    @Annotation("output filename [out.mhap.gz]")
    public String outPutFile = "";

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

    public String getOutPutFile() {
        return outPutFile;
    }

    public void setOutPutFile(String outPutFile) {
        this.outPutFile = outPutFile;
    }
}

package com.args;

import java.io.Serializable;

public class MergeArgs implements Serializable {
    public String inputFile = ""; // input files, multiple .mhap.gz files to merge
    public String cpgPath = ""; // genomic CpG file, gz format and Indexed
    public String outPutFile = ""; // output filename [out.mhap.gz]

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

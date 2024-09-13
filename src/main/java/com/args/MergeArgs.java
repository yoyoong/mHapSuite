package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class MergeArgs implements Serializable {
    @Annotation(Constants.MULTIMHAPPATH_DESCRIPTION)
    public String inputFile = "";
    @Annotation(Constants.CPGPATH_DESCRIPTION)
    public String cpgPath = "";
    @Annotation(Constants.OUTPUTFILE_DESCRIPTION + "[out.mhap.gz]")
    public String outputFile = "";

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

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }
}

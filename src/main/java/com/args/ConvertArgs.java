package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class ConvertArgs implements Serializable {
    @Annotation(Constants.SAMBAMPATH_DESCRIPTION)
    public String inputFile = "";
    @Annotation(Constants.CPGPATH_DESCRIPTION)
    public String cpgPath = "";
    @Annotation(Constants.CPGPATH_DESCRIPTION)
    public String region = "";
    @Annotation(Constants.BEDPATH_DESCRIPTION)
    public String bedPath = "";
    @Annotation(Constants.NONDIRECTIONAL_DESCRIPTION)
    public boolean nonDirectional = false;
    @Annotation(Constants.OUTPUTFILE_DESCRIPTION + "[out.mhap.gz]")
    public String outPutFile = "";
    @Annotation(Constants.MODE_DESCRIPTION)
    public String mode = "BS";
    @Annotation(Constants.PAT_DESCRIPTION)
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

    public String getBedPath() {
        return bedPath;
    }

    public void setBedPath(String bedPath) {
        this.bedPath = bedPath;
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

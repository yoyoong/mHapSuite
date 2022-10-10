package com.args;

import java.io.Serializable;

public class ConvertArgs implements Serializable {
    public String inputFile = "";
    public String cpgPath = "";
    public String region = "";
    public String bedFile = "";
    public boolean non_directional = false;
    public String outPutFile = "";
    public String mode = "BS";

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

    public boolean isNon_directional() {
        return non_directional;
    }

    public void setNon_directional(boolean non_directional) {
        this.non_directional = non_directional;
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
}

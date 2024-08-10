/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yatra.payment.offline.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author YATRAONLINE\rohit.lohia
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ClientFileDisplayMods")
public class ClientFileDisplayMods implements ClientFileMods {
    @XmlElement(name = "CRSID")
    private String crsId;
    @XmlElement(name = "PCC")
    private String pcc;
    @XmlElement(name = "BusinessTitle")
    private String businessTitle;
    @XmlElement(name = "PersonalTitle")
    private String personalTitle;
    @XmlElement(name = "FileInd")
    private String fileInd;
    @XmlElement(name = "MergeInd")
    private String mergeInd;

    public String getCrsId() {
        return crsId;
    }

    public void setCrsId(String crsId) {
        this.crsId = crsId;
    }

    public String getPcc() {
        return pcc;
    }

    public void setPcc(String pcc) {
        this.pcc = pcc;
    }

    public String getBusinessTitle() {
        return businessTitle;
    }

    public void setBusinessTitle(String businessTitle) {
        this.businessTitle = businessTitle;
    }

    public String getPersonalTitle() {
        return personalTitle;
    }

    public void setPersonalTitle(String personalTitle) {
        this.personalTitle = personalTitle;
    }

    public String getFileInd() {
        return fileInd;
    }

    public void setFileInd(String fileInd) {
        this.fileInd = fileInd;
    }

    public String getMergeInd() {
        return mergeInd;
    }

    public void setMergeInd(String mergeInd) {
        this.mergeInd = mergeInd;
    }
    
    
}

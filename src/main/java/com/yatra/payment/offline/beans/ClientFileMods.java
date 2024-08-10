/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yatra.payment.offline.beans;

/**
 *
 * @author YATRAONLINE\rohit.lohia
 */
public interface ClientFileMods {
    
    public void setCrsId(String crsId);
    public void setPcc(String pcc);
    public void setBusinessTitle(String businessTitle);
    public void setPersonalTitle(String personalTitle);
    public void setFileInd(String fileInd);
    public void setMergeInd(String mergeInd);
}

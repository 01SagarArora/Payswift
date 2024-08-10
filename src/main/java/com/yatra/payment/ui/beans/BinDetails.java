package com.yatra.payment.ui.beans;

import java.io.Serializable;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BinDetails implements Serializable {
    private static final long serialVersionUID = 1211171035917858409L;

    private String binNumber;
    private String bankId;
    private String binType;
    private boolean isCardInternational;
    private boolean isCorporateSupported;
    private boolean isAtmSupportedOnBin;
    private boolean isAtmSupportedOnBank;
    private boolean isOtpSupportedOnBank;
    private String multiPayFlowSequence;
    private String bankName;
    private String bankCode;
    
    private String fromBin;
	private String toBin;
	
    public String getFromBin() {
		return fromBin;
	}

	public void setFromBin(String fromBin) {
		this.fromBin = fromBin;
	}

	public String getToBin() {
		return toBin;
	}

	public void setToBin(String toBin) {
		this.toBin = toBin;
	}

	public BinDetails() {}

    public String getBinNumber() {
        return binNumber;
    }

    public String getBankId() {
        return bankId;
    }
    
    public String getBankName() {
        return bankName;
    }

    public String getBinType() {
        return binType;
    }
    
    public boolean isCardInternational() {
        return isCardInternational;
    }

    public boolean isCorporateSupported() {
        return isCorporateSupported;
    }

    public boolean isAtmSupportedOnBin() {
        return isAtmSupportedOnBin;
    }

    public boolean isAtmSupportedOnBank() {
        return isAtmSupportedOnBank;
    }

    public boolean isOtpSupportedOnBank() {
        return isOtpSupportedOnBank;
    }

    public String getMultiPayFlowSequence() {
        return multiPayFlowSequence;
    }

    public void setBinNumber(String binNumber) {
        this.binNumber = binNumber;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public void setBinType(String binType) {
        this.binType = binType;
    }
    
    public void setCardInternational(boolean cardInternational) {
        isCardInternational = cardInternational;
    }

    public void setIsCorporateSupported(boolean corporateSupported) {
        isCorporateSupported = corporateSupported;
    }

    public void setAtmSupportedOnBin(boolean atmSupportedOnBin) {
        isAtmSupportedOnBin = atmSupportedOnBin;
    }

    public void setAtmSupportedOnBank(boolean atmSupportedOnBank) {
        isAtmSupportedOnBank = atmSupportedOnBank;
    }

    public void setOtpSupportedOnBank(boolean otpSupportedOnBank) {
        isOtpSupportedOnBank = otpSupportedOnBank;
    }

    public void setMultiPayFlowSequence(String multiPayFlowSequence) {
        this.multiPayFlowSequence = multiPayFlowSequence;
    }
    

    public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}

	private BinDetails(BinDetailsBuilder binDetailsBuilder) {
        this.binNumber = binDetailsBuilder.binNumber;
        this.bankId = binDetailsBuilder.bankId;
        this.binType = binDetailsBuilder.binType;
        this.isCardInternational = binDetailsBuilder.isCardInternational;
        this.isCorporateSupported = binDetailsBuilder.isCorporateSupported;
        this.isAtmSupportedOnBin = binDetailsBuilder.isAtmSupportedOnBin;
        this.isAtmSupportedOnBank = binDetailsBuilder.isAtmSupportedOnBank;
        this.isOtpSupportedOnBank = binDetailsBuilder.isOtpSupportedOnBank;
        this.multiPayFlowSequence = binDetailsBuilder.multiPayFlowSequence;
        this.bankName = binDetailsBuilder.bankName;
        this.bankCode = binDetailsBuilder.bankCode;
        
    }

    public static class BinDetailsBuilder {
        private String binNumber;
        private String bankId;
        private String binType;
        private boolean isCardInternational;
        private boolean isCorporateSupported;
        private boolean isAtmSupportedOnBin;
        private boolean isAtmSupportedOnBank;
        private boolean isOtpSupportedOnBank;
        private String multiPayFlowSequence;
        private String bankName;
        private String bankCode ;

        public BinDetailsBuilder withBinNumber(String binNumber) {
            this.binNumber = binNumber;
            return this;
        }

        public BinDetailsBuilder withBankId(String bankId) {
            this.bankId = bankId;
            return this;
        }
        
        public BinDetailsBuilder withBinType(String binType) {
            this.binType = binType;
            return this;
        }
        
        public BinDetailsBuilder withBankName(String bankName) {
            this.bankName = bankName;
            return this;
        }

        public BinDetailsBuilder withMultiPayFlowSequence(String multiPayFlowSequence) {
            this.multiPayFlowSequence = multiPayFlowSequence;
            return this;
        }

        public BinDetailsBuilder withOtpSupportedOnBank(boolean otpSupportedOnBank) {
            this.isOtpSupportedOnBank = otpSupportedOnBank;
            return this;
        }

        public BinDetailsBuilder withAtmSupportedOnBank(boolean atmSupportedOnBank) {
            this.isAtmSupportedOnBank = atmSupportedOnBank;
            return this;
        }

        public BinDetailsBuilder withAtmSupportedOnBin(boolean atmSupportedOnBin) {
            isAtmSupportedOnBin = atmSupportedOnBin;
            return this;
        }

        public BinDetailsBuilder withCorporateSupported(boolean corporateSupported) {
            this.isCorporateSupported = corporateSupported;
            return this;
        }

        public BinDetailsBuilder withCardInternational(boolean cardInternational) {
            this.isCardInternational = cardInternational;
            return this;
        }
        public BinDetailsBuilder withBankCode(String bankCode) {
        	            this.bankCode = bankCode;
        	            return this;
        	        }


        public BinDetails build() {
            return new BinDetails(this);
        }
    }
}

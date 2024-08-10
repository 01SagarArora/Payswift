package com.yatra.payment.qb.corporate.bean;

import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.yatra.payment.qb.corporate.constant.QBConstant;

public class GetEntityGroupsResponse {
	
	private String status;
	@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL) 
	@JsonProperty("error_msg")
	private String errorMessage;
	private List<String> groupCodes = Collections.emptyList();
	
	public GetEntityGroupsResponse(String errorMessage) {
		this.status = QBConstant.STATUS_FAILURE;
		this.errorMessage = errorMessage;
	}
	
	public GetEntityGroupsResponse(List<String> groupCodes) {
		this.status = QBConstant.STATUS_SUCCESS;
		this.groupCodes = groupCodes;
	}

	public List<String> getGroupCodes() {
		return groupCodes;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setGroupCodes(List<String> groupCodes) {
		this.groupCodes = groupCodes;
	}
	
	/*public void addGroupCode(String groupCode) {
		if(this.groupCodes == null)
			this.groupCodes = Collections.emptyList();
		this.groupCodes.add(groupCode);
	}
	
	public void addGroupCodes(List<String> groupCodes) {
		if(this.groupCodes == null)
			this.groupCodes = Collections.emptyList();
		this.groupCodes.addAll(groupCodes);
	}*/
}

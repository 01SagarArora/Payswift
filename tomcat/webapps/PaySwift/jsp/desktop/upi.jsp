<!--UPI starts here*-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="content cpmt_payop" id="tab_upi" style="display:none;">
<c:if test="${not empty payopMessage.upi}">
	<c:forEach var="messageObj" items="${payopMessage.upi}">
		<div class="pmt_msg_${messageObj.messageType} ui_payop_msg">
			<span><yatra:languageTag content="${messageObj.messageText}" language="${language}"/></span>
		</div>
	</c:forEach>
</c:if>
<span class="head-tp">Pay with UPI</span>

<div class="upiRow">
  <div class="upiColumn">
    <div class="upi-1 upiSteps">
    
    </div>
    <p>Keep UPI app open on your mobile phone.</p>
  </div>
  <div class="upiColumn" >
     <div class="upi-2 upiSteps">
    
    </div>
    <p>Enter VPA and click on Pay Now.</p>
  </div>
  <div class="upiColumn">
     <div class="upi-3 upiSteps">
    
    </div>
    <p>Yatra will send a payment request to your UPI app.</p>
  </div>
  
  <div class="upiColumn">
     <div class="upi-4 upiSteps">
    
    </div>
    <p>Enter MPIN on your UPI app to authorize payment.</p>
  </div>
  
  <div class="upiColumn">
     <div class="upi-5 upiSteps">
    
    </div>
    <p>Congratulations your payment is done.</p>
  </div>
</div>


	<input type="hidden" name="payop" value="upi"/>
	<input id="sub_payopQR" type="hidden" name="sub_payop" value/>
	<input type="hidden" name="bankCode" value=""/>
	<input type="hidden" id="iframeEnabledFor-upi" value=""/>
	<input type="hidden" id="iframeEnabledFor-upi-bank" value=""/>
	<div style="float:;clear: both;width: 100%;overflow: hidden;">
	
	<div class="UPIDiv">

	<label for="payerVA" class="cpmt_cardNameLabel">Virtual Payment Address <i class="ico_info_paySwift PaymentSprite" style="display: inline-block; left: 2%;" onClick="showFAQVPA()"></i>   </label>


		<input type="text" placeholder="Enter your Mobile Number or UPI ID" autocomplete="off" data-msginfo="* required|Enter Valid Virtual Address" data-validation="required|checkVA" name="payerVA" id="payerVA" class="w300 ie6Input">
	</div>
  <div class="UPIor" style="display:none"></div>
	<div class="QRDiv" style="display:none" id="QRCodeDiv" >

	<label class="cpmt_cardNameLabel">Don't have VPA? Scan QR with your UPI app</label>

		<input type="button" value="Show QR Code" id="showQRButton" onClick="ProcessPayment.QRPayNowRequest()" class="showQRButton">

		<div class="loading-dots" id="loadingQR"><span style="color:#ea2330">Loading</span>

  <div class="loading-dots--dot"></div>

  <div class="loading-dots--dot"></div>

 <div class="loading-dots--dot"></div>

			</div>

		<div id="qrCode">

		<img id="QRCodeImage" style="display:none">

		</div>

		

	</div>
	
	</div>
</div>
<!--UPI ends here-->
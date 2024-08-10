<y:purl var="cssversion" key="jsversion" def="1"/>
<link rel="stylesheet" href="http://devyatra.com/css/hotel/hotel.css?ver=${cssversion}" type="text/css" />
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script>
var trackingJsonList=${trackingJsonList};
</script>


<div id="ContentContainer">
	<!-- Error Strat -->
	<div id="ErrorContainer">
		<div class="error_container_top"><div class="cbox_top_r"></div></div>
		<div class="error_container_center">
			<div class="error_container_center_r">
				<div id="errormsgcontent-fetal"><fmt:message key="yatra.fatal.error"/></div>				
				<!--<div id="timer-limit"></div>-->				
			</div>
		</div>
		<div class="error_container_bottom"><div class="cbox_bot_r"></div></div>
	</div>
	<!-- Error Strat -->
</div>

<!-- SCRIPT FOR TIMER FOR 10 SECONDS -->
<!--
<script type="text/javascript">
var c=0;
var t;
var timer_is_on=0;
function timedCount()
{
	document.getElementById('timer-limit').innerHTML=c;
	c=c+1;
	if (c>10)
	{
		window.location="https://www.yatra.com/";
	}
	t=setTimeout("timedCount()",1000);
}
function doTimer()
{
if (!timer_is_on)
  {
  timer_is_on=1;
  timedCount();
  }
}
onload=function()
{
	doTimer();
}
</script>-->
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>

 <style type="text/css">
        
        /* Task1 */

  .overlay {
    position: fixed;
    top: 0;
    bottom: 0;
    left: 0;
    right: 0;
    background: rgba(0, 0, 0, 0.7);
    transition: opacity 500ms;
    visibility: visible;
    opacity: 1;
  }
  .popup {
    width: 500px;
    padding: 20px;
    background: #fff;
    position: absolute;
    border:1px solid black;
    top: 0;
    left:30%;
    box-sizing:  border-box;
  } 
  .popup h2 {
    margin-top: 0;
    color: #e05343;
    font-family: Arial;
  }
  .popup .close {
    position: absolute;
    top: 4px;
    right: 20px;
    font-size: 30px;
    text-decoration: none;
    color: #333;
  }
  .popup .content {
    line-height: 28px;
    color: #333;
    margin-top: 26px;
    font-size: 17px;
  }
  .content p{
   color:#333;
  }
  .popup-buttons{
      margin-top:19px;
      padding-bottom: 19px;
  }
  .button {
    border: none;
    color: white;
    padding: 10px 20px;
    text-align: center;
    text-decoration: none;
    display: inline-block;
    font-size: 16px;
    cursor: pointer;
    border:1px solid #ccc;    
    border-radius: 4px;
}
  .popup-hallow-btn {
    margin-right: 10px;
    background-color: white;
    color:black;
  }
  .popup-primary-btn {
    background:#e05343;
   
  }
  .popup-hallow-btn:hover,
  .popup-hallow-btn:focus,
  .popup-hallow-btn:active{
    background:#e05343;
    color:white;
  }


  
.popup-primary-btn:active, 
.popup-primary-btn:hover,
.popup-primary-btn:focus
{

    background: #cc4030;
    color:white;
  }


  @media screen and (max-width: 767px){
    .box{
      width: 100%;
    }
    .popup{
      width: 100%;
    }
  }
  //loader starts here
  
    .loader-container{
               display: block;
    background: #fff;
    border-radius: 4px;
    text-align: center;
    position: absolute;
    padding: 10px;
    box-shadow: 0px 2px 3px rgba(0,0,0,0.2);
    width: 185px;
    left: 0px;
    right: 0px;
    margin: 0 auto;
            }
            .loader-text{
                font-size: 22px;
            }
  
.circle {
  display: inline-block;
  position: relative;
  width: 64px;
  height: 24px;
  top: -20px;
}
.circle  div {
  position: absolute;
  top: 27px;
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #ea2330;
  animation-timing-function: cubic-bezier(0, 1, 1, 0);
}
.circle  div:nth-child(1) {
  left: 6px;
  animation: custom 0.6s infinite;
}
.circle  div:nth-child(2) {
  left: 6px;
  animation: custom1 0.6s infinite;
}
.circle  div:nth-child(3) {
  left: 26px;
  animation: custom1 0.6s infinite;
}
.circle  div:nth-child(4) {
  left: 45px;
  animation: custom2 0.6s infinite;
}
@keyframes custom {
  0% {
    transform: scale(0);
  }
  100% {
    transform: scale(1);
  }
}
@keyframes custom2 {
  0% {
    transform: scale(1);
  }
  100% {
    transform: scale(0);
  }
}
@keyframes custom1 {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(19px, 0);
  }
}
            
          
//loader end here
        </style>

       <div class="loader-container modal" id="loading-popup-id" style="
       
       
       display: none;
    background: #fff;
    max-width: 185px;
    overflow: hidden;
    height: 90px;
    border-radius: 4px;
    margin: 0 auto;
    text-align:center;
    padding:20px;
    box-shadow:0px 2px 3px rgba(0,0,0,0.2);">
            <p class="loader-text">Processing</p>
        <div class="circle">
            
            <div></div>
            <div></div>
            <div></div>
        </div>
            </div>
    
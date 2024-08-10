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


        </style>
 <div class="modal fade yt-modal" id="delete-yatra-card-popup-id" tabindex="-1" role="dialog" aria-labelledby="mySmallModalLabel" aria-hidden="true" style="display: none;">
    <div class="popup">
        <a class="close" href="#" data-dismiss="modal">&times;</a>
        <div class="content">
          <h2>Are you sure!</h2>
           <p>You want to delete this card?</p> 
           <p>Post deletion, this card won't be used for ticketing on GDS even if it has been used in Yatra Cards Configuration. </p>
        
           
        </div>
        <div class="popup-buttons delete_bts">
           <button confirm="yes" class="button popup-hallow-btn active" id="btn1">Yes I want to</button>
           <button confirm="no" class="button popup-primary-btn">No</button>
          
        </div>
       
    </div>
      
 </div> 
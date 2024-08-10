 <div id="overlay">
      <div class="slide-out slide-left">
        <div class="close-wrapper" onClick="hideFAQVPA()"><a>x</a></div>
        <div class="vpa-faq">
          <div class="heading yt-grad">
            FAQ for VPA (Virtual Payment Address)
          </div>
          <div class="vpa-faq-content">
            <button class="vpa-accordion">What is VPA ?</button>
            <div class="vpa-panel">
              <p>Virtual Payment Address also referred as VPA is something like an email-ID, which is given to an individual using the Unified Payment Interface (UPI) service to send or receive money. </p>
            </div>

            <button class="vpa-accordion">How to create a VPA?</button>
            <div class="vpa-panel">
              <ul class="inner-list">
                <li>
                  Download a UPI-enabled app (example Google Pay, PhonePe, Paytm, BHIM, MobiKwik, Airtel Payments Bank etc)
                </li>
                <li>
                  Provide your bank account details
                </li>
                <li>
                  Choose a VPA
                </li>
                <li>
                  Link VPA to bank account
                </li>
                <li>
                  Submit details after verification
                </li>
              </ul>
            </div>

            <button class="vpa-accordion">
              Is it possible to link multiple bank account to one VPA?
            </button>
            <div class="vpa-panel">
              <p>Yes. This is possible. The same VPA can be linked to different banks accounts.</p>
            </div>
            <button class="vpa-accordion">
              If I have an existing VPA, can i link the same to a new app?
            </button>
            <div class="vpa-panel">
              <p>Yes. This will be possible but it also depends on the type of app you are using to make the payment or initiate the fund transfer. Some banks do not allow you to use an already existing VPA.</p>
            </div>
            <button class="vpa-accordion">
              Will my VPA expire, If i did not use?
            </button>
            <div class="vpa-panel">
              <p>Even if you do not use it for a set period of time, it will not expire.</p>
            </div>
            <button class="vpa-accordion">
              Is any other bank account information required, If you are using
              VPA?
            </button>
            <div class="vpa-panel">
              <p>No. Just the VPA is required.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <script>
      var acc = document.getElementsByClassName("vpa-accordion");
      var i;

      for (i = 0; i < acc.length; i++) {
        acc[i].addEventListener("click", function() {
          this.classList.toggle("active");
          var panel = this.nextElementSibling;
          if (panel.style.display === "block") {
            panel.style.display = "none";
          } else {
            panel.style.display = "block";
          }
        });
      }
      
      function showFAQVPA(){
    	  $("body").css({"overflow-x":"hidden"});
    	  $("#VPA_FAQ").show()
      }
      
      
      function hideFAQVPA(){
    	  $("body").css({"overflow-x":""});
    	  $("#VPA_FAQ").hide()
      }
    </script>    

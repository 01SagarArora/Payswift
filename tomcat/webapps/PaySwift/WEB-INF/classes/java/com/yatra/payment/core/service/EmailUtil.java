package com.yatra.payment.core.service;

import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * User: jainku
 * Date: Apr 14, 2009
 * Time: 4:08:56 PM
 */
public class EmailUtil  implements Runnable{

    private static Logger logger = Logger.getLogger(EmailUtil.class);
    private static int mailattempt=0;
    private boolean error=false;

    private JavaMailSenderImpl javaMailSender;
    private MimeMessage mimeMessage;
    
    int maxemailtrials = Integer.parseInt("10");
	

    public EmailUtil(JavaMailSenderImpl javaMailSender, MimeMessage mimeMessage)
    {
        this.javaMailSender = javaMailSender;
        this.mimeMessage = mimeMessage;
    }

    public void run()
    {
    	logger.info("sending email through : "+this.javaMailSender.getHost());
    	// try email sending for maximum allowable number of trials 
    	for(int i=1;i<=maxemailtrials ;i++){
        try
        {
        this.javaMailSender.send(this.mimeMessage);
        logger.info("email sent successfully");
        break;
        }
        catch (MailException ex)
        {   if(i==maxemailtrials){
        	logger.error("Error in sending email"+ex);
            throw new RuntimeException(ex);
            }
        }
    }

}
}

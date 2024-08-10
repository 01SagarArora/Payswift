package com.yatra.payment.core.service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


/**
 * User: jainku
 * Date: Apr 14, 2009
 * Time: 4:24:44 PM
 */
@Service
public class EmailService {

    private static Logger logger = Logger.getLogger(EmailService.class);

    @Autowired
    private MailSender mailSender;
    

    public void sendEmailWithCodeNew(String emailIdTo,  String emailIdFrom, String emailSubject, String text)
            throws RuntimeException
    {
        logger.info("Sending email to : "+emailIdTo+", with Subject : "+emailSubject);
        MimeMessage msg = ((JavaMailSenderImpl)mailSender).createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(msg,true);
            helper.setTo(emailIdTo);
            helper.setReplyTo("notification.paymentsdev@yatra.com");
            helper.setSubject(emailSubject);
            helper.setBcc("notification.paymentsdev@yatra.com");
            helper.setText(text);
            helper.setFrom(emailIdFrom);
        }catch(MessagingException ex){
            logger.error("Exception during creation of mail message !"+ex.getMessage());
        }

        try
        {
            Thread mailThread = new Thread(new EmailUtil((JavaMailSenderImpl)mailSender, msg));
            mailThread.start();

        }
        catch (MailException ex)
        {
            logger.error("Error in sending email"+ex);
            throw new RuntimeException(ex);
        }
    }
   
    
}

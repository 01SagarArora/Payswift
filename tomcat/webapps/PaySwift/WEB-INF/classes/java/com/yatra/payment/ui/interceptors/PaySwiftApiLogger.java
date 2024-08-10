package com.yatra.payment.ui.interceptors;

import com.yatra.platform.commons.property.PropertyManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PaySwiftApiLogger extends HandlerInterceptorAdapter {

    private static final Logger logger = Logger.getLogger(PaySwiftApiLogger.class);
    private static int responseTimeThreshold;

    @Autowired
    private PropertyManager propertyManager;

    @PostConstruct
    public void postConstruct() throws Exception {
        responseTimeThreshold = propertyManager.getPropertyAsInt("api.response.time.threshold.ms");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        logger.info("Request URL:" + request.getRequestURL().toString() + ", Start Time: " + startTime);
        request.setAttribute("startTime", startTime);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        long startTime = (Long) request.getAttribute("startTime");
        long responseTime = System.currentTimeMillis() - startTime;
        logger.info("Request URL:" + request.getRequestURI() + ", Total Time taken: " + responseTime);

        if(responseTime > responseTimeThreshold){
            logger.error("ResponseTime Threshold crossed - " + request.getRequestURI() + " : " + responseTime);
        }
    }

}

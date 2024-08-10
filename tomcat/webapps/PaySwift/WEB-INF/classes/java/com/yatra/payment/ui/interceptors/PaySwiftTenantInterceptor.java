package com.yatra.payment.ui.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.yatra.platform.tenant.TenantKeeper;

public class PaySwiftTenantInterceptor extends HandlerInterceptorAdapter
{
	private long PAYSWIFT_TENANT_ID = 1011;
	
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		TenantKeeper.setTenantId(PAYSWIFT_TENANT_ID);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    	TenantKeeper.removeTenantId();
    }

}

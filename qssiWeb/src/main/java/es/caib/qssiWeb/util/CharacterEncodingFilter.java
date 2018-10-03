package es.caib.qssiWeb.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

@WebFilter(filterName="CharacterEncodingFilter")
public class CharacterEncodingFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");
		filterChain.doFilter(request,response);
    }
		 
    public void init(FilterConfig filterConfig) throws ServletException
	{
	    // We can initialize a filter using the init-params here
	    // (which we defined in the deployment descriptor - web.xml)
	}
    
    public void destroy()
    {
    	
    }
}
package jp.dodododo.sql.servlet;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import jp.dodododo.sql.flyweight.FlyweightFactory;
import jp.dodododo.sql.log.SqlLogRegistry;
import jp.dodododo.sql.object.PropertyDesc;

public class ThreadLocalCleanerFilter implements Filter {

	/**
	 * @see jakarta.servlet.Filter#doFilter(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse, jakarta.servlet.FilterChain)
	 */
    @Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			chain.doFilter(request, response);
		} finally {
			SqlLogRegistry.getInstance().clear();
			FlyweightFactory.dispose();
			PropertyDesc.cacheModeOff();
		}
	}

	/**
	 * @see jakarta.servlet.Filter#init(jakarta.servlet.FilterConfig)
	 */
    @Override
	public void init(FilterConfig config) throws ServletException {
	}

	/**
	 * @see jakarta.servlet.Filter#destroy()
	 */
    @Override
	public void destroy() {
	}

}

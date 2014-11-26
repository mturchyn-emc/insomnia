package mturchyn.blackwater.web.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class TimeLoggerFilter implements Filter {

    private final static Logger LOG = LogManager.getLogger(TimeLoggerFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing.
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        Long startTime = System.currentTimeMillis();
        try {
            chain.doFilter(req, res);
        } finally {
            HttpServletRequest request = (HttpServletRequest) req;
            long takenInMs = System.currentTimeMillis() - startTime;
            LOG.debug("Request [{} {}] was processed in {} ms.",
              request.getMethod(),
              request.getRequestURI(),
              takenInMs
            );
        }
    }

    @Override
    public void destroy() {
        // Nothing.
    }
}

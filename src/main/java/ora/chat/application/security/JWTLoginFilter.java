package ora.chat.application.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import ora.chat.application.models.AccountCredentials;
import ora.chat.application.models.wrapper.OutputResults;
import ora.chat.application.models.Users;
import ora.chat.application.services.TokenHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {

    private TokenHelper tokenHelper;

    static final String TOKEN_PREFIX = "Bearer";
    static final String HEADER_STRING = "Authorization";

    public JWTLoginFilter(String url, AuthenticationManager authManager) {
        super(new AntPathRequestMatcher(url));
        setAuthenticationManager(authManager);
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest req, HttpServletResponse res)
            throws AuthenticationException, IOException, ServletException {

        AccountCredentials creds = new ObjectMapper()
                .readValue(req.getInputStream(), AccountCredentials.class);

        return getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(
                        creds.getEmail(),
                        creds.getPassword(),
                        Collections.emptyList()
                )
        );



    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest req,
            HttpServletResponse res, FilterChain chain,
            Authentication auth) throws IOException, ServletException {

        tokenHelper = WebApplicationContextUtils
                .getWebApplicationContext(req.getServletContext()).getBean(TokenHelper.class);
        String toke = tokenHelper.getToken(req) != null ?  tokenHelper.getToken(req) :
                tokenHelper.generateToken(auth.getName()) ;

        res.addHeader(HEADER_STRING, TOKEN_PREFIX + " " + toke);

        Users u =(Users)auth.getPrincipal();
        OutputResults output = new OutputResults(u.getId(), u.getName(),u.getEmail());

        res.setContentType("application/json");
        res.getWriter().write( new ObjectMapper().writeValueAsString(output));

    }
}

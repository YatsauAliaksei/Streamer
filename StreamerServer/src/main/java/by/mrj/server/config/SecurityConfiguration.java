package by.mrj.server.config;

import by.mrj.server.security.AuthoritiesConstants;
import by.mrj.server.security.jwt.TokenProvider;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = false, securedEnabled = true)
//@ComponentScan(basePackages = "by.mrj.server")
//@Import(SecurityProblemSupport.class)
public class SecurityConfiguration { // extends WebSecurityConfigurerAdapter {

    private final TokenProvider tokenProvider;

    private final SecurityProblemSupport problemSupport;

    public SecurityConfiguration(TokenProvider tokenProvider, SecurityProblemSupport problemSupport) {
        this.tokenProvider = tokenProvider;
        this.problemSupport = problemSupport;
    }

//    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                .antMatchers("/app/**/*.{js,html}")
                .antMatchers("/i18n/**")
                .antMatchers("/content/**")
                .antMatchers("/swagger-ui/index.html")
                .antMatchers("/test/**");
    }

//    @Override
    public void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .csrf()
                .disable()
//            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport)
                .and()
                .headers()
                .frameOptions()
                .disable()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/authenticate").permitAll()
                .antMatchers("/api/**").authenticated()
                .antMatchers("/websocket/tracker").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/websocket/**").permitAll()
                .antMatchers("/management/health").permitAll()
                .antMatchers("/management/info").permitAll()
                .antMatchers("/management/prometheus").permitAll()
                .antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN);
//                .and()
//                .apply(securityConfigurerAdapter());
        // @formatter:on
    }
}

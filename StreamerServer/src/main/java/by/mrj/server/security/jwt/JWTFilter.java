package by.mrj.server.security.jwt;

import by.mrj.server.security.AuthoritiesConstants;
import com.google.common.collect.Lists;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class JWTFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private static final GrantedAuthority POST_AUTHORITY = new SimpleGrantedAuthority(AuthoritiesConstants.POST);
    private static final GrantedAuthority READ_AUTHORITY = new SimpleGrantedAuthority(AuthoritiesConstants.READ);

    private TokenProvider tokenProvider;

    public JWTFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public String authorize(FullHttpRequest request) {
        SecurityContextHolder.clearContext();
        String token = request.headers().get(HttpHeaderNames.AUTHORIZATION);

        if (!StringUtils.hasText(token)) {
            return "";
        }

        String jwt = resolveTokenJwt(token);
        String basic;

        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            authenticate(jwt);
        } else if (StringUtils.hasText(basic = resolveTokenBasic(token))) {
            jwt = createJwt(basic);
            // todo: double Authentication creation
            authenticate(jwt);
        }

        return jwt;
    }

    private void authenticate(String jwt) {
        Authentication authentication = tokenProvider.getAuthentication(jwt);
        // todo: check authorities
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String createJwt(String basic) {
        String[] loginAndPwd =
//                    new String(Base64.getDecoder()
//                    .decode(auth.substring(6)))
                basic.split(":");

        String login = loginAndPwd[0];
        String pwd = loginAndPwd[1];
        // todo: Authenticate/Authorize user

        List<String> topics = Lists.newArrayList("READ_TOPIC_NAME", "POST_TOPIC_NAME");
        Collection<? extends GrantedAuthority> authorities = topics.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return tokenProvider.createToken(new UsernamePasswordAuthenticationToken(login, "", authorities), true);
    }

    private String resolveTokenJwt(String token) {
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return "";
    }

    private String resolveTokenBasic(String token) {
        if (token.startsWith("Basic ")) {
            return token.substring(6);
        }
        return "";
    }
}

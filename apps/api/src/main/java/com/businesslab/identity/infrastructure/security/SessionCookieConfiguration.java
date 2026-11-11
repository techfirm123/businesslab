
package com.businesslab.identity.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class SessionCookieConfiguration {

    @Bean
    public CookieSerializer businessLabSessionCookieSerializer(
        @Value("${BUSINESSLAB_SESSION_COOKIE_SECURE:false}")
        boolean secureCookie
    ) {
        DefaultCookieSerializer serializer =
            new DefaultCookieSerializer();

        serializer.setCookieName("BUSINESSLAB_SESSION");
        serializer.setCookiePath("/");
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(secureCookie);
        serializer.setSameSite("Lax");

        return serializer;
    }
}

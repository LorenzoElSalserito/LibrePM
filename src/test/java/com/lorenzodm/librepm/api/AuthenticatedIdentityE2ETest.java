package com.lorenzodm.librepm.api;

import com.lorenzodm.librepm.security.LibrePMPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import java.util.Map;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "librepm.auth.enabled=true",
    "spring.security.oauth2.client.registration.librepm.client-id=test",
    "spring.security.oauth2.client.registration.librepm.client-secret=test",
    "spring.security.oauth2.client.registration.librepm.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}",
    "spring.security.oauth2.client.registration.librepm.authorization-grant-type=authorization_code",
    "spring.security.oauth2.client.provider.librepm.authorization-uri=https://example.test/authorize",
    "spring.security.oauth2.client.provider.librepm.token-uri=https://example.test/token",
    "spring.security.oauth2.client.provider.librepm.user-info-uri=https://example.test/userinfo",
    "spring.security.oauth2.client.provider.librepm.user-name-attribute=sub"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticatedIdentityE2ETest {
    @Autowired MockMvc mvc;
    @Test void authenticatedSessionCannotImpersonateUrlOrHeaderUser() throws Exception {
        var principal = new LibrePMPrincipal(new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), Map.of("sub", "external-user"), "sub"), "local-user");
        mvc.perform(get("/api/users/another-user/projects").with(oauth2Login().oauth2User(principal)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/users/local-user/projects").header("X-User-Id", "another-user")
                .with(oauth2Login().oauth2User(principal))).andExpect(status().isForbidden());
        mvc.perform(get("/api/users/local-user/projects/project/messages").with(oauth2Login().oauth2User(principal)))
                .andExpect(status().isForbidden());
    }
}

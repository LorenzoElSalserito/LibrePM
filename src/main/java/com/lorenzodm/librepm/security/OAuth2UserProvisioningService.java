package com.lorenzodm.librepm.security;

import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
public class OAuth2UserProvisioningService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Logger log = LoggerFactory.getLogger(OAuth2UserProvisioningService.class);

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public OAuth2UserProvisioningService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        Map<String, Object> attrs = oauth2User.getAttributes();
        String username = firstNonBlank(
                asString(attrs.get("preferred_username")),
                asString(attrs.get("email")),
                asString(attrs.get("sub")),
                oauth2User.getName()
        );

        String email = firstNonBlank(asString(attrs.get("email")), null);
        String displayName = firstNonBlank(
                asString(attrs.get("name")),
                asString(attrs.get("given_name")),
                username
        );

        User localUser = findOrCreateLocalUser(username, email, displayName);
        return new LibrePMPrincipal(oauth2User, localUser.getId());
    }

    private User findOrCreateLocalUser(String username, String email, String displayName) {
        Optional<User> byUsername = userRepository.findByUsernameIgnoreCase(username);
        if (byUsername.isPresent()) {
            User u = byUsername.get();
            if (u.getEmail() == null && email != null) u.setEmail(email);
            if (u.getDisplayName() == null && displayName != null) u.setDisplayName(displayName);
            return userRepository.save(u);
        }

        if (email != null) {
            Optional<User> byEmail = userRepository.findByEmailIgnoreCase(email);
            if (byEmail.isPresent()) {
                User u = byEmail.get();
                if (u.getUsername() == null || u.getUsername().isBlank()) u.setUsername(username);
                if (u.getDisplayName() == null && displayName != null) u.setDisplayName(displayName);
                return userRepository.save(u);
            }
        }

        User created = new User();
        created.setUsername(username);
        created.setEmail(email);
        created.setDisplayName(displayName);
        created.setPasswordHash(randomPlaceholderPassword()); // serve per @NotBlank attuale nella entity
        created.setActive(true);

        log.info("👤 Creato nuovo utente locale LibrePM da OAuth2: username={}, email={}", username, email);
        return userRepository.save(created);
    }

    private String randomPlaceholderPassword() {
        byte[] buf = new byte[32];
        secureRandom.nextBytes(buf);
        return "OAUTH2_" + Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}

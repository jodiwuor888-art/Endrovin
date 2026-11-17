package com.portfolio.security;

import com.portfolio.entity.Admin;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AdminUserDetails implements UserDetails {

    private final Admin admin;

    public AdminUserDetails(Admin admin) {
        this.admin = admin;
    }

    public Admin getAdmin() {
        return admin;
    }

    public Long getId() {
        return admin.getId();
    }

    public String getEmail() {
        return admin.getEmail();
    }

    public String getFullName() {
        return admin.getFullName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (admin.getRoles() != null) {
            admin.getRoles().forEach(role -> {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));
                if (role.getPermissions() != null) {
                    role.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getPermissionName())));
                }
            });
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return admin.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return admin.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (admin.getLockoutUntil() == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(admin.getLockoutUntil());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return admin.isActive();
    }
}

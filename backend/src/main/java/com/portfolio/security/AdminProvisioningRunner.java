package com.portfolio.security;

import com.portfolio.entity.Admin;
import com.portfolio.entity.Role;
import com.portfolio.repository.AdminRepository;
import com.portfolio.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Component
public class AdminProvisioningRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminProvisioningRunner.class);

    private final AdminRepository adminRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminProvisioningRunner(AdminRepository adminRepository,
                                   RoleRepository roleRepository,
                                   PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Ensure default roles exist in the database
        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "System Super Administrator with full privileges")));

        if (!roleRepository.existsByRoleName("EDITOR")) {
            roleRepository.save(new Role("EDITOR", "Content editor with read/write access to portfolio items"));
        }

        // Check if admin creation CLI arguments were supplied:
        // Example: java -jar app.jar --create-admin --admin-user=joash --admin-pass=Secret123! --admin-email=joash@example.com --admin-name="Joash Odiwuor"
        if (args.containsOption("create-admin")) {
            String username = getOption(args, "admin-user", "admin");
            String password = getOption(args, "admin-pass", null);
            String email = getOption(args, "admin-email", "admin@portfolio.local");
            String fullName = getOption(args, "admin-name", "Portfolio Administrator");

            if (password == null || password.trim().length() < 8) {
                log.error("[PROVISIONING ERROR] You must provide a strong password using --admin-pass=<your-password> (min 8 chars)");
                return;
            }

            if (adminRepository.existsByUsername(username)) {
                log.warn("[PROVISIONING] Admin user '{}' already exists in SQL Server. Skipping creation.", username);
                return;
            }

            Admin newAdmin = new Admin(username, email, passwordEncoder.encode(password), fullName);
            newAdmin.setRoles(Collections.singleton(adminRole));
            adminRepository.save(newAdmin);

            log.info("====================================================================");
            log.info(" [SECURITY PROVISIONING] First administrator created successfully!");
            log.info(" Username: {}", username);
            log.info(" Email:    {}", email);
            log.info(" Roles:    ADMIN");
            log.info("====================================================================");
            return;
        }

        long adminCount = adminRepository.count();
        if (adminCount == 0) {
            log.warn("========================================================================");
            log.warn(" [INITIAL SETUP NOTICE] Zero administrator records exist in database.");
            log.warn(" To create your first administrator securely, you may either:");
            log.warn(" 1. Run with CLI flags: --create-admin --admin-user=NAME --admin-pass=PASS --admin-email=EMAIL");
            log.warn(" 2. Open the Admin UI at /admin/login.html to run the one-time Setup Wizard");
            log.warn("    (Setup wizard automatically locks permanently after the first admin is created)");
            log.warn("========================================================================");
        } else {
            log.info("Portfolio Database initialized with {} verified administrator account(s).", adminCount);
        }
    }

    private String getOption(ApplicationArguments args, String name, String defaultValue) {
        if (args.containsOption(name) && !args.getOptionValues(name).isEmpty()) {
            return args.getOptionValues(name).get(0);
        }
        return defaultValue;
    }
}

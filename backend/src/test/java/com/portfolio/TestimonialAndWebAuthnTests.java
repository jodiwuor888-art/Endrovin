package com.portfolio;

import com.portfolio.dto.*;
import com.portfolio.entity.*;
import com.portfolio.exception.BadRequestException;
import com.portfolio.exception.UnauthorizedException;
import com.portfolio.repository.*;
import com.portfolio.security.WebAuthnService;
import com.portfolio.service.AdminManagementService;
import com.portfolio.service.PublicPortfolioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TestimonialAndWebAuthnTests {

    @Autowired
    private PublicPortfolioService publicPortfolioService;

    @Autowired
    private AdminManagementService adminManagementService;

    @Autowired
    private TestimonialRepository testimonialRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private WebAuthnService webAuthnService;

    @Autowired
    private AdminRecoveryCodeRepository recoveryCodeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        testimonialRepository.deleteAll();
        recoveryCodeRepository.deleteAll();
        adminRepository.deleteAll();

        testAdmin = new Admin("secadmin", "secadmin@test.com", passwordEncoder.encode("Secret123!"), "Security Admin");
        testAdmin = adminRepository.save(testAdmin);
    }

    @Test
    @DisplayName("Public submission creates a PENDING testimonial and requires consent")
    void testPublicSubmissionRequiresConsentAndIsPending() {
        PublicTestimonialSubmissionRequest req = new PublicTestimonialSubmissionRequest();
        req.setFullName("Jane Doe");
        req.setEmail("jane@example.com");
        req.setOrganization("Tech Corp");
        req.setPosition("Lead Architect");
        req.setRelationship("Colleague");
        req.setMessage("Outstanding engineer with deep full-stack proficiency.");
        req.setConsentToPublish(false); // No consent

        assertThrows(BadRequestException.class, () -> publicPortfolioService.submitPublicRecommendation(req, "127.0.0.1"),
                "Should reject submission without consent");

        req.setConsentToPublish(true);
        String msg = publicPortfolioService.submitPublicRecommendation(req, "127.0.0.1");
        assertNotNull(msg);

        List<Testimonial> all = testimonialRepository.findAll();
        assertEquals(1, all.size());
        Testimonial saved = all.get(0);
        assertEquals("PENDING", saved.getStatus());
        assertEquals("Jane Doe", saved.getFullName());
        assertTrue(saved.isConsentToPublish());

        // Must not be visible in public testimonials yet
        List<PublicTestimonialResponse> publicList = publicPortfolioService.getApprovedPublicTestimonials();
        assertTrue(publicList.isEmpty(), "Pending testimonials must never appear on public website");
    }

    @Test
    @DisplayName("Admin approval workflow transitions PENDING to APPROVED and exposes publicly")
    void testAdminApprovalWorkflow() {
        Testimonial t = new Testimonial();
        t.setFullName("John Smith");
        t.setEmail("john@client.com");
        t.setOrganization("Global Solutions");
        t.setPosition("CTO");
        t.setRelationship("Client");
        t.setMessage("Delivered a flawless portfolio system ahead of schedule.");
        t.setConsentToPublish(true);
        t.setStatus("PENDING");
        t = testimonialRepository.save(t);

        // Approve
        TestimonialDto approved = adminManagementService.updateApprovalStatus(t.getId(), "APPROVED", testAdmin.getUsername(), "127.0.0.1");
        assertEquals("APPROVED", approved.getStatus());

        // Now must appear publicly
        List<PublicTestimonialResponse> publicList = publicPortfolioService.getApprovedPublicTestimonials();
        assertEquals(1, publicList.size());
        assertEquals("John Smith", publicList.get(0).getFullName());
        assertEquals("Global Solutions", publicList.get(0).getOrganization());

        // Reject
        adminManagementService.rejectTestimonial(t.getId(), "Spam content", testAdmin.getUsername(), "127.0.0.1");
        List<PublicTestimonialResponse> publicListAfterReject = publicPortfolioService.getApprovedPublicTestimonials();
        assertTrue(publicListAfterReject.isEmpty(), "Rejected testimonials must not appear publicly");
    }

    @Test
    @DisplayName("WebAuthn emergency recovery codes generation and single-use verification")
    void testWebAuthnRecoveryCodes() {
        List<String> codes = webAuthnService.generateRecoveryCodes(testAdmin);
        assertNotNull(codes);
        assertEquals(8, codes.size());

        String firstCode = codes.get(0);
        assertNotNull(firstCode);

        // Verify with first code
        assertDoesNotThrow(() -> webAuthnService.verifyRecoveryCode(testAdmin, firstCode, "127.0.0.1"));

        // Using same code a second time must fail (single-use enforcement)
        assertThrows(UnauthorizedException.class, () -> webAuthnService.verifyRecoveryCode(testAdmin, firstCode, "127.0.0.1"));

        // Remaining codes count must be 7
        assertEquals(7, webAuthnService.getRemainingRecoveryCodesCount(testAdmin));
    }
}

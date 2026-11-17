/* =====================================================
   PORTFOLIO JAVASCRIPT
===================================================== */

document.addEventListener("DOMContentLoaded", function () {

    /* =================================================
       1. TYPING EFFECT
    ================================================= */

    const typingElement = document.querySelector(".hero h2");

    const text = "Diploma in Computer Science ";

    let index = 0;
    let deleting = false;

    function typeEffect() {

        if (!typingElement) return;

        if (!deleting) {

            typingElement.textContent = text.substring(0, index + 1);
            index++;

            if (index === text.length) {
                setTimeout(() => {
                    deleting = true;
                }, 2000);
            }

        } else {

            typingElement.textContent = text.substring(0, index - 1);
            index--;

            if (index === 0) {
                deleting = false;
            }
        }

        const speed = deleting ? 50 : 100;

        setTimeout(typeEffect, speed);
    }

    typeEffect();


    /* =================================================
       2. MOBILE NAVIGATION
    ================================================= */

    const navbar = document.querySelector(".navbar");
    const navLinks = document.querySelector(".nav-links");

    if (navbar && navLinks) {

        const menuButton = document.createElement("button");

        menuButton.innerHTML = "☰";
        menuButton.classList.add("menu-button");

        navbar.insertBefore(menuButton, navLinks);

        menuButton.addEventListener("click", function () {

            navLinks.classList.toggle("active");

            if (navLinks.classList.contains("active")) {
                menuButton.innerHTML = "✕";
            } else {
                menuButton.innerHTML = "☰";
            }

        });


        /* Close menu when a link is clicked */

        const links = navLinks.querySelectorAll("a");

        links.forEach(function (link) {

            link.addEventListener("click", function () {

                navLinks.classList.remove("active");

                menuButton.innerHTML = "☰";

            });

        });
    }


    /* =================================================
       3. ACTIVE NAVIGATION LINK
    ================================================= */

    const sections = document.querySelectorAll("section");
    const navigationLinks = document.querySelectorAll(".nav-links a");

    window.addEventListener("scroll", function () {

        let currentSection = "";

        sections.forEach(function (section) {

            const sectionTop = section.offsetTop - 150;
            const sectionHeight = section.offsetHeight;

            if (
                window.scrollY >= sectionTop &&
                window.scrollY < sectionTop + sectionHeight
            ) {
                currentSection = section.getAttribute("id");
            }

        });


        navigationLinks.forEach(function (link) {

            link.classList.remove("active");

            if (
                link.getAttribute("href") === "#" + currentSection
            ) {
                link.classList.add("active");
            }

        });

    });


    /* =================================================
       4. SCROLL REVEAL ANIMATION
    ================================================= */

    const revealElements = document.querySelectorAll(
        ".section-title, .skill-card, .project-card, .service-card, .timeline-item, .about-content, .about-image, .contact-info, .contact-form"
    );


    const observer = new IntersectionObserver(
        function (entries) {

            entries.forEach(function (entry) {

                if (entry.isIntersecting) {

                    entry.target.classList.add("show");

                    observer.unobserve(entry.target);
                }

            });

        },
        {
            threshold: 0.15
        }
    );


    revealElements.forEach(function (element) {

        element.classList.add("reveal");

        observer.observe(element);

    });


    /* =================================================
       5. CONTACT FORM VALIDATION
    ================================================= */

    const contactForm = document.querySelector(".contact-form");

    if (contactForm) {

        contactForm.addEventListener("submit", function (event) {

            event.preventDefault();

            const name = document.querySelector("#name").value.trim();
            const email = document.querySelector("#email").value.trim();
            const subject = document.querySelector("#subject").value.trim();
            const message = document.querySelector("#message").value.trim();


            /* Check empty fields */

            if (
                name === "" ||
                email === "" ||
                subject === "" ||
                message === ""
            ) {

                showMessage(
                    "Please fill in all the required fields.",
                    "error"
                );

                return;
            }


            /* Validate email */

            const emailPattern =
                /^[^\s@]+@[^\s@]+\.[^\s@]+$/;


            if (!emailPattern.test(email)) {

                showMessage(
                    "Please enter a valid email address.",
                    "error"
                );

                return;
            }


            /* Successful submission */

            showMessage(
                "Thank you! Your message has been submitted successfully.",
                "success"
            );


            contactForm.reset();

        });

    }


    /* =================================================
       6. FORM MESSAGE FUNCTION
    ================================================= */

    function showMessage(message, type) {

        let messageBox =
            document.querySelector(".form-message");


        if (!messageBox) {

            messageBox = document.createElement("div");

            messageBox.classList.add("form-message");

            contactForm.prepend(messageBox);
        }


        messageBox.textContent = message;

        messageBox.className = "form-message " + type;


        setTimeout(function () {

            messageBox.remove();

        }, 5000);

    }


    /* =================================================
       7. UPDATE COPYRIGHT YEAR
    ================================================= */

    const copyrightYear =
        document.querySelector(".copyright p");


    if (copyrightYear) {

        const currentYear = new Date().getFullYear();

        copyrightYear.textContent =
            `© ${currentYear} Your Name. All Rights Reserved.`;

    }


    /* =================================================
       8. BACK TO TOP BUTTON
    ================================================= */

    const backToTop = document.createElement("button");

    backToTop.innerHTML = "↑";

    backToTop.classList.add("back-to-top");

    document.body.appendChild(backToTop);


    window.addEventListener("scroll", function () {

        if (window.scrollY > 500) {

            backToTop.classList.add("visible");

        } else {

            backToTop.classList.remove("visible");

        }

    });


    backToTop.addEventListener("click", function () {

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });

    });


    /* =================================================
       9. PROJECT CARD INTERACTION
    ================================================= */

    const projectCards =
        document.querySelectorAll(".project-card");


    projectCards.forEach(function (card) {

        card.addEventListener("mouseenter", function () {

            card.style.transform =
                "translateY(-10px)";

        });


        card.addEventListener("mouseleave", function () {

            card.style.transform =
                "translateY(0)";

        });

    });


    /* =================================================
       10. CURRENT YEAR IN FOOTER
    ================================================= */

    const footer =
        document.querySelector("footer");

    if (footer) {

        footer.setAttribute(
            "data-year",
            new Date().getFullYear()
        );

    }

});
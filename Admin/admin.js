const API_URL =
    "http://localhost:8000";


// ==========================================
// LOGIN
// ==========================================

const loginForm =
    document.querySelector("#loginForm");


if (loginForm) {

    loginForm.addEventListener(
        "submit",
        async function(event) {

            event.preventDefault();


            const username =
                document.querySelector("#username").value;

            const password =
                document.querySelector("#password").value;


            try {

                const response =
                    await fetch(
                        `${API_URL}/api/admin/login`,
                        {

                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body: JSON.stringify({
                                username,
                                password
                            })

                        }
                    );


                const data =
                    await response.json();


                if (!response.ok) {

                    document.querySelector(
                        "#loginMessage"
                    ).textContent = data.error;

                    return;

                }


                localStorage.setItem(
                    "adminToken",
                    data.token
                );


                window.location.href =
                    "dashboard.html";


            } catch (error) {

                console.error(error);

                alert(
                    "Unable to connect to server."
                );

            }

        }
    );

}


// ==========================================
// CHECK LOGIN
// ==========================================

function checkAuthentication() {

    const token =
        localStorage.getItem("adminToken");


    if (!token) {

        window.location.href =
            "login.html";

    }

}


// ==========================================
// LOGOUT & SESSION INVALIDATION
// ==========================================

async function logout() {
    localStorage.removeItem("adminToken");
    localStorage.removeItem("adminUser");
    sessionStorage.clear();
    try {
        await fetch("logout.php", {
            method: "POST",
            credentials: "same-origin"
        });
    } catch (_) {}

    window.location.href = "login.html";
}

function exitAdminToPublic(e) {
    if (e && e.preventDefault) e.preventDefault();
    localStorage.removeItem("adminToken");
    localStorage.removeItem("adminUser");
    sessionStorage.clear();
    try {
        fetch("logout.php", { method: "POST", credentials: "same-origin" });
    } catch (_) {}
    window.location.href = "../frontendport/index.html";
}

window.addEventListener("pageshow", function() {
    const isLoginPage = window.location.pathname.endsWith("login.html");
    if (!isLoginPage) {
        const token = localStorage.getItem("adminToken");
        if (!token) {
            window.location.replace("login.html");
        }
    }
});





// ==========================================
// LOAD PROJECTS
// ==========================================

async function loadProjects() {

    try {

        const response =
            await fetch(
                `${API_URL}/api/projects`
            );


        const projects =
            await response.json();


        const container =
            document.querySelector(
                "#projectList"
            );


        if (!container) return;


        container.innerHTML = "";


        projects.forEach(project => {

            const card =
                document.createElement("div");


            card.className =
                "admin-project";


            card.innerHTML = `

                <h3>
                    ${project.ProjectName}
                </h3>

                <p>
                    ${project.Description || ""}
                </p>

                <small>
                    ${project.Technologies || ""}
                </small>

                <br><br>

                <button
                    onclick="deleteProject(${project.ProjectID})"
                >
                    Delete
                </button>

            `;


            container.appendChild(card);

        });


        const count =
            document.querySelector(
                "#projectCount"
            );


        if (count) {

            count.textContent =
                projects.length;

        }


    } catch (error) {

        console.error(error);

    }

}


// ==========================================
// ADD PROJECT
// ==========================================

const projectForm =
    document.querySelector("#projectForm");


if (projectForm) {

    checkAuthentication();


    loadProjects();


    projectForm.addEventListener(
        "submit",
        async function(event) {

            event.preventDefault();


            const token =
                localStorage.getItem(
                    "adminToken"
                );


            const project = {

                projectName:
                    document.querySelector(
                        "#projectName"
                    ).value,

                category:
                    document.querySelector(
                        "#category"
                    ).value,

                description:
                    document.querySelector(
                        "#description"
                    ).value,

                technologies:
                    document.querySelector(
                        "#technologies"
                    ).value,

                projectLink:
                    document.querySelector(
                        "#projectLink"
                    ).value,

                imageURL:
                    document.querySelector(
                        "#imageURL"
                    ).value

            };


            try {

                const response =
                    await fetch(
                        `${API_URL}/api/projects`,
                        {

                            method: "POST",

                            headers: {

                                "Content-Type":
                                    "application/json",

                                "Authorization":
                                    `Bearer ${token}`

                            },

                            body:
                                JSON.stringify(project)

                        }
                    );


                const data =
                    await response.json();


                if (response.ok) {

                    alert(
                        "Project added successfully!"
                    );

                    projectForm.reset();

                    loadProjects();

                } else {

                    alert(data.error);

                }


            } catch (error) {

                console.error(error);

            }

        }
    );

}


// ==========================================
// DELETE PROJECT
// ==========================================

async function deleteProject(id) {

    const confirmation =
        confirm(
            "Are you sure you want to delete this project?"
        );


    if (!confirmation) return;


    const token =
        localStorage.getItem(
            "adminToken"
        );


    try {

        const response =
            await fetch(
                `${API_URL}/api/projects/${id}`,
                {

                    method: "DELETE",

                    headers: {

                        "Authorization":
                            `Bearer ${token}`

                    }

                }
            );


        if (response.ok) {

            alert(
                "Project deleted successfully."
            );

            loadProjects();

        }


    } catch (error) {

        console.error(error);

    }

}



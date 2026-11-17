
<?php
session_start();
header("Content-Type: application/json");

if (empty($_SESSION["admin_id"])) {
    http_response_code(401);
    echo json_encode(["error" => "Please log in first."]);
    exit;
}

$conn = null;
require_once "db.php";

$data = json_decode(file_get_contents("php://input"), true);

if (!is_array($data) ||
    empty(trim($data["projectName"] ?? ""))) {
    http_response_code(400);
    echo json_encode(["error" => "Project name is required."]);
    exit;
}

$sql = "INSERT INTO Projects
        (ProjectName, Category, Description,
         Technologies, ProjectLink, ImageURL)
        VALUES (?, ?, ?, ?, ?, ?)";

$params = [
    trim($data["projectName"]),
    $data["category"] ?? "",
    $data["description"] ?? "",
    $data["technologies"] ?? "",
    $data["projectLink"] ?? "",
    $data["imageURL"] ?? ""
];

if (!function_exists('sqlsrv_query')) {
    http_response_code(500);
    echo json_encode(["error" => "SQL Server support is unavailable."]);
    exit;
}

$stmt = call_user_func('sqlsrv_query', $conn, $sql, $params);

if ($stmt === false) {
    error_log(print_r(call_user_func('sqlsrv_errors'), true));
    http_response_code(500);
    echo json_encode(["error" => "Could not save project."]);
    exit;
}

echo json_encode(["message" => "Project saved successfully."]);
?>

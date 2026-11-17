
<?php
session_start();
header("Content-Type: application/json");

$conn = null;
require_once "db.php";

$data = json_decode(file_get_contents("php://input"), true);

$username = trim($data["username"] ?? "");
$password = $data["password"] ?? "";

$sql = "SELECT AdminID, Username, PasswordHash
        FROM Admins
        WHERE Username = ?";

if (!function_exists("sqlsrv_query")) {
    http_response_code(500);
    echo json_encode(["message" => "Login service unavailable."]);
    exit;
}

$stmt = call_user_func("sqlsrv_query", $conn, $sql, [$username]);

if ($stmt === false) {
    if (function_exists("sqlsrv_errors")) {
        error_log(print_r(sqlsrv_errors(), true));
    }
    http_response_code(500);
    echo json_encode(["message" => "Login service unavailable."]);
    exit;
}

if (!function_exists("sqlsrv_fetch_array") || !defined("SQLSRV_FETCH_ASSOC")) {
    http_response_code(500);
    echo json_encode(["message" => "Login service unavailable."]);
    exit;
}

$admin = call_user_func("sqlsrv_fetch_array", $stmt, constant("SQLSRV_FETCH_ASSOC"));

if (!$admin || !password_verify($password, $admin["PasswordHash"])) {
    http_response_code(401);
    echo json_encode(["message" => "Invalid username or password."]);
    exit;
}

session_regenerate_id(true);
$_SESSION["admin_id"] = $admin["AdminID"];

echo json_encode([
    "message" => "Login successful.",
    "redirect" => "dashboard.html"
]);
?>


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

if (!function_exists("sqlsrv_query")) {
    http_response_code(500);
    echo json_encode(["error" => "The SQL Server PHP extension is not available."]);
    exit;
}

$sql = "SELECT ProjectID, ProjectName, Category,
               Description, Technologies,
               ProjectLink, ImageURL
        FROM Projects
        ORDER BY ProjectID DESC";

$stmt = call_user_func("sqlsrv_query", $conn, $sql);

if ($stmt === false) {
    error_log(print_r(call_user_func("sqlsrv_errors"), true));
    http_response_code(500);
    echo json_encode(["error" => "Could not retrieve projects."]);
    exit;
}

$projects = [];

while ($row = call_user_func("sqlsrv_fetch_array", $stmt, 2)) {
    $projects[] = $row;
}

echo json_encode($projects);
?>


<?php
$serverName = "DESKTOP-KNACU2Q";

$connectionOptions = [
    "Database" => "PortfolioDB",
    "Trusted_Connection" => true,
    "TrustServerCertificate" => true
];

if (!function_exists("sqlsrv_connect")) {
    error_log("The Microsoft SQLSRV PHP extension is not enabled.");
    http_response_code(500);
    exit("Database connection failed. Enable the Microsoft SQLSRV PHP extension.");
}

$connect = "sqlsrv_connect";
$conn = $connect(
    $serverName,
    $connectionOptions
);

if ($conn === false) {
    $getErrors = "sqlsrv_errors";
    error_log(print_r($getErrors(), true));
    http_response_code(500);
    exit("Database connection failed. Check the server and PHP configuration.");
}
?>

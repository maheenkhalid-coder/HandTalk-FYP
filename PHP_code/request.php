<?php

$serverUrl = 'http://localhost:5000/predict';

// Check if the form was submitted
if ($_SERVER["REQUEST_METHOD"] === "POST") {
	header('Access-Control-Allow-Origin: *');
	header('Access-Control-Allow-Methods: GET, POST');
	header("Access-Control-Allow-Headers: X-Requested-With");
	
	// Set the detection type (alphabets, words, or numbers)
	$detectionType = $_GET['type'];
	// Specify the URL of the Flask server with the detection type as a query parameter
	$url = $serverUrl."?type=".$detectionType;

    // Check if there was an error during file upload
    if ($_FILES["image"]["error"] === UPLOAD_ERR_OK) {
		
        // Create a cURL file object
        $cFile = new CURLFile($_FILES["image"]["tmp_name"], $_FILES["image"]["type"], $_FILES["image"]["name"]);

        // Set POST data
        $postData = array('image' => $cFile);

        // Initialize cURL session
        $ch = curl_init();

        // Set cURL options
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_POST, 1);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $postData);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

        // Execute cURL session and get the response
        $response = curl_exec($ch);
		
		$uploadDir = "uploads/"; // Directory where images will be stored
        $uploadedFile = $uploadDir . basename($_FILES["image"]["name"]);

        // Move the uploaded file to the specified directory
        if (move_uploaded_file($_FILES["image"]["tmp_name"], $uploadedFile)) {
            //echo "Image uploaded and saved successfully.";
        } else {
            //echo "Failed to move uploaded file.";
        }

        // Check for cURL errors
        if (curl_errno($ch)) {
            echo 'Curl error: ' . curl_error($ch);
        } else {
            // Print the response from the server
            echo $response;
        }

        // Close cURL session
        curl_close($ch);
    } else {
        echo "Error during file upload.";
    }
}
?>




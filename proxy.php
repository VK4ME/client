<?php
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
	$urli = !empty($_GET["site"]) ? $_GET["site"] : $_GET["proxy_link"];
	if (!preg_match('/^(?:vk\.com|(?:[-_a-zA-Z0-9]+)\.(?:userapi\.com|vk-cdn\.net|vk\.(?:me|com)|mycdn\.me|vkuser(?:live|video|audio|)\.(?:net|com)))$/', parse_url($urli, PHP_URL_HOST))) {
		echo 'NOT VK URL. THIS PROXY FOR VK ONLY. ' . $urli;
		return;
	}
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL, $urli);
	curl_setopt($ch, CURLOPT_USERAGENT, $_SERVER['HTTP_USER_AGENT']);
	curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
	curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);    
	$response = curl_exec($ch);
	curl_close($ch);
	echo $response;
} else if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $urli = !empty($_POST["site"]) ? $_POST["site"] : $_POST["proxy_link"];
	if (!preg_match('/^(?:vk\.com|(?:[-_a-zA-Z0-9]+)\.(?:userapi\.com|vk-cdn\.net|vk\.(?:me|com)|mycdn\.me|vkuser(?:live|video|audio|)\.(?:net|com)))$/', parse_url($urli, PHP_URL_HOST))) {
		echo 'NOT VK URL. THIS PROXY FOR VK ONLY. ' . $urli;
		return;
	}
	foreach($_FILES as $key => $element) {
		if (!is_uploaded_file($_FILES[$key]['tmp_name'])) {
			echo 'ATTACK ERROR';
			return;
		}
		
		$cFile = curl_file_create($_FILES[$key]['tmp_name'], null, $_FILES[$key]['name']);
		$post = array($key => $cFile);
		$ch = curl_init();
		curl_setopt($ch, CURLOPT_URL, $urli);
		curl_setopt($ch, CURLOPT_POST,1);
		curl_setopt($ch, CURLOPT_POSTFIELDS, $post);
		curl_setopt($ch, CURLOPT_USERAGENT, $_SERVER['HTTP_USER_AGENT']);
		curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
		$response = curl_exec($ch);
		curl_close($ch);
		echo $response;
		
		return;
	}
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL, $urli);
	curl_setopt($ch, CURLOPT_USERAGENT, $_SERVER['HTTP_USER_AGENT']);
	curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
	curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
	$response = curl_exec($ch);
	curl_close($ch);
	echo $response;
} else {
	echo 'USE GET OR POST';
}
?>
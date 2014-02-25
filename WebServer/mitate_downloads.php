<?php
session_start();
if(isset($_SESSION['mitateLoggedInUser'])) {
	include('header.php'); ?>
	<div style="font-size: 18;text-align: justify;">
	<h3 style="text-decoration:underline">Downloads:</h3>
	1. Sample MITATE XML Configuration File (<a target="_blank" href="sample/Mitate_Sample_Configuration_File_XML_Format.xml" style="color: red;">view</a>)
	<br />
	2. Sample MITATE XSD File (<a target="_blank" href="sample/Mitate_Sample_Configuration_File_XML_Format.xsd" style="color: red;">view</a>)
	<br />
	3. MITATE Android Application (<a target="_blank" href="sample/MITATEActivity.apk" style="color: red;">download</a>)
	<br />
	4. MITATE Command Line API (<a target="_blank" href="sample/mitate.sh" style="color: red;">download</a>)
	<br />
	5. MITATE Documentation V 1.0 (<a target="_blank" href="sample/MITATE_Documentation_v1.0.pdf" style="color: red;">download</a>)
	</div>
	<?php include('footer.php'); 
}
else {
	printf("<script>location.href = 'mitate_signin.php'</script>");
}
?>
<?php
session_start();
if(isset($_SESSION['mitateLoggedInUser'])) {
	include('header.php'); ?>
	<div style="font-size: 18;text-align: justify;">
	<h3 style="text-decoration:underline">Downloads:</h3>
	1. Sample MITATE XML Configuration File [<a target="_blank" href="sample/exp_conf.xml" style="color: red;">view</a>]
	<br />
	2. Sample MITATE XSD File [<a target="_blank" href="sample/Mitate_Sample_Configuration_File_XML_Format.xsd" style="color: red;">view</a>]
	<br />
	3. MITATE Android Application [<a target="_blank" href="sample/MITATE.apk" style="color: red;">download</a>]
	<br />
	4. MITATE Command Line API [<a target="_blank" href="sample/mitate.sh" style="color: red;">download</a>]
	<br />
	5. MITATE User Manual V 1.0 [<a target="_blank" href="sample/MITATE_User_Manual_v1.0.pdf" style="color: red;">view</a>]
	<br />
	
	<h3 style="text-decoration:underline">Sample SQL Queries</h3>
	6. TCP Uplink Throughput [<a target="_blank" href="sample/tcp_uplink_throughput.sql" style="color: red;">view</a>]
	<br />
	7. TCP Uplink Latency [<a target="_blank" href="sample/tcp_uplink_latency.sql" style="color: red;">view</a>]
	<br />
	8. TCP Uplink Jitter [<a target="_blank" href="sample/tcp_uplink_jitter.sql" style="color: red;">view</a>]
	<br />
	9. TCP Uplink Packet Loss [<a target="_blank" href="sample/tcp_uplink_packet_loss.sql" style="color: red;">view</a>]
	<br />
	10. Device Metrics [<a target="_blank" href="sample/device_metric.sql" style="color: red;">view</a>]
	<br />
	11. Device Details [<a target="_blank" href="sample/device_detail.sql" style="color: red;">view</a>]
	<br />
	
	<h3 style="text-decoration:underline">Java Code for Per Packet metrics</h3>
	12. Per Packet Network Metrics [<a target="_blank" href="sample/MITATE_Per_Packet_Network_Metrics.zip" style="color: red;">download</a>]
	</div>
	<?php include('footer.php'); 
}
else {
	printf("<script>location.href = 'mitate_signin.php'</script>");
}
?>
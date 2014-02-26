<?php include('header.php'); ?>
	<div style="font-size: 18;">
	<h3 style="text-decoration:underline">MITATE Tutorial for Beginners</h3>
	This tutorial assumes that you have read and understood the <a href="http://mitate.cs.montana.edu/sample/MITATE_User_Manual_v1.0.pdf" target="_blank">MITATE User Manual</a>. This tutorial will provide you step-by-step instructions to execute a basic experiment with MITATE.
	
	<h3>Step 1: Initialization</h3>
	1. Go to the <a href="http://mitate.cs.montana.edu/mitate_signup.php" target="_blank">signup page</a> and create an account with MITATE system. After the signup process, you will receive an email with a verification link to verify your account. The email might get delivered to your spam folder. Please note that, this is an important step and will complete the registration process with the MITATE system. You will not be able to proceed without verifying your account.<br /><br />
	2. Go to the <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads page</a> and download the 'MITATE Command Line API'. <br /><br />
	3. Open your terminal, go to the folder where you downloaded the 'MITATE Command Line API' file. <br /><br />
	4. Run <b>chmod 777 mitate.sh</b> command.<br /><br />
	5. Run <b>./mitate.sh help</b> to see all possible API calls. <br /><br />
	6. Now, run <b>./mitate.sh login</b> to authenticate yourself. After you execute this command, you will be asked to enter MITATE username and password. You will then get a message <b>You are now authenticated.</b><br /><br />
	7. Run <b>./mitate.sh init localdb.sql</b> to generate SQL scripts for your local MySQL instance.<br /><br />
	8. Run <b>mysql -u m_user -p m_schema < localdb.sql</b> to create and initialize tables in m_schema.<br /><br />
	
	<h3>Step 2: Uploading an experiment</h3>
	9. Go to the <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads page</a> and download the 'Sample MITATE XML Configuration File'.<br /><br />
	10. Run <b>./mitate.sh validate exp_conf.xml</b> to validate the XML against <a target="_blank" href="http://mitate.cs.montana.edu/sample/Mitate_Sample_Configuration_File_XML_Format.xsd">MITATE XML Schema Definition</a> file. <br /><br />
	11. If the validation was successful, you will see a message <b>XML experiment  is valid.</b> If you do not see this message, fix you XML file based on the error message(s) you get. (Refer to MITATE XML Schema Definition) <br /><br />
	12. Run <b>./mitate.sh upload exp_conf.xml</b> to submit your experiment to MITATE system. If successful, you will receive an experiment ID for the experiment you just uploaded.<br /><br />
	13. Connect your mobile phone to a Wi-Fi network and go to the <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads page</a> and download the 'MITATE Android Application'. After downloading, install the application on your mobile, enter the login credentials and finally press the <b>Start Service</b> button. It may take a minute for the experiment to execute.<br /><br />
	
	<h3>Step 3: Getting results</h3>
	14. To check the status of your uploaded experiment, run <b>./mitate.sh getExpStatus experiment_ID</b>, where <i>experiment_ID</i> is the experiment ID you received in step 12.<br /><br />
	15. To pull results of executed transfer from the MITATE database, run <b>./mitate.sh query results.sql</b>. This will create a file <b>results.sql</b> in the directory where mitate.sh resides.<br /><br />
	16. Run <b>mysql -u m_user -p m_schema < results.sql</b> to populate tables in m_schema.<br /><br />
	
	<h3>Step 4: Querying local database</h3>
	17. To query data for TCP Uplink Throughput, run the following SQL query from your MySQL command line: <br /><br />
	<b>
	select md.value as tcp_uplink_throughput <br />
	from experiment exp, transactions trans, trans_transfer_link ttl, metric mt, metricdata md <br />
	where exp.experiment_id = <i>experiment_id_from_step_12</i><br />
	and trans.experiment_id = exp.experiment_id <br />
	and ttl.transactionid = trans.transactionid <br />
	and ttl.transferid = md.transferid <br />
	and md.metricid = mt.metricid <br />
	and mt.name = 'tcp_uplink_throughput'
	</b><br /><br />
	18. To query data for Minimum, Mean and Maximum TCP Uplink Latency, run the following SQL query from your MySQL command line: <br /><br />
	<b>
	select mt.name as metric_name, md.value as tcp_uplink_latency <br />
	from experiment exp, transactions trans, trans_transfer_link ttl, metric mt, metricdata md <br />
	where exp.experiment_id = <i>experiment_id_from_step_12</i><br />
	and trans.experiment_id = exp.experiment_id<br />
	and ttl.transactionid = trans.transactionid <br />
	and ttl.transferid = md.transferid <br />
	and md.metricid = mt.metricid <br />
	and mt.name in ('tcp_min_uplink_delay', 'tcp_mean_uplink_delay', 'tcp_max_uplink_delay')
	</b>
	<br /><br />
	19. To query data for Maximum TCP Uplink Jitter, run the following SQL query from your MySQL command line: <br /><br />
	<b>
	select md.value as max_tcp_uplink_jitter <br />
	from experiment exp, transactions trans, trans_transfer_link ttl, metric mt, metricdata md <br />
	where exp.experiment_id = <i>experiment_id_from_step_12</i><br />
	and trans.experiment_id = exp.experiment_id <br />
	and ttl.transactionid = trans.transactionid <br />
	and ttl.transferid = md.transferid <br />
	and md.metricid = mt.metricid <br />
	and mt.name = 'tcp_maximum_uplink_jitter' <br />
	</b><br /><br />
	20. To query data for TCP Uplink Packet Loss, run the following SQL query from your MySQL command line: <br /><br />
	<b>
	select md.value as uplink_tcp_max_jitter <br />
	from experiment exp, transactions trans, trans_transfer_link ttl, metric mt, metricdata md <br />
	where exp.experiment_id = <i>experiment_id_from_step_12</i><br />
	and trans.experiment_id = exp.experiment_id <br />
	and ttl.transactionid = trans.transactionid <br />
	and ttl.transferid = md.transferid <br />
	and md.metricid = mt.metricid <br />
	and mt.name = 'tcp_uplink_packet_loss'
	</b><br /><br />
	21. To query data for Device Travel Speed in (Km/hr), Device Signal Strength, run the following SQL query from your MySQL command line: <br /><br />
	<b>
	select mt.name as metric_name, md.value as device_metric_value <br />
	from experiment exp, transactions trans, trans_transfer_link ttl, metric mt, metricdata md <br />
	where exp.experiment_id = <i>experiment_id_from_step_12</i><br />
	and trans.experiment_id = exp.experiment_id <br />
	and ttl.transactionid = trans.transactionid <br />
	and ttl.transferid = md.transferid <br />
	and md.metricid = mt.metricid <br />
	and mt.name in ('device_travel_speed', 'signal_strength')
	</b><br /><br />
	22. To query data like the unique deviceid, the network carrier name, and the model name, for the mobile device that executed your experiment, run the following SQL query from your MySQL command line: <br /><br />
	<b>
	select DISTINCT md.deviceid as deviceId, ud.devicename as deviceName, ud.devicecarrier as deviceCarrier <br />
	from experiment exp, transactions trans, trans_transfer_link ttl, metricdata md, userdevice ud <br />
	where exp.experiment_id = <i>experiment_id_from_step_12</i><br />
	and trans.experiment_id = exp.experiment_id <br />
	and ttl.transactionid = trans.transactionid <br />
	and ttl.transferid = md.transferid <br />
	and md.deviceid = ud.deviceid <br />
	</b>
	</div>
<?php include('footer.php'); ?>
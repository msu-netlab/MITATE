<?php include('header.php'); ?>
	<div style="font-size: 18;" align="justify">
	<h3 style="text-decoration:underline">MITATE Tutorial for Beginners</h3>
	This tutorial assumes that you have read and understood the <a href="http://mitate.cs.montana.edu/sample/MITATE_User_Manual_v1.0.pdf" target="_blank">MITATE User Manual</a>, you have MySQL and JAVA installed on your computer. This tutorial will provide you step-by-step instructions to execute a basic experiment with MITATE.
	
	<h3>Step 1: Initialization</h3>
	1. Go to the <a href="http://mitate.cs.montana.edu/mitate_signup.php" target="_blank">signup</a> page and create an account with MITATE system. After the signup process, you will receive an email with a verification link to verify your account. The email might get delivered to your spam folder. Click on the link to complete the registration process with the MITATE system. You will not be able to proceed without verifying your account.<br /><br />
	2. Go to the <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a> page and download the <i>MITATE Command Line API</i>. <br /><br />
	3. Open your terminal, go to the folder where you downloaded the <i>MITATE Command Line API</i> file. <br /><br />
	4. Run <b>chmod 777 mitate.sh</b> command.<br /><br />
	5. Run <b>./mitate.sh help</b> to see all possible API calls. <br /><br />
	6. Now, run <b>./mitate.sh login</b> to authenticate yourself. After you execute this command, you will be asked to enter MITATE username and password. You will then get a message <b>You are now authenticated</b> if the login is successful.<br /><br />
	7. Run <b>CREATE DATABASE m_schema;</b> to create a database schema in your local MySQL instance.<br /><br />
	8. Run <b>USE m_schema;</b> to set m_schema as your default schema.<br /><br />
	9. To create a database user and grant it all privileges, run <b>CREATE USER 'm_user' IDENTIFIED BY 'm_pass';</b> and then <b>GRANT ALL PRIVILEGES ON m_schema . * TO 'm_user';</b>.<br /><br />
	10. Run <b>./mitate.sh init localdb.sql</b> to generate SQL scripts to setup your local MySQL instance.<br /><br />
	11. Run <b>mysql -u m_user -p m_schema < localdb.sql</b> to create and initialize tables in m_schema.<br /><br />
	
	<h3>Step 2: Uploading an experiment</h3>
	12. Now, download a sample MITATE <a target="_blank" href="http://mitate.cs.montana.edu/sample/exp_conf.xml">XML configuration file.</a><br /><br />
	13. Run <b>./mitate.sh validate exp_conf.xml</b> to validate the XML against <a target="_blank" href="http://mitate.cs.montana.edu/sample/Mitate_Sample_Configuration_File_XML_Format.xsd">MITATE XML Schema Definition</a> file. <br /><br />
	14. If the validation was successful, you will see a message <b>XML experiment  is valid.</b> If you do not see this message, fix the XML file based on the error message(s). (Refer to MITATE XML Schema Definition) <br /><br />
	15. Run <b>./mitate.sh upload exp_conf.xml</b> to submit your experiment to MITATE system. If successful, you will receive an experiment ID for the experiment you just uploaded.<br /><br />
	16. Connect your mobile phone to a Wi-Fi network and go to the <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a> page and download the 'MITATE Android Application'. After downloading, install the application on your mobile, enter the login credentials and finally press the <b>Start Service</b> button. It may take a minute for the experiment to execute.<br /><br />
	
	<h3>Step 3: Getting results</h3>
	17. To check the status of your uploaded experiment, run <b>./mitate.sh getExpStatus experiment_ID</b>, where <i>experiment_ID</i> is the experiment ID you received in step 15.<br /><br />
	18. To pull results of executed transfer from the MITATE database, run <b>./mitate.sh query results.sql</b>. This will create a file <b>results.sql</b> in the directory where mitate.sh resides.<br /><br />
	19. Run <b>mysql -u m_user -p m_schema < results.sql</b> to populate tables in m_schema.<br /><br />
	
	<h3>Step 4: Querying local database</h3>
	20. To query data for TCP Uplink Throughput, go to the <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a> page and download the SQL file for <i>TCP Uplink Throughput</i> and run <b> mysql -u m_user -p m_schema < tcp_uplink_throughput.sql	</b><br /><br />
	21. To query data for minimum, mean, and maximum TCP Uplink Latency, go to the <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a> page and download the SQL file for <i>TCP Uplink Latency</i> and run <b> mysql -u m_user -p m_schema < tcp_uplink_latency.sql	</b><br /><br />
	22. To query data for TCP Uplink Jitter, go to the <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a> page and download the SQL file for <i>TCP Uplink Jitter</i> and run <b> mysql -u m_user -p m_schema < tcp_uplink_jitter.sql	</b><br /><br />
	23. To query data for TCP Uplink Packet Loss, go to the <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a> page and download the SQL file for <i>TCP Uplink Packet Loss</i> and run <b> mysql -u m_user -p m_schema < tcp_uplink_packet_loss.sql	</b><br /><br />
	24. To query data for Device Travel Speed in (Km/hr), Device Signal Strength, go to the <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a> page and download the SQL file for <i>Device Metrics</i> and run <b> mysql -u m_user -p m_schema < device_metric.sql	</b><br /><br />
	25. To query data like the unique deviceid, the network carrier name, and the model name, for the mobile device that executed your experiment, go to the <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a> page and download the SQL file for <i>Device Details</i> and run <b> mysql -u m_user -p m_schema < device_detail.sql	</b><br /><br />
	26. If you want to get the per packet network metrics for your experiment, which includes per packet throughput and latency, go to the <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a> page and download <i>MITATE_Per_Packet_Network_Metrics.java</i>. Open this file and replace the default database username and password with the one you have on your MySQL.<br /><br />
	27. Now run <b>javac MITATE_Per_Packet_Network_Metrics.java</b> from your command line terminal.<br /><br />
	28. Run, <b>java MITATE_Per_Packet_Network_Metrics</b>. <br /><br />
	29. 
	</div>
<?php include('footer.php'); ?>
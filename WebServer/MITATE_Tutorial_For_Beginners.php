<?php include('header.php'); ?>
    <div style="font-size: 18;" align="justify">
        <h3 style="text-decoration:underline">MITATE Tutorial for Beginners</h3>
        This tutorial will provide you step-by-step instructions to execute a basic experiment with MITATE. This
        tutorial assumes that you have read and understood the <a
                href="http://mitate.cs.montana.edu/sample/MITATE_User_Manual_v1.0.pdf" target="_blank">MITATE User
            Manual</a>, you have MySQL and JAVA installed on your computer, and you are working in a UNIX environment.

        <h3>Step 1: Initialization</h3>
        1. Go to the <a href="http://mitate.cs.montana.edu/mitate_signup.php" target="_blank">signup</a> page and create
        an account with MITATE system. After the signup process, you will receive an email with a verification link to
        verify your email. The email might get delivered to your spam folder. Click on the link in the email to complete
        the registration process with the MITATE system. You will not be able to proceed without verifying your account.<br/><br/>
        2. From your terminal, run <br/><br/><b style="font-family: consolas;">$ mkdir MITATE</b><br/><br/> followed by
        <br/><br/><b style="font-family: consolas;">$ cd MITATE</b>.<br/><br/>
        3. Download the <i>MITATE Command Line API</i>, which is also available at the <a
                href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a> page, by
        using<br/><br/><b style="font-family: consolas;">$ wget
            http://nl.cs.montana.edu/mitate/sample/mitate.sh</b><br/><br/>
        4. Run <br/><br/><b style="font-family: consolas;">$ chmod 777 mitate.sh</b><br/><br/>
        5. To see all possible API calls, run <br/><br/><b style="font-family: consolas;">$ ./mitate.sh help</b>
        <br/><br/>
        6. Now, authenticate yourself by executing <br/><br/><b style="font-family: consolas;">$ ./mitate.sh
            login</b><br/><br/>After you execute this command, you will be asked to enter MITATE username and password.
        You will then get a message <b style="font-family: consolas;">You are now authenticated</b> if the login is
        successful.<br/><br/>
        7. The next few steps create a local instance of a MySQL database to store data returned by MITATE. If you don't
        have a local instance of a MySQL database installed, here's a <a
                href="http://community.linuxmint.com/tutorial/view/458">link</a> to a tutorial on how to set up mysql.
        To run the following commands that will set your database, you'll need MySQL root user credentials.<br/><br/>
        8. Connect to the MySQL database using: <br/><br/><b style="font-family: consolas;">$ mysql -u root
            -p</b><br/><br/>
        9. Now, create a database schema in your local MySQL instance by using the following command from the mysql
        prompt. <br/><br/><b style="font-family: consolas;">mysql> CREATE DATABASE m_schema;</b><br/><br/>
        10. Now create a database user and grant it all privileges, run <br/><br/><b style="font-family: consolas;">mysql>
            CREATE USER 'm_user' IDENTIFIED BY 'm_pass';</b><br/><br/> followed by <br/><br/><b
                style="font-family: consolas;">mysql> GRANT ALL PRIVILEGES ON m_schema . * TO 'm_user';</b>.<br/><br/>
        11. Now, exit the MySQL command prompt by using:<br/><br/><b style="font-family: consolas;">mysql>
            EXIT;</b><br/><br/>
        12. Generate SQL scripts to setup your local MySQL database, run <br/><br/><b style="font-family: consolas;">$
            ./mitate.sh init localdb.sql</b><br/><br/>
        13. Create and initialize tables in <i>m_schema</i>, run <br/><br/><b style="font-family: consolas;">$ mysql
            --user=m_user --password=m_pass m_schema < localdb.sql</b><br/><br/>

        <h3>Step 2: Uploading an experiment</h3>
        14. Now, download a <i>sample MITATE XML configuration file</i> by using <br/><br/><b
                style="font-family: consolas;">$ wget http://mitate.cs.montana.edu/sample/exp_conf.xml</b></a><br/><br/>
        15. Now, validate the XML file against <a target="_blank"
                                                  href="http://mitate.cs.montana.edu/sample/Mitate_Sample_Configuration_File_XML_Format.xsd">MITATE
            XML Schema Definition</a> file by using <br/><br/><b style="font-family: consolas;">$ ./mitate.sh validate
            exp_conf.xml</b><br/><br/>If the validation is successful, you will see a message <i>XML experiment is
            valid.</i><br/><br/>
        16. Now submit your experiment to MITATE system by using <br/><br/><b style="font-family: consolas;">$
            ./mitate.sh upload exp_conf.xml</b><br/><br/>If successful, you will receive an experiment ID for the
        experiment you just uploaded.<br/><br/>
        17. Now go to the <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a> page
        from your mobile phone and download the 'MITATE Android Application'. After downloading and installing the
        application on your mobile phone, connect your mobile phone to a Wi-Fi network, as this experiment is configured
        to execute on a Wi-Fi network.<br/><br/>
        18. Now open the application and enter the MIATE login credentials and press the <b
                style="font-family: consolas;">Start Service</b> button. It may take upto a minute for the experiment to
        execute.<br/><br/>

        <h3>Step 3: Getting results</h3>
        19. To check the status of your uploaded experiment, run <br/><br/><b style="font-family: consolas;">$
            ./mitate.sh getExpStatus experiment_ID</b><br/><br/>where <i>experiment_ID</i> is the experiment ID you
        received in step 16.<br/><br/>
        20. Once the experiment has executed, pull the results the executed transfer from MITATE database by using <br/><br/><b
                style="font-family: consolas;">$ ./mitate.sh query results.sql</b><br/><br/>This will create a file <b
                style="font-family: consolas;">results.sql</b> in the directory where mitate.sh resides.<br/><br/>
        21. Now populate tables in <i>m_schema</i> by using<br/><br/><b style="font-family: consolas;">$ mysql
            --user=m_user --password=m_pass m_schema < results.sql</b><br/><br/>
        22. Now, download the <i>Per Packet Network Metrics</i> file (available at the <a
                href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a> page), which will
        get you the per packet network metrics containing throughput and latency for every packet in your experiment, by
        using: <br/><br/><b style="font-family: consolas;">$ wget
            http://nl.cs.montana.edu/mitate/sample/MITATE.jar</b><br/><br/>
        23. Execute the jar file to populate the table <i>packetmetrics</i> in your local database by using:<br/><br/><b
                style="font-family: consolas;">$ java -jar MITATE.jar -u m_user -p m_pass -s localhost -d m_schema -t
            3306</b><br/><br/>where <i>m_user</i> is the database username, <i>m_pass</i> is the database password, <i>localhost</i>
        is your database server address, <i>m_schema</i> is the database name, and <i>3306</i> is the port number on
        which your MySQL service is running. You may want to change these parameters.

        <h3>Step 4: Querying local database</h3>
        24. To query data for TCP Uplink Throughput, download the SQL file for <i>TCP Uplink Throughput</i> (also
        available at <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a> page) by
        using: <br/><br/><b style="font-family: consolas;">$ wget
            http://mitate.cs.montana.edu/sample/tcp_uplink_throughput.sql</b><br/><br/>followed by <i>(replace the
            experiment ID in the SQL file with the experiment ID you got in step 16 before executing the command
            below)</i><br/><br/><b style="font-family: consolas;">$ mysql --user=m_user --password=m_pass m_schema <
            tcp_uplink_throughput.sql </b><br/><br/>
        25. To query data for minimum, mean, and maximum TCP Uplink Latency, download the SQL file for <i>TCP Uplink
            Latency</i> (also available at <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a>
        page) by using: <br/><br/><b style="font-family: consolas;">$ wget
            http://mitate.cs.montana.edu/sample/tcp_uplink_latency.sql</b><br/><br/>followed by <i>(replace the
            experiment ID in the SQL file with the experiment ID you got in step 16 before executing the command
            below)</i><br/><br/><b style="font-family: consolas;">$ mysql --user=m_user --password=m_pass m_schema <
            tcp_uplink_latency.sql </b><br/><br/>

        26. To query data for TCP Uplink Jitter, download the SQL file for <i>TCP Uplink Jitter</i> (also available at
        <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a> page) by using:
        <br/><br/><b style="font-family: consolas;">$ wget http://mitate.cs.montana.edu/sample/tcp_uplink_jitter.sql</b><br/><br/>followed
        by <i>(replace the experiment ID in the SQL file with the experiment ID you got in step 16 before executing the
            command below)</i><br/><br/><b style="font-family: consolas;">$ mysql --user=m_user --password=m_pass
            m_schema < tcp_uplink_jitter.sql </b><br/><br/>

        27. To query data for TCP Uplink Packet Loss, download the SQL file for <i>TCP Uplink Packet Loss</i> (also
        available at <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a> page) by
        using: <br/><br/><b style="font-family: consolas;">$ wget
            http://mitate.cs.montana.edu/sample/tcp_uplink_packet_loss.sql</b><br/><br/>followed by <i>(replace the
            experiment ID in the SQL file with the experiment ID you got in step 16 before executing the command
            below)</i><br/><br/><b style="font-family: consolas;">$ mysql --user=m_user --password=m_pass m_schema <
            tcp_uplink_packet_loss.sql </b><br/><br/>

        28. To query data for Device Travel Speed in (Km/hr), Device Signal Strength, download the SQL file for <i>Device
            Metrics</i> (also available at <a href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a>
        page) by using: <br/><br/><b style="font-family: consolas;">$ wget
            http://mitate.cs.montana.edu/sample/device_metric.sql</b><br/><br/>followed by <i>(replace the experiment ID
            in the SQL file with the experiment ID you got in step 16 before executing the command
            below)</i><br/><br/><b style="font-family: consolas;">$ mysql --user=m_user --password=m_pass m_schema <
            device_metric.sql </b><br/><br/>

        29. To query data like the unique deviceid, the network carrier name, and the model name, for the mobile device
        that executed your experiment, download the SQL file for <i>Device Details</i> (also available at <a
                href="http://mitate.cs.montana.edu/mitate_downloads.php" target="_blank">downloads</a> page) by using:
        <br/><br/><b style="font-family: consolas;">$ wget
            http://mitate.cs.montana.edu/sample/device_detail.sql</b><br/><br/>followed by <i>(replace the experiment ID
            in the SQL file with the experiment ID you got in step 16 before executing the command
            below)</i><br/><br/><b style="font-family: consolas;">$ mysql --user=m_user --password=m_pass m_schema <
            device_detail.sql </b><br/><br/>
    </div>
<?php include('footer.php'); ?>
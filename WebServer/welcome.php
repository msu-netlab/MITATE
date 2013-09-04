<?php
include("usermenu.php");
?>
<p style="font-family:calibri;">
<font size="4">
Many mobile applications allow users to interact with their environment and each other in near real-time. These interactions take place in multiplayer games, video chat, and in-context services, such as augmented reality, or human machine interaction, for example through Siri. These applications differ from static content browsing, in that their usability depends on low latency network service delivering user requests between mobile devices and cloud datacenters, on which back-end logic is deployed, in a timely manner.
<br /><br />
To design communication protocols that keep user request delay low across a range of network conditions, application developers need to know now only network performance characteristics such as latency, jitter, and bandwidth, but also specific network configuration and provisioning details that affect packet delay. Mobile Internet Testbed for Application Traffic Experimentation (MITATE) is a platform that allows application developers to experiment with different transaction traffic to tune application communication protocols for different network settings. MITATE allows participants to evaluate application layer transaction traffic exchanged between mobile users and cloud datacenters, on which most mobile application backend logic is deployed. These transactions are defined as sequences of messages of specific size, to be exchanged with specific timing, between identified Internet end-points. MITATE reports several network performance metrics for these message sequences as measured at individual mobile devices.
<br /><br />
MITATE deployment incentive model is similar PlanetLab, where participants are allowed into the system by contributing network resources of their mobile devices, to ensure that the system has enough capacity to support experimentation without overwhelming user willingness to contribute mobile bandwidth, or battery power.
</font>
</p>
<center><p style="font-family:calibri;"><font size="4">Test Credit</font></p>

<?php include("chart.php");?>
</center>
<br />
<br />
<p style="font-family:calibri;"><font size="4">
The above graph shows your history of earned, used, and balance of test data credit. You earn test data credit over time by running other users' network experiments on your mobile devices. You use test data credit when other users' devices run your experiments. The top line shows your current balance (currently capped at 20MB).
</font>
</p>
<br />
<?php
include("bmenu.php");
?>

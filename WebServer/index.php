<?php include('header.php'); ?>
    <br/>
    <div style="font-size: 18;text-align: justify;">Innovative mobile applications, such as multiplayer games and
        augmented reality, will require low message delay to provide a high quality of user experience (QoE). Low
        message delay, in turn, depends on low network latency and high available bandwidth between mobile devices and
        cloud datacenters, on which application back-end logic is deployed. Unfortunately, mobile network performance
        can change rapidly. Worse, traffic shaping mechanisms in cellular networks, such as as cap-and-throttle, traffic
        redundancy elimination, and deep packet inspection (DPI), can delay application messages without being reflected
        in standard metrics of network performance.<br/><br/>
        If innovation in the mobile space is to achieve broad adoption, new applications must deliver a high QoE across
        a range of network conditions. In other words, application communication protocols must be smart enough to adapt
        to changing network performance to keep message delay low. Such adaptations might include changing packet size,
        OR moving between server endpoints to deliver best traffic performance for a given client. <br/><br/>
        To design and validate adaptive communication protocols developers need to prototype their implementations in
        production networks. The research community has produced several testbeds capable of application prototyping in
        the wired Internet. To date, however, cellular network measurement platforms are not programmable in that they
        do not provide an foreign code execution environment. Instead applications are evaluated in network simulators
        configured to reflect measurements of network performance. While measurement based simulation allows repeatable
        experiments, it misses the dynamic effects of competing traffic in cellular schedulers and of traffic shaping
        mechanisms.
        <br/><br/>
        The technical problem we address is a lack of a programmable testbed for mobile application prototyping in
        production cellular networks. We have identified two challenges to building such a testbed. First, the personal
        nature of mobile devices creates user concerns over privacy, accountability for actions of foreign code being
        prototyped, and abuse of limited data plan and battery resources. Striking a balance between a flexible
        application prototyping environment and the safe execution of foreign code has been a difficult problem even in
        the more permissive wired environment. Second, because mobile battery and data plan resources are limited,
        testbed participants need adequate incentives to share them. Difficulty in enlisting mobile users has limited
        measurement studies to small samples, high cost of testbeds based on dedicated hardware, and collection of only
        high level network performance metrics. <br/><br/>
        MITATE is a Mobile Internet Testbed for Application Traffic Experimentation made possible by novel solutions to
        the problems of security and mobile resource sharing. MITATE is unique in that it allows programmable
        application traffic experiments between mobile hosts and backend server infrastructure. MITATE provides strong
        client security by separating application code execution from traffic generation. MITATE also provides
        incentives and protections for mobile resource sharing through tit-for-tat mechanisms.
        <br/><br/>
        MITATE's specialized traffic experiments can help developers answer questions crucial to mobile application
        design such as: What is the largest game state update message that can be reliably delivered under 100 ms?,"
        Does my application traffic need to contend with traffic shaping mechanisms?," OR Which CDN provides fastest
        downloads through a particular mobile service provider's network peering points?"
    </div>
<?php include('footer.php'); ?>
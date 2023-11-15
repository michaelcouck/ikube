ikube:
=====

Distributed, high volume, analytics processing platform, and search.

***

Ikube runs in a Java based server, specifically a servlet container, like [Tomcat](http://tomcat.apache.org/) or 
[Jetty](http://www.eclipse.org/jetty/). Any server can be used as it turns out, like [WildFly](http://wildfly.org/) 
or [GlassFish](https://glassfish.java.net/).

***

Ikube exposes the analytics(neural networks and statistical models), and search functionality by rest web services, providing
Json. As such analytics models and algorithms can be used without any programming, essentially from any rest client, even from a 
browser.

Features:
====
<ul>
    <li>Grid enabled/distributed</li>
    <ul>
        <li>Allows for extremely large volumes</li>
        <ul>
            <li>Big data and beyond</li>
            <li>IoT type of volumes</li>
        </ul>
        <li>Perfectly laterally scalable</li>
        <li>Automatic network discovery of nodes</li>
        <li>Automatic load distribution throughout the cluster</li>
    </ul>
    <li>Enterprise search embedded</li>
    <ul>
        <li>Allows for analytics to be indexed, and made searchable, for complex queries, and derived analytics</li>
        <li>All the features of enterprise search, including indexing of log files, file systems, networks, 
                 and databases in complex configurations</li>
    </ul>
    <li>Geospatial out of the box, for analytics and search</li>
    <li>Connectors for all major sources of data</li>
    <li>Process complex relational database hierarchies, adding enrichment strategies</li>
    <li>Cutting edge analytic statistical models and neural networks available</li>
</ul>

ikube:
=====

What is Ikube? Distributed, high volume, analytics processing platform, with embedded enterprise search.

***

Ikube runs in a Java based server, specifically a servlet container, like [Tomcat](http://tomcat.apache.org/) or 
[Jetty](http://www.eclipse.org/jetty/). Any server can be used as it turns out, like [WildFly](http://wildfly.org/) 
or [GlassFish](https://glassfish.java.net/).

***

Configuration for analytics models, and indeed all configuration is done in Spring configuration files. There is a base 
configuration, that can be downloaded from the [Artifactory](http://ikube.be/artifactory) in the libs-release-local repository, 
the ikube-war(to be dropped in the server) and the ikube-libs jar(to be unpacked). For more information please refer to the 
documentation at [site](http://ikube.be/site) or [web](http://ikube.be/web) if available.
 
***

Ikube exposes the analytics(neural networks and statistical models), and search functionality by rest web services, providing 
Json. As such analytics models and algorithms can be used without any programming, essentially from any rest client, even from a 
browser. Free and open server for this is at [production](http://ikube.be/ikube/system/dash.html) with login 
administrator/administrator. Please refer to the documentation how to call the analytics web services. There is also a living source 
of documentation at [apis exposed](http://ikube.be/ikube/documentation/apis.html). This will give the basic information of the web 
service, and a very scant dummy example of the input Json for the call. The documentation will contain a more fleshed out example 
of the Json for each call in the analytics services.

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
    <li>More to come...</li>
</ul>
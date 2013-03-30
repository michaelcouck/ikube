<div id="maincontent">
	<h2>The Ikube Project - Scalable search for big boys</h2>
	
	Ikube is a full text search platform for enterprise data. Clusterable and multithreaded for high volume environments. Ikube 
	is a pull model, meaning that you point Ikube at your data, set the schedules and Ikube will get the data, parse the content and 
	index it, making fast searches available to any application in XML or JSON format over rest web services.<br><br>

	* Complex full text search<br>
	* Automatic clustering for scalability and performance in high volume environments<br>
	* Geospatial search out of the box, either with data from database or CSV file, can be extended to any data format<br>
	* Cluster monitoring application for indexing and searching performance<br>
	* GUI for index action administration<br>
	* Flexible with configuration to integrate with any source of data<br>
	* Configurable text analysis, and pluggable/customizable<br>
	* Search API based on REST with formats in XML and Json for integration with any technology platform<br>
	* Administrative interface for monitoring indexing and searching, and for server control<br>
	* Auto index backup and restore for failover and fault tolerance<br>
	* Indexing performance optimizations for high volumes<br>
	* Rich document parsing, like Word, PDF etc. Most human readable formats will be parsed and indexed<br>
	* Email notification of system health and index availability<br>
	* Indexable CSV files with geospatial data<br>
	* Indexing of hierarchical/relational tables in databases<br>
	* Plugin points for strategies to combine sources of data, tables and internet for example<br><br>
	
	<h3>Detailed features:</h3> <br>
	* Possibility to define fields and types per resource, i.e. per column in a database or CSV file for example<br>
	* Declarative Lucene Analyzer for special cases like n-gram analysis etc.<br>
	* Spring loaded for easy customization and configuration<br><br>
	
	<h3>Query:</h3> <br>
	* Sort by any number of fields<br>
	* Highlighted fragments in results<br>
	* Range and numeric queries<br><br>
	
	<h3>Core:</h3> <br>
	* Cluster monitoring<br>
	* Cluster indexing load balancing and distribution<br>
	* Custom pluggable document processing chains and strategies<br>
	* Auto database/table discovery and indexing<br>
	* Soft landing for indexing failover and recovery on large indexes<br>
	* Flexible scheduled indexing<br>
	* Start and stop indexing in the user interface<br>
	* Auto cpu throttling, i.e.the indexing gets throttled as the cpu load goes past the threshold set in the parameters<br><br>
	
	<h3>Crawlers/indexers/connectors for:</h3><br>
	* Databases - any database, Oracle, Db2,  Postgres etc.<br>
	* File systems - Linux, NFS, NTFS etc.<br>
	* Internet - intranets and internet<br>
	* Log files - line by line indexing for easy access to key words in large log files<br>
	* CSV files - just like tables, with possibility to include geospatial data in 'columns'<br>
	* Email - Pop3 and Imap, typically any type of email server<br>
	
	Because Ikube is based on Spring, it is completely pluggable and customizable, just by implementing an interface and 
	adding the bean to the base configuration your bean will be used internally. This allows unparalleled configurability and 
	customizability. In most cases however this will not benecessary. Setting up an index on a database for example should take no 
	more than a few hours in the worst case scenario.<br><br>
	
	With embedded crawlers for internet, file system, database and email 
	most of the bases are covered. Having said that to add a custom handler, for SVN or FTP for example, would entail 
	implementing one interface for the handler, one interface for the entity and adding the handler to the configuration, 
	total time for development for FTP handler? Five days, including volume testing.<br><br>
	
</div>
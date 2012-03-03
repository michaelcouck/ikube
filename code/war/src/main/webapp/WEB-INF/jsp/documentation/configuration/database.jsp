<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="2">
			<span class="top-content-header">configuration</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	
	<tr>
		<td colspan="2">
			<strong>database</strong>&nbsp;
			This is the primary focus of Ikube, and the configuration definitions in this section are for the tables and the columns 
			in a relational database. Note that Ikube is completely extendable, as such other types of datasources could be handled 
			relatively simply by adding an indexable and handler.
		</td>
	</tr>

	<tr>
		<th colspan="2">Indexable table definition parameters</th>
	</tr>
	<tr>
		<td colspan="2"> 
			The table and database definition is the primary focus of Ikube. Ikube is designed to index databases in arbitrary complex structures. 
			First a table must be defined in the Spring configuration, including the data source as in the following:<br><br>
			
			<img src="<c:url value="/images/geoname.xml.jpg" />" alt="The geospatial table" /><br><br>
			
			The id and the class are for Spring, the rest of the properties of the bean are user defined, described below.
		</td>
	</tr>
	
	<tr>
		<th>Parameter</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>name</td>
		<td> 
			The name of the table. This will be used to generate the sql to access the table.
		</td>
	</tr>
	<tr>
		<td>predicate</td>
		<td> 
			 This is an optional parameter where a predicate can be defined to limit the results. A typical example is 
			 where faq.faqid &lt; 10000. Please see the spring-client.xml file for an example. 
		</td>
	</tr>
	<tr>
		<td>primary</td>
		<td> 
			Whether this table is a top level table. This will determine when the data collected while accessing the table 
			hierarchy will be written to the index. Sub tables are iterated over, the data is collected, and when the logic reaches 
			the top level table the data will be passed to Lucene in the form of a document.
		</td>
	</tr>
	<tr>
		<td>address</td>
		<td> 
			This flag indicates that the table and possibly columns and even sub tables form part of a physical address. Addresses 
			are used for GeoSpatial functionality. Address tables and the data contained in the columns are concatenated, a search 
			is done against the Ikube GeoSpatial index to find the closest match for the address and the latitude and longitude 
			properties for the address are added to the Lucene index. This facilitates searching for results around a point and ordering 
			them according to distance from that point. More information on how to configure the index with geospatial functionality 
			is available on the GeoSpatial page of the documentation(todo when the static ip is configured and the geospatial data 
			is enhanced).
		</td>
	</tr>
	<tr>
		<td>dataSource</td>
		<td> 
			The reference to the datasource where the table is. The datasource must be defined in the Spring configuration, using 
			perhaps C3p0 as the pooled datasource provider. Please see below the shot of the data source definition in the Spring 
			configuration:<br><br>
			
			<img src="<c:url value="/images/geoname.datasource.xml.jpg" />" alt="The geospatial data source" /><br><br>
			
			As you can see the properties for the database are quite self explanatory and common for databases per se.
		</td>
	</tr>
	<tr>
		<td>children</td>
		<td> 
			The children of the table. This is a list of mainly columns but also the sub tables will be defined in the child list 
			for the table. Please note the screen shot below which is of some column definitions for the GeoName table, the 
			id column and the name column:<br><br>
			
			<img src="<c:url value="/images/geoname.columns.xml.jpg" />" alt="The geospatial columns" /><br><br>
		</td>
	</tr>
	<tr>
		<td>maxExceptions</td>
		<td> 
			The maximum number of exception to allow before indexing the database is abandoned.
		</td>
	</tr>
	<tr>
		<td>allColumns</td>
		<td> 
			This parameter will force all the columns in the table to be indexed. You can still define columns as children, 
			and then if this property is true all the rest of the columns will be added dynamically with the default properties 
			for the columns. Please refer to the columns definitions below for more information on column definition.
		</td>
	</tr>
	<tr>
		<td colspan="2">
			As mentioned previously the sql to access the data is generated from the configuration. Tables can be nested within each 
			other as is normally the case with tables in a relational database. If a table is defined as a primary table and a child table is added 
			to the parent table then Ikube, while iterating over the results from the parent table, select related records from the child table(s) 
			and add the data to the parents' index documents.<br><br>
			
			In the spring-client.xml configuration file is the definition of the 'faq' and 'attachment' tables. These are an example 
			of the table nesting in the configuration.
			
			The result of this is a Lucene document with the following fields and values:<br><br>
					
			&lt;{id=faq.1}, {question=where is Paris}, {answer=In France}, {name=documentOne.doc}, 
			{attachment=Paris and Lyon are both situated in France}&gt;<br><br>
					
			The configuration of tables can be arbitrarily complex, nesting depth can be up to 10 tables or more.<br><br>
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<td colspan="2">
			Columns are defined and added to the tables as children. Below is a table of parameters that can be defined for columns.
		</td>
	</tr>
	<tr>
		<th colspan="2">Indexable column definition parameters</th>
	</tr>
	<tr>
		<th>Parameter</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>name</td>
		<td> 
			The name of the column.
		</td>
	</tr>
	<tr>
		<td>idColumn</td>
		<td> 
			Whether this is the id column in the table.
		</td>
	</tr>
	<tr>
		<td>nameColumn</td>
		<td> 
			This is used during indexing. For example in the case where there is a blob in the attachments table, and the name of the document 
			is in the 'name' column, this parameter is used to determine the mime type. If the name is document.doc, and there is a blob of the document 
			data then during indexing the .doc suffix will be used to get the correct parser to extract the text from the document, i.e. the Word parser.
		</td>
	</tr>
	<tr>
		<td>fieldName</td>
		<td> 
			 The name of the field in the Lucene index. This allows columns to have separate field names, increasing the flexibility when searching. For 
			 example if there are timestamps for creation and they are defined as separate Lucene fields then searches like timestamp &gt; 12/12/2010 
			 AND timestamp &lt; 12/12/2011 are possible.
		</td>
	</tr>
	<tr>
		<td>address</td>
		<td> 
			 As described above in the address field definition for the table, this flag is used to add the column data to the accumulated data for the 
			 address. The eventual data collected for the address will be used to search the geospatial index to find the co-ordinates. Typically an address 
			 column will be the name and number of a street, the city and the country. 
		</td>
	</tr>
	<tr>
		<td>foreignKey</td>
		<td> 
			  The reference to the foreign key in the 'parent' table. This is used to select the records from the 'child' table referring to the parent id.
		</td>
	</tr>
	<tr>
		<td>analyzed</td>
		<td> 
			A Lucene parameter, whether the data should be analyzed. Generally this is true.
		</td>
	</tr>
	<tr>
		<td>stored</td>
		<td> 
			A Lucene parameter, whether the data should be stored in the index. Generally this is true. Of course if there is very large volumes of data 
			then storing the data could be prohibitively expensive, in terms of disk space and time. The write time of the index is a large proportion of the 
			indexing time.   
		</td>
	</tr>
	<tr>
		<td>vectored</td>
		<td> 
			A Lucene parameter, whether the data should vectored. Generally this is true.
		</td>
	</tr>
	<tr>
		<td colspan="2">Please refer to the default configuration for a complete example of a nested table configuration.</td>
	</tr>
	
</table>

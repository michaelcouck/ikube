<%@ page errorPage="/WEB-INF/jsp/error.jsp" contentType="text/html" %>
<table class="table-content" width="100%">
	<tr>
		<td class="top-content">
			<span class="top-content-header">extra, extra</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	<tr>
		<td class="td-content">
			<strong>geospatial</strong>&nbsp;
			Geospatial search means adding co-ordinates to the results, addresses for example and sorting the 
			results according to distance from each other. Ikube has included the GeoNames database in an index, 
			called the 'geospatial' index. This index can be used to search for addresses. For example if you have a 
			client in the database who's address is Avenue Road, Cape Town South Africa, you could add these 
			columns in your database to the indexable table definition, and during the indexing Ikube will go to 
			the geospatial index, do a search for the address, take the top result and add the latitude and longitude 
			details to your index.<br><br>
		</td>
	</tr>
	<tr>
		<td class="td-content">
			Of course the accuracy of the results from the geospatial index are determined by the 
			data that was used to generate the index. The data has been enriched, and addresses include city and 
			country which should help to refine the search. When Telenet get around to giving me the subnet mask 
			and the default gateway(which could be never of course, this is Belgium) the url for the GeoSpatial web service 
			will be http://ikube.dyndns.org:8080/ikube/service/search/... Please refer to the documentation on searching 
			for more details on the searcher service.<br><br>
		</td>
	</tr>
	<tr>
		<td class="td-content">
			<strong>geospatial cont...</strong>&nbsp;
			The geospatial enricher is defined in spring-aop.xml file. If the indexable is defined as an address 
			then all the children will be iterated over to concatenate the address columns and data. The result of the concatenation 
			will be used to search the geospatial index, and the co-ordinates added to the index. The Geocoder can be switched 
			for another one of course, just by implementing the interface and changing the configuration. Of course this is the case 
			with all Spring beans, and indeed the purpose of Spring. For example if you have an agreement with Google to use their 
			geospatial data, you could use the GoogleGeocoder that has a simple implementation already, or NavTec for that matter.<br><br>
		</td>
	</tr>
	<tr>
		<td class="td-content">
			<strong>lucene</strong>&nbsp;
			Lucene in the indexing and search library that is at the heart of Ikube. Parameters can be 
			specified in the configuration and are passed to Lucene during the indexing process. For more information 
			on Lucene and how to configure it please refer to the excellent documentation for Lucene.<br><br>
		</td>
	</tr>
	<tr>
		<td class="td-content">
			<strong>disk full</strong>&nbsp;
			If at any point the disk where the indexes are fills up to within 10 meg, the instance of Ikube will close down, exiting the 
			Jvm completely. This is to prevent the disk becoming un-usable. This action, along with all other actions defined in the spring-actins.xml 
			can be removed simply by deleting the line.
			<br><br>
		</td>
	</tr>
	<tr>
		<td class="td-content">
			<strong>rules</strong>&nbsp;
			The rules for the actions to be executed are defined in the Spring configuration. The actions will be intercepted 
			by the rule intercepter, the rule(s) for those actions will be evaluated, and based on the result of the evaluation the 
			action will be allowed to continue. These rules can be removed to suit, but I really don't suggest it!
			<br><br>
		</td>
	</tr>
	<tr>
		<td class="td-content">
			<strong>production testing</strong>&nbsp;
			There are some definitions to test indexes in production, to verify that they are indeed up and running 
			and that they have some expected data. In the case where this is not the case a mail can be sent to one 
			or several addresses. This functionality will be enhanced in the future with SMS, which I think is more 
			friendly. Note that the Mailer bean in the spring-beans.xml needs to be changed to suit your own environment 
			or I will be getting mails from your system!
			<br><br>
		</td>
	</tr>
	<tr>
		<td class="td-content">
			<strong>databases</strong>&nbsp;
			The data sources must not be transactional. This is quite important as the connections are comitted 
			to allow the cursors to be released.
			<br><br>
		</td>
	</tr>

	<tr>
		<td class="td-content">
			Have a fantastic day!<br>
			Index the planet!<br>
			You are here =><br>
			Michael
		</td>
	</tr>
	
</table>

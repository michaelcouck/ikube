<%@ page errorPage="/WEB-INF/jsp/error.jsp" contentType="text/html" %>
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
			<strong>internet</strong>&nbsp;
			This indexable is an internet site or an intranet site. Note that the crawler is multi-threaded but not clusterable.
		</td>
	</tr>
	
	<tr>
		<th>Parameter</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>name</td>
		<td> 
			This is the name of the indexable, it should be unique within the configuration. It can be an arbitrary string.
		</td>
	</tr>
	<tr>
		<td>url</td>
		<td>
			The url of the site to index. Note that the host fragment of the url will be used as the base url 
			and the point to start the index. All pages and documents that are linked to this host or have the host as the 
			fragment in their url will be indexed.
		</td>
	</tr>
	<tr>
		<td>idFieldName</td>
		<td>
			The name of the field in the Lucene index for the identifier of this url. This is a field that will 
			be searched against when the index is created.
		</td>
	</tr>
	<tr>
		<td>titleFieldName</td>
		<td> 
			As above with the id field name this is the field in the Lucene index that will be searched against 
			for the title of the document. In the case of an HTML page the title tag. In the case of a word document 
			the parser will attempt to extract the title from the document for this field and so on.
		</td>
	</tr>
	<tr>
		<td>contentFieldName</td>
		<td> 
			The name of the lucene content field for the documents. When searching this index the field 
			and search string will be logically something like 'where {contentFieldName} = {searchString}'. 
		</td>
	</tr>
	<tr>
		<td>excludedPattern</td>
		<td> 
			Patterns that will be excluded from the indexing process. If there are files that should not be 
			indexed like images for example this can be used to exclude them from the indexing process.
		</td>
	</tr>
	<tr>
		<td>analyzed</td>
		<td> 
			Whether the data will be analyzed by Lucene before being written to the index. Typically this 
			will be true. For more information on the Lucene parameters please refer to the Lucene documentation.
		</td>
	</tr>
	<tr>
		<td>stored</td>
		<td> 
			Whether to store the data in the index. This will also typically be true as the fragment of text 
			returned by the search results will need the stored data to generate the fragment. However in the 
			case of very large document sets this will increase the index size considerably and my not be necessary.
		</td>
	</tr>
	<tr>
		<td>vectored</td>
		<td> 
			Whether the data from the documents will be vectored by Lucene. Please refer to the Lucene 
			documentation for more details on this parameter.
		</td>
	</tr>
	
	<tr>
		<td colspan="2">&nbsp;</td>
	</tr>
		
</table>
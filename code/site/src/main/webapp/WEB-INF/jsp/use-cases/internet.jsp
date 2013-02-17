<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="maincontent">
	<h2>Use cases</h2>
	<h4>Use case three: A web site</h4>
	Index a web site, all the pages and anything down loaded like PDF documents etc.:<br><br>

	<jsp:include page="/WEB-INF/jsp/use-cases/index-context.jsp" />
	<br><br>
	
	2) Create the internet site configuration: We first create the 'children' of the index, and then the internet 'bean', in this case there will be the only 
	child of the index. The list of children has one reference to the internet bean. And the index context above has a reference to the list of children.<br><br>
	
	In the internet bean replae the url placeholder with the url of the site you want to index. We'll leave all the other parameters default, the id field name can be id, 
	the title field name title and the content field name content. These are the fields that you will search, by name, essentially something like 'select from content field where 
	contains eclipse' for example. We will analyze the data, store it and vector it too. As we don't need to login to this site the login details are not necessary, and the batch size of 
	100 is more than enough per thread.<br><br>
	
	Take the snippit below and copy it under the myIndex bean.
	<br><br>
	
	<textarea rows="15" cols="30">
		<util:list id="myIndexChildren">
			<ref local="myIndexFolder" />
		</util:list>
		
		<bean
			id="ikubeInternet"
			class="ikube.model.IndexableInternet"
			property:name="internet"
			property:url="your-url-here" 
			property:idFieldName="id"
			property:titleFieldName="title"
			property:contentFieldName="content"
			property:excludedPattern="search"
			property:analyzed="true"
			property:stored="true"
			property:vectored="true"
			property:loginUrl="not-necessary-at-the-moment"
			property:userid="guest"
			property:password="guest"
			property:internetBatchSize="100" />
	</textarea>
	<br><br>
	
	<jsp:include page="/WEB-INF/jsp/use-cases/add-config.jsp" />
	
</div>
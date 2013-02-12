<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="maincontent">
	<h2>Use cases</h2>
	<h4>Use case one:</h4>
	Index a set of files on the file system. This can be the full hard disk of the machine or just a specific directory, or a set of directories:<br><br>

	1) Create the index configuration: In this Spring bean you define the name of the index (myIndex), importantly the maximum age, which will determine when 
	Ikube will re-index the data, the batch size for the files and the directory where Ikube will write the indexes, and the backup. Note that if you define the index 
	directory and the backup directory to be the same then there will be no backup, Ikube will see that there is a backup and that it is up to date and will just 
	carry on without any action.<br>
	The compoundFile parameter is for Lucene, as are bufferedDocs, bufferSize, mergeFactor, maxFieldLength and maxReadLength. Please refer to the Lucene 
	documentation on how to set these for tuning the memory allocation. Generally these will not need to be changed, unless the volume of data exceeds hundreds of 
	millions of documents. Copy the below snippit and paste it into a file called spring-custom.xml.
	
	<br><br>
	<textarea rows="15" cols="30">
		<?xml version="1.0" encoding="UTF-8"?>
		<beans
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xmlns="http://www.springframework.org/schema/beans"
			xmlns:context="http://www.springframework.org/schema/context"
			xmlns:property="http://www.springframework.org/schema/p"
			xmlns:util="http://www.springframework.org/schema/util"
			xsi:schemaLocation="
				http://www.springframework.org/schema/beans
				http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
				http://www.springframework.org/schema/context
				http://www.springframework.org/schema/context/spring-context-3.0.xsd
				http://www.springframework.org/schema/util
				http://www.springframework.org/schema/util/spring-util.xsd">
		
		<bean
			id="myIndex"
			class="ikube.model.IndexContext"
			property:name="myIndex"
			property:maxAge="600"
			property:compoundFile="true"
			property:bufferedDocs="100"
			property:bufferSize="32"
			property:internetBatchSize="10"
			property:mergeFactor="100"
			property:maxFieldLength="10000"
			property:maxReadLength="10000000"
			property:throttle="0"
			property:indexDirectoryPath="./ikube/index"
			property:indexDirectoryPathBackup="./ikube/index/backup"
			property:children-ref="myIndexFolders" />
			
		</beans>
	</textarea>
	<br><br>
	
	2) Create the file configuration: We first create the 'children' of the index, and indeed the file 'bean' in this case will be the only child of the index. The list of children 
	has one reference to the file system bean. And the index context above has a reference to the list of children, see it? In the file system bean, the path is the path where Ikube will 
	start reading the files off the file system. The path field name and the other field names are the names that Lucene will use when creating the index, and indeed this is what you 
	will use when you do a search on these files. We will not unpack the zips, we will store the data to create fragments, we will analyze it using Lucene, verrtor it too. We exclude 
	sys file pattern because system files are probably not interesting in binary form. The batch size is how many files each thread will process at a time. The maximum read length is 
	a meg, generally this is enough, and we set the maximum errors before we stop processing. Errors can happen when there is a badly formatted or corrupt file, or in the case of 
	permissions, etc. Take the snippit below and insert it under the myIndex bean.
	<br><br>
	<textarea rows="15" cols="30">
		
		<util:list id="myIndexFolders">
			<ref local="myIndexFolder" />
		</util:list>
		
		<bean
			id="myIndexFolder"
			class="ikube.model.IndexableFileSystem"
			property:name="myIndexFolder"
			property:path="/tmp"
			property:pathFieldName="path"
			property:nameFieldName="name"
			property:lengthFieldName="length"
			property:contentFieldName="contents"
			property:lastModifiedFieldName="lastmodified"
			property:unpackZips="false"
			property:stored="true"
			property:analyzed="true"
			property:vectored="true"
			property:excludedPattern=".*(sys).*"
			property:batchSize="1000"
			property:maxReadLength="1000000" 
			property:maxExceptions="100" />
		
	</textarea>
	<br><br>
	
	3) Place the configuration file in the Ikube folder: This folder is in the bin directory of the Tomcat, or if you are using another server it will be in the directory where 
	the startup script is. Start the server, either ./startup.sh or startup.bat.
	<br><br>
	
	Ikube will start indexing the file system where you specified in five minutes. Note that depending on the number of files, this could take some time, and you can monitor 
	the progress of the indexing process in the user interface.
</div>
	1) Create the index configuration: In this Spring bean you define the name of the index (myIndex), importantly the maximum age, which will determine when 
	Ikube will re-index the data, the batch size for the files, the directory where Ikube will write the indexes, and the backup directory. Note that if you define the index 
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
			property:children-ref="myIndexChildren" />
		</beans>
	</textarea>
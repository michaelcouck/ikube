<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

	2) Create the datasource: To index a database you have to have the database details. This bean will define where it is(i.e. the url) and how to access it(userid and password). Some other 
	details are also specified like the fail over and connection recovery. We won't concentrate too much on these details, you can accept the defaults. What yo do need to fill in is the userid and 
	the password that you use to connect to your database. For the driver class, you can use org.h2.Driver for the H2 database, 
	or com.ibm.db2.jcc.DB2Driver for DB2, or oracle.jdbc.driver.OracleDriver forOracle or org.postgresql.Driver for Postgres.If you have another database then you must add the 
	driver jar to the common libs directory in the server, and specify the class name of your driver, perhaps MySql for example. But we will assume that you are using a database which has direct driver 
	support in Ikube(DB2, Oracle, Postgres and H2). Copy the snippit below and paste it into your configuration file below the myIndex bean. Replacing the userid, the password, the url and the driver placeholders with your details 
	of course.
	<br><br>
	
	<textarea rows="15" cols="30">
		<bean id="myIndexDatasource"
			class="com.mchange.v2.c3p0.ComboPooledDataSource"
			destroy-method="close"
			property:user="your-user-id-here"
			property:password="your-password-here"
			property:driverClass="your-database-driver-class"
			property:jdbcUrl="your-database-url"
			property:initialPoolSize="${jdbc.minPoolSize}"
			property:maxPoolSize="${jdbc.maxPoolSize}"
			property:maxStatements="${jdbc.maxStatements}"
			property:checkoutTimeout="${jdbc.checkOutTimeout}"
			property:numHelperThreads="${jdbc.numHelperThreads}"
			property:breakAfterAcquireFailure="${jdbc.breakAfterAcquireFailure}"
			property:debugUnreturnedConnectionStackTraces="${jdbc.debugUnreturnedConnectionStackTraces}"
			property:testConnectionOnCheckin="${jdbc.testConnectionOnCheckin}"
			property:testConnectionOnCheckout="${jdbc.testConnectionOnCheckout}"
			property:unreturnedConnectionTimeout="${jdbc.unreturnedConnectionTimeout}" />
	</textarea>
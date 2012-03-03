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
			<strong>email</strong>&nbsp;
			This configuration is for indexing email accounts. Note that this handler is not multi threaded.
		</td>
	</tr>
	
	<tr>
		<th>Parameter</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>mailHost</td>
		<td>
			The mail host url to access the mail account.
		</td>
	</tr>
	<tr>
		<td>name</td>
		<td>
			The unique name of the indexable, this can be an arbitrary value.
		</td>
	</tr>
	<tr>
		<td>port</td>
		<td>
			The port to use for accessing the mail account. In the case of Google mail for example this is 995. This 
			has to be gotten from the mail provider.
		</td>
	</tr>
	<tr>
		<td>protocol</td>
		<td>
			The protocol to use. Also with Gmail the protocol is pop3 but is different for Hotmail and others, could be 
			imap for example.
		</td>
	</tr>
	<tr>
		<td>secureSocketLayer</td>
		<td>
			Whether to use secure sockets. Generally this will be true but need not be.
		</td>
	</tr>
	<tr>
		<td>password</td>
		<td>
			The password for the account.
		</td>
	</tr>
	<tr>
		<td>username</td>
		<td>
			The user account. In the case of the default mail account in the configuration this is ikube.ikube@gmail.com.
		</td>
	</tr>
	<tr>
		<td>idField</td>
		<td>
			The id field name in the Lucene index for the identifier of the message. The id is a concatenation of the 
			mail account, the message number and the user name.
		</td>
	</tr>
	<tr>
		<td>contentField</td>
		<td>
			The field name of the content field in the Lucene index. This is where the message data like the content and 
			the header will be added to the index. 
		</td>
	</tr>
	<tr>
		<td>titleField</td>
		<td>
			The name of the title field in the Lucene index.
		</td>
	</tr>
	<tr>
		<td></td>
		<td> 
		</td>
	</tr>
</table>
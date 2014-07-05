<!DOCTYPE html>
<html>
<head>
	<script src="http://code.jquery.com/jquery-latest.js"></script>
	<script src="http://malsup.github.com/jquery.form.js"></script>
</head>

<script>
	// wait for the DOM to be loaded 
	$(document).ready(function() {
		// bind 'myForm' and provide a simple callback function
		var forms = document.getElementsByTagName('form');
		for (var i=0; i < forms.length; i++) {
		    alert('Form : ' + forms[i] + ', ' + forms[i].name + ', ' + forms[i].id);
			$('#' + forms[i].id).ajaxForm(function() {
				alert("Thank you for your comment!");
			});
		} 
	});
</script>

<body>
	<form id="myForm" action="http://localhost:9080/ikube/service/monitor/set-properties" method="post">
		File: <input type="text" name="file" value="/usr/share/eclipse/workspace/ikube/code/war/./ikube/common/spring.properties" /> 
		Contents: <textarea name="contents">property=value</textarea>
		<input type="submit" value="Submit Comment" />
	</form>
</body>
</html>
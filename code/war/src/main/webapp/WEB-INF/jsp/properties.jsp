<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<table ng-controller="PropertiesController">
	<tr ng-model="propertyFiles" ng-repeat="(key, value) in propertyFiles">
		<td>
			<form 
				id="{{$index}}" 
				name="{{$index}}" 
				method="post"
				ng-submit="onSubmit()">
			<security:authorize access="hasRole('ROLE_ADMIN')">
				<input 
					id="button-{{$index}}" 
					name="button-{{$index}}" 
					type="submit" 
					value="Update">
			</security:authorize>
			<img src="<c:url value="/img/icons/jar_l_obj.gif" />" />&nbsp;Property file: {{key}}<br><br>
			<textarea name="contents" rows="10" cols="450" style="width: 95%;" ng-model="propertyFiles[key]">{{value}}</textarea>
			</form>
		</td>
	</tr>
</table>
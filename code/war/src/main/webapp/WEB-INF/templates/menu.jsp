<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<script type="text/javascript">
// Focus on the first field in the form
angular.element(document).ready(function() {
	doFocus('search');
});
</script>

<nav id="primary" class="main-nav">
	<ul>
		
		<li class="active"><a href="<c:url value="/index.html" />"><i class="icon-dashboard"></i>Dashboard</a></li>
		<li class=""><a href="<c:url value="/search.html" />"><i class="icon-list-alt"></i>Search</a></li>
		<li class=""><a href="<c:url value="/analytics.html" />"><i class="icon-beaker"></i>Analytics</a></li>
		<li class=""><a href="<c:url value="/indexes.html" />"><i class="icon-plus-sign"></i>Indexes</a></li>
		<li class=""><a href="<c:url value="/properties.html" />"><i class="icon-bar-chart"></i>Properties</a></li>
		<li class=""><a href="<c:url value="/admin.html" />"><i class="icon-bar-chart"></i>Admin</a></li>
		
		<li class="dropdown">
			<a class="dropdown-toggle" data-toggle="dropdown">
				<i class="icon-share-alt"></i>More<span class="caret"></span>
			</a>
			<ul class="dropdown-menu">
				<li><a href="#"><i class="icon-warning-sign"></i>Cluster graphs</a></li>
				<li class="divider"></li>
				<li>
					<a href="<spring:url value="/logout" htmlEscape="true" />" title="<spring:message code="security.logout" />">
							<i class="icon-off"></i><spring:message code="security.logout" />
						</a>
				</li>
			</ul></li>
	</ul>
</nav>

<nav id="secondary" class="main-nav">

	<div class="profile-menu">

		<div class="pull-left">
			<div class="avatar">
				<img src="<c:url value="/assets/images/MYP_1376-small.jpg" />" />
			</div>
		</div>

		<div class="pull-left">
			<div class="title">Ikube</div>
			<div class="btn-group">
				<button class="button mini inset black">
					<i class="icon-search"></i>
				</button>
				<button class="button mini inset black">Projects</button>
				<button class="button mini inset black dropdown-toggle"
					data-toggle="dropdown">
					<i class="icon-cog"></i>
				</button>
				<ul class="dropdown-menu black-box-dropdown">
					<li><a href="#">Action</a></li>
					<li><a href="#">Another action</a></li>
					<li><a href="#">Something else here</a></li>
					<li class="divider"></li>
					<li><a href="#">Separated link</a></li>
				</ul>
			</div>
		</div>
		
		<div class="pull-right profile-menu-nav-collapse">
			<button class="button black">
				<i class="icon-reorder"></i>
			</button>
		</div>

	</div>
	
	<tiles:insertAttribute name="sub-menu" />

</nav>

<div id="modal" class="black-box modal hide fade">
	<div class="modal-header tab-header">
		<button type="button" class="close" data-dismiss="modal">&times;</button>
		<span>Some modal title</span>
	</div>
	<div class="modal-body separator">
		<h4>Text in a modal</h4>
		<p>Duis mollis, est non commodo luctus, nisi erat porttitor
			ligula, eget lacinia odio sem.</p>
	</div>
	<div class="modal-footer">
		<div class="inner-well">
			<a class="button mini rounded light-gray" data-dismiss="modal">Close</a>
			<a class="button mini rounded blue">Save changes</a>
		</div>
	</div>
</div>

<div id="modal-gallery" class="black-box modal modal-gallery hide fade">
	<div class="modal-header tab-header">
		<button type="button" class="close" data-dismiss="modal">&times;</button>
		<span class="modal-title"></span>
	</div>
	<div class="modal-body">
		<div class="modal-image"></div>
	</div>
	<div class="modal-footer">
		<div class="pull-left">
			You can also change the images<br /> by scrolling the mouse wheel!
		</div>
		<div class="pull-right">
			<a class="button blue modal-next">Next<i class="icon-arrow-right icon-white"></i></a>
			<a class="button gray modal-prev"><i class="icon-arrow-left icon-white"></i> Previous</a>
			<a class="button green modal-play modal-slideshow" data-slideshow="5000"><i class="icon-play icon-white"></i>Slideshow</a>
			<a class="button black" target="_blank"><i class="icon-download"></i>Download</a>
		</div>
	</div>
</div>

<script type="text/html" id="template-notification">
<div class="notification animated fadeInLeftMiddle fast{{ item.itemClass }}">
	<div class="left">
		<div style="background-image: url({{ item.imagePath }})" class="{{ item.imageClass }}"></div>
	</div>
	<div class="right">
		<div class="inner">{{ item.text }}</div>
		<div class="time">{{ item.time }}</div>
	</div>
	<i class="icon-remove-sign hide"></i>
</div>
</script>

<script type="text/html" id="template-notifications">
<div class="container">
	<div class="row" id="notifications-wrapper">
		<div id="notifications"
			class="{{ bootstrapPositionClass }} notifications animated">
			<div id="dismiss-all" class="dismiss-all button blue">Dismiss
				all</div>
			<div id="content">
				<div id="notes"></div>
			</div>
		</div>
	</div>
</div>
</script>
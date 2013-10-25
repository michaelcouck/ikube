<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<div class="container-fluid">
	<div class="row-fluid">
		<div class="span6">
			<div class="row-fluid">
				<div class="span12">
					<div style="margin-bottom: 20px;">

						<div class="big-button-bar">
							<a class="button large" href="#"><i class="icon-bookmark"></i>
								<span>Analytics</span>
							</a> <a class="button large" href="#"><i class="icon-signal"></i>
								<span>Database</span>
							</a> <a class="button large" href="#"><i class="icon-comments"></i>
								<span>File system</span>
							</a> <a class="button large" href="#"><i class="icon-user"></i>
								<span>Users</span>
							</a> <a class="button large" href="#"><i class="icon-picture"></i>
								<span>Photos</span>
							</a> <a class="button large" href="#"><i class="icon-tags"></i>
								<span>Tags</span>
							</a>
						</div>

					</div>
				</div>
			</div>

			<div class="row-fluid">
				<div class="span12">
					<div class="box">
						<div class="tab-header">Searching performance</div>
						<div class="padded">
							<!-- <div id="chart1" style="width: 100%; height: 250px;"></div> -->
							<div searching style="width: 95%;">Searching performance graph</div>
						</div>
					</div>
				</div>
			</div>
			
			<div class="row-fluid">
				<div class="span12">
					<div class="box">
						<div class="tab-header">Indexing performance</div>
						<div class="padded">
							<!-- <div id="chart1" style="width: 100%; height: 250px;"></div> -->
							<div indexing style="width: 95%;">The indexing performance graph</div>
						</div>
					</div>
				</div>
			</div>

			<div class="row-fluid">
				<div class="span12">
					<table class="table table-striped table-bordered box">
						<thead>
							<tr>
								<th colspan="2">Table title</th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<td>Webhosting:</td>
								<td><strong>VPS Server 1</strong></td>
							</tr>
							<tr>
								<td>Available space:</td>
								<td>239/1000 GB used</td>
							</tr>
							<tr>
								<td>MySQL databases:</td>
								<td class="red">5/5 used</td>
							</tr>
							<tr>
								<td>Email accounts:</td>
								<td class="red">25/25 used</td>
							</tr>
							<tr>
								<td>Ruby version:</td>
								<td>1.9.3</td>
							</tr>
							<tr>
								<td>Rails version:</td>
								<td>3.0.1</td>
							</tr>
						</tbody>
						<tfoot>
							<tr>
								<td colspan="2">
									<div class="clearfix" style="padding: 0 5px;">
										<div class="pull-left">
											<a href="#" class="button blue">Buy now!</a> <a href="#"
												class="button">Cancel subscription</a>
										</div>
									</div>
								</td>
							</tr>
						</tfoot>
					</table>

				</div>
			</div>

		</div>

		<div class="span6">
			<div class="row-fluid">
				<div class="span12">
					<div class="tabbable black-box" style="margin-bottom: 18px;">

						<div class="tab-header">
							Server status <span class="pull-right"> <span
								class="options">
									<div class="btn-group">
										<a class="dropdown-toggle" data-toggle="dropdown"><i
											class="icon-cog"></i></a>
										<ul class="dropdown-menu black-box-dropdown dropdown-left">
											<li><a href="#">Action</a></li>
											<li><a href="#">Another action</a></li>
											<li><a href="#">Something else here</a></li>
											<li class="divider"></li>
											<li><a href="#">Separated link</a></li>
										</ul>
									</div>
							</span>
							</span>
						</div>

						<ul class="nav nav-tabs">
							<li class=""><a href="#tab1" data-toggle="tab"><i
									class="icon-globe"></i> System</a></li>
							<li class="active"><a href="#tab2" data-toggle="tab"><i
									class="icon-hdd"></i> Server</a></li>
						</ul>
						<div class="tab-content">

							<div class="tab-pane" id="tab1">
								<div class="separator">
									<div class="inner-well clearfix">
										<div class="pull-left">Antivirus status</div>

										<div class="pull-right">
											<input rel="confirm-check" type="checkbox" id="VKZp4"
												class="checky" checked="checked" /> <label for="VKZp4"
												class="checky"><span></span></label>
										</div>
									</div>

									<div class="inner-well clearfix">
										<div class="pull-left">Proxy server status</div>

										<div class="pull-right">
											<input rel="confirm-check" type="checkbox" id="FNNqp"
												class="checky" /> <label for="FNNqp" class="checky"><span></span></label>
										</div>
									</div>
								</div>
								<div class="separator">
									<div class="inner-well">
										<div id="stats1" style="width: 100%; height: 100px;"></div>
									</div>
								</div>
								<div class="padded">
									<div id="fix-stats">
										<p>
											<a rel="action" class='button mini rounded inset light-gray'>Delete
												stats</a>
										</p>

										<div style="display: none;" rel="confirm-action">

											<div class="inner-well clearfix">
												<b>Are you sure?</b>
												<div class="pull-right">
													<input rel="confirm-check" type="checkbox" id="NxwYk"
														class="checky" /> <label for="NxwYk" class="checky green"><span></span></label>
												</div>
											</div>

											<div class="clearfix vpadded">
												<div class="pull-left">
													<a href="#" class="button red" rel="confirm-do">Delete</a>
												</div>
												<div class="pull-right">
													<a href="#" class="button gray" rel="confirm-cancel">Cancel</a>
												</div>
											</div>

										</div>

									</div>
								</div>
							</div>

							<div class="tab-pane active" id="tab2">
								<div class="separator">
									<div class="inner-well">
										<div id="stats2" style="width: 100%; height: 100px;"></div>
									</div>
								</div>
								<div class="separator">
									<div class="inner-well clearfix">
										<div class="pull-left">Antivirus status</div>

										<div class="pull-right">
											<input rel="confirm-check" type="checkbox" id="GhVm3"
												class="checky" checked="checked" /> <label for="GhVm3"
												class="checky"><span></span></label>
										</div>
									</div>

									<div class="inner-well clearfix">
										<div class="pull-left">Proxy server status</div>

										<div class="pull-right">
											<input rel="confirm-check" type="checkbox" id="mxaz8"
												class="checky" /> <label for="mxaz8" class="checky"><span></span></label>
										</div>
									</div>
								</div>
								<div class="padded">
									<div id="fix-stats2">
										<p>
											<a rel="action" class='button mini rounded inset light-gray'>Delete
												stats</a>
										</p>

										<div style="display: none;" rel="confirm-action">

											<div class="inner-well clearfix">
												<b>Are you sure?</b>
												<div class="pull-right">
													<input rel="confirm-check" type="checkbox" id="gtnDu"
														class="checky" /> <label for="gtnDu" class="checky green"><span></span></label>
												</div>
											</div>

											<div class="clearfix vpadded">
												<div class="pull-left">
													<a href="#" class="button red" rel="confirm-do">Delete</a>
												</div>
												<div class="pull-right">
													<a href="#" class="button gray" rel="confirm-cancel">Cancel</a>
												</div>
											</div>

										</div>

									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>

			<div class="row-fluid">
				<div class="span12">
					<div class="black-box tex">
						<div class="tab-header">Recent comments</div>
						<ul class="recent-comments">



							<li class="separator">
								<div class="avatar pull-left">
									<img src="<c:url value="/assets/images/MYP_1376-small.jpg" />" />
								</div>

								<div class="article-post">
									<div class="user-info">Posted by jordan, 3 days ago</div>
									<div class="user-content">Vivamus sed auctor nibh congue,
										ligula vitae tempus pharetra... Vivamus sed auctor nibh
										congue, ligula vitae tempus pharetra... Vivamus sed auctor
										nibh congue, ligula vitae tempus pharetra...</div>

									<div class="btn-group">
										<button class="button black mini">
											<i class="icon-pencil"></i> Edit
										</button>
										<button class="button black mini">
											<i class="icon-remove"></i> Delete
										</button>
										<button class="button black mini">
											<i class="icon-ok"></i> Approve
										</button>
									</div>
								</div>
							</li>



							<li class="separator">
								<div class="avatar pull-left">
									<img src="<c:url value="/assets/images/MYP_1376-small.jpg" />" />
								</div>

								<div class="article-post">
									<div class="user-info">Posted by jordan, 3 days ago</div>
									<div class="user-content">Vivamus sed auctor nibh congue,
										ligula vitae tempus pharetra... Vivamus sed auctor nibh
										congue, ligula vitae tempus pharetra... Vivamus sed auctor
										nibh congue, ligula vitae tempus pharetra...</div>

									<div class="btn-group">
										<button class="button black mini">
											<i class="icon-pencil"></i> Edit
										</button>
										<button class="button black mini">
											<i class="icon-remove"></i> Delete
										</button>
										<button class="button black mini">
											<i class="icon-ok"></i> Approve
										</button>
									</div>
								</div>
							</li>


							<li class="separator" style="text-align: center"><a href="#">View
									all</a></li>
						</ul>
					</div>
				</div>
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
      <div id="notifications" class="{{ bootstrapPositionClass }} notifications animated">
        <div id="dismiss-all" class="dismiss-all button blue">Dismiss all</div>
        <div id="content">
          <div id="notes"></div>
        </div>
      </div>
    </div>
  </div>
</script>
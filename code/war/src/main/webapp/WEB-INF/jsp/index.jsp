<ul class="tabs">
	<li class="active" rel="dash">Dash</li>
	<li rel="search">Search</li>
	<li rel="indexes">Indexes</li>
	<li rel="place-holder" style="width : 750px;">&nbsp;</li>
</ul>

<div class="tab_container">
	<div id="dash" class="tab_content">
		<jsp:include page="/WEB-INF/jsp/dash.jsp" />
	</div>
	<div id="search" class="tab_content">
		<jsp:include page="/search.jsp" />
	</div>
	<div id="indexes" class="tab_content">
		<jsp:include page="/WEB-INF/jsp/indexes.jsp" />
	</div>
	<div id="place-holder" class="tab_content">
		&nbsp;
	</div>
</div>
</table>

<script type="text/javascript">
	$(document).ready(function() {
		$(".tab_content").hide();
		$(".tab_content:first").show();
		$("ul.tabs li").click(function() {
			$("ul.tabs li").removeClass("active");
			$(this).addClass("active");
			$(".tab_content").hide();
			var activeTab = $(this).attr("rel");
			$("#" + activeTab).fadeIn();
		});
	});
</script>

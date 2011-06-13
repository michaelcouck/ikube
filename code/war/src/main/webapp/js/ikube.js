function writeDate() {
	var d = new Date();
	document.write(d.toLocaleTimeString());
	document.write(' ');
	document.write(d.toLocaleDateString());
}

function popup(mylink, windowname) {
	if (!window.focus) {
		return true;
	}
	var href;
	if (typeof(mylink) == 'string') {
		href=mylink;
	} else {
		href=mylink.href;
	}
	window.open(href, windowname, 'width=400,height=200,scrollbars=yes');
	return false;
}

var refreshUrls = ['servers', 'discovery'];
function timedRefresh(timeout) {
	alert('Document location : ' + document.location);
	for (var i = 0; i < refreshUrls.length; i++) {
		var refreshUrl = refreshUrls[i];
		var contains = document.location.toString().indexOf(refreshUrl) > -1;
		alert('Refresh url : ' + refreshUrl + ', location contains : ' + contains);
		if (contains) {
			setTimeout('document.location.reload(true);', timeout);
		}
	}
}
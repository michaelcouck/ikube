/** Note: This file must be loaded after all the other JavaScript files. */

/**
 * This function will track the page view for Google Analytics.
 */ 
function track() {
	try {
		var pageTracker = _gat._getTracker("UA-13044914-4");
		pageTracker._trackPageview();
	} catch (err) {
		// document.write('<!-- ' + err + ' -->');
	}
}

/** The global refresh variable. */
var refreshInterval = 20000;
var chartRefreshInterval = 5000;

/**
 * This is the main Angular module for the iKube application on the 
 * client. This module will spawn and create the controllers and other
 * artifacts as required.
 */
var module = angular.module('ikube', [ 'ui.bootstrap' ]);

function writeDate() {
	var d = new Date();
	document.write(d.toLocaleTimeString());
	document.write(' ');
	document.write(d.toLocaleDateString());
}

function printMethods(object) {
	if (object == null) {
		return;
	}
	for (var m in object) {
		print('M : ' + m);
	}
}

var ua = navigator.userAgent.toLowerCase();
var check = function(r) {
    return r.test(ua);
};
var DOC = document;
var isStrict = DOC.compatMode == "CSS1Compat";
var isOpera = check(/opera/);
var isChrome = check(/chrome/);
var isWebKit = check(/webkit/);
var isSafari = !isChrome && check(/safari/);
var isSafari2 = isSafari && check(/applewebkit\/4/); // unique to
// Safari 2
var isSafari3 = isSafari && check(/version\/3/);
var isSafari4 = isSafari && check(/version\/4/);
var isIE = !isOpera && check(/msie/);
var isIE7 = isIE && check(/msie 7/);
var isIE8 = isIE && check(/msie 8/);
var isIE9 = isIE && check(/msie 9/);
var isIE6 = isIE && !isIE7 && !isIE8;
var isGecko = !isWebKit && check(/gecko/);
var isGecko2 = isGecko && check(/rv:1\.8/);
var isGecko3 = isGecko && check(/rv:1\.9/);
var isBorderBox = isIE && !isStrict;
var isWindows = check(/windows|win32/);
var isMac = check(/macintosh|mac os x/);
var isAir = check(/adobeair/);
var isLinux = check(/linux/);
var isSecure = /^https/i.test(window.location.protocol);
var isIE7InIE8 = isIE7 && DOC.documentMode == 7;

var jsType = '', browserType = '', browserVersion = '', osName = '';
var ua = navigator.userAgent.toLowerCase();
var check = function(r) {
    return r.test(ua);
};

if (isWindows) {
	osName = 'Windows';
	if (check(/windows nt/)) {
		var start = ua.indexOf('windows nt');
		var end = ua.indexOf(';', start);
		osName = ua.substring(start, end);
	}
} else {
	osName = isMac ? 'Mac' : isLinux ? 'Linux' : 'Other';
} 

if (isIE) {
	browserType = 'IE';
	jsType = 'IE';

	var versionStart = ua.indexOf('msie') + 5;
	var versionEnd = ua.indexOf(';', versionStart);
	browserVersion = ua.substring(versionStart, versionEnd);

	jsType = isIE6 ? 'IE6' : isIE7 ? 'IE7' : isIE8 ? 'IE8' : 'IE';
} else if (isGecko) {
	var isFF = check(/firefox/);
	browserType = isFF ? 'Firefox' : 'Others';
	;
	jsType = isGecko2 ? 'Gecko2' : isGecko3 ? 'Gecko3' : 'Gecko';

	if (isFF) {
		var versionStart = ua.indexOf('firefox') + 8;
		var versionEnd = ua.indexOf(' ', versionStart);
		if (versionEnd == -1) {
			versionEnd = ua.length;
		}
		browserVersion = ua.substring(versionStart, versionEnd);
	}
} else if (isChrome) {
	browserType = 'Chrome';
	jsType = isWebKit ? 'Web Kit' : 'Other';

	var versionStart = ua.indexOf('chrome') + 7;
	var versionEnd = ua.indexOf(' ', versionStart);
	browserVersion = ua.substring(versionStart, versionEnd);
} else {
	browserType = isOpera ? 'Opera' : isSafari ? 'Safari' : '';
}

function doFocus(elementId) {
	var element = document.getElementById(elementId);
	if (element != null) {
		// Bug in IE can't focus ... :)
		if (!isIE) {
			element.focus();
		}
	}
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
	window.open(href, windowname, 'width=750,height=463,scrollbars=yes');
	return false;
}

/**
 * This function will capitalize the first letter of a string.
 * 
 * @returns the string with the first letter capital
 */ 
String.prototype.capitalize = function() {
	return this.charAt(0).toUpperCase() + this.slice(1);
};

/**
 * This function builds the url to the rest search service.
 * 
 * @param the path part of the url  
 * @returns the url to the search rest web service
 */
function getServiceUrl(path) {
	var url = [];
	url.push(window.location.protocol);
	url.push('//');
	url.push(window.location.host);
	url.push(path);
	return url.join('');
}

//This function will set the width of the iframe dynamically
//so it can take advantage of more space on the screen if it is available
function setIframeWidth(iframe) {
	var PositionXY = {
		Width : 0,
		Height : 0
	};
	var db = document.body;
	var dde = document.documentElement;
	PositionXY.Width = Math.max(db.scrollTop, dde.scrollTop, db.offsetWidth, dde.offsetWidth, db.clientWidth, dde.clientWidth);
	PositionXY.Height = Math.max(db.scrollHeight, dde.scrollHeight, db.offsetHeight, dde.offsetHeight, db.clientHeight, dde.clientHeight);
	// Now take the smaller of the document width and the actual browser width
	PositionXY.Width = Math.min(PositionXY.Width, $(window).width());
	$('#' + iframe).attr('width', PositionXY.Width);
	$('#' + iframe).attr('height', PositionXY.Height);
	return PositionXY;
}
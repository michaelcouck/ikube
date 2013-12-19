/** Note: This file must be loaded after all the other JavaScript files. */

/**
 * This function will track the page view for Google Analytics..
 * 
 * @author Michael Couck
 * @since long time, probably around 2008 I guess
 */ 
function track() {
	try {
		var pageTracker = _gat._getTracker("UA-13044914-4");
		pageTracker._trackPageview();
	} catch (err) {
		// document.write('<!-- ' + err + ' -->');
	}
}

/** The global variables. */
var refreshInterval = 15000;
var chartRefreshInterval = 5000;
var allLanguages = new Array();
allLanguages.push('汉语', 'Nederlands', 'English', 'Español', '日本語', 'Le français', 'Deutsch', 'Svenska', 'ภาษาพูด', 'العربية', 'Türkçe', 'ру́сский');

/**
 * This is the main Angular module for the iKube application on the 
 * client. This module will spawn and create the controllers and other
 * artifacts as required.
 */
var module = angular.module('ikube', [ 'ui.bootstrap', 'ikube-services', 'googlechart', 'ui.slider' ]);

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

/**
 * This function will capitalize the first letter of a string.
 * 
 * @returns the string with the first letter capital
 */ 
String.prototype.capitalize = function() {
	return this.charAt(0).toUpperCase() + this.slice(1);
};

Array.prototype.last = function() {
	return this[this.length - 1];
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

(function() {
	$(function() {
		var _this = this;
		new Notifications({
			container : $("body"),
			bootstrapPositionClass : "span8 offset2"
		});
		return $("#notification-enterprise-version").click(function() {
			var imagePath = getServiceUrl('/ikube/assets/images/michael-couck.jpg');
			return Notifications.push({
				imagePath : imagePath,
				text : "<p>This feature is only in the enterprise version, contact michael.couck@gmail.com</p>",
				autoDismiss : 5
			});
		});
	});
}).call(this);

function notification(text, image, duration) {
	var imagePath = getServiceUrl(image);
	new Notifications({
		container : $("body"),
		bootstrapPositionClass : "span8 offset2"
	});
	Notifications.push({
		imagePath : imagePath,
		text : text,
		autoDismiss : duration
	});
};

function enterpriseNotification() {
	notification('This feature is only in the enterprise version, contact michael.couck@gmail.com', '/ikube/assets/images/michael-couck.jpg', '5');
}

function modal(element) {
	return $(element).modal();
}
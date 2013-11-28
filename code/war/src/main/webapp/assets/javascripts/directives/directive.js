/**
 * Load the Google visual after the directives to avoid some kind of recursive lookup.
 */
try {
	google.load('visualization', '1', { packages : [ 'corechart' ] });
} catch (err) {
	window.status = err;
}
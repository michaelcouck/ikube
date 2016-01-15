<?php
/**
 * The base configurations of the WordPress.
 *
 * This file has the following configurations: MySQL settings, Table Prefix,
 * Secret Keys, and ABSPATH. You can find more information by visiting
 * {@link http://codex.wordpress.org/Editing_wp-config.php Editing wp-config.php}
 * Codex page. You can get the MySQL settings from your web host.
 *
 * This file is used by the wp-config.php creation script during the
 * installation. You don't have to use the web site, you can just copy this file
 * to "wp-config.php" and fill in the values.
 *
 * @package WordPress
 */

// ** MySQL settings - You can get this info from your web host ** //
/** The name of the database for WordPress */
define('DB_NAME', 'ikube_dev');

/** MySQL database username */
define('DB_USER', 'ikube_dev');

/** MySQL database password */
define('DB_PASSWORD', '5yRP9JT4m8');

/** MySQL hostname */
define('DB_HOST', 'localhost');

/** Database Charset to use in creating database tables. */
define('DB_CHARSET', 'utf8');

/** The Database Collate type. Don't change this if in doubt. */
define('DB_COLLATE', '');

/**#@+
 * Authentication Unique Keys and Salts.
 *
 * Change these to different unique phrases!
 * You can generate these using the {@link https://api.wordpress.org/secret-key/1.1/salt/ WordPress.org secret-key service}
 * You can change these at any point in time to invalidate all existing cookies. This will force all users to have to log in again.
 *
 * @since 2.6.0
 */
define('AUTH_KEY',         '9!Qq&]qBc~o id4[suuRIjyDbb@T8y<$V*8xw8=e%D1@=FS|vs1d)#gn0xLF7l}j');
define('SECURE_AUTH_KEY',  '/t~!*tH.I_NF!]*dzD$;ewbbn$->(`Lfp<ShL6&?<1Od}#gVRw5X-PAa]nfjJ{|*');
define('LOGGED_IN_KEY',    'U(046~H7]WNLpwpcNGARa>6N1:]N4E{:o@tL1-9=kE]?QftvN$|,TVwVgK`]McP+');
define('NONCE_KEY',        'mCf+$6JHqp?~g].HOu7n0!P4-$%[uSXQZFpZ%]Ttxf@W$T moJt_4UH-Ljsa?dUK');
define('AUTH_SALT',        'X51P$wvFT]jK>-wWp:*E6q$c7tL/I)Rghj8J|gE 6Y=/=AP9^{+p.|t]dO&RAH^g');
define('SECURE_AUTH_SALT', '9+;U[3rUzuhYjxi9l>,+8*I$:/Wz<c-Qx_K1^;1/W^-f?rpgAjU@)Pe/hGA-Q[3d');
define('LOGGED_IN_SALT',   ']W:)(&$!U:<2[b9[w-vL5v.I})`32yT&8>;8L6Wji-os/lM:sI|0*AQpJs-rs<q-');
define('NONCE_SALT',       '1eJP<004w= Dq=FeHn@<XI+S6#nLrWLO|[Ux,ch=Rb7OKMEb+l23Zc`AdebJWj6P');

/**#@-*/

/**
 * WordPress Database Table prefix.
 *
 * You can have multiple installations in one database if you give each a unique
 * prefix. Only numbers, letters, and underscores please!
 */
$table_prefix  = 'wp_';

/**
 * For developers: WordPress debugging mode.
 *
 * Change this to true to enable the display of notices during development.
 * It is strongly recommended that plugin and theme developers use WP_DEBUG
 * in their development environments.
 */
define('WP_DEBUG', true);

/* That's all, stop editing! Happy blogging. */

/** Absolute path to the WordPress directory. */
if ( !defined('ABSPATH') )
	define('ABSPATH', dirname(__FILE__) . '/');

/** Sets up WordPress vars and included files. */
require_once(ABSPATH . 'wp-settings.php');

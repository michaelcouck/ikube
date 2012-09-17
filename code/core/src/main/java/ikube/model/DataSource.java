package ikube.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * @author Michael Couck
 * @since 14.09.12
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class DataSource extends Persistable {

	@Column
	private String name; // ="nonXaDataSourceDb2"
	@Column
	private String klass; // class="com.mchange.v2.c3p0.ComboPooledDataSource"
	@Column
	private String user; // ="${db2.jdbc.userid}"
	@Column
	private String password; // ="${db2.jdbc.password}"
	@Column
	private String driverClass; // ="${db2.jdbc.driver}"
	@Column
	private String jdbcUrl; // ="${db2.jdbc.url}"
	@Column
	private String initialPoolSize; // ="${jdbc.minPoolSize}"
	@Column
	private String maxPoolSize; // ="${jdbc.maxPoolSize}"
	@Column
	private String maxStatements; // ="${jdbc.maxStatements}"
	@Column
	private String maxStatementsPerConnection; // ="${jdbc.maxStatements}"
	@Column
	private String checkoutTimeout; // ="${jdbc.checkOutTimeout}"
	@Column
	private String numHelperThreads; // ="${jdbc.numHelperThreads}"
	@Column
	private String breakAfterAcquireFailure; // ="${jdbc.breakAfterAcquireFailure}"
	@Column
	private String debugUnreturnedConnectionStackTraces; // ="${jdbc.debugUnreturnedConnectionStackTraces}"
	@Column
	private String testConnectionOnCheckin; // ="${jdbc.testConnectionOnCheckin}"
	@Column
	private String testConnectionOnCheckout; // ="${jdbc.testConnectionOnCheckout}"
	@Column
	private String unreturnedConnectionTimeout; // ="${jdbc.unreturnedConnectionTimeout}"

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKlass() {
		return klass;
	}

	public void setKlass(String klass) {
		this.klass = klass;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getInitialPoolSize() {
		return initialPoolSize;
	}

	public void setInitialPoolSize(String initialPoolSize) {
		this.initialPoolSize = initialPoolSize;
	}

	public String getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(String maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public String getMaxStatements() {
		return maxStatements;
	}

	public void setMaxStatements(String maxStatements) {
		this.maxStatements = maxStatements;
	}

	public String getMaxStatementsPerConnection() {
		return maxStatementsPerConnection;
	}

	public void setMaxStatementsPerConnection(String maxStatementsPerConnection) {
		this.maxStatementsPerConnection = maxStatementsPerConnection;
	}

	public String getCheckoutTimeout() {
		return checkoutTimeout;
	}

	public void setCheckoutTimeout(String checkoutTimeout) {
		this.checkoutTimeout = checkoutTimeout;
	}

	public String getNumHelperThreads() {
		return numHelperThreads;
	}

	public void setNumHelperThreads(String numHelperThreads) {
		this.numHelperThreads = numHelperThreads;
	}

	public String getBreakAfterAcquireFailure() {
		return breakAfterAcquireFailure;
	}

	public void setBreakAfterAcquireFailure(String breakAfterAcquireFailure) {
		this.breakAfterAcquireFailure = breakAfterAcquireFailure;
	}

	public String getDebugUnreturnedConnectionStackTraces() {
		return debugUnreturnedConnectionStackTraces;
	}

	public void setDebugUnreturnedConnectionStackTraces(String debugUnreturnedConnectionStackTraces) {
		this.debugUnreturnedConnectionStackTraces = debugUnreturnedConnectionStackTraces;
	}

	public String getTestConnectionOnCheckin() {
		return testConnectionOnCheckin;
	}

	public void setTestConnectionOnCheckin(String testConnectionOnCheckin) {
		this.testConnectionOnCheckin = testConnectionOnCheckin;
	}

	public String getTestConnectionOnCheckout() {
		return testConnectionOnCheckout;
	}

	public void setTestConnectionOnCheckout(String testConnectionOnCheckout) {
		this.testConnectionOnCheckout = testConnectionOnCheckout;
	}

	public String getUnreturnedConnectionTimeout() {
		return unreturnedConnectionTimeout;
	}

	public void setUnreturnedConnectionTimeout(String unreturnedConnectionTimeout) {
		this.unreturnedConnectionTimeout = unreturnedConnectionTimeout;
	}

}
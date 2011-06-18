package ikube.cluster.discovery;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * TODO Document this class.
 * 
 * @author 7518871 [YourFullNameHere]
 * @version %PR%
 */
public class Base implements Serializable {

	public String name;

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

package ikube.model.validation;

import ikube.model.Indexable;
import ikube.service.IMonitorService;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Configurable
@Scope(value = "prototype")
@Component(value = "IndexableNameValidator")
public class IndexableNameValidator implements Validator {

	@Autowired
	private IMonitorService monitorService;

	@Override
	public boolean supports(Class<?> clazz) {
		return Indexable.class.isAssignableFrom(clazz);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void validate(Object target, Errors errors) {
		Collection<Indexable> indexables = new ArrayList<Indexable>(monitorService.getIndexContexts().values());
		boolean containsIndexable = containsIndexableName((String) target, indexables);
		if (containsIndexable) {
			errors.reject("", "Index context already contains indexable with name : " + target);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean containsIndexableName(final String name, final Collection<Indexable> indexables) {
		if (indexables == null) {
			return Boolean.FALSE;
		}
		for (Indexable indexable : indexables) {
			if (indexable.getName().equals(name)) {
				return true;
			}
			if (containsIndexableName(name, indexable.getChildren())) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

}

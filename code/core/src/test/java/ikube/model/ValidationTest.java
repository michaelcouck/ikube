package ikube.model;

import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Test;

public class ValidationTest extends AbstractTest {

	public ValidationTest() {
		super(ValidationTest.class);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void validate() {
		IndexContext indexContext = new IndexContext<Object>();
		ValidatorFactory validationFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validationFactory.getValidator();
		Set<ConstraintViolation<IndexContext>> constraintViolations = validator.validate(indexContext);
		logger.info("Constraint violations : " + constraintViolations);
		for (ConstraintViolation<IndexContext> constraintViolation : constraintViolations) {
			logger.info("        : message : " + constraintViolation.getMessage());

			logger.info("        : descriptor : " + constraintViolation.getConstraintDescriptor());
			logger.info("        : annotation : " + constraintViolation.getConstraintDescriptor().getAnnotation());
			logger.info("        : attributes : " + constraintViolation.getConstraintDescriptor().getAttributes());
			logger.info("        : composing constraints : " + constraintViolation.getConstraintDescriptor().getComposingConstraints());
			logger.info("        : groups : " + constraintViolation.getConstraintDescriptor().getGroups());
			logger.info("        : payload : " + constraintViolation.getConstraintDescriptor().getPayload());

			logger.info("        : invalid value : " + constraintViolation.getInvalidValue());
			logger.info("        : leaf bean : " + constraintViolation.getLeafBean());
			logger.info("        : property path : " + constraintViolation.getPropertyPath());
			logger.info("        : root bean : " + constraintViolation.getRootBean());
			logger.info("        : bean class : " + constraintViolation.getRootBeanClass());
		}
		assertTrue("There should be some violations : ", constraintViolations.size() > 0);
	}

}

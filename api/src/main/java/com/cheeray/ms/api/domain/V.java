package com.cheeray.ms.api.domain;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Generic validator for domain entities.
 * @author Chengwei.Yan
 */
public class V {

	/** Holder */
	private static class SV {
		private static final V I = new V();
	}

	public static V instance() {
		return SV.I;
	}

	private final Validator validator;

	private V() {
		final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		this.validator = factory.getValidator();
	}

	/**
	 * Validate an entity.
	 */
	public <T extends DomainObject> void validate(T t) throws InvalidFieldsException {
		try {
			final Set<ConstraintViolation<T>> violations = validator.validate(t);
			if (!violations.isEmpty()) {
				throw new InvalidFieldsException(
						violations.toArray(new ConstraintViolation[0]));
			}
		} catch (ValidationException | IllegalArgumentException e) {
			throw new InvalidFieldsException(e);
		}
	}
}

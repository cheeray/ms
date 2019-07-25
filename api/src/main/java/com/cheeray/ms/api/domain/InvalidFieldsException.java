package com.cheeray.ms.api.domain;

import java.util.Arrays;

import javax.validation.ConstraintViolation;

import com.cheeray.ms.api.ex.MsException;

/**
 * Validation exception.
 * @author Chengwei.Yan
 */
public class InvalidFieldsException extends MsException {
	private static final long serialVersionUID = 1L;

	public InvalidFieldsException(Throwable e) {
		super(e);
	}

	public InvalidFieldsException(ConstraintViolation<?>... vs) {
		super(Arrays.stream(vs)
				.map(v -> "\"" + v.getPropertyPath().toString() + "\":\"" + v.getMessage()
						+ "\",")
				.reduce("{", String::concat).concat("}").replaceAll(",}", "}"));
	}

}

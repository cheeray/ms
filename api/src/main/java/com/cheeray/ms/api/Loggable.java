package com.cheeray.ms.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Logging AOP.
 * @author Chengwei.Yan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Loggable {
	/**
	 * Log as debug? Ideally should be log level.
	 */
	public boolean debug() default false;

	/**
	 * Use stop watch?
	 */
	public boolean watch() default true;
}

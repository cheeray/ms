
package com.cheeray.ms.aop;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cheeray.ms.api.Loggable;
import com.cheeray.ms.api.domain.DomainObject;
import com.cheeray.ms.api.ex.MsException;
import com.cheeray.ms.api.ex.RepositoryException;
import com.cheeray.ms.api.ex.ServiceException;
import com.cheeray.ms.domain.validators.InvalidFieldsException;
import com.cheeray.ms.domain.validators.V;

/**
 * AOP of log and exception handling.
 * @author Chengwei.Yan
 */
@Component
@Aspect
public class MicroAppAspect {
	private static Logger LOG = LoggerFactory.getLogger(MicroAppAspect.class);

	@Pointcut("execution(* com.cheeray.ms.api.domain.IRepository+.save(..)) && args(o) && @annotation(javax.validation.Valid)")
	private void valid(DomainObject o) {
	}

	@Pointcut("execution(* *.*(..)) && @annotation(l)")
	private void log(Loggable l) {
	}

	@Around("log(l)")
	public Object aroundLog(ProceedingJoinPoint pjp, Loggable l) throws Throwable {
		final Method method = MethodSignature.class.cast(pjp.getSignature()).getMethod();
		final Logger log = LoggerFactory.getLogger(method.getDeclaringClass());

		// Stop watch ...
		if (l.watch()) {
			final long start = System.currentTimeMillis();
			try {
				return pjp.proceed();
			} finally {
				final long dur = System.currentTimeMillis() - start;
				if (l.debug()) {
					log.debug("{}({}) took {}ms.", method.getName(),
							Arrays.toString(pjp.getArgs()), dur);
				} else {
					log.info("{}({}) took {}ms.", method.getName(),
							Arrays.toString(pjp.getArgs()), dur);
				}
			}
		} else {
			if (l.debug()) {
				if (log.isDebugEnabled()) {
					log.debug("{}({}).", method.getName(),
							Arrays.toString(pjp.getArgs()));
				}
			} else {
				log.info("{}({}).", method.getName(), Arrays.toString(pjp.getArgs()));
			}
		}
		return pjp.proceed();
	}

	@AfterThrowing(pointcut = "execution(public * *.*(..))", throwing = "e")
	public void appExc(MsException e) {
		LOG.error("MS failure: " + e.getMessage(), e.getCause());
	}

	@AfterThrowing(pointcut = "execution(public * com.cheeray.ms.api.domain.IService+.*.*(..))", throwing = "e")
	public void srvExc(ServiceException e) {
		LOG.error("Service failure: " + e.getMessage(), e.getCause());
	}

	@AfterThrowing(pointcut = "execution(public * com.cheeray.ms.api.domain.IRepository+.*.*(..))", throwing = "e")
	public void repExc(RepositoryException e) {
		LOG.error("Repository failure: " + e.getMessage(), e.getCause());
	}

	@Before("valid(o)")
	public void validate(DomainObject o) throws InvalidFieldsException {
		V.instance().validate(o);
	}
}

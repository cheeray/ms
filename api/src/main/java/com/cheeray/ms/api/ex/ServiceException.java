package com.cheeray.ms.api.ex;

/**
 * Service layer exception.
 * @author Chengwei.Yan
 */
public class ServiceException extends MsException {
	private static final long serialVersionUID = 1L;

	public ServiceException(Throwable e) {
		super(e);
	}

	public ServiceException(String msg) {
		super(msg);
	}

}

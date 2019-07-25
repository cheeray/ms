package com.cheeray.ms.api.ex;

/**
 * Root of micro service exception.
 * @author Chengwei.Yan
 */
public class MsException extends Exception {

	private static final long serialVersionUID = 1L;

	public MsException(String msg) {
		super(msg);
	}

	public MsException(Throwable e) {
		super(e);
	}
}

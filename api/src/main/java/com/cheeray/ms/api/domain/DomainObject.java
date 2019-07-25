package com.cheeray.ms.api.domain;

import java.io.Serializable;
import java.math.BigDecimal;
/**
 * Domain interface.
* @author Chengwei.Yan
 */
public interface DomainObject extends Serializable {
	public static final BigDecimal ONE_HUNDRED = new BigDecimal("100.0000");
}

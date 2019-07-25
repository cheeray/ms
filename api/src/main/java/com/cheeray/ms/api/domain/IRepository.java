package com.cheeray.ms.api.domain;

import com.cheeray.ms.api.ex.RepositoryException;

/**
 * Data repository interface.
 * @author Chengwei.Yan
 */
public interface IRepository<T extends DomainObject> {

	/**
	 * Find entity by ID.
	 */
	public T get(Long id);

	/**
	 * Save an entity.
	 */
	public T save(T object, boolean newRecord) throws RepositoryException;
	
	/**
	 * Update an entity.
	 */
	public T save(T object, String query) throws RepositoryException;

	/**
	 * Delete an entity.
	 */
	public void delete(T object) throws RepositoryException;

}
/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cheeray.ms.example.repo;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import com.cheeray.ms.api.Loggable;
import com.cheeray.ms.api.domain.IRepository;
import com.cheeray.ms.api.ex.RepositoryException;
import com.cheeray.ms.example.domain.Product;

/*
* @author Chengwei.Yan
*/
@Repository("products")
public class ProductRepo extends NamedParameterJdbcDaoSupport
		implements IRepository<Product> {

	@Autowired
	public ProductRepo(DataSource dataSource) {
		super();
		setDataSource(dataSource);
	}

	@Override
	@Loggable
	public Product get(Long id) {
		return getJdbcTemplate().queryForObject("SELECT * FROM products WHERE id=" + id,
				(rs, rowNum) -> new Product(rs.getLong("id"), rs.getString("name"),
						rs.getInt("stock")));
	}

	@Override
	public Product save(Product product, boolean newRecord) throws RepositoryException {
		getJdbcTemplate().update("INSERT INTO products(name,stock) VALUES (?,?)",
				product.getName(), product.getStock());
		return product;
	}

	@Override
	public Product save(Product product, String query) throws RepositoryException {
		getJdbcTemplate().update(query);
		return product;
	}

	@Override
	public void delete(Product product) throws RepositoryException {
		getJdbcTemplate().update("DELETE FROM products WHERE id=?", product.getId());
	}
}

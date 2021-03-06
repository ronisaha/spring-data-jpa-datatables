package org.springframework.data.jpa.datatables.qrepository;

import static org.springframework.data.jpa.datatables.repository.DataTablesUtils.getPageable;
import static org.springframework.data.jpa.datatables.repository.DataTablesUtils.getPredicate;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.path.PathBuilder;

/**
 * Repository implementation
 * 
 * @author Damien Arrachequesne
 */
public class QDataTablesRepositoryImpl<T, ID extends Serializable>
    extends QueryDslJpaRepository<T, ID> implements QDataTablesRepository<T, ID> {

  private static final EntityPathResolver DEFAULT_ENTITY_PATH_RESOLVER =
      SimpleEntityPathResolver.INSTANCE;

  private final EntityPath<T> path;
  private final PathBuilder<T> builder;

  public QDataTablesRepositoryImpl(JpaEntityInformation<T, ID> entityInformation,
      EntityManager entityManager) {
    this(entityInformation, entityManager, DEFAULT_ENTITY_PATH_RESOLVER);
  }

  public QDataTablesRepositoryImpl(JpaEntityInformation<T, ID> entityInformation,
      EntityManager entityManager, EntityPathResolver resolver) {
    super(entityInformation, entityManager);
    this.path = resolver.createPath(entityInformation.getJavaType());
    this.builder = new PathBuilder<T>(path.getType(), path.getMetadata());
  }

  @Override
  public DataTablesOutput<T> findAll(DataTablesInput input) {
    return findAll(input, null, null);
  }

  @Override
  public DataTablesOutput<T> findAll(DataTablesInput input, Predicate additionalPredicate) {
    return findAll(input, additionalPredicate, null);
  }

  @Override
  public DataTablesOutput<T> findAll(DataTablesInput input, Predicate additionalPredicate,
      Predicate preFilteringPredicate) {
    DataTablesOutput<T> output = new DataTablesOutput<T>();
    output.setDraw(input.getDraw());
    try {
      long recordsTotal = preFilteringPredicate == null ? count() : count(preFilteringPredicate);
      if (recordsTotal == 0) {
        return output;
      }
      output.setRecordsTotal(recordsTotal);

      Page<T> data = findAll(new BooleanBuilder().and(getPredicate(this.builder, input))
          .and(additionalPredicate).and(preFilteringPredicate).getValue(), getPageable(input));

      output.setData(data.getContent());
      output.setRecordsFiltered(data.getTotalElements());

    } catch (Exception e) {
      output.setError(e.toString());
      output.setRecordsFiltered(0L);
    }

    return output;
  }
}

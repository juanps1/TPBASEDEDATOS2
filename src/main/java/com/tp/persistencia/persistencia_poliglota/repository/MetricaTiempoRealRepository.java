package com.tp.persistencia.persistencia_poliglota.repository;

import com.tp.persistencia.persistencia_poliglota.model.redis.MetricaTiempoReal;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricaTiempoRealRepository extends CrudRepository<MetricaTiempoReal, String> {
}

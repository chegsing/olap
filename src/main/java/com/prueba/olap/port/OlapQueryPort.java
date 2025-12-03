package com.prueba.olap.port;

import com.prueba.olap.service.dto.AggregationResponse;

public interface OlapQueryPort {
    AggregationResponse query(String mdxOrSql);
}

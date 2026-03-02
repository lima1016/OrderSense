package com.lima.orderservice.domain.order.repository;

import com.lima.orderservice.domain.order.model.document.OrderDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface OrderSearchRepository extends ElasticsearchRepository<OrderDocument, Long> {
}

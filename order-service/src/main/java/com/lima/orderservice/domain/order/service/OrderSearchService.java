package com.lima.orderservice.domain.order.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.lima.orderservice.domain.order.model.document.OrderDocument;
import com.lima.orderservice.domain.order.model.entity.Order;
import com.lima.orderservice.domain.order.repository.OrderRepository;
import com.lima.orderservice.domain.order.repository.OrderSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSearchService {

    private final OrderSearchRepository orderSearchRepository;
    private final OrderRepository orderRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * ES 검색 - 텍스트 검색 + 필터 + 페이징
     */
    public Map<String, Object> search(String query, String status, String region, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderTime"));

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        // 텍스트 검색 (customerName, deliveryAddress, region)
        if (query != null && !query.isBlank()) {
            boolBuilder.must(Query.of(q -> q
                    .multiMatch(m -> m
                            .query(query)
                            .fields("customerName", "deliveryAddress", "region")
                    )
            ));
        }

        // status 필터
        if (status != null && !status.isBlank()) {
            boolBuilder.filter(Query.of(q -> q
                    .term(t -> t.field("status").value(status))
            ));
        }

        // region 필터
        if (region != null && !region.isBlank()) {
            boolBuilder.filter(Query.of(q -> q
                    .term(t -> t.field("region").value(region))
            ));
        }

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(Query.of(q -> q.bool(boolBuilder.build())))
                .withPageable(pageRequest)
                .build();

        SearchHits<OrderDocument> searchHits = elasticsearchOperations.search(searchQuery, OrderDocument.class);

        List<OrderDocument> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        long totalElements = searchHits.getTotalHits();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        Map<String, Object> result = new HashMap<>();
        result.put("content", content);
        result.put("totalElements", totalElements);
        result.put("totalPages", totalPages);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    /**
     * 단건 인덱싱
     */
    public void indexOrder(Order order) {
        try {
            OrderDocument doc = OrderDocument.from(order);
            orderSearchRepository.save(doc);
            log.debug("ES 인덱싱 완료: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("ES 인덱싱 실패: orderId={}, error={}", order.getId(), e.getMessage());
        }
    }

    /**
     * PostgreSQL 전체 데이터를 ES에 일괄 인덱싱
     */
    public int reindexAll() {
        List<Order> allOrders = orderRepository.findAll();
        log.info("전체 리인덱싱 시작: {}건", allOrders.size());

        List<OrderDocument> docs = allOrders.stream()
                .map(OrderDocument::from)
                .collect(Collectors.toList());

        orderSearchRepository.saveAll(docs);
        log.info("전체 리인덱싱 완료: {}건", docs.size());
        return docs.size();
    }
}

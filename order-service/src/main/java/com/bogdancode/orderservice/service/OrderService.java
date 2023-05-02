package com.bogdancode.orderservice.service;

import brave.Span;
import brave.Tracer;
import com.bogdancode.orderservice.dto.InventoryResponse;
import com.bogdancode.orderservice.dto.OrderLineItemsDto;
import com.bogdancode.orderservice.dto.OrderRequest;
import com.bogdancode.orderservice.model.Order;
import com.bogdancode.orderservice.model.OrderLineItems;
import com.bogdancode.orderservice.repository.OrderRepository;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;

    @NewSpan("placeOrder")
    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        Random random = new Random();
        order.setOrderNumber(String.valueOf(random.nextInt(0, 10000)));
        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();


        Span newSpan = tracer.nextSpan().name("InventoryServiceLookUp").start();

        try (Tracer.SpanInScope spanInScope = tracer.withSpanInScope(newSpan)) {

            InventoryResponse[] resultsFromInventory = webClientBuilder.build().get()
                    .uri("http://inventory/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCodes", skuCodes).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();

            assert resultsFromInventory != null;
            boolean allProductsInStock = Arrays.stream(resultsFromInventory).allMatch(InventoryResponse::getIsInStock);

            if (!allProductsInStock) {
                throw new IllegalArgumentException("Product is not in stock!");
            }

            orderRepository.save(order);
            return "Order Placed Successfully";
        } finally {
            newSpan.finish();
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;

    }
}

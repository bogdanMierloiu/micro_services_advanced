package com.bogdancode.inventoryservice.service;

import com.bogdancode.inventoryservice.dto.InventoryResponse;
import com.bogdancode.inventoryservice.model.Inventory;
import com.bogdancode.inventoryservice.repository.InventoryRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InventoryService {

    private final InventoryRepo inventoryRepo;

//    @SneakyThrows
    public List<InventoryResponse> isInStock(List<String> skuCodes) {
//        log.info("Wait Started");
//        Thread.sleep(10000);
//        log.info("Wait Ended");
        List<Inventory> inventoryList = inventoryRepo.findBySkuCodeIn(skuCodes);
        if (inventoryList.size() != skuCodes.size()) {
            throw new IllegalArgumentException("One or more skuCodes not found in inventory.");
        }
        return inventoryList.stream()
                .map(inventory ->
                        InventoryResponse.builder()
                                .skuCode(inventory.getSkuCode())
                                .isInStock(inventory.getQuantity() > 0)
                                .build()
                )
                .toList();
    }

}

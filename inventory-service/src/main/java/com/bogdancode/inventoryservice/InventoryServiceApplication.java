package com.bogdancode.inventoryservice;

import com.bogdancode.inventoryservice.model.Inventory;
import com.bogdancode.inventoryservice.repository.InventoryRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(InventoryRepo inventoryRepo) {
        return args -> {
            Inventory inventory1 = new Inventory();
            inventory1.setSkuCode("abc");
            inventory1.setQuantity(100);

            Inventory inventory2 = new Inventory();
            inventory2.setSkuCode("def");
            inventory2.setQuantity(0);

            inventoryRepo.save(inventory1);
            inventoryRepo.save(inventory2);
        };
    }
}

package com.devguilhrm.API_ERP.sale.event;

import com.devguilhrm.API_ERP.product.entity.Product;
import com.devguilhrm.API_ERP.sale.entity.Sale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SaleEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(SaleEventPublisher.class);

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public SaleEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void saleCreated(Sale sale) {
		publish("sales.created", sale.getId().toString(), new SaleCreatedEvent(
				sale.getId(),
				sale.getSeller().getId(),
				sale.getClient().getId(),
				sale.getTotalAmount(),
				Instant.now()
		));
	}

	public void saleCompleted(Sale sale) {
		publish("sales.completed", sale.getId().toString(), new SaleCompletedEvent(
				sale.getId(),
				sale.getSeller().getId(),
				sale.getTotalAmount(),
				Instant.now()
		));
	}

	public void saleCancelled(Sale sale) {
		publish("sales.cancelled", sale.getId().toString(), new SaleCancelledEvent(
				sale.getId(),
				sale.getSeller().getId(),
				sale.getTotalAmount(),
				sale.getCancelReason(),
				Instant.now()
		));
	}

	public void stockUpdated(Sale sale, Product product, int deltaQuantity, String operation) {
		publish("stock.updated", product.getId().toString(), new StockUpdatedEvent(
				sale.getId(),
				product.getId(),
				deltaQuantity,
				product.getStockQuantity(),
				operation,
				Instant.now()
		));
	}

	private void publish(String topic, String key, Object event) {
		kafkaTemplate.send(topic, key, event)
				.whenComplete((result, ex) -> {
					if (ex != null) {
						log.error("Falha ao publicar evento Kafka no topico {}", topic, ex);
					} else {
						log.info("Evento Kafka publicado no topico {} com chave {}", topic, key);
					}
				});
	}
}

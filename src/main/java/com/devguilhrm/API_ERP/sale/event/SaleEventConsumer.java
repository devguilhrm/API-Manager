package com.devguilhrm.API_ERP.sale.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SaleEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(SaleEventConsumer.class);

	@KafkaListener(topics = "sales.created")
	public void onSaleCreated(SaleCreatedEvent event) {
		audit("sales.created", event);
	}

	@KafkaListener(topics = "sales.completed")
	public void onSaleCompleted(SaleCompletedEvent event) {
		audit("sales.completed", event);
	}

	@KafkaListener(topics = "sales.cancelled")
	public void onSaleCancelled(SaleCancelledEvent event) {
		audit("sales.cancelled", event);
	}

	@KafkaListener(topics = "stock.updated")
	public void onStockUpdated(StockUpdatedEvent event) {
		audit("stock.updated", event);
	}

	private void audit(String topic, Object event) {
		log.info("Auditoria assincrona recebida do topico {}: {}", topic, event);
		log.debug("Log de evento Kafka {}: {}", topic, event);
	}
}

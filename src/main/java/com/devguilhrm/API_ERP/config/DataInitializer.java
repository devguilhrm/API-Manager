package com.devguilhrm.API_ERP.config;

import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.auth.repository.UserRepository;
import com.devguilhrm.API_ERP.product.entity.Product;
import com.devguilhrm.API_ERP.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

	private final UserRepository userRepository;
	private final ProductRepository productRepository;
	private final PasswordEncoder passwordEncoder;

	public DataInitializer(
			UserRepository userRepository,
			ProductRepository productRepository,
			PasswordEncoder passwordEncoder
	) {
		this.userRepository = userRepository;
		this.productRepository = productRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		if (userRepository.count() == 0) {
			log.info("Criando usuarios iniciais de desenvolvimento");
			userRepository.save(User.builder()
					.name("Administrador")
					.email("admin@crm.com")
					.password(passwordEncoder.encode("admin123"))
					.role(Role.MANAGER)
					.enabled(true)
					.build());
			userRepository.save(User.builder()
					.name("Vendedor Um")
					.email("seller1@crm.com")
					.password(passwordEncoder.encode("seller123"))
					.role(Role.SELLER)
					.enabled(true)
					.build());
			userRepository.save(User.builder()
					.name("Vendedor Dois")
					.email("seller2@crm.com")
					.password(passwordEncoder.encode("seller123"))
					.role(Role.SELLER)
					.enabled(true)
					.build());
		}

		if (productRepository.count() == 0) {
			log.info("Criando produtos iniciais de desenvolvimento");
			productRepository.saveAll(List.of(
					product("Notebook Pro", "Notebook para equipes comerciais", "5500.00", 12),
					product("Monitor 27", "Monitor para produtividade", "1450.00", 20),
					product("Mouse Wireless", "Mouse ergonomico sem fio", "180.00", 80),
					product("Teclado Mecanico", "Teclado mecanico ABNT2", "390.00", 45),
					product("Headset Comercial", "Headset para atendimento", "260.00", 35)
			));
		}
	}

	private Product product(String name, String description, String price, int stock) {
		return Product.builder()
				.name(name)
				.description(description)
				.price(new BigDecimal(price))
				.stockQuantity(stock)
				.active(true)
				.build();
	}
}

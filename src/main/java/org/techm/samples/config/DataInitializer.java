package org.techm.samples.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.techm.samples.entity.Role;
import org.techm.samples.entity.User;
import org.techm.samples.repository.UserInfoRepository;

@Component
public class DataInitializer implements CommandLineRunner {
	
	@Autowired
	private UserInfoRepository userInfoRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) throws Exception {
		
		if(!userInfoRepository.existsByUsername("admin")) {
			User admin = new User();
			admin.setUsername("admin");
			admin.setEmail("admin@productcatalog.com");
			admin.setPassword(passwordEncoder.encode("root"));
			admin.setRole(Role.ADMIN);
			userInfoRepository.save(admin);
		}

	}

}

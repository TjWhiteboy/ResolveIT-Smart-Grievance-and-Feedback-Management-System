package com.example.resolveit.config;
import com.example.resolveit.model.User;
import com.example.resolveit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@resolveit.com") == null) {
            User admin = new User();
            admin.setName("System Administrator");
            admin.setEmail("admin@resolveit.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println(">>> Default Admin Created: admin@resolveit.com / admin123");
        }
        
        if (userRepository.findByEmail("staff@resolveit.com") == null) {
            User staff = new User();
            staff.setName("Support Staff");
            staff.setEmail("staff@resolveit.com");
            staff.setPassword(passwordEncoder.encode("staff123"));
            staff.setRole("STAFF");
            userRepository.save(staff);
            System.out.println(">>> Default Staff Created: staff@resolveit.com / staff123");
        }
    }
}

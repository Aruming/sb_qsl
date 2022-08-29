package com.ll.exam.qsl.base;

import com.ll.exam.qsl.user.entity.SiteUser;
import com.ll.exam.qsl.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.List;

@Configuration
@Profile("test") //이 클래스 정의된 Bean들은 test 모드에서만 활성화
public class TestInitData {
    @Bean
    CommandLineRunner init(UserRepository userRepository){
        return args -> {
            SiteUser u1 = SiteUser.builder()
                    .username("user1")
                    .password("{noop}1234")
                    .email("user1@test.com")
                    .build();

            SiteUser u2 = SiteUser.builder()
                    .username("user2")
                    .password("{noop}1234")
                    .email("user2@test.com")
                    .build();

            List<SiteUser> siteUsers = userRepository.saveAll(Arrays.asList(u1, u2));
        };
    }
}

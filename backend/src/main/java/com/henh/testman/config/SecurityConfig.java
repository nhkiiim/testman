package com.henh.testman.config;

import com.henh.testman.auth.JwtAuthenticationFilter;
import com.henh.testman.auth.TestmanUserDetailService;
import com.henh.testman.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private final TestmanUserDetailService testmanUserDetailService;

	private final UserService userService;

	@Autowired
	public SecurityConfig(TestmanUserDetailService testmanUserDetailService, UserService userService) {
		this.testmanUserDetailService = testmanUserDetailService;
		this.userService = userService;
	}

	// DAO 기반으로 Authentication Provider를 생성
	// BCrypt Password Encoder와 UserDetailService 구현체를 설정
	@Bean
	DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder());
		daoAuthenticationProvider
				.setUserDetailsService(this.testmanUserDetailService);
		return daoAuthenticationProvider;
	}

	// DAO 기반의 Authentication Provider가 적용되도록 설정
	@Override
	protected void configure(AuthenticationManagerBuilder auth) {
		auth.authenticationProvider(authenticationProvider());
	}

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
			.httpBasic().disable()
			.csrf().disable()
			.headers()
			.frameOptions().sameOrigin()
			.and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 토큰 기반 인증이므로 세션 사용 하지않음
			.and()
			.addFilter(new JwtAuthenticationFilter(authenticationManager(), userService)) //HTTP 요청에 JWT 토큰 인증 필터를 거치도록 필터를 추가
			.authorizeRequests()
			.antMatchers("/api/v1/users/regist").access("permitAll")
			.antMatchers("/api/v1/users/me", "/api/v2/concert/regist", "/api/v2/ticket/buy").authenticated()       //인증이 필요한 URL과 필요하지 않은 URL에 대하여 설정
			.anyRequest().permitAll()
			.and().cors();
    }
}
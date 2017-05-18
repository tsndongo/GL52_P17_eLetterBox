package com.florian;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.security.Principal;
import java.util.Optional;
import java.util.stream.Stream;


@EnableResourceServer
@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}
}

@RestController
class PrincipalRestControler {


	@RequestMapping ("/user")
	Principal principal (Principal p){
		return p;
	}

}

@EnableAuthorizationServer
@Configuration
class OAuthConfiguration extends AuthorizationServerConfigurerAdapter{
	private final AuthenticationManager authenticationManager;

	@Autowired
	public OAuthConfiguration(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory().withClient("acme")
						.secret("acmesecret")
				.authorizedGrantTypes("password")
				.scopes("openid");




	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.authenticationManager(this.authenticationManager);
	}
}


@Component
class AccountCLR implements CommandLineRunner{
	@Override
	public void run(String... args) throws Exception {
		Stream.of("florian,florian","test,test","test2,test2" ).map(t->t.split(",")).
				forEach(t->{this.accountRepository.save(new Account(t[0],t[1],true));
					System.out.println(" login "+t[0]+" password "+t[1]);

				});
		System.out.println("entrée");
	}


	private AccountRepository accountRepository;
	@Autowired
	public AccountCLR(AccountRepository repo) {
		this.accountRepository = repo;
	}
}


@Service
 class AccountUserDetailsService implements UserDetailsService{
	 @Override
	 public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		 return this.repo.findByUsername(username).
				 map(account ->new User(
				 		account.getUsername(),
						 account.getPassword(),
		 				account.isActive(),account.isActive(),account.isActive(),account.isActive(),
						 AuthorityUtils.createAuthorityList("ROLE_ADMIN","ROLE_USER")
				 )  ).orElseThrow(()-> new UsernameNotFoundException("no user add "+username ));
	 }


	 private AccountRepository repo;
	@Autowired
	public AccountUserDetailsService(AccountRepository repo) {
		this.repo = repo;
	}
}

@Repository
interface AccountRepository extends JpaRepository<Account,Long> {


	Optional<Account> findByUsername(String username);
}


@Entity
class Account{

	@GeneratedValue
	@Id
	private Long Id;

	private String username;
	private String password;

	private boolean active;

	public Account( String username, String password, boolean active) {

		this.username = username;
		this.password = password;
		this.active = active;
	}

	public Account(Long id, String username, String password, boolean active) {
		Id = id;
		this.username = username;
		this.password = password;
		this.active = active;
	}

	@Override
	public String toString() {
		return "Account{" +
				"Id=" + Id +
				", username='" + username + '\'' +
				", password='" + password + '\'' +
				", active=" + active +
				'}';
	}

	public Account() {
	}

	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
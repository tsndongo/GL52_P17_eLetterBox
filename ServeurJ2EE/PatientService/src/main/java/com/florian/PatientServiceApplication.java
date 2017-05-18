package com.florian;


import com.florian.Service.mongoRepo.PersonneRepository;
import com.florian.entity.Personne;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.SubscribableChannel;


@EnableBinding (PatientChanel.class)
@EnableDiscoveryClient
@SpringBootApplication
public class PatientServiceApplication {

	public static void main(String[] args) {
		//ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
		SpringApplication.run(PatientServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner runner(PersonneRepository rr){
		return args ->{
		//	Arrays.asList("florian".split(","))
		//			.forEach(n->rr.insert(new Personne(n,"bidule", Genre.MASCULIN,"10","note mal de gorge","mon SYmptoe","2016-06-30 10:30:21.213")));

		//	rr.findAll().forEach(System.out::println);
		//	System.out.println("------------------------");
		//	rr.updatetestSauvegarde("dede","update");
		//	rr.updatetestSauvegarde("rienEcrit","rienEcrit");
		};
	}
	@Autowired
	private final PersonneRepository  reservationRepository;

	public PatientServiceApplication(PersonneRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}
}
















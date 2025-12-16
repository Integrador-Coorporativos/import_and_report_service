package br.com.ifrn.ClassService;

import br.com.ifrn.ClassService.config.minio.MinioPropertiesConfig;
import br.com.ifrn.ClassService.config.keycloak.KeycloakPropertiesConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@EnableConfigurationProperties({
        MinioPropertiesConfig.class,
        KeycloakPropertiesConfig.class
})
@SpringBootApplication
@ComponentScan(basePackages = "br.com.ifrn.ClassService")
public class ClassServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClassServiceApplication.class, args);
	}

}
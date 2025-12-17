package br.com.ifrn.ImportReportService;

import br.com.ifrn.ImportReportService.config.minio.MinioPropertiesConfig;
import br.com.ifrn.ImportReportService.config.keycloak.KeycloakPropertiesConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@EnableConfigurationProperties({
        MinioPropertiesConfig.class,
        KeycloakPropertiesConfig.class
})
@SpringBootApplication
@ComponentScan(basePackages = "br.com.ifrn.ImportReportService")
public class ImportReportServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImportReportServiceApplication.class, args);
	}

}
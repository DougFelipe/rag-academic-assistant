package br.ufrn.distribuida.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server Application - Centralized Configuration Management.
 * 
 * <p>
 * This microservice provides centralized external configuration management
 * for all services in the distributed system. It serves configuration files
 * from a Git repository (or native filesystem) and supports:
 * <ul>
 * <li>Multiple environment profiles (dev, docker, prod)</li>
 * <li>Dynamic configuration refresh without service restart</li>
 * <li>Encrypted sensitive properties</li>
 * <li>Configuration versioning via Git</li>
 * </ul>
 * 
 * <p>
 * <b>Annotations:</b>
 * <ul>
 * <li>{@code @SpringBootApplication} - Enables Spring Boot
 * auto-configuration</li>
 * <li>{@code @EnableConfigServer} - Activates Spring Cloud Config Server</li>
 * </ul>
 * 
 * <p>
 * <b>Architecture:</b>
 * 
 * <pre>
 * Microservices → Config Server → Git Repository (config-repo)
 *                      ↓
 *                 Port 8888
 *                      ↓
 *              Configuration Files
 *              (application.yml,
 *               service-name.yml,
 *               service-name-profile.yml)
 * </pre>
 * 
 * <p>
 * <b>Configuration Priority:</b>
 * <ol>
 * <li>application.yml (global defaults)</li>
 * <li>{service-name}.yml (service-specific)</li>
 * <li>{service-name}-{profile}.yml (profile-specific)</li>
 * </ol>
 * 
 * @see org.springframework.cloud.config.server.EnableConfigServer
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

	/**
	 * Main method to bootstrap the Config Server application.
	 * 
	 * <p>
	 * Initializes Spring Boot context, starts embedded Tomcat server on
	 * port 8888, and begins serving configuration files from the configured
	 * repository location (default: file:../config-repo).
	 * 
	 * <p>
	 * <b>Startup Sequence:</b>
	 * <ol>
	 * <li>Load application.yml (bootstrap configuration)</li>
	 * <li>Initialize Config Server with repository location</li>
	 * <li>Start embedded web server on port 8888</li>
	 * <li>Expose configuration endpoints</li>
	 * <li>Enable actuator endpoints for monitoring</li>
	 * </ol>
	 * 
	 * @param args Command line arguments (not used)
	 */
	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}

}

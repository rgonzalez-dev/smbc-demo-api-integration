package rgonzalez.smbc.integration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for async task execution.
 * Configures thread pools for async operations.
 */
@Configuration
public class AsyncConfig {

    /**
     * Configure the executor for async operations.
     * 
     * @return Executor bean for async task execution
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }

    /**
     * Configure the executor specifically for SSN verification tasks.
     * 
     * @return Executor bean for SSN verification operations
     */
    @Bean(name = "ssnVerificationExecutor")
    public Executor ssnVerificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("ssn-verification-");
        executor.initialize();
        return executor;
    }
}

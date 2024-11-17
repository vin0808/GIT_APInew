package Base;

import config_pack.Config;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;

public class BaseTest {

    protected RequestSpecification request;
    private Config config;

    // Logger for logging information instead of System.out.println
    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);

    @BeforeClass
    public void setup() {
        // Initialize Config instance with default configuration or pass custom configurations
        config = new Config(); // or new Config("https://api.github.com", "Bearer <YOUR-TOKEN>")

        // Validate if the configuration values are set correctly
        if (config.getBaseUrl() == null || config.getBaseUrl().isEmpty()) {
            logger.error("Base URL is missing in configuration.");
            throw new IllegalArgumentException("Base URL is required.");
        }

        if (config.getToken() == null || config.getToken().isEmpty()) {
            logger.error("Token is missing in configuration.");
            throw new IllegalArgumentException("Token is required.");
        }

        // Set the base URI for RestAssured
        RestAssured.baseURI = config.getBaseUrl();

        // Create request specification with Authorization header and content type
        request = RestAssured
            .given()
            .header("Authorization", config.getToken())
            .header("Content-Type", "application/json");

        // Log the Authorization header (for debugging, remove in production)
        logger.info("Authorization Header: {}", config.getToken());
    }
}

package Test;

import Base.BaseTest;
import config_pack.Config;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import pojo.GitHubUser;
import utils.ApiUtils;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;


public class GitHubApiTests extends BaseTest {

    @Test
    public void testGetSingleRepository() {
        // Use a known valid GitHub repository for testing
        String owner = "vin0808";  // Replace with your owner
        String repo = "GIY_HUB_API";  // Replace with your repository

        // Send the GET request to fetch the repository details
        Response response = request
                .header("Authorization", "Bearer " + Config.getToken())  // Add Authorization header
                .pathParam("owner", owner)
                .pathParam("repo", repo)
                .when()
                .get("/repos/{owner}/{repo}")
                .then()
                .statusCode(200)   // Validate status code is 200
                .extract()
                .response();

        // Print the response for debugging purposes
        System.out.println(response.getBody().asPrettyString());
        System.out.println("*****************************************************************************************");

        // Validate the full_name in the response
        String expectedFullName = owner + "/" + repo;
        Assert.assertEquals(response.jsonPath().getString("full_name"), expectedFullName, "Full name does not match!");

        // Validate the default branch is "main"
        Assert.assertEquals(response.jsonPath().getString("default_branch"), "main", "Default branch is not 'main'!");

        // Validate Content-Type header
        String contentType = response.getHeader("Content-Type");
        Assert.assertEquals(contentType, "application/json; charset=utf-8", "Content-Type header is incorrect!");
    }
   
    
    
    
    @Test
    public void testGetSingleRepositoryWithNonExistingRepo() {
        // Replace with your owner and a non-existing repository name
        String owner = "vin0808";  // Replace with your GitHub username
        String repo = "NonExistingRepo";  // Replace with a non-existing repository name

        // Send the GET request to fetch the repository details
        Response response = request
                .pathParam("owner", owner)
                .pathParam("repo", repo)
                .when()
                .get("/repos/{owner}/{repo}")
                .then()
                .extract()
                .response();

        // Print the response for debugging purposes
        System.out.println(response.getBody().asPrettyString());
        System.out.println("*****************************************************************************************");

        // Validate status code is 404
        Assert.assertEquals(response.getStatusCode(), 404, "Expected status code 404 but got: " + response.getStatusCode());

        // Validate the message is "Not Found"
        String message = response.jsonPath().getString("message");
        Assert.assertEquals(message, "Not Found", "Error message does not match! Expected 'Not Found', but got: " + message);
    }
    
    @Test
    public void testGetAllRepositories() {
        // Make GET request to /user/repos endpoint
        int repoCount = 
            given()
                .header("Authorization", "Bearer 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") // Use Bearer token
            .when()
                .get("https://api.github.com/user/repos")
            .then()
                .statusCode(200) // Validate status code is 200
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("repo-schema.json")) // Validate schema
                .header("Content-Type", "application/json; charset=utf-8") // Validate Content-Type
                .extract().jsonPath().getList("$").size(); // Get number of repositories

        System.out.println("******************************************************************************************** ");
        
        
        // Print the number of repositories
        System.out.println("Number of repositories: " + repoCount);
        
        System.out.println("******************************************************************************************** ");

        // Additional validations: Print repository names which are public
        given()
            .header("Authorization", "Bearer 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")
        .when()
            .get("https://api.github.com/user/repos")
            
        .then()
            .statusCode(200)
            .log().all(); // Log the response body to see the details of repositories
    
    }

    @Test
    public void testCreateRepository() {
        // Define the body for creating a new repository
        String requestBody = "{\n" +
                "    \"name\": \"Hello-World-" + System.currentTimeMillis() + "\",\n" +  // Adding timestamp to make sure the repo name is unique
                "    \"description\": \"This is your first repo!\",\n" +
                "    \"homepage\": \"https://github.com\",\n" +
                "    \"private\": false\n" +
                "}";

        // Make POST request to create a new repository
        Response response = given()
            .header("Authorization", "Bearer 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") // Use Bearer token
            .contentType("application/json")
            .body(requestBody) // Send the request body
        .when()
            .post("https://api.github.com/user/repos")
        .then()
            .statusCode(201) // Validate that the status code is 201 (Created)
            .header("Content-Type", "application/json; charset=utf-8") // Validate Content-Type header
            .extract().response(); // Extract the response

        // Validate that the repository name in the response is the same as the one created
        String createdRepoName = response.jsonPath().getString("name");
        assertThat(createdRepoName, is(requestBody.split("\"name\": \"")[1].split("\"")[0])); // Compare created name with the request body

        // Validate that the 'login' field in the response matches the username
        String login = response.jsonPath().getString("owner.login");
        assertThat(login, is("vin0808")); // Replace with your GitHub username

        // Validate that the 'type' field in the response is "User"
        String type = response.jsonPath().getString("owner.type");
        assertThat(type, is("User"));

        // Print the details of the created repository
        System.out.println("******************************************************************************************** ");
        System.out.println("Repository Created Successfully!");
        System.out.println("Repository Name: " + createdRepoName);
        System.out.println("Repository Owner: " + login);
        System.out.println("Repository Type: " + type);
        System.out.println("******************************************************************************************** ");
    }
    
    @Test
    public void testCreateRepositoryWithExistingName() {
        // Define the name of the repository that already exists in your GitHub account
        String existingRepoName = "JAVA_Basics";  // Ensure this name exists in your GitHub account

        // Define the request body for creating a new repository with an existing name
        String requestBody = "{\n" +
                "    \"name\": \"" + existingRepoName + "\",\n" +
                "    \"description\": \"This is a test repo!\",\n" +
                "    \"homepage\": \"https://github.com\",\n" +
                "    \"private\": false\n" +
                "}";

        // Make POST request to create a new repository
        Response response = given()
            .header("Authorization", "Bearer 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") // Replace with your token
            .contentType("application/json")
            .body(requestBody)
        .when()
            .post("https://api.github.com/user/repos")
        .then()
            .extract().response();

        // Print status code and response body for debugging
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Verify the status code is 422
        assertEquals(422, response.getStatusCode(), "Expected status code 422 for existing repository");

        // Extract the nested error message from the "errors" array
        String nestedErrorMessage = response.jsonPath().getString("errors[0].message");
        
        // Print the nested error message for debugging
        System.out.println("Nested Error Message: " + nestedErrorMessage);

        // Validate that the nested error message is "name already exists on this account"
        assertThat(nestedErrorMessage, is("name already exists on this account"));
    }

    
    @Test
    public void testUpdateRepositoryName() {
        // Direct URL for the specific repository ID provided in the redirect message
        String url = "https://api.github.com/repositories/886077295";

        // Define a unique new name for the repository
        String uniqueID = UUID.randomUUID().toString().substring(0, 8); // Generate a short unique ID
        String newRepoName = "JAVA_Basics_Updated_" + uniqueID;

        // Define the request body
        String requestBody = "{\n" +
                "    \"name\": \"" + newRepoName + "\",\n" +
                "    \"description\": \"Updated description\"\n" +
                "}";

        // Debug: Print the full URL
        System.out.println("API URL: " + url);

        // Make a PATCH request to update the repository
        Response response = given()
            .header("Authorization", "Bearer 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") // Replace with your token
            .header("X-GitHub-Api-Version", "2022-11-28") // Specify GitHub API version
            .contentType("application/json")
            .body(requestBody)
            .redirects().follow(true)
        .when()
            .patch(url)
        .then()
            .extract().response();

        // Print status and response body for debugging
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Validate the status code
        assertEquals(200, response.getStatusCode(), "Expected status code 200");

        // Validate that the repository name in the response matches the new name
        String updatedRepoName = response.jsonPath().getString("name");
        assertThat(updatedRepoName, is(newRepoName));

        // Output updated repository details
        System.out.println("Repository Updated Successfully!");
        System.out.println("Updated Repository Name: " + updatedRepoName);
        System.out.println("Updated Repository Description: " + response.jsonPath().getString("description"));
    }

    @Test
    public void testDeleteNonExistingRepository() {
        // Define the owner and non-existing repository name
        String owner = "vin0808"; // Replace with your GitHub username
        String nonExistingRepoName = "NonExistentRepo"; // A repository name that does not exist

        // Construct the full URL for the API endpoint to delete the non-existing repository
        String url = "https://api.github.com/repos/" + owner + "/" + nonExistingRepoName;
        System.out.println("API URL for Delete: " + url); // Print the URL for debugging

        // Make DELETE request to attempt to delete the non-existing repository
        Response response = given()
            .header("Authorization", "Bearer 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") // Use Bearer token
            .when()
            .delete(url) // Use the constructed URL
            .then()
            .extract().response();

        // Print the status code and response body for debugging
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Validate that the status code is 404
        assertEquals(404, response.getStatusCode(), "Expected status code 404 for non-existing repository deletion");

        // Validate that the error message is "Not Found"
        String errorMessage = response.jsonPath().getString("message");
        System.out.println("Error Message: " + errorMessage);
        assertThat(errorMessage, is("Not Found"));
    }

}


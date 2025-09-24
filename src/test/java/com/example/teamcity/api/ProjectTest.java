package com.example.teamcity.api;

import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.CheckedRequests;
import com.example.teamcity.api.requests.UncheckedRequests;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import com.example.teamcity.api.spec.Specifications;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.example.teamcity.api.enums.Endpoint.PROJECTS;
import static com.example.teamcity.api.enums.Endpoint.USERS;
import static com.example.teamcity.api.generators.TestDataGenerator.generate;

@Test(groups = {"Regression"})
public class ProjectTest extends BaseApiTest {
    @Test(description="User should be able to create project", groups = {"Positive", "CRUD"})
    public void userCreatesProjectTest() {
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());
        var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));

        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

        var createdProject = userCheckRequests.<Project>getRequest(PROJECTS).read(testData.getProject().getId());

        softy.assertEquals(testData.getProject().getName(), createdProject.getName(), "Project name is not correct");
    }

    @Test(description = "User should not be able to create two projects with the same id", groups = {"Negative", "CRUD"})
    public void userCreatesTwoProjectsWithTheSameIdTest() {
        var projectWithSameId = generate(Arrays.asList(testData.getProject()), Project.class, testData.getProject().getId());
        projectWithSameId.setId(testData.getProject().getId());

        superUserCheckRequests.getRequest(USERS).create(testData.getUser());
        var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));
        
        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

        new UncheckedBase(Specifications.authSpec(testData.getUser()), PROJECTS)
                .create(projectWithSameId)
                .then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project ID \"%s\" is already used by another project".formatted(testData.getProject().getId())));

    }

    // This test passes if id in class Project - @Parametrizable
    @Test(description = "User should not be able to create project with empty id", groups = {"Negative", "CRUD", "Project"})
    public void userCreatesProjectWithEmptyIdTest() {
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());
        var userCheckRequests = new UncheckedRequests(Specifications.authSpec(testData.getUser()));
        var projectEmptyId = generate(Project.class, "");

        userCheckRequests.getRequest(PROJECTS)
                .create(projectEmptyId)
                .then().assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body(Matchers.containsString("Project ID must not be empty"));
    }

    @Test(description = "User should not be able to create project with empty name", groups = {"Negative", "CRUD", "Project"})
    public void userCreatesProjectWithEmptyName() {
        var project = testData.getProject();
        project.setName("");

        superUserCheckRequests.getRequest(USERS).create(testData.getUser());

        new UncheckedBase(Specifications.authSpec(testData.getUser()), PROJECTS)
                .create(project)
                .then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project name cannot be empty"));
    }

    public void userCreatesProjectWithInvalidSymbolsInId() {
        var invalidProjectId = "!@#$%^&*():;|/";
        var projectWithInvalidId = generate(Project.class, invalidProjectId);

        superUserCheckRequests.getRequest(USERS).create(testData.getUser());
        var userCheckRequests = new UncheckedRequests(Specifications.authSpec(testData.getUser()));

        userCheckRequests.getRequest(PROJECTS)
                .create(projectWithInvalidId)
                .then().assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body(Matchers.containsString("Project ID \"%s\" is invalid".formatted(projectWithInvalidId.getId())));
    }

    @Test(description = "User should not be able to create a project if id starts with number", groups = {"Negative", "CRUD", "Project"})
    public void userCreatesProjectWithIdStartingWithNumberTest() {
        var projectWithDigitInId = generate(Project.class);

        projectWithDigitInId.setId("1" + projectWithDigitInId.getId());

        superUserCheckRequests.getRequest(USERS).create(testData.getUser());

        var userCheckRequests = new UncheckedRequests(Specifications.authSpec(testData.getUser()));

        userCheckRequests.getRequest(PROJECTS)
                .create(projectWithDigitInId)
                .then().assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body(Matchers.containsString("Project ID \"%s\" is invalid".formatted(projectWithDigitInId.getId())));
    }

    @Test(description = "User should not be able to create a project if id contains Cyrillic symbols",
            groups = {"Negative", "CRUD", "Project"})
    public void userCreatesProjectWithCyrillicIdTest() {
        var cyrillicId = "тестПроект"; // кириллица
        var projectWithCyrillicId = generate(Project.class, cyrillicId);

        superUserCheckRequests.getRequest(USERS).create(testData.getUser());
        var userCheckRequests = new UncheckedRequests(Specifications.authSpec(testData.getUser()));

        userCheckRequests.getRequest(PROJECTS)
                .create(projectWithCyrillicId)
                .then().assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body(Matchers.containsString(
                        "Project ID \"%s\" is invalid".formatted(projectWithCyrillicId.getId())));
    }

    @Test(description = "User should not be able to create a project if id has more than 225 symbols",
            groups = {"Negative", "CRUD", "Project"})
    public void userCreatesProjectWithTooLongIdTest() {
        var tooLongId = RandomStringUtils.randomAlphabetic(400); // строка из 226 символов
        var projectWithTooLongId = generate(Project.class, tooLongId);

        superUserCheckRequests.getRequest(USERS).create(testData.getUser());
        var userCheckRequests = new UncheckedRequests(Specifications.authSpec(testData.getUser()));

        userCheckRequests.getRequest(PROJECTS)
                .create(projectWithTooLongId)
                .then().assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body(Matchers.containsString(
                        "Project ID \"%s\" is invalid".formatted(projectWithTooLongId.getId())));
    }
}

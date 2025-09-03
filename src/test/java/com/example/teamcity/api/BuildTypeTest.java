package com.example.teamcity.api;

import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.checked.CheckedBase;
import com.example.teamcity.api.spec.Specifications;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

import static com.example.teamcity.api.generators.TestDataGenerator.generate;
import static io.qameta.allure.Allure.step;

@Test(groups = {"Regression"})
public class BuildTypeTest extends BaseApiTest {
    @Test(description = "User should be able to create build type", groups = {"Positive", "CRUD"})
    public void userCreatesBuildTypeTest() {
        var user = generate(User.class);

        step("Crete user", () -> {
            var requester = new CheckedBase<User>(Specifications.superUserAuth(), Endpoint.USERS);
            requester.create(user);
        });

        var project = generate(Project.class);
        AtomicReference<String> projectId = new AtomicReference<>("");

        step("Crete project by user", () -> {
            var requester = new CheckedBase<Project>(Specifications.authSpec(user), Endpoint.PROJECTS);
//            requester.create(project);
            projectId.set(requester.create(project).getId());
        });

        var buildType = generate(BuildType.class);
        buildType.setProject(Project.builder().id(projectId.get()).locator(null).build());

        var requester = new CheckedBase<BuildType>(Specifications.authSpec(user), Endpoint.BUILD_TYPES);
        AtomicReference<String> buildTypeId = new AtomicReference<>("");

        step("Crete buildType for project by user", () -> {
            buildTypeId.set(requester.create(buildType).getId());
        });

        step("Check buildType was created successfully with correct data", () -> {
            var createdBuildType = requester.read(buildTypeId.get());

            softy.assertEquals(buildType.getName(), createdBuildType.getName(), "BuildType name is not correct");
        });
    }

    @Test(description = "User should not be able to create two build types with the same id", groups = {"Negative", "CRUD"})
    public void userCreatesTwoBuildTypesWithTheSameIdTest() {
        step("Crete user");
        step("Crete project by user");
        step("Crete buildType1 for project by user");
        step("Crete buildType2 with same id as buildType1 for project by user");
        step("Check buildType2 was not created with bad request code");
    }

    @Test(description = "Project admin should be able to create build type for their project", groups = {"Positive", "Roles"})
    public void projectAdminCreatesBuildTypeTest() {
        step("Crete user1");
        step("Crete project1");
        step("Grant user1 PROJECT_ADMIN role in project1");

        step("Crete buildType for project by user (PROJECT_ADMIN)");
        step("Check buildType was created successfully");
    }

    @Test(description = "Project admin should not be able to create build type for not their project", groups = {"Negative", "Roles"})
    public void projectAdminCreatesBuildTypeForAnotherUserProjectTest() {
        step("Crete user1");
        step("Crete project1");
        step("Grant user1 PROJECT_ADMIN role in project1");

        step("Crete user2");
        step("Crete project2");
        step("Grant user2 PROJECT_ADMIN role in project2");

        step("Crete buildType for project1 by user2");
        step("Check buildType was not created with forbidden code");
    }

}

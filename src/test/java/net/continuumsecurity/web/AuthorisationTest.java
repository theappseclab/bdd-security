package net.continuumsecurity.web;

import net.continuumsecurity.web.drivers.DriverFactory;
import net.continuumsecurity.web.steps.WebApplicationSteps;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

public class AuthorisationTest {
    protected WebApplicationSteps webAppSteps = new WebApplicationSteps();
    protected List<HashMap> authorisedTable;
    protected List<HashMap> unauthorisedTable;
    String msg = "";
    int failures = 0;

    @BeforeTest
    public void beforeScenario() {
        webAppSteps.createApp();
        String workingDirectory = System.getProperty("user.dir");
        this.authorisedTable = NgUtils.createListOfMaps(workingDirectory + "/src/main/stories/tables/authorised.resources.table");
        this.unauthorisedTable = NgUtils.createListOfMaps(workingDirectory + "/src/main/stories/tables/unauthorised.resources.table");
        webAppSteps.createAppAndCredentials();
    }

    @AfterClass
    public void tearDown() {
        DriverFactory.quitAll();
    }

    @Test
    public void authorised_users_can_view_restricted_resources() {
        msg = "";
        failures = 0;
        for (HashMap item : this.authorisedTable) {
            webAppSteps.createApp();
            webAppSteps.enableLoggingDriver();
            webAppSteps.clearProxy();
            webAppSteps.openLoginPage();
            webAppSteps.setUsernameFromExamples((String) item.get("username"));
            webAppSteps.setCredentialsFromExamples((String) item.get("password"));
            webAppSteps.loginWithSetCredentials();
            webAppSteps.clearProxy();
            try {
                webAppSteps.checkAccessToResource((String) item.get("sensitiveData"), (String) item.get("method"));
            } catch (AssertionError e) {
                failures++;
                msg = msg + e.getMessage()+"\n";
            }
        }
        assertEquals(msg, 0, failures);
    }

    @Test(dependsOnMethods = { "authorised_users_can_view_restricted_resources" })
    public void users_must_not_be_able_to_view_resources_for_which_they_are_not_authorised() {
        msg = "";
        failures = 0;
        for (HashMap item : this.unauthorisedTable) {
            webAppSteps.createApp();
            webAppSteps.openLoginPage();
            webAppSteps.setUsernameFromExamples((String) item.get("username"));
            webAppSteps.setCredentialsFromExamples((String) item.get("password"));
            webAppSteps.loginWithSetCredentials();
            try {
                webAppSteps.checkNoAccessToResource((String) item.get("sensitiveData"), (String) item.get("method"));
            } catch (AssertionError e) {
                failures++;
                msg = msg + e.getMessage()+"\n";
            }
        }
        assertEquals(msg, 0, failures);
    }

    @Test(dependsOnMethods = { "authorised_users_can_view_restricted_resources" })
    public void un_authenticated_users_should_not_be_able_to_view_restricted_resources() {
        msg = "";
        failures = 0;
        for (HashMap item : this.authorisedTable) {
            webAppSteps.checkIfMapPopulated();
            webAppSteps.createApp();
            webAppSteps.openLoginPage();
            try {
                webAppSteps.checkNoAccessToResource((String) item.get("sensitiveData"), (String) item.get("method"));
            } catch (AssertionError e) {
                failures++;
                msg = msg + e.getMessage()+"\n";
            }
        }
    }
}


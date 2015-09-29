package net.continuumsecurity.examples.ropeytasks;

import net.continuumsecurity.Config;
import net.continuumsecurity.Credentials;
import net.continuumsecurity.Restricted;
import net.continuumsecurity.UserPassCredentials;
import net.continuumsecurity.behaviour.ICaptcha;
import net.continuumsecurity.behaviour.ILogin;
import net.continuumsecurity.behaviour.ILogout;
import net.continuumsecurity.behaviour.IRecoverPassword;
import net.continuumsecurity.web.CaptchaSolver;
import net.continuumsecurity.web.WebApplication;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;


public class ReachTest extends WebApplication implements ILogin,ILogout {
    @Override
    public void openLoginPage() {
        driver.get(Config.getInstance().getBaseUrl());
        verifyTextPresent("Welcome");
    }

    public void login(Credentials credentials) {
        UserPassCredentials creds = new UserPassCredentials(credentials);
        driver.findElement(By.id("id_username")).clear();
        driver.findElement(By.id("id_username")).sendKeys(creds.getUsername());
        driver.findElement(By.id("id_password")).clear();
        driver.findElement(By.id("id_password")).sendKeys(creds.getPassword());
        driver.findElement(By.xpath("//button[@type='submit']")).click();
    }

    public void logout() {
        driver.get(Config.getInstance().getBaseUrl() + "accounts/logout?logout_success=true");
        verifyTextPresent("Welcome");
    }

    @Override
    public boolean isLoggedIn() {
        if (driver.getPageSource().contains("Account")) {
            return true;
        } else {
            return false;
        }
    }

    public void navigate() {
        openLoginPage();
        login(Config.getInstance().getUsers().getDefaultCredentials());
        //navigate the app
    }
}

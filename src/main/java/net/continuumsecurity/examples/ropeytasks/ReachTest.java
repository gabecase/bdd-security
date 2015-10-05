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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.Keys;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.System;
import java.util.Map;
import java.util.Properties;


public class ReachTest extends WebApplication implements ILogin,ILogout {
    @Override
    public void openLoginPage() {
        driver.get(Config.getInstance().getBaseUrl());
        verifySelectorPresent(By.id(Config.getInstance().getUserNameSelector()));
    }

    public void login(Credentials credentials) {
        UserPassCredentials creds = new UserPassCredentials(credentials);
        fillText(By.id(Config.getInstance().getUserNameSelector()), creds.getUsername());
        fillText(By.id(Config.getInstance().getPasswordSelector()), creds.getPassword());
        driver.findElement(By.id(Config.getInstance().getPasswordSelector())).sendKeys(Keys.RETURN);
    }

    public void verifySelectorPresent(By by) {
        driver.findElement(by);
    }

    public void fillText(By by, String text) {
        WebDriverWait wait = new WebDriverWait(driver, 20);
        wait.until(ExpectedConditions.elementToBeClickable(by));
        WebElement element = driver.findElement(by);
        element.clear();
        element.sendKeys(text);
    }

    public void logout() {
        driver.get(Config.getInstance().getLogOutUrl());
        verifySelectorPresent(By.id(Config.getInstance().getUserNameSelector()));
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

package com.wiley.autotest;

import com.wiley.autotest.selenium.elements.TextField;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

public final class ExpectedConditions2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpectedConditions2.class);

    private ExpectedConditions2() {
    }

    public static ExpectedCondition<Boolean> presenceOfAllElementsLocatedBy(final By locator) {
        return driver -> !driver.findElements(locator).isEmpty();
    }

    public static ExpectedCondition<Boolean> visibilityOf(final WebElement element) {
        return driver -> element.isDisplayed();
    }

    public static ExpectedCondition<List<WebElement>> visibilityOfAllElementsLocatedBy(final By locator) {
        return driver -> {
            final List<WebElement> foundElements = driver.findElements(locator);
            List<WebElement> visibleElements1 = new ArrayList<>();
            for (final WebElement element : foundElements) {
                if (element.isDisplayed()) {
                    visibleElements1.add(element);
                }
            }
            List<WebElement> visibleElements = visibleElements1;
            return isNotEmpty(visibleElements) ? visibleElements : null;
        };
    }

    public static ExpectedCondition<WebElement> visibilityOfElementLocatedBy(By locator) {
        return driver -> {
            try {
                final WebElement foundElement = driver.findElement(locator);
                return foundElement.isDisplayed() ? foundElement : null;
            } catch (Exception e) {
                return null;
            }
        };
    }


    public static ExpectedCondition<WebElement> invisibleOf(final By locator) {
        return driver -> elementIfInvisible(driver.findElement(locator));
    }

    public static ExpectedCondition<WebElement> invisibleOf(final WebElement element) {
        return driver -> elementIfInvisible(element);
    }

    public static ExpectedCondition<Boolean> absenceElementBy(final By locator) {
        return driver -> {
            try {
                driver.findElement(locator);
                return false;
            } catch (WebDriverException ignored) {
                return true;
            }
        };
    }

    /**
     * We don't know the actual window title without switching to one.
     * This method was always used to make sure that the window appeared. After it we switched to appeared window.
     * Switching between windows is rather time consuming operation
     * <p>
     * To avoid the double switching to the window we are switching to window in this method
     * <p>
     * The same approach is applies to all ExpectedConditions for windows
     *
     * @param title - title of window
     * @return - handle of expected window
     */
    public static ExpectedCondition<String> appearingOfWindowAndSwitchToIt(final String title) {
        return driver -> {
            final String initialHandle = driver.getWindowHandle();
            for (final String handle : driver.getWindowHandles()) {
                if (needToSwitch(initialHandle, handle)) {
                    driver.switchTo().window(handle);
                    if (driver.getTitle().equals(title)) {
                        return handle;
                    }
                }
            }
            driver.switchTo().window(initialHandle);
            return null;
        };
    }

    public static ExpectedCondition<String> appearingOfWindowByPartialTitle(final String fullTitle) {
        return driver -> {
            final String initialHandle = driver.getWindowHandle();
            for (final String handle : driver.getWindowHandles()) {
                if (needToSwitch(initialHandle, handle)) {
                    driver.switchTo().window(handle);
                    if (fullTitle.contains(driver.getTitle().split("\\(")[0].trim())) {
                        return handle;
                    }
                }
            }
            driver.switchTo().window(initialHandle);
            return null;
        };
    }

    public static ExpectedCondition<String> appearingOfWindowWithNewTitle(final Set<String> oldTitle) {
        return driver -> {
            Set<String> windowHandles = driver.getWindowHandles();
            if (windowHandles.containsAll(oldTitle)) {
                windowHandles.removeAll(oldTitle);
                if (!windowHandles.isEmpty()) {
                    return windowHandles.iterator().next();
                }
            }
            return null;
        };
    }

    public static ExpectedCondition<String> appearingOfWindowByUrl(final String url) {
        return driver -> {
            final String initialHandle = driver.getWindowHandle();
            for (final String handle : driver.getWindowHandles()) {
                if (needToSwitch(initialHandle, handle)) {
                    driver.switchTo().window(handle);
                    try {
                        if (url.equals(URLDecoder.decode(driver.getCurrentUrl(), "UTF-8"))) {
                            return handle;
                        }
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.error("UnsupportedEncodingException occured while decoding url - " + driver.getCurrentUrl());
                    }
                }
            }
            driver.switchTo().window(initialHandle);
            return null;
        };
    }

    public static ExpectedCondition<String> appearingOfWindowByPartialUrl(final String url) {
        return driver -> {
            final String initialHandle = driver.getWindowHandle();
            for (final String handle : driver.getWindowHandles()) {
                if (needToSwitch(initialHandle, handle)) {
                    driver.switchTo().window(handle);
                    if (driver.getCurrentUrl().contains(url)) {
                        return handle;
                    }
                }
            }
            driver.switchTo().window(initialHandle);
            return null;
        };
    }

    public static ExpectedCondition<Boolean> textToBePresentInElement(final WebElement element, final String text) {
        return driver -> element.getText().contains(text);
    }

    public static ExpectedCondition<Boolean> textToBePresentInTextField(final TextField textField, final String text) {
        return driver -> textField.getText().contains(text);
    }

    public static ExpectedCondition<Boolean> textToBePresentInElement(final By by) {
        return driver -> !driver.findElement(by).getText().isEmpty();
    }

    public static ExpectedCondition<Boolean> textToBePresentInElement(final WebElement element) {
        return driver -> !element.getText().isEmpty() || (element.getAttribute("value") != null && !element.getAttribute("value").isEmpty());
    }

    public static ExpectedCondition<Boolean> pageToLoad() {
        return webDriver -> "complete".equals(((JavascriptExecutor) webDriver).executeScript("return document.readyState"));
    }

    public static ExpectedCondition<Boolean> isListLoaded() {
        return webDriver -> {
            Boolean loaded = false;
            try {
                loaded = (Boolean) ((JavascriptExecutor) webDriver).executeScript("return $.active==0");
                Object result = ((JavascriptExecutor) webDriver).executeScript("return portionKey");
                return (result == null || ((Long) result) == 0) && loaded;
            } catch (WebDriverException ignored) {
                return loaded;
            }
        };
    }

    public static ExpectedCondition<Boolean> presenceOfElementCount(final By locator, final int expectedNumberOfElements) {
        return driver -> driver.findElements(locator).size() == expectedNumberOfElements;
    }

    public static ExpectedCondition<Boolean> attributeContainsValue(final WebElement element, final String attributeName, final String attributeValue) {
        return driver -> element.getAttribute(attributeName).contains(attributeValue);
    }

    public static ExpectedCondition<Boolean> attributeNotContainsValue(final WebElement element, final String attributeName, final String attributeValue) {
        return driver -> !element.getAttribute(attributeName).contains(attributeValue);
    }

    public static ExpectedCondition<Boolean> elementHasAttribute(final WebElement element, final String attributeName) {
        return driver -> element.getAttribute(attributeName) != null;
    }

    public static ExpectedCondition<Boolean> elementDoesNotHaveAttribute(final WebElement element, final String attributeName) {
        return driver -> element.getAttribute(attributeName) == null;
    }

    public static ExpectedCondition<Boolean> onlyOneWindowIsOpen() {
        return driver -> driver.getWindowHandles().size() == 1;
    }

    private static boolean needToSwitch(String initialHandle, String handle) {
        if (handle.isEmpty()) {
            LOGGER.error("*****EMPTY WINDOW HANDLE*****Attempt to switch to window with '' handle detected! Ignoring it.");
            return false;
        }
        return !initialHandle.equals(handle);
    }

    private static WebElement elementIfInvisible(final WebElement element) {
        return !element.isDisplayed() ? element : null;
    }
}
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebElement;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Driver 对象。
 */
public class AppiumClient extends Driver {

    private AppiumDriver<WebElement> driver;

    public AppiumClient(String url, Capabilities capabilities) throws MalformedURLException {
        driver = new AndroidDriver<WebElement>(new URL(url), capabilities);
    }

    public void setCurrentPageSource() {
        setCurrentPageSource(driver.getPageSource());
    }

    public void setCurrentPageDom() {
    }

    public void setCurrentActivity() {
        if (driver instanceof AndroidDriver) {
            setCurrentActivity(((AndroidDriver<WebElement>) driver).currentActivity());
        } else if (driver instanceof IOSDriver) {

        }
    }
}

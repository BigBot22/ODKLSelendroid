import io.selendroid.SelendroidCapabilities;

import io.selendroid.SelendroidDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.touch.TouchActions;
import org.openqa.selenium.remote.DesiredCapabilities;


public class ExampleTest {

    private SelendroidDriver driver = null;

    @Before
    public void startSelendroidServer() throws Exception {

        DesiredCapabilities caps = SelendroidCapabilities.android();

        driver = new SelendroidDriver(caps);
    }

    /**
     * 0) Установить Genymotion
     *
     * 1) В Genymotion сздать и запустить Google Nexus 7 5.0.0 API 21 800x1280
     *
     * 2) adb push "./AppiumBootstrap.jar" /data/local/tmp/
     *
     * 3) java -jar ./selendroid-standalone/target/selendroid-standalone-0.10.0-with-dependencies.jar -aut ./selendroid-test-app/target/selendroid-test-app.10.0.apk
     *
     * 4) Запустить тест notificationTest
     *
     */

    @Test
    public void notificationTest() throws Exception {
        driver.get("http://m.ebay.de");

        int height = driver.findElement(By.xpath("/*")).getSize().height;

        driver.switchTo().window("UIAUTOMATOR");
        new TouchActions(driver).down(25, 25).move(25, height).up(25, height).perform();
        WebElement header = driver.findElement(By.xpath("//*[contains(@text,' AM')]"));
        header.click();
        WebElement settings = driver.findElement(By.xpath("//*[contains(@text,'WiredSSID')]"));
        settings.click();

        WebElement wifiSwitch = driver.findElement(By.xpath("//*[contains(@text,'On')]"));
        wifiSwitch.click();

        driver.switchTo().window("INSTRUMENTATION");

        driver.quit();
    }

    @After
    public void teardown() {
        driver.quit();
    }
}
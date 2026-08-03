package com.cibaoc.helpers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.WheelInput.ScrollOrigin;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.openqa.selenium.support.locators.RelativeLocator.*;

//javadoc this by typing mvn site -> mvn clean javadoc:test-javadoc

/**
 * <h2>Method Groups</h2>
 * <ul>
 * <li>Instance Methods: {@link #instantiateDriver(EdgeOptions)},
 * {@link #instantiateDriver(ChromeOptions)},
 * {@link #instantiateDriver(FirefoxOptions)},
 * {@link #instantiateHTTP(HttpClient)}, {@link #getHttpClient()},
 * {@link #getEdgeOptions()}, {@link #getChromeOptions()},
 * {@link #getFirefoxOptions()}</li>
 * 
 * <li>Driver Methods: {@link #getDriver()}, {@link #go(String)}
 * {@link #getUrl()}, {@link #title()}, {@link #alert()}, {@link #alertText()},
 * {@link #getCurrentTab()}, {@link #getSource()}, {@link #getTabs()},
 * {@link #changeTab(Object)}, {@link #changeTab(int)}, {@link #getTabSize()},
 * {@link #deleteCookie()}, {@link #deleteCookie(Cookie)},
 * {@link #deleteCookie(String)}, {@link #addCookie(Cookie)},
 * {@link #addCookie(String, String)}, {@link #getCookies()},
 * {@link #getCookie(String)}, {@link #minimizeWindow()},
 * {@link #maximizeWindow()}, {@link #fullscreenWindow()},
 * {@link #getWindowLocation()}, {@link #getWindowSize()},
 * {@link #setWindowSize(Dimension)}, {@link #setWindowPos(Point)},
 * {@link #closeTab()}, {@link #changeFrame(int)},
 * {@link #changeFrame(String)}</li>
 * 
 * </ul>
 */
public class SeleniumAbstractions {
	private WebDriver driver;
	private WebDriverWait wait;
	private Actions actions;
	private ArrayList<String> windowHandles;
	private String alertText;
	private EdgeOptions eo;
	private ChromeOptions co;
	private FirefoxOptions fo;
	private Duration explicitWaitTime;
	private Duration implicitWaitTime;
	private Duration pollEvery;
	private HttpClient httpClient;
	private final Duration httpTimeout;

	// region class constructor and instantiations

	/**
	 * Constructor for SeleniumAbstractions. Acts as the default constructor, only
	 * needs Durations and the String driver. Instantiates the WebDriver and
	 * HttpClient of this object
	 * 
	 * @param driver           - A String that may contain
	 *                         ["EDGE"||"CHROME"||"FIREFOX"]
	 * @param implicitWaitTime - A Duration object for the implicit wait time for
	 *                         this driver.
	 * @param explicitWaitTime - A Duration object for the explicit wait time for
	 *                         the WebDriverWait object.
	 * @param pollEvery        - A Duration object for the polling duration of the
	 *                         WebDriverWait object.
	 * @param arguments        - Additional arguments to add to the options argument
	 *                         if necessary.
	 */
	public SeleniumAbstractions(
		String driver, Duration implicitWaitTime, Duration explicitWaitTime, Duration pollEvery, 
		String... arguments) {

		this.httpTimeout = Duration.ofSeconds(5);
		this.implicitWaitTime = implicitWaitTime;
		this.explicitWaitTime = explicitWaitTime;
		this.pollEvery = pollEvery;
		instantiateHTTP(HttpClient.newBuilder().connectTimeout(httpTimeout)
			.followRedirects(HttpClient.Redirect.NORMAL).build());

		switch (driver) {
		case "EDGE": {
			this.eo = new EdgeOptions();
			// NORMAL (not EAGER): EAGER returns control once the DOM is parsed but before
			// stylesheets finish applying, which races page.java's getText() assertions against
			// Tailwind-driven layout/text-transform (uppercase eyebrow, step-circle line breaks) —
			// harmless on a warm local Chrome profile, but flaky under CI's colder cold-start.
			this.eo.setPageLoadStrategy(PageLoadStrategy.NORMAL);
			this.eo.addArguments("disable-infobars", "--start-maximized", "--window-size=1920,1080", "--lang=en-US");
			this.eo.addArguments(arguments);
			this.eo.setExperimentalOption("excludeSwitches",
				Collections.singletonList("enable-automation"));
			instantiateDriver(eo);
			break;
		}
		case "CHROME": {
			this.co = new ChromeOptions();
			// NORMAL (not EAGER): see the EDGE case above for why.
			this.co.setPageLoadStrategy(PageLoadStrategy.NORMAL);
			this.co.addArguments("disable-infobars", "--start-maximized", "--window-size=1920,1080", "--lang=en-US");
			this.co.addArguments(arguments);
			this.co.setExperimentalOption("excludeSwitches",
				Collections.singletonList("enable-automation"));
			instantiateDriver(co);
			break;

		}
		case "FIREFOX": {
			this.fo = new FirefoxOptions();
			// NORMAL (not EAGER): see the EDGE case above for why.
			this.fo.setPageLoadStrategy(PageLoadStrategy.NORMAL);
			this.fo.addArguments("disable-infobars", "--start-maximized", "--lang=en-US");
			this.fo.addArguments(arguments);
			instantiateDriver(fo);
			break;

		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + driver);
		}
	}

	/**
	 * Secondary Constructor for SeleniumAbstractions. Requires a WebDriver Object
	 * and Explicit Durations only. Instantiates the WebDriver and HttpClient of
	 * this object.
	 * 
	 * @param driver           - A String that may contain
	 *                         ["EDGE"||"CHROME"||"FIREFOX"]
	 * @param explicitWaitTime - A Duration object for the explicit wait time for
	 *                         the WebDriverWait object.
	 * @param pollEvery        - A Duration object for the polling duration of the
	 *                         WebDriverWait object.
	 * 
	 */
	public SeleniumAbstractions(WebDriver driver, Duration explicitWaitTime, Duration pollEvery) {
		this.driver = driver;
		this.wait = new WebDriverWait(this.driver, explicitWaitTime, pollEvery);
		this.actions = new Actions(this.driver);
		this.explicitWaitTime = explicitWaitTime;
		this.httpTimeout = Duration.ofSeconds(5);
		instantiateHTTP(HttpClient.newBuilder().connectTimeout(httpTimeout)
			.followRedirects(HttpClient.Redirect.NORMAL).build());
	}

	/**
	 * Instantiates the driver given an EdgeOptions Object. Called by the
	 * constructor and can be called by other classes if needed. Useful for
	 * 'resetting' the driver in some cases.
	 * 
	 * @param eo - An EdgeOptions object
	 */
	public void instantiateDriver(EdgeOptions eo) {
		this.driver = new EdgeDriver(eo);
		this.driver.manage().timeouts().implicitlyWait(this.implicitWaitTime);
		this.wait = new WebDriverWait(this.driver, this.explicitWaitTime, this.pollEvery);
		this.actions = new Actions(this.driver);
	}

	/**
	 * Instantiates the driver given an ChromeOptions Object. Called by the
	 * constructor and can be called by other classes if needed. Useful for
	 * 'resetting' the driver in some cases.
	 * 
	 * @param co - A ChromeOptions object
	 */
	public void instantiateDriver(ChromeOptions co) {
		this.driver = new ChromeDriver(co);
		this.driver.manage().timeouts().implicitlyWait(this.implicitWaitTime);
		this.wait = new WebDriverWait(this.driver, this.explicitWaitTime, this.pollEvery);
		this.actions = new Actions(this.driver);
	}

	/**
	 * Instantiates the driver given a FirefoxOptions Object. Called by the
	 * constructor and can be called by other classes if needed. Useful for
	 * 'resetting' the driver in some cases.
	 * 
	 * @param fo - A FirefoxOptions object
	 */
	public void instantiateDriver(FirefoxOptions fo) {
		this.driver = new FirefoxDriver(fo);
		this.driver.manage().timeouts().implicitlyWait(this.implicitWaitTime);
		this.wait = new WebDriverWait(this.driver, this.explicitWaitTime, this.pollEvery);
		this.actions = new Actions(this.driver);
	}

	/**
	 * Instantiates the HTTP Client for any HTTP related request
	 * 
	 * @param client - The HTTP Client object
	 */
	public void instantiateHTTP(HttpClient client) {
		this.httpClient = client;
	}

	/**
	 * @return the HTTP CLient object
	 */
	public HttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * @return the EdgeOptions object of this object.
	 */
	public EdgeOptions getEdgeOptions() {
		return eo;
	}

	/**
	 * @return the ChromeOptions object of this object.
	 */
	public ChromeOptions getChromeOptions() {
		return co;
	}

	/**
	 * @return the FirefoxOptions object of this object.
	 */
	public FirefoxOptions getFirefoxOptions() {
		return fo;
	}

	// endregion

	// region Driver Methods

	/**
	 * @return the WebDriver object of this object.
	 */
	public WebDriver getDriver() {
		return driver;
	}

	public void go(String url) {
		driver.get(url);
	}

	public void refresh() {
		driver.navigate().refresh();
	}

	public String getUrl() {
		return driver.getCurrentUrl();
	}

	public String title() {
		return driver.getTitle();
	}

	public Alert alert() {
		alertText = driver.switchTo().alert().getText();
		return driver.switchTo().alert();
	}

	public String alertText() {
		return alertText;
	}

	public String getCurrentTab() {
		return driver.getWindowHandle();
	}

	public String getSource() {
		return driver.getPageSource();
	}

	public ArrayList<String> getTabs() {
		windowHandles = new ArrayList<String>(driver.getWindowHandles());
		System.out.println("Current Window Handles: " + windowHandles);
		return windowHandles;
	}

	public void newTab() {
		driver.switchTo().newWindow(WindowType.TAB);
	}

	public void newTab(String url) {
		driver.switchTo().newWindow(WindowType.TAB);
		driver.get(url);
	}

	public void newWindow() {
		driver.switchTo().newWindow(WindowType.WINDOW);
	}

	public void newWindow(String url) {
		driver.switchTo().newWindow(WindowType.WINDOW);
		driver.get(url);
	}

	public void changeTab(Object var) {
		driver.switchTo().window((String) var);
	}

	public void changeTab(int i) {
		getTabs();
		driver.switchTo().window(windowHandles.get(i));
	}

	public void nextTab(int offset) {
		getTabs();
		System.out.println(windowHandles.get(windowHandles.indexOf(driver.getWindowHandle()) + offset));
		driver.switchTo().window(windowHandles.get(windowHandles.indexOf(driver.getWindowHandle()) + offset));
	}
	
	public void nextTab()
	{
		getTabs();
		System.out.println("Switching to handle: " + windowHandles.get(windowHandles.size()-1));
		driver.switchTo().window(windowHandles.get(windowHandles.size()-1));
	}

	public int getTabSize() {
		try {
			getTabs();
			return windowHandles.size();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			return 0;
		}
	}

	public void deleteCookie() {
		driver.manage().deleteAllCookies();
	}

	public void deleteCookie(Cookie cookie) {
		driver.manage().deleteCookie(cookie);
	}

	public void deleteCookie(String name) {
		driver.manage().deleteCookieNamed(name);
	}

	public void addCookie(Cookie cookie) {
		driver.manage().addCookie(cookie);
	}

	public void addCookie(String name, String value) {
		driver.manage().addCookie(new Cookie(name, value));
	}

	public Set<Cookie> getCookies() {
		return driver.manage().getCookies();
	}

	public Cookie getCookie(String name) {
		return driver.manage().getCookieNamed(name);
	}

	public void minimizeWindow() {
		driver.manage().window().minimize();
	}

	public void maximizeWindow() {
		driver.manage().window().maximize();
	}

	public void fullscreenWindow() {
		driver.manage().window().fullscreen();
	}

	public Point getWindowLocation() {
		return driver.manage().window().getPosition();
	}

	public Dimension getWindowSize() {
		return driver.manage().window().getSize();
	}

	public void setWindowSize(Dimension d) {
		driver.manage().window().setSize(d);
	}

	public void setWindowPos(Point p) {
		driver.manage().window().setPosition(p);
	}

	public void closeTab() {
		driver.close();
		try {
			windowHandles = new ArrayList<String>(driver.getWindowHandles());
		} catch (Exception e) {
		}
	}

	public void changeFrame(int i) {
		driver.switchTo().frame(i);
	}

	public void changeFrame(String id) {
		driver.switchTo().frame(id);
	}
	
	public void screenshotElement(By locator, String folderPath)
	{
		File partialSC = driver.findElement(locator).getScreenshotAs(OutputType.FILE);
		System.out.println("file path: " + partialSC.getAbsolutePath());
		Path scFolder = Paths.get(folderPath);
		try {
			Files.move(partialSC.toPath(), scFolder, REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void screenshotPage(String folderPath)
	{
		File partialSC = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		Path scFolder = Paths.get(folderPath);
		try {
			Files.move(partialSC.toPath(), scFolder, REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// endregion

	// region Lone Element Methods - Click, Type, Find, FindAll, GetText, Focus,
	// FocusClick

	public WebElement click(By locator) {
		try {
			WebElement element = driver.findElement(locator);
			element.click();
			return element;
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			return null;
		}
	}

	public WebElement click(WebElement element) {
		element.click();
		return element;
	}

	public WebElement type(By locator, String msg) {
		WebElement element = driver.findElement(locator);
		element.sendKeys(msg);
		return element;
	}

	public WebElement type(WebElement element, String msg) {
		element.sendKeys(msg);
		return element;
	}

	public WebElement shiftType(By locator, String msg) {
		WebElement element = driver.findElement(locator);
		actions.keyDown(Keys.SHIFT).sendKeys(element, msg).perform();
		return element;
	}

	public WebElement shiftType(WebElement element, String msg) {
		actions.keyDown(Keys.SHIFT).sendKeys(element, msg).perform();
		return element;
	}

	public WebElement clear(By locator) {
		WebElement element = driver.findElement(locator);
		element.clear();
		return element;
	}

	public WebElement clear(WebElement element) {
		element.clear();
		return element;
	}

	public WebElement findNull(By locator)
	{
		try {
			return driver.findElement(locator);
		} catch (Exception e) {
			return null;
		}
	}

	public WebElement find(By locator) {
		return driver.findElement(locator);
	}

	public List<WebElement> findAll(By locator) {
		return driver.findElements(locator);
	}

	public WebElement above(By target, By locator) {
		return driver.findElement(with(target).above(locator));
	}

	public WebElement below(By target, By locator) {
		return driver.findElement(with(target).below(locator));
	}

	public WebElement toLeftOf(By target, By locator) {
		return driver.findElement(with(target).toLeftOf(locator));
	}

	public WebElement toRightOf(By target, By locator) {
		return driver.findElement(with(target).toRightOf(locator));
	}

	public WebElement above(By target, WebElement source) {
		return driver.findElement(with(target).above(source));
	}

	public WebElement below(By target, WebElement source) {
		return driver.findElement(with(target).below(source));
	}

	public WebElement toLeftOf(By target, WebElement source) {
		return driver.findElement(with(target).toLeftOf(source));
	}

	public WebElement toRightOf(By target, WebElement source) {
		return driver.findElement(with(target).toRightOf(source));
	}

	public WebElement near(By target, By locator) {
		return driver.findElement(with(target).near(locator));
	}

	public WebElement near(By target, WebElement element) {
		return driver.findElement(with(target).near(element));
	}

	public String getText(By locator) {
		return driver.findElement(locator).getText();
	}

	public String getText(WebElement element) {
		return element.getText();
	}

	public String getAttribute(By locator, String attribute) {
		return driver.findElement(locator).getAttribute(attribute);
	}

	public String getAttribute(WebElement element, String attribute) {
		return element.getAttribute(attribute);
	}

	public Boolean elementVisible(By locator) {
		return (driver.findElement(locator).isDisplayed() && driver.findElement(locator).isEnabled());
	}

	public WebElement focus(By locator) {
		WebElement element = driver.findElement(locator);
		actions.moveToElement(element).perform();
		return element;
	}

	public void focus(WebElement element) {
		actions.moveToElement(element).perform();
	}

	public WebElement focusClick(By locator) {
		WebElement element = driver.findElement(locator);
		actions.moveToElement(element).click().perform();
		return element;
	}

	public void focusClick(WebElement element) {
		actions.moveToElement(element).click().perform();
	}

	public void controlClick(By locator) {
		actions.keyDown(Keys.LEFT_CONTROL).click(driver.findElement(locator)).keyUp(Keys.LEFT_CONTROL)
			.perform();
	}

	public void controlClick(WebElement element) {
		actions.keyDown(Keys.LEFT_CONTROL).click(element).keyUp(Keys.LEFT_CONTROL).perform();
	}

	public void rightClick(By locator) {
		actions.contextClick(driver.findElement(locator)).perform();
	}

	public void rightClick(WebElement element) {
		actions.contextClick(element).perform();
	}

	public void focusRightClick(By locator) {
		actions.moveToElement(driver.findElement(locator)).contextClick().perform();
	}

	public void focusRightClick(WebElement element) {
		actions.moveToElement(element).contextClick().perform();
	}

	public WebElement scrollUntilFound(By locator)
	{
		while(true)
		{
			try {
				WebElement element = find(locator);
				actions.scrollToElement(element).perform();
				System.out.println("Scrolling until element is found");
				return find(locator);
			} catch (Exception e) {
				actions.scrollByAmount(0, 500).perform();
			}
		}
	}
	
	public void scrollToElement(By locator) {
		actions.scrollToElement(driver.findElement(locator)).perform();
	}

	public void scrollToElement(WebElement element) {
		actions.scrollToElement(element).perform();
	}

	public void scrollFromElement(By locator, int xAxis, int yAxis) {
		actions.scrollFromOrigin(ScrollOrigin.fromElement(driver.findElement(locator)), xAxis, yAxis)
			.perform();
	}

	public void scrollFromElement(WebElement element, int xAxis, int yAxis) {
		actions.scrollFromOrigin(ScrollOrigin.fromElement(element), xAxis, yAxis).perform();
	}

	public void scrollBy(int xAxis, int yAxis) {
		actions.scrollByAmount(xAxis, yAxis).perform();
	}
	
	public void hold(By locator)
	{
		actions.clickAndHold(driver.findElement(locator)).perform();
	}
	
	public void hold(WebElement element)
	{
		actions.clickAndHold(element).perform();
	}
	
	public void moveTo(int x, int y)
	{
		actions.moveByOffset(x, y).perform();
	}
	
	public void moveToEdge(String Direction)
	{
		int x;
		int y;
		switch (Direction)
		{
		case "UP":{x=0;y=-10;break;}
		case "DOWN":{x=0;y=10;break;}
		case "LEFT":{x=-10;y=0;break;}
		case "RIGHT":{x=10;y=-10;break;}
		case "NORTHEAST":{x=10;y=-10;break;}
		case "NORTHWEST":{x=-10;y=-10;break;}
		case "SOUTHEAST":{x=10;y=10;break;}
		case "SOUTHWEST":{x=-10;y=10;break;}
		default: throw new IllegalArgumentException("Choose from allowed cardinal direction pls");
		}
		
		while(true)
		{
			try {
				actions.moveByOffset(x, y).perform();
			} catch (Exception e) {
				// TODO: handle exception
				break;
			}
		}
	}

	public void keyDown(Keys key) {
		actions.keyDown(key).perform();
	}

	public void keyUp(WebElement element, Keys key) {
		actions.keyUp(key).perform();
	}

	public void keyPress(Keys key) {
		actions.keyDown(key).perform();
		actions.keyUp(key).perform();
	}

	// endregion

	// region Parented Element Methods

	public void clickFromParent(By locator, WebElement parent) {
		parent.findElement(locator).click();
	}

	public void typeFromParent(By locator, WebElement parent, String message) {
		parent.findElement(locator).sendKeys(message);
	}

	public WebElement findOneFromParent(By locator, WebElement parent) {
		return parent.findElement(locator);
	}

	public List<WebElement> findAllFromParent(By locator, WebElement parent) {
		return parent.findElements(locator);
	}

	public String getTextFromParent(By locator, WebElement parent) {
		return parent.findElement(locator).getText();
	}

	public String getAttributeFromParent(By locator, WebElement parent, String attribute) {
		return parent.findElement(locator).getAttribute(attribute);
	}

	public WebElement focusFromParent(By locator, WebElement parent) {
		WebElement object = parent.findElement(locator);
		actions.moveToElement(object).perform();
		return object;
	}

	public void focusClickFromParent(By locator, WebElement parent) {
		actions.moveToElement(parent.findElement(locator)).click().perform();
	}

	public void controlClickFromParent(By locator, WebElement parent) {
		actions.keyDown(Keys.CONTROL).click(parent.findElement(locator)).keyUp(Keys.CONTROL).perform();
	}

	public void rightClickFromParent(By locator, WebElement parent) {
		actions.contextClick(parent.findElement(locator)).perform();
	}

	public void focusRightClickFromParent(By locator, WebElement parent) {
		actions.moveToElement(parent.findElement(locator)).contextClick().perform();
	}

	// endregion

	// region Wait Methods

	public WebElement waitClickable(By locator) {
		WebElement waitElement = driver.findElement(locator);
		wait.until(ExpectedConditions.elementToBeClickable(waitElement));
		return waitElement;
	}

	public WebElement waitClickable(WebElement element) {
		wait.until(ExpectedConditions.elementToBeClickable(element));
		return element;
	}

	public WebElement waitTextToBe(By locator, String text) {
		WebElement waitElement = driver.findElement(locator);
		wait.until(ExpectedConditions.textToBe(locator, text));
		return waitElement;
	}

	public WebElement waitAttribute(By locator, String attribute, String value) {
		WebElement waitElement = driver.findElement(locator);
		wait.until(ExpectedConditions.attributeContains(waitElement, attribute, value));
		return waitElement;
	}

	public WebElement waitVisible(By locator) {
		WebElement element = driver.findElement(locator);
		wait.until(ExpectedConditions.visibilityOf(element));
		return element;
	}

	public WebElement waitDisappear(By locator) {
		WebElement waitElement = driver.findElement(locator);
		while (true) {
			try {
				wait.until(ExpectedConditions.not(ExpectedConditions.visibilityOf(waitElement)));
			} catch (Exception e) {
				break;
			}
		}
		return waitElement;	
	}

	public WebElement waitPresent(By locator) {
		return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
	}

	/**
	 * Unlike {@link #waitPresent(By)} (DOM presence only) or {@link #waitVisible(By)} (requires
	 * the element to already exist, else throws immediately), this waits for a locator to become
	 * both present AND visible in one poll loop - needed for anything with a CSS entrance
	 * animation/transition (e.g. a toast fading/rising in), where the element can be present in
	 * the DOM for a few frames before it's actually rendered/readable.
	 */
	public WebElement waitPresentAndVisible(By locator) {
		return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	}

	public WebElement waitNotPresent(By locator) {
		try {
			WebElement waitElement = driver.findElement(locator);
			wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfElementLocated(locator)));
			return waitElement;
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			return null;
		}
	}

	public void waitURL(String url) {
		wait.until(ExpectedConditions.urlToBe(url));
	}

	public void waitBlock() {
		try {
			this.driver.findElement(By.id("iHaveNever7E6x5i4s3t2e1d"));
		} catch (Exception e) {
			/* Do nothing */}
	}

	public void waitBlock(Duration time) {
		driver.manage().timeouts().implicitlyWait(time);
		try {
			this.driver.findElement(By.id("iHaveNever7E6x5i4s3t2e1d"));
		} catch (Exception e) {
			/* Do nothing */}

		driver.manage().timeouts().implicitlyWait(implicitWaitTime);
	}

	// endregion

	// region HTTP Methods

	public int getResponseCode(String url) {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
			.method("HEAD", HttpRequest.BodyPublishers.noBody()).timeout(implicitWaitTime).build();
		try {
			return this.httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public int getResponseCodeBy(By locator) {
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(driver.findElement(locator).getAttribute("href")))
			.method("HEAD", HttpRequest.BodyPublishers.noBody()).timeout(implicitWaitTime).build();
		try {
			return this.httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public int getResponseCodeBy(WebElement element) {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(element.getAttribute("href")))
			.method("HEAD", HttpRequest.BodyPublishers.noBody()).timeout(implicitWaitTime).build();
		try {
			return this.httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public String assessResponseCode(int code) {
		String assessmentString = "";
		if (code >= 500)
			assessmentString = "Server problem with status code: " + code;
		else if (code >= 400) {
			assessmentString = "Link problem with status code: " + code;
		} else if (code >= 300) {
			assessmentString = "Redirect of some kind with status code: " + code;
		} else if (code >= 200) {
			assessmentString = "Link is working with status code: " + code;
		} else if (code >= 100) {
			assessmentString = "information daw sabi ni rex: " + code;
		}

		System.out.println(assessmentString);
		return assessmentString;
	}

	// endregion

	public void time(ITestResult tr) {
		double time = (tr.getEndMillis() - tr.getStartMillis()) / 1000.0;
		System.out.println(tr.getName() + " Runtime: " + time + " seconds.");
	}

	public void tearDown() {
		if (driver != null)
			driver.quit();
	}
}

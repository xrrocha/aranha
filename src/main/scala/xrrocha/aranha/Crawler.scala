package xrrocha.aranha

import org.openqa.selenium.{JavascriptExecutor, TakesScreenshot, WebDriver}

trait Crawler {
  implicit def webDriver2JavascriptExecutor(webDriver: WebDriver) = webDriver.asInstanceOf[JavascriptExecutor]
  implicit def webDriver2TakesScreenshot(webDriver: WebDriver) = webDriver.asInstanceOf[TakesScreenshot]
}

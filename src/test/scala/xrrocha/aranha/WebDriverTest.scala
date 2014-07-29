package xrrocha.aranha

import org.openqa.selenium.{JavascriptExecutor, WebElement, By, WebDriver}
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.scalatest.FunSuite
import xrrocha.util.NetUtils

class WebDriverTest extends FunSuite {
  test("Parses HTML correctly when using raw WebDriver") {
    import collection.JavaConversions._
    import Countries._

    val driver: WebDriver = new HtmlUnitDriver(true)
    driver get writeFile("target/countries.html")

    val actualCountryList = driver findElements(By.cssSelector("table > tbody > tr")) map { row =>
      Country(
        name       = row findElement By.xpath("td[2]") getText,
        code       = row findElement By.xpath("td[3]") getText,
        capital    = row findElement By.xpath("td[4]") getText,
        currency   = row findElement By.xpath("td[5]") getText,
        population = (row findElement By.xpath("td[6]") getText) replaceAll("[^.0-9]", "") toDouble,
        languages  = row findElements By.xpath("td[8]/ul/li") map (_.getText)
                                                              filter(_ != "Newspeak"),
        summary    = source(row findElement By.xpath("td[7]"), driver),
        flagUrl    = {
          val url = row findElement By.xpath("td[1]/img") getAttribute ("src")
          NetUtils.resolveUrl(driver.getCurrentUrl, url)
        }
      )
    }

    driver.close()

    assert(actualCountryList == countryList)
  }

  val htmlFormatter = new HtmlCleanerFormatter
  def source(element: WebElement, webDriver: WebDriver) = {
    val javascriptExecutor = webDriver.asInstanceOf[JavascriptExecutor]
    val result = javascriptExecutor executeScript("return arguments[0].innerHTML;", element)
    if   (result == null) ""
    else htmlFormatter.formatHtml(result.toString.trim)
  }
}

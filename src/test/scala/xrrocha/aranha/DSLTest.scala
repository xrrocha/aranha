package xrrocha.aranha

import java.io.File
import java.nio.file.StandardCopyOption

import org.openqa.selenium.WebDriver
import org.scalatest.FunSuite
import xrrocha.util.IOUtils

class DSLTest extends FunSuite with Crawler with WebDriverParsers with NestableLocatorContext {
  import xrrocha.aranha.Countries._
  import xrrocha.aranha.Locator._

  val webDriver: WebDriver = new org.openqa.selenium.phantomjs.PhantomJSDriver // org.openqa.selenium.htmlunit.HtmlUnitDriver(true)

  test("Parses HTML correctly when using DSL") {
    webDriver get writeFile("target/countries.html")

    // WebElements matching the locator can be mapped, filtered, etc.
    val actualCountryList = for (row <- cs"table > tbody > tr") yield within(row) {
      Country(
        name       = xp"td[2]",                     // nth-child breaks HtmlUnit :-( Use PhantomJS instead
        code       = xp"td[3]",
        capital    = xp"td[4]",
        currency   = xp"td[5]"       as Text,                 // Redundant as conversion to text is the default
        population = xp"td[6]"       as Decimal,               // Converts to double removing non-numeric chars
        languages  = xp"td[8]/ul/li" map (_ as Text)
                                     filter(_ != "Newspeak"), // Converts to Seq mapping and filtering
        summary    = xp"td[7]"       as Html,                 // Preserves inner HTML markup in string
        flagUrl    = xp"td[1]/img"   as ImageSrc           // Relative src made absolute per current location
      )
    }

    import org.openqa.selenium.OutputType
    webDriver.executeScript("document.body.bgColor = 'white';")
    val screenshotFile = webDriver.getScreenshotAs(OutputType.FILE)
    IOUtils.copyFile(screenshotFile, new File("target/screenshot.png"), StandardCopyOption.REPLACE_EXISTING)

    webDriver.close()

    assert(actualCountryList == countryList)
  }
}

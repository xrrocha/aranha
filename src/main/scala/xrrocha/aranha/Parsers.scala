package xrrocha.aranha

import java.text.DecimalFormat

import org.openqa.selenium.{JavascriptExecutor, WebDriver, WebElement}
import xrrocha.parser.{BigDecimalParser, DoubleParser, LongParser, Parser}
import xrrocha.util.NetUtils

// TODO Implement WebElement parser unit tests

trait WebDriverParsers { this: WithSearchContext =>
  lazy val Text = new WebElementParser[String] {
    def parse(webElement: WebElement) = webElement.getText
  }

  lazy val ImageSrc = new ImageSrc(false)(webDriver)
  lazy val AbsoluteImageSrc = new ImageSrc(true)(webDriver)

  lazy val LinkHref = new LinkHref(false)(webDriver)
  lazy val AbsoluteLinkHref = new LinkHref(true)(webDriver)

  lazy val cleanHtmlFormatter = new HtmlCleanerFormatter
  def htmlFormatter: HtmlFormatter = cleanHtmlFormatter
  lazy val Html = new HtmlParser(htmlFormatter)(webDriver)

  def integerPattern = "###,###,###,###"
  lazy val Integer = new LongFormatWebElementParser(new DecimalFormat(integerPattern))

  def decimalPattern = "###,###,###,###.##"
  lazy val Decimal = new BigDecimalFormatWebElementParser(new DecimalFormat(decimalPattern))

  val dollarAmountPattern = "$###,###,###,###.##"
  lazy val DollarAmount = new BigDecimalFormatWebElementParser(new DecimalFormat(dollarAmountPattern))

  implicit def long2Int(long: Long) = long.toInt
  implicit def bigDecimal2Double(bigDecimal: BigDecimal) = bigDecimal.toDouble

  implicit def webElement2ParseTarget(webElement: WebElement) = new WebElementParseTarget {
    def as[A](parser: WebElementParser[A]): A = parser parse webElement
  }
  implicit def locator2ParseTarget(locator: Locator) = new WebElementParseTarget {
    def as[A](parser: WebElementParser[A]): A = {
      val webElement = findElement(locator)
      parser parse webElement
    }
  }
}

trait WebElementParser[A] {
  def parse(webElement: WebElement): A
}

trait WebElementParseTarget {
  def as[A](parser: WebElementParser[A]): A
}

trait TextWebElementParser[A] extends WebElementParser[A] {
  def extractText(webElement: WebElement): String = webElement.getText
}

trait DelegatingWebElementParser[A] extends TextWebElementParser[A] {  self: Parser[A] =>
  def parse(webElement: WebElement): A = self.parse(extractText(webElement))
}

class DoubleFormatWebElementParser(numberFormat: DecimalFormat) extends DoubleParser(numberFormat) with DelegatingWebElementParser[Double]
class BigDecimalFormatWebElementParser(numberFormat: DecimalFormat) extends BigDecimalParser(numberFormat) with DelegatingWebElementParser[BigDecimal]
class LongFormatWebElementParser(numberFormat: DecimalFormat) extends LongParser(numberFormat) with DelegatingWebElementParser[Long]

class HtmlParser(htmlFormatter: HtmlFormatter)(implicit val webDriver: WebDriver) extends WebElementParser[String] {
  import xrrocha.util.EmbeddedLanguages._

  val jsCode = js"return arguments[0].innerHTML;"
  val javascriptExecutor = webDriver.asInstanceOf[JavascriptExecutor]

  def parse(webElement: WebElement): String = {
    val result = javascriptExecutor executeScript(jsCode, webElement)
    val html = Option(result) getOrElse {
      throw new IllegalStateException(s"Null source returned from Javascript")
    }
    htmlFormatter.formatHtml(html.toString.trim)
  }
}

trait AttributeParser[A] extends TextWebElementParser[A] {
  def attributeName: String

  override def extractText(webElement: WebElement): String = Option(webElement.getAttribute(attributeName)) getOrElse {
    throw new IllegalArgumentException(s"No such attribute: $attributeName")
  }
}

trait StringAttributeParser extends AttributeParser[String] {
  def parse(webElement: WebElement) = extractText(webElement)
}

trait UrlAttributeParser extends StringAttributeParser {
  def absolute: Boolean
  def webDriver: WebDriver

  override def extractText(webElement: WebElement): String = {
    val url = super.extractText(webElement)
    if (absolute) NetUtils.resolveUrl(webDriver.getCurrentUrl, url)
    else url
  }
}

class UrlParser(val attributeName: String, override val absolute: Boolean = true)(implicit val webDriver: WebDriver) extends UrlAttributeParser
class ImageSrc(absolute: Boolean)(implicit webDriver: WebDriver) extends UrlParser("src", absolute)
class LinkHref(absolute: Boolean)(implicit webDriver: WebDriver) extends UrlParser("href", absolute)

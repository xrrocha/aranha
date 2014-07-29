package xrrocha.aranha

trait HtmlFormatter {
  def formatHtml(html: String): String
}

import org.htmlcleaner.{CleanerProperties, SimpleHtmlSerializer}
object HtmlCleanerFormatter {
  lazy val DefaultProperties = {
    val properties = new CleanerProperties
    properties.setOmitHtmlEnvelope(true)
    properties.setOmitXmlDeclaration(true)
    properties.setOmitDoctypeDeclaration(true)
    properties
  }
}
class HtmlCleanerFormatter(cleanerProperties: CleanerProperties = HtmlCleanerFormatter.DefaultProperties) extends HtmlFormatter {
  def formatHtml(html: String) = new SimpleHtmlSerializer(cleanerProperties).getAsString(html)
}

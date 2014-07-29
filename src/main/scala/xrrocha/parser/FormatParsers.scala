package xrrocha.parser

import java.text.{DateFormat, DecimalFormat}
import java.util.Date

// TODO Implement format parser unit tests

abstract class DecimalFormatParser(numberFormat: DecimalFormat, parseBigDecimal: Boolean, parseIntegerOnly: Boolean) {
  numberFormat.setParseBigDecimal(parseBigDecimal)
  numberFormat.setParseIntegerOnly(parseIntegerOnly)
}

class DoubleParser(numberFormat: DecimalFormat) extends DecimalFormatParser(numberFormat, false, false) with Parser[Double] {
  def parse(text: String): Double = numberFormat.parse(text).asInstanceOf[java.lang.Double]
}

class BigDecimalParser(numberFormat: DecimalFormat) extends DecimalFormatParser(numberFormat, true, false) with Parser[BigDecimal] {
  def parse(text: String): BigDecimal = numberFormat.parse(text).asInstanceOf[java.math.BigDecimal]
}

class LongParser(numberFormat: DecimalFormat) extends DecimalFormatParser(numberFormat, false, true) with Parser[Long] {
  def parse(text: String): Long = numberFormat.parse(text).asInstanceOf[java.lang.Long]
}

class DateFormatParser(dateFormat: DateFormat) extends Parser[Date] {
  def parse(text: String): Date = dateFormat.parse(text)
}


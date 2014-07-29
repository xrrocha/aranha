package xrrocha.parser

trait Parser[A] {
  def parse(text: String): A
}

trait ParseTarget {
  def as[A](parser: Parser[A]): A
}
object ParseTarget {
  implicit def string2ParseTarget(string: String) = new ParseTarget {
    def as[A](parser: Parser[A]): A = parser.parse(string)
  }
}

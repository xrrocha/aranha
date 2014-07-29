package xrrocha.util

object EmbeddedLanguages {
  implicit class HtmlBuilder(val sc: StringContext) extends AnyVal {
    def js(args: Any*) = sc.s(args: _*)
    def css(args: Any*) = sc.s(args: _*)
    def html(args: Any*) = sc.s(args: _*).stripMargin.trim
  }
}

package xrrocha.aranha

import org.openqa.selenium.{WebDriver, WebElement, SearchContext, By}

sealed trait Locator extends By {
  def query: String
  def name: String

  override def toString = s"$name($query)"
}
case class XPath(query: String) extends By.ByXPath(query) with Locator {
  final val name = "xpath"
}
case class CssSelector(query: String) extends By.ByCssSelector(query) with Locator {
  final val name = "css"
}
case class Id(query: String) extends By.ById(query) with Locator {
  final val name = "id"
}

object Locator {
  import scala.language.experimental.macros

  import collection.JavaConversions._
  //implicit def locator2Seq(locator: Locator)(implicit searchContext: SearchContext): Seq[WebElement] = asScalaBuffer(searchContext findElements locator)

  implicit class LocatorBuilder(val sc: StringContext) extends AnyVal {
    def cs(args: Any*): Locator = macro cssImpl
    def cssLocator(args: Any*): Locator = CssSelector(sc.s(args: _*))

    def xp(args: Any*): Locator = macro xpathImpl
    def xpathLocator(args: Any*): Locator = XPath(sc.s(args: _*))

    def id(args: Any*): Locator = Id(sc.s(args: _*))
  }

  import scala.reflect.macros.blackbox.Context

  def cssImpl(c: Context)(args: c.Expr[Any]*): c.Expr[Locator] = {
    import c.universe._

    c.prefix.tree match {
      case Apply(_, List(Apply(_, List(queryLiteral @Literal(Constant(query: String)))))) =>
        try {
          import jodd.csselly.CSSelly
          new CSSelly(query).parse
        } catch {
          case e: Exception => c.abort(c.enclosingPosition, s"Invalid css expression: $query")
        }
        reify(CssSelector(c.Expr[String](queryLiteral).splice))
      case compound =>
        val rts = compound.tpe.decl(TermName("cssLocator"))
        val rt = internal.gen.mkAttributedSelect(compound, rts)
        c.Expr[Locator](Apply(rt, args.map(_.tree).toList))
    }
  }

  def xpathImpl(c: Context)(args: c.Expr[Any]*): c.Expr[Locator] = {
    import c.universe._

    c.prefix.tree match {
      case Apply(_, List(Apply(_, List(queryLiteral @Literal(Constant(query: String)))))) =>
        try {
          import javax.xml.xpath.XPathFactory
          XPathFactory.newInstance.newXPath compile query
        } catch {
          case e: Exception => c.abort(c.enclosingPosition, s"Invalid xpath expression: $query")
        }
        reify(XPath(c.Expr[String](queryLiteral).splice))
      case compound =>
        val rts = compound.tpe.decl(TermName("xpathLocator"))
        val rt = internal.gen.mkAttributedSelect(compound, rts)
        c.Expr[Locator](Apply(rt, args.map(_.tree).toList))
    }
  }
}

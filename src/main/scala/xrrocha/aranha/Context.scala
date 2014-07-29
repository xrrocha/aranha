package xrrocha.aranha

import org.openqa.selenium.{WebElement, SearchContext, WebDriver}

// TODO Create delegating WebElement/WebDriver?
trait WithWebDriver {
  def webDriver: WebDriver

  def onError(error: Exception) {
    // TODO Dump DOM source, take screenshot
    throw error
  }
}

trait WithSearchContext extends WithWebDriver {
  def searchContext: SearchContext

  def findElement(locator: Locator) =
    try {
      searchContext findElement locator
    } catch {
      case e: Exception =>
        onError(e)
        throw e
    }

  def findElements(locator: Locator, nonEmpty: Boolean = false) =
    try {
      val webElements = searchContext findElements locator
      if (nonEmpty && webElements.isEmpty)
        throw new IllegalStateException(s"Empty elements for locator: $locator")
      webElements
    } catch {
      case e: Exception =>
        onError(e)
        throw e
    }

  import collection.JavaConversions._
  implicit def locator2Seq(locator: Locator): Seq[WebElement] = findElements(locator)
  implicit def locator2String(locator: Locator): String = findElement(locator).getText
}

trait NestableLocatorContext extends WithSearchContext {
  lazy val searchContextStack = collection.mutable.Stack[SearchContext](webDriver)

  implicit def searchContext = searchContextStack.top

  def within[A](searchContext: SearchContext)(block: => A) = {
    searchContextStack.push(searchContext)

    try {
      block
    }
    finally {
      searchContextStack.pop
    }
  }

  def within[A](locator: Locator)(block: => A) = {
    // TODO Map possible findElement exception in within
    val newContext = searchContextStack.top findElement locator
    searchContextStack.push(newContext)

    try {
      block
    }
    finally {
      searchContextStack.pop
    }
  }
}


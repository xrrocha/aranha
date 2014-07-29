package xrrocha.util

import java.net.URI

// TODO Write NetUtils tests
object NetUtils {
  def resolveUrl(basePath: String, path: String): String = resolveUrl(new URI(basePath), path).toString

  def resolveUrl(baseUri: URI, rawPath: String): URI = {
    val path = rawPath.trim

    if (path contains ":/") {
      new URI(path)
    } else {
      val adjustedPath =
        if (path startsWith "/") {
          path
        } else {
          val suffix = if (baseUri.getPath.endsWith("/")) "" else "/"
          s"$suffix$path"
        }

      baseUri.resolve(adjustedPath)
    }
  }
}

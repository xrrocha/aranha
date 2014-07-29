package xrrocha.util

import java.io.{FileInputStream, File}
import java.net.{InetAddress, InetSocketAddress}

import com.sun.net.httpserver.{HttpContext, HttpExchange, HttpHandler, HttpServer}
import com.typesafe.scalalogging.Logging
import com.typesafe.scalalogging.slf4j.Logger
import org.slf4j.LoggerFactory

trait WebServer extends Logging {
  val logger = Logger(LoggerFactory.getLogger(classOf[WebServer]))

  def port: Int = 4269
  def localAddress = InetAddress.getLoopbackAddress()
  def baseDirectory: File = new File(System.getProperty("user.dir"))

  val OK = 200
  val NotFound = 404
  val NotAuthorized = 401
  val InternalError = 500

  def responseNotFound = "Not found"
  def responseNotAuthorized = "Not authorized"
  def responseInternalError(cause: Exception) = s"InternalError: $cause"

  def checks = Seq[(File => Boolean, (Int, Array[Byte]))](
    (_.exists, (NotFound, responseNotFound.getBytes)),
    (_.canRead, (NotAuthorized, responseNotAuthorized.getBytes))
  )

  def contentTypes = Map(
    "html" -> "text/html",
    "gif" -> "image/gif",
    "jpg" -> "image/jpeg",
    "png" -> "image/png",
    "txt" -> "text/plain"
  )
  def DefaultContentType = "application/octet-stream"

  lazy val server = HttpServer.create(new InetSocketAddress(localAddress, port), 0)
  private val mounts = collection.mutable.Map[String, HttpContext]()

  def start() {
    if (mounts.isEmpty)
      mount("/", baseDirectory)
    server.start()
  }
  def stop(delay: Int = 0) { server.stop(delay) }

  def mount(mountPoint: String, directory: File) {
    logger.debug(s"Mounting $directory as $mountPoint")
    val context = server createContext(mountPoint, new DirectoryHandler(mountPoint, directory))
    mounts += mountPoint -> context
  }
  def unmount(mountPoint: String) {
    logger.debug(s"Unmounting $mountPoint")
    val context = mounts(mountPoint)
    server removeContext context
    mounts -= mountPoint
  }

  def onOK(contents: Array[Byte]) {}

  def synthesizeIndex(file: File) = {
    val directory = file.getParentFile
    val (subdirs, files) = directory.listFiles().partition(_.isDirectory)
    s"""
      |<html>
      |<head>
      |<title>${directory.getName}</title>
      |</head>
      |<body>
      |<h1>${directory.getName}</h1>
      |<ul>
      |${subdirs.toSeq.sorted map (d => s"<li>Directory: <a href='${d.getName}'>${d.getName}</a></li>")}
      |${files.toSeq.sorted map (f => s"<li>File: <a href='${f.getName}'>${f.getName}</a></li>")}
      |</ul>
      |</body>
      |</html>
    """.stripMargin.trim
  }

  class DirectoryHandler(mountPoint: String, directory: File) extends HttpHandler {
    override def handle(exchange: HttpExchange) {
      val path = exchange.getRequestURI.getPath
      logger.debug(s"Handling request $path")

      val file = new File(directory, path.substring(mountPoint.length))

      val (code, response) = checks find (!_._1(file)) map(_._2) getOrElse {
        try {
          val servedFile = {
            if (file.isDirectory) new File(file.getPath, "index.html")
            else file
          }
          logger.debug(s"Path: $path. File: ${servedFile.getAbsolutePath}")

          val contentType = {
            val pos = servedFile.getName.lastIndexOf('.')
            if (pos < 0) DefaultContentType
            else {
              val extension = servedFile.getName.substring(pos + 1)
              contentTypes.getOrElse(extension, DefaultContentType)
            }
          }
          logger.debug(s"Content-Type: $contentType")
          exchange.getResponseHeaders.set("Content-Type", contentType)

          val contents =
            if (servedFile.exists)
              IOUtils.readBytes(new FileInputStream(servedFile))
            else
              synthesizeIndex(servedFile).getBytes

          onOK(contents)
          (OK, contents)
        } catch {
          case e: Exception =>
            logger.error(s"Error handling HTTP request: $e", e)
            (InternalError, responseInternalError(e).getBytes)
        }
      }

      val responseLength = response.length
      logger.debug(s"Sending respose with code $code and length $responseLength")
      exchange.sendResponseHeaders(code, responseLength)

      IOUtils.write(response, exchange.getResponseBody)
      logger.debug(s"Finished request processing")
    }
  }
}

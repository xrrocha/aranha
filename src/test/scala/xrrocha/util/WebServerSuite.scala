package xrrocha.util

import java.io.File
import java.net.{URLConnection, HttpURLConnection, URL}

import org.scalatest.{BeforeAndAfterAll, FunSuite}

class WebServerSuite extends FunSuite with BeforeAndAfterAll {
  val tmpDir = new File(System.getProperty("java.io.tmpdir"))

  var failOnOK = false
  val server = new WebServer {
    override val baseDirectory = {
      val baseDirectory = new File("target")
      val directory = new File(baseDirectory, "aranha")
      directory.mkdir()
      assert(directory.isDirectory, s"Can't create web server directory: ${directory.getAbsolutePath}")
      directory
    }

    override def onOK(contents: Array[Byte]) {
      if (failOnOK)
        throw new Exception("Throwing exception")
    }
  }

  val index =
    """
      |<html>
      |<head>
      |<title>Aranha Test</title>
      |</head>
      |<body>
      |<h1>Aranha Test</h1>
      |<p>Welcome to Aranha Test!</p>
      |</body>
      |</html>
    """.stripMargin

  override def beforeAll() {
    val indexFile = new File(server.baseDirectory, "index.html")
    IOUtils.write(index, indexFile)

    server.start()
  }

  override def afterAll() {
    server.stop()
  }

  implicit def URLConnection2HttpURLConnection(urlConnection: URLConnection) = urlConnection.asInstanceOf[HttpURLConnection]

  test("Returns appropriate content type given resource extension") {
    server.contentTypes.keySet map { suffix =>
      val file = {
        val file = new File(server.baseDirectory, s"tmp.$suffix")
        file.deleteOnExit()
        file.createNewFile()
        file
      }
      val location = s"http://localhost:${server.port}/${file.getName}"
      val urlConnection = new URL(location) openConnection()
      assert(urlConnection.getHeaderField("Content-Type") == server.contentTypes(suffix))
    }
  }

  test("Returns default content for unknown resource extensions") {
    val file = {
      val file = new File(server.baseDirectory, s"tmp.unknown")
      file.deleteOnExit()
      file.createNewFile()
      file
    }
    val location = s"http://localhost:${server.port}/${file.getName}"
    val urlConnection = new URL(location) openConnection()
    assert(urlConnection.getHeaderField("Content-Type") == server.DefaultContentType)
  }

  test("Returns 200 and appropriate content length for existing resources") {
    val contents = "someContent"
    val file = {
      val file = new File(server.baseDirectory, s"tmp.existing")
      file.deleteOnExit()
      IOUtils.write(contents, file)
      file
    }
    val location = s"http://localhost:${server.port}/${file.getName}"
    val urlConnection = new URL(location).openConnection()
    assert(urlConnection.getResponseCode == 200 && urlConnection.getContentLength == contents.length)
  }

  test("Returns 404 and appropriate content length for non-existing resources") {
    val file = new File(server.baseDirectory, "nonExistent.wtf")
    val location = s"http://localhost:${server.port}/${file.getName}"
    val urlConnection = new URL(location).openConnection()
    assert(urlConnection.getResponseCode == 404 && urlConnection.getContentLength == server.responseNotFound.getBytes.length)
  }

  test("Returns 401 and appropriate content length for unauthorized resources") {
    val contents = "someContent"
    val file = {
      val file = new File(server.baseDirectory, s"tmp.unauthorized")
      file.deleteOnExit()
      IOUtils.write(contents, file)
      file.setReadable(false)
      file
    }
    val location = s"http://localhost:${server.port}/${file.getName}"
    val urlConnection = new URL(location).openConnection()
    assert(urlConnection.getResponseCode == 401 && urlConnection.getContentLength == server.responseNotAuthorized.getBytes.length)
  }

  test("Returns 500 and appropriate content length for internal errors") {
    val contents = "someContent"
    val file = {
      val file = new File(server.baseDirectory, s"force.error")
      file.deleteOnExit()
      IOUtils.write(contents, file)
      file
    }
    val location = s"http://localhost:${server.port}/${file.getName}"

    intercept[Exception] {
      failOnOK = true
      try {
        val urlConnection = new URL(location).openStream()
      } finally {
        failOnOK = false
      }
    }
  }

  test("Returns 'index.html' for directory resources") {
    val location = s"http://localhost:${server.port}/"
    val urlConnection = new URL(location) openConnection()
    assert(urlConnection.getHeaderField("Content-Type") == server.contentTypes("html"))
    assert(IOUtils.read(urlConnection.getInputStream) == index)
  }

  test("Synthesizes 'index.html' for directory resources not containing one") {
    val dirName = "dir"
    val dir = new File(server.baseDirectory, dirName)
    dir.mkdir()

    val subDirName = "subdir"
    val subDir = new File(dir, subDirName)
    subDir.mkdir()

    val filename = "file.txt"
    val file = new File(dir, filename)
    file.createNewFile()

    val location = s"http://localhost:${server.port}/$dirName"
    val urlConnection = new URL(location).openConnection()
    assert(urlConnection.getResponseCode == 200)
    assert(urlConnection.getHeaderField("Content-Type") == server.contentTypes("html"))

    val content = IOUtils.read(urlConnection.getInputStream)
    assert(content contains s"<a href='$subDirName")
    assert(content contains s"<a href='$filename")
  }

  test("Mounts and unmounts directory") {
    val dirName = "neverland"
    val mountPoint = s"/neverland"
    val location = s"http://localhost:${server.port}/$dirName/some.html"

    val mountDirectory = new File(tmpDir, s"${dirName}_${System.currentTimeMillis()}")
    mountDirectory.deleteOnExit()
    assert(mountDirectory.mkdir())
    val file = new File(mountDirectory, "some.html")
    file.deleteOnExit()

    try {
      val firstUrlConnection = new URL(location).openConnection()
      assert(firstUrlConnection.getResponseCode == 404)

      val contents = "<div>Some HTML</div>"
      IOUtils.write(contents, file)
      assert(file.exists() && file.length() == contents.getBytes.length)

      server.mount(mountPoint, mountDirectory)

      val secondUrlConnection = new URL(location).openConnection()
      assert(secondUrlConnection.getResponseCode == 200)

      server.unmount(mountPoint)
      val thirdUrlConnection = new URL(location).openConnection()
      assert(thirdUrlConnection.getResponseCode == 404)
    } finally {
      file.delete()
      mountDirectory.delete()
    }
  }
}


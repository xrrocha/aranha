package xrrocha.util

import java.io.{FileOutputStream, File, ByteArrayOutputStream, ByteArrayInputStream}

import org.scalatest.FunSuite
import java.nio.file._

class IOUtilsSuite extends FunSuite {
  val tempDir = new File(System.getProperty("java.io.tmpdir"))

  def newTempFile = {
    val file = File.createTempFile("IOUtils", ".tmp")
    file.deleteOnExit()
    file
  }

  def newFile = {
    val file = new File(tempDir, s"${System.currentTimeMillis()}.tmp")
    ensureNonExistent(file)
    file.deleteOnExit()
    file
  }

  def ensureNonExistent(file: File) = {
    if (file.exists())
      assert(file.delete())
    file
  }

  test("Creates temporary file") {
    val contents = "someContents"
    val file = IOUtils.createTempFile(contents)
    assert(file.exists() && file.length() == contents.length)
  }

  test("Copies file to new file") {
    val source = newTempFile
    val destination = newFile
    IOUtils.copyFile(source, destination)
    assert(destination.exists())
  }

  test("Copies file to existing file") {
    val source = newTempFile
    val destination = newTempFile
    intercept[FileAlreadyExistsException] {
      IOUtils.copyFile(source, destination)
    }
    IOUtils.copyFile(source, destination, StandardCopyOption.REPLACE_EXISTING)
  }

  test("Writes string to named file") {
    val contents = "someContents"
    val file = File.createTempFile("tmp_", ".tmp")
    file.deleteOnExit()
    IOUtils.write(contents, file.getAbsolutePath)
    assert(file.exists() && file.length() == contents.length)
  }

  test("Writes string to file") {
    val contents = "someContents"
    val file = File.createTempFile("tmp_", ".tmp")
    file.deleteOnExit()
    IOUtils.write(contents, file)
    assert(file.exists() && file.length() == contents.length)
  }

  test("Writes string to output stream") {
    val contents = "someContents"
    val os = new ByteArrayOutputStream()
    IOUtils.write(contents, os)
    assert(os.toString == contents)
  }

  test("Writes bytes to output stream") {
    val contents = "someContents"
    val os = new ByteArrayOutputStream()
    IOUtils.write(contents.getBytes, os)
    assert(os.toString == contents)
  }

  test("Reads string from URL") {
    val contents = "someContents"
    val file = File.createTempFile("tmp_", ".tmp")
    file.deleteOnExit()
    val fos = new FileOutputStream(file)
    fos.write(contents.getBytes)
    fos.flush()
    fos.close()
    val url = file.toURI.toURL.toExternalForm
    val read = IOUtils.read(url)
    assert(read == contents)
  }

  test("Reads string from file") {
    val contents = "someContents"
    val file = File.createTempFile("tmp_", ".tmp")
    file.deleteOnExit()
    val fos = new FileOutputStream(file)
    fos.write(contents.getBytes)
    fos.flush()
    fos.close()
    val read = IOUtils.read(file)
    assert(read == contents)
  }

  test("Reads string from input stream") {
    val inputString = "Hey, I'm a string!"
    val from = new ByteArrayInputStream(inputString.getBytes)
    val resultString = IOUtils.read(from)
    assert(resultString == inputString)
  }

  test("Reads bytes from input stream") {
    val inputString = "Hey, I'm a string!"
    val from = new ByteArrayInputStream(inputString.getBytes)
    val result = IOUtils.readBytes(from)
    assert(result zip inputString.getBytes forall (p => p._1 == p._2))
  }

  test("Transcribes from input stream to output stream returning appropriate byte count") {
    val inputString = "Hey, I'm a string!"
    val from = new ByteArrayInputStream(inputString.getBytes)
    val to = new ByteArrayOutputStream
    IOUtils.transcribe(from, to)
    assert(new String(to.toByteArray) == inputString)
  }
}

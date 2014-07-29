package xrrocha.util

import java.io._
import java.net.URL
import java.nio.file._

object IOUtils {
  def createTempFile(contents: String, prefix: String = "tmp_", suffix: String = ".tmp") = {
    val file = File.createTempFile(prefix, suffix)
    file.deleteOnExit()

    write(contents, file)

    file
  }

  def copyFile(from: File, to: File, copyOptions: StandardCopyOption*) {
    Files.copy(from.toPath, to.toPath, copyOptions: _*)
  }

  def write(string: String, to: String) {
    transcribe(new ByteArrayInputStream(string.getBytes), new FileOutputStream(to))
  }

  def write(string: String, to: File) {
    transcribe(new ByteArrayInputStream(string.getBytes), new FileOutputStream(to))
  }

  def write(string: String, to: OutputStream) {
    transcribe(new ByteArrayInputStream(string.getBytes), to)
  }

  def write(bytes: Array[Byte], to: OutputStream) {
    transcribe(new ByteArrayInputStream(bytes), to)
  }

  def read(from: String): String = {
    read {
      if (from contains ":/")
        new URL(from).openStream()
      else
        new FileInputStream(from)
    }
  }

  def read(from: File): String = {
    read(new FileInputStream(from))
  }

  def read(from: InputStream) = {
    val baos = new ByteArrayOutputStream
    transcribe(from, baos)
    baos.toString
  }

  def readBytes(from: InputStream) = {
    val baos = new ByteArrayOutputStream
    transcribe(from, baos)
    baos.toByteArray
  }

  val DefaultBufferSize = 4096
  def transcribe(from: InputStream, to: OutputStream, bufferSize: Int = DefaultBufferSize) {
    val buffer = new Array[Byte](bufferSize)

    Iterator.continually(from.read(buffer)).
      takeWhile(_ > 0).
      foldLeft(0L) { (totalCount, length) =>
        to.write(buffer, 0, length)
        totalCount +  length
      }

    from.close()
    to.flush()
    to.close()
  }
}

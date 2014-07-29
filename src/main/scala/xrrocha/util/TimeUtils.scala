package xrrocha.util

import scala.util.Try

object TimeUtils {
  def time[A](action: => A): (Try[A], Long) = {
    val startTime = System.currentTimeMillis()
    val result = Try(action)
    val endTime = System.currentTimeMillis()
    (result, endTime - startTime)
  }
}

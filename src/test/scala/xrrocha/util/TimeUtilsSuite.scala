package xrrocha.util

import org.scalatest.FunSuite

class TimeUtilsSuite extends FunSuite {
  test("Measures time correctly") {
    val period = 1000l
    val (_, elapsedTime) = TimeUtils.time(Thread.sleep(period))
    assert(elapsedTime / period.toDouble >= .98)
  }
  test("Returns correct result on success") {
    val (result, _) = TimeUtils.time(42)
    assert(result.isSuccess && result.get == 42)
  }
  test("Returns exception on failure") {
    val (result, _) = TimeUtils.time(throw new IllegalStateException("Oops!"))
    assert(result.isFailure && result.failed.get.isInstanceOf[IllegalStateException])
  }
}

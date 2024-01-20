package Utilities

import java.lang.reflect.Method

object SpinWait {

  // We want to use Thread.onSpinWait() if it's available,
  // but that's only in Java 9+

  private lazy val onSpinWait: Option[Method] = classOf[Thread].getDeclaredMethods.find(_.getName == "onSpinWait")
  def apply():  Unit = {
    if (onSpinWait.isDefined) {
      onSpinWait.get.invoke(Thread.currentThread())
    } else {
      Thread.sleep(0, 100000)
    }
  }
}

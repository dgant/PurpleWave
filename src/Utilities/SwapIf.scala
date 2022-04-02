package Utilities

object SwapIf {
  def apply(predicate: Boolean, a: () => Unit, b: () => Unit): Unit = {
    if (predicate) {
      b()
      a()
    } else {
      a()
      b()
    }
  }
}

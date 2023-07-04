package Utilities

object SwapIf {
  def apply[T](predicate: Boolean, a: => T, b: => T): T = {
    if (predicate) {
      b
      a
    } else {
      a
      b
    }
  }
}

package Utilities

object In {
  @inline final def apply[T](element: T, among: T*): Boolean = among.contains(element)
}

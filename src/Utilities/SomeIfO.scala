package Utilities

object SomeIfO {
  @inline final def apply[T](predicate: Boolean, value: => Option[T]): Option[T] = ?(predicate, value, None)
}

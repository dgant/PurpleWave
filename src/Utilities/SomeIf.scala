package Utilities

object SomeIf {
  @inline final def apply[T](predicate: Boolean, value: => T): Option[T] = ?(predicate, Some(value), None)
}

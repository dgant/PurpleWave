package Utilities

//noinspection ScalaFileName
object ? {
  @inline final def apply[T](predicate: Boolean, yes: => T, no: => T): T = {
    if (predicate) yes else no
  }
}

package Utilities

//noinspection ScalaFileName
object ? {
  def apply[T](predicate: Boolean, yes: T, no: T): T = {
    if (predicate) yes else no
  }
}

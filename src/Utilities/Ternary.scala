package Utilities

import scala.language.experimental.macros

//noinspection ScalaFileName
object ? {
  def apply[T](predicate: Boolean, yes: => T, no: => T): T = macro TernaryMacros.apply[T]
}

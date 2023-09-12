package Utilities

import scala.collection.JavaConverters._

object AsJava {
  def apply[T](x: Iterable[T]): java.lang.Iterable[T] = x.asJava
}

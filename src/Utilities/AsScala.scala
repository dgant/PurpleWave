package Utilities

import scala.collection.JavaConverters._

object AsScala {
  def apply[T](x: java.lang.Iterable[T]): Iterable[T] = x.asScala
}

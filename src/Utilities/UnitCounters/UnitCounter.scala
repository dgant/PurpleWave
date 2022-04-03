package Utilities.UnitCounters

trait UnitCounter {
  def minimum: Int = 1
  def maximum: Int
  final def continue[T](iterable: Iterable[T]): Boolean = iterable.size < maximum
  final def accept[T](iterable: Iterable[T]): Boolean = iterable.size >= minimum && iterable.size <= maximum
}

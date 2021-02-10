package Planning.UnitCounters

trait UnitCounter {
  def maximum: Int
  final def continue[T](iterable: Iterable[T]): Boolean = iterable.size < maximum
  final def accept[T](iterable: Iterable[T]): Boolean = iterable.size <= maximum
}

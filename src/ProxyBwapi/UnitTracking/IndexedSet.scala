package ProxyBwapi.UnitTracking

final class IndexedSet[T] extends IndexedSeq[T]  {
  private var _set: Set[T] = Set.empty
  private var _seq: IndexedSeq[T] = IndexedSeq.empty
  private def this(set: Set[T], seq: IndexedSeq[T]) {
    this
    _set = set
    _seq = seq
  }
  def this(other: Iterable[T]) {
    this
    _seq = other.toIndexedSeq
    _set = _seq.toSet
  }

  override def length: Int = _seq.length
  override def apply(idx: Int): T = _seq(idx)
  override def contains[A1 >: T](elem: A1): Boolean = elem.isInstanceOf[T] && _set.contains(elem.asInstanceOf[T])
}

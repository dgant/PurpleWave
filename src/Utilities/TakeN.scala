package Utilities

object TakeN {
  def by[T](number: Int, iterable: Iterable[T])(implicit ordering: Ordering[T]): IndexedSeq[T] = {
    val queue = collection.mutable.PriorityQueue[T](iterable.toSeq: _*)
    (0 until Math.min(number, queue.size)).map(i => queue.dequeue())
  }
}

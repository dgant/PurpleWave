package Utilities

import Mathematics.PurpleMath

object TakeN {
  def by[T](number: Int, iterable: Iterable[T])(implicit ordering: Ordering[T]): IndexedSeq[T] = {
    val queue = collection.mutable.PriorityQueue[T](iterable.toSeq: _*)
    (0 until Math.min(number, queue.size)).map(i => queue.dequeue())
  }

  def percentile[T](percentile: Double, iterable: Iterable[T])(implicit ordering: Ordering[T]): Option[T] = {
    val nth = (iterable.size * PurpleMath.clamp(percentile, 0, 1)).toInt
    val queue = collection.mutable.PriorityQueue[T](iterable.toSeq: _*)
    (0 until nth).foreach(i => queue.dequeue())
    queue.headOption
  }
}

package ProxyBwapi.UnitTracking

import scala.collection.mutable.ArrayBuffer

/**
  * UnorderedBuffer is a collection type designed for maximal performance.
  * It is a mutable collection with contiguous memory access,
  * but also has constant time indexing, insertion, and deletion,
  * and only reallocates memory when exceeding its capacity.
  *
  * Limitations include the absence of a stable order of elements,
  * unsafe iteration, producing unsafe views, and using memory greedily.
  *
  * It achieves these qualities by:
  * - Being a final class, maximizing likelihood of inlining
  * - Implementing insertions as swap-to-end
  * - Reserving capacity on construction
  * - Never shrinking; holding its allocated capacity and dynamic growth forever
  */
final class UnorderedBuffer[T >: Null](capacity: Int = 8) extends IndexedSeq[T] {
  private val values: ArrayBuffer[T] = ArrayBuffer.fill[T](capacity)(null)
  private var _size = 0

  def this(initialValues: Iterable[T]) {
    this(initialValues.size)
    addAll(initialValues)
  }

  // Add to end, for fast insertion
  @inline def add(value: T): T = {
    if (_size >= values.size) {
      values += value
    } else {
      values(_size) = value
    }
    _size += 1
    value
  }

  // Swap-and-pop for fast removal
  @inline def remove(value: T): Unit = {
    var i = 0
    while (i < _size) {
      if (values(i) == value) {
        values(i) = values(_size - 1)
        values(_size - 1) = null
        _size -= 1
        return
      }
      i += 1
    }
  }

  // Swap-and-pop for fast removal
  @inline def removeIf(predicate: (T) => Boolean): Unit = {
    var i = 0
    while (i < _size) {
      val value = values(i)
      if (predicate(value)) {
        values(i) = values(_size - 1)
        values(_size - 1) =  null
        _size -= 1
      } else {
        i += 1
      }
    }
  }

  @inline def addAll(values: TraversableOnce[T]): Unit = { values.foreach(add) }
  @inline def removeAll(values: TraversableOnce[T]): Unit = { values.foreach(remove) }

  @inline def clear(): Unit = {
    // Empty all populated values without losing allocated space
    while (_size > 0) {
      _size -= 1
      values(_size) = null
    }
  }

  // This view needs a null check for anyone who foolishly tries to hold on to a copy of the view,
  // as the view can produce a null value if size decreases in between constructing and evaluating the view.
  //
  // That still doesn't fix the case where the view *increases* in size, causing old views to omit elements.
  // The bugs this produces will be subtle and confusing, because evaluating the collection in an immediate window
  // will produce a new view and show a perfectly dandy collection.
  //
  // So the hacky mitigation here is to add a margin to take (eg. take(size + epsilon))
  // and rely on the filter to trim the view back down to size
  @inline def all: Seq[T] = values.view.take(_size + 5).filter(_ != null)

  @inline override def foreach[U](f: T => U): Unit = {
    var i = 0
    while (i < _size) {
      f(values(i))
      i += 1
    }
  }

  // Overrides for IndexedSeq
  @inline override def length: Int = size
  @inline override def apply(i: Int): T = values(i)

  // These overrides aren't strictly necessary because the supertrait TraversableLike implements these using foreach(),
  // but they may improve performance, as TraversibleLike's implementations of these produce a closure and a break
  // and breaks appear to be very slow.
  @inline override def head: T = values(0)
  @inline override def isEmpty: Boolean = _size == 0
  @inline override def nonEmpty: Boolean = _size != 0
  @inline override def size: Int = _size
  @inline override def exists(p: T => Boolean): Boolean = {
    var i = 0
    while (i < _size) {
      if (p(values(i))) return true
      i += 1
    }
    false
  }
  @inline override def forall(p: T => Boolean): Boolean = {
    var i = 0
    while (i < _size) {
      if ( ! p(values(i))) return false
      i += 1
    }
    true
  }
  @inline override def find(p: T => Boolean): Option[T] = {
    var i = 0
    while (i < _size) {
      if (p(values(i))) return Some(values(i))
      i += 1
    }
    None
  }
}

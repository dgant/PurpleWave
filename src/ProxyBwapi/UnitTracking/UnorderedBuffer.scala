package ProxyBwapi.UnitTracking

import scala.collection.mutable.ArrayBuffer

class UnorderedBuffer[T >: Null](val capacity: Int) {
  private val values: ArrayBuffer[T] = ArrayBuffer.fill[T](capacity)(null)
  private var size = 0

  // Add to end, for fast insertion
  def add(value: T): T = {
    values(size) = value
    size += 1
    value
  }

  // Swap-and-pop for fast removal
  def remove(value: T): Unit = {
    var i = 0
    while (i < size) {
      if (values(i) == value) {
        values(i) = values(size - 1)
        values(size - 1) =  null
        size -= 1
        return
      }
      i += 1
    }
  }

  // Swap-and-pop for fast removal
  def removeIf(predicate: (T) => Boolean): Unit = {
    var i = 0
    while (i < size) {
      val value = values(i)
      if (predicate(value)) {
        values(i) = values(size - 1)
        values(size - 1) =  null
        size -= 1
      }
      i += 1
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
  def all: Seq[T] = values.view.take(size + 5).filter(_ != null)
}

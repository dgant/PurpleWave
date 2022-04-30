package Mathematics.Functions

import scala.collection.mutable

trait Sorting {
  /**
    * Applies a stable, in-place insertion sort to a mutable array-like collection.
    *
    * This is most advantageous for collections that will likely require minimal re-ordering as it avoids allocation.
    */
  @inline final def sortStablyInPlaceBy[T, O: Ordering](a: mutable.IndexedSeq[T])(feature: T => O): Unit = {
    // Insertion sort: Stable, in-place, and adaptive
    val ordering = Ordering.by(feature)
    val length = a.length
    var i = 0
    while (i < length) {
      var j = i
      while (j > 0 && ordering.gt(a(j-1), a(j))) {
        val swap = a(j)
        a(j) = a(j-1)
        a(j-1) = swap
        j = j - 1
      }
      i = i + 1
    }
  }
}

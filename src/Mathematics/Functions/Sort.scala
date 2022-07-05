package Mathematics.Functions

import scala.collection.mutable

trait Sort {
  /**
    * Applies a stable, in-place insertion sort to a mutable array-like collection.
    *
    * This is most advantageous for collections that will likely require minimal re-ordering as it avoids allocation.
    */
  @inline final def sortStablyInPlaceBy[T, O: Ordering](a: mutable.IndexedSeq[T], startIndex: Int = 0, endIndex: Int = Int.MaxValue)(feature: T => O): Unit = {
    // Insertion sort: Stable, in-place, and adaptive
    val ordering = Ordering.by(feature)
    val boundary = Math.min(a.length, endIndex)
    var i = startIndex
    while (i < boundary) {
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

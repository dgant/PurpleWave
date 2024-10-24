package Performance

import Utilities.UnitFilters.UnitFilter
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

final class TotalUnitCounter[T <: UnitInfo](source: () => Iterable[T]) {
  
  val units = new Cache(source)
  val counts = new Cache(() => new mutable.HashMap[UnitFilter, Int] {
    override def default(key: UnitFilter): Int = {
      val count = units().count(_.isPrerequisite(key))
      put(key, count)
      count
    }
  })

  @inline def apply(predicate: (T) => Boolean): Int = {
    // How can we speed this up?
    p(predicate)
  }

  @inline def p(predicate: (T) => Boolean): Int = {
    units().count(predicate)
  }
}

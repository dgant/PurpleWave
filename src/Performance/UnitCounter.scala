package Performance

import Planning.Composition.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class UnitCounter[T <: UnitInfo](source: () => Iterable [T]) {
  
  val units = new Cache(source)
  val counts = new Cache(() => new mutable.HashMap[UnitMatcher, Int] {
    override def default(key: UnitMatcher): Int = {
      val count = units().count(_.is(key))
      put(key, count)
      count
    }
  })
  
  def apply(matcher: UnitMatcher*): Int = {
    matcher.map(m => counts()(m)).sum
  }
  
  def apply(predicate: (T) => Boolean): Int = {
    // How can we speed this up?
    units().count(predicate)
  }
}

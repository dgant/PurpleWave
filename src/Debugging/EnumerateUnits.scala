package Debugging

import ProxyBwapi.UnitInfo.UnitInfo

object EnumerateUnits {
  def apply(units: Iterable[UnitInfo]): String = {
    val counts = units
      .toVector
      .map(_.unitClass)
      .groupBy(x => x)
    val output = counts
      .toSeq
      .sortBy(-_._2.size)
      .map(p => p._2.size + p._1.abbr)
      .mkString(".")
    output
  }
}

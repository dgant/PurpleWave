package Planning.Compositions

import Planning.Plans.Macro.Automatic.MatchingRatio
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable.ArrayBuffer

class CompositionUnit(val unit: UnitClass) {
  var min: Int = 0
  var max: Int = 400
  var weight: Double = 1.0
  var sink: Boolean = false
  val ratios = new ArrayBuffer[MatchingRatio]
}
package Information.Battles.Prediction

import Information.Battles.Prediction.Simulation.Simulacrum
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable.ArrayBuffer

object CPCount {
  @inline def apply(units: ArrayBuffer[UnitInfo], extract: Simulacrum => Double): Double = {
    var output = 0.0
    val length = units.length
    var i = 0
    while (i < length) {
      output += extract(units(i).simulacrum)
      i += 1
    }
    output
  }
}

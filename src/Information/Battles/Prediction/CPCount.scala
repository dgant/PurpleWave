package Information.Battles.Prediction

import Information.Battles.Prediction.Simulation.Simulacrum
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable.ArrayBuffer

object CPCount {
  // Already captured and filtered values, such as a checkpoint's immutable snapshots.
  @inline def apply[A <: AnyRef](values: Array[A])(extract: A => Double): Double = {
    var output = 0.0
    val length = values.length
    var i = 0
    while (i < length) {
      output += extract(values(i))
      i += 1
    }
    output
  }

  @inline def apply(units: ArrayBuffer[UnitInfo], extract: Simulacrum => Double): Double = {
    var output = 0.0
    val length = units.length
    var i = 0
    while (i < length) {
      val simulacrum = units(i).simulacrum
      if (simulacrum.shouldScoreIfOurs || simulacrum.isEnemy) {
        output += extract(simulacrum)
      }
      i += 1
    }
    output
  }
}

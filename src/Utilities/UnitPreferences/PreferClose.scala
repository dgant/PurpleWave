package Utilities.UnitPreferences

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Points}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?

case class PreferClose(pixel: Pixel = Points.middle) extends UnitPreference {
  override def apply(unit: FriendlyUnitInfo): Double = {
    val inMiningCycle = unit.unitClass.isWorker && unit.orderTarget.exists(t => t.unitClass.isGas || (t.unitClass.isMinerals && unit.pixelDistanceCenter(t) < 72))
    (?(With.performance.disqualificationDanger, unit.pixelDistanceCenter(pixel), unit.pixelDistanceTravelling(pixel))
      * Maff.or1(1.2,   unit.carryingMinerals)
      * Maff.or1(1.25,  unit.carryingGas)
      * Maff.or1(1.3,   inMiningCycle))
  }
}

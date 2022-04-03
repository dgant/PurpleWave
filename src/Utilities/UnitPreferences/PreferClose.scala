package Utilities.UnitPreferences

import Lifecycle.With
import Mathematics.Points.{Pixel, SpecificPoints}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class PreferClose(pixel: Pixel = SpecificPoints.middle) extends UnitPreference {
  override def apply(unit: FriendlyUnitInfo): Double = {
    val inMiningCycle = unit.unitClass.isWorker && unit.orderTarget.exists(t => t.unitClass.isGas || (t.unitClass.isMinerals && unit.pixelDistanceCenter(t) < 72))
    ((if (With.performance.danger) unit.pixelDistanceCenter(pixel) else unit.pixelDistanceTravelling(pixel))
    * (if (unit.carryingMinerals) 1.2   else 1.0)
    * (if (unit.carryingGas)      1.25  else 1.0)
    * (if (inMiningCycle)         1.3   else 1.0))
  }
}

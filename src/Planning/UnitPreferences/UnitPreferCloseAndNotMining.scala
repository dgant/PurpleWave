package Planning.UnitPreferences

import Mathematics.Points.{Pixel, SpecificPoints}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class UnitPreferCloseAndNotMining(pixel: Pixel = SpecificPoints.middle) extends UnitPreference {
  
  override def preference(unit: FriendlyUnitInfo): Double = {
    val busyCarrying  = unit.carryingResources
    val busyGassing   = unit.target.exists(t => t.unitClass.isGas)
    val busyDrilling  = unit.target.exists(t => t.unitClass.isMinerals && ! unit.moving && unit.pixelDistanceCenter(t) < 72.0)
    val baseCost      = unit.framesToTravelTo(pixel)
    val multiplier    = if (busyCarrying || busyGassing || busyDrilling) 1.3 else 1.0
    baseCost * multiplier
  }
}

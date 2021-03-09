package Planning.UnitPreferences

import Mathematics.Points.{Pixel, SpecificPoints}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class PreferCloseAndNotMining(pixel: Pixel = SpecificPoints.middle) extends UnitPreference {
  
  override def apply(unit: FriendlyUnitInfo): Double = {
    val busyCarrying  = unit.carrying
    val busyGassing   = unit.orderTarget.exists(t => t.unitClass.isGas)
    val busyDrilling  = unit.orderTarget.exists(t => t.unitClass.isMinerals && unit.pixelDistanceCenter(t) < 72.0)
    val baseCost      = unit.framesToTravelTo(pixel)
    val busy          = busyCarrying || busyGassing || busyDrilling
    baseCost * (if (busy) 1.3 else 1) + (if (busy) 96 else 0)
  }
}

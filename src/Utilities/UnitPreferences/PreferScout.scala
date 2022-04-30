package Utilities.UnitPreferences
import Mathematics.Maff
import Mathematics.Points.{Pixel, Points}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class PreferScout(val to: Pixel*) extends UnitPreference {
  override def apply(unit: FriendlyUnitInfo): Double = {
    (unit.remainingOccupationFrames
      + Maff.orElse(to, Seq(Points.middle)).view.map(unit.framesToTravelTo).min
      + (if (unit.intent.toScoutTiles.nonEmpty) 0 else 240)
      + (if (unit.carrying) 160 else 0))
  }
}

package Planning.UnitPreferences
import Mathematics.Maff
import Mathematics.Points.{Pixel, SpecificPoints}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class PreferScout(val to: Pixel*) extends UnitPreference {
  override def apply(unit: FriendlyUnitInfo): Double = {
    (unit.remainingOccupationFrames
      + Maff.orElse(to, Seq(SpecificPoints.middle)).view.map(unit.framesToTravelTo).min
      + (if (unit.intent.toScoutTiles.nonEmpty) 0 else 240)
      + (if (unit.carrying) 160 else 0))
  }
}

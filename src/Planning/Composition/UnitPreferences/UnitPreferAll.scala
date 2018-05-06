package Planning.Composition.UnitPreferences
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class UnitPreferAll(preferences: UnitPreference*) extends UnitPreference {
  
  override def preference(unit: FriendlyUnitInfo): Double = {
    val weighted = preferences
      .zipWithIndex
      .map(pair =>
        pair._1.preference(unit)
        * Math.pow(10, -pair._2))
    val output = weighted.sum
    output
  }
}

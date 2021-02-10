package Planning.UnitPreferences
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class PreferAll(preferences: Preference*) extends Preference {
  
  override def apply(unit: FriendlyUnitInfo): Double = {
    val weighted = preferences
      .zipWithIndex
      .map(pair =>
        pair._1.apply(unit)
        * Math.pow(10, -pair._2))
    val output = weighted.sum
    output
  }
}

package Utilities.UnitPreferences
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class PreferAll(preferences: Function[FriendlyUnitInfo, Double]*) extends UnitPreference {
  override def apply(unit: FriendlyUnitInfo): Double = {
    preferences
      .zipWithIndex
      .map(pair => pair._1.apply(unit) * Math.pow(10, -pair._2))
      .sum
  }
}

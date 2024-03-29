package Utilities.UnitPreferences
import Mathematics.Maff
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class PreferTiers(preferences: Function[FriendlyUnitInfo, Double]*) extends UnitPreference {
  override def apply(unit: FriendlyUnitInfo): Double = {
    preferences
      .zipWithIndex
      .map(pair => Maff.fastSigmoid01(pair._1(unit)) * Math.pow(10, -pair._2))
      .sum
  }
}

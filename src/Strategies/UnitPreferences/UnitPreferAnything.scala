package Strategies.UnitPreferences
import bwapi.Unit

object UnitPreferAnything extends UnitPreference {
  override def preference(unit: Unit): Double = 0
}

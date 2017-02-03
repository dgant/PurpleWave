package Strategies.UnitPreferences

trait UnitPreference {
  def preference(unit:bwapi.Unit):Double
}

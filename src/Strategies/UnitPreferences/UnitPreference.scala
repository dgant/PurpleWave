package Strategies.UnitPreferences

import Types.UnitInfo.FriendlyUnitInfo

trait UnitPreference {
  def preference(unit:FriendlyUnitInfo):Double
}

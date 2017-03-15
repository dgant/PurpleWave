package Planning.Composition.UnitPreferences

import BWMirrorProxy.UnitInfo.FriendlyUnitInfo

trait UnitPreference {
  def preference(unit:FriendlyUnitInfo):Double
}

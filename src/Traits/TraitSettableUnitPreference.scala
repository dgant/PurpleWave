package Traits

import Strategies.UnitPreferences.{UnitPreferAnything, UnitPreference}

trait TraitSettableUnitPreference {
  
  var _unitPreference:UnitPreference = UnitPreferAnything
  
  def getUnitPreference:UnitPreference = {
    _unitPreference
  }
  
  def setUnitPreference(UnitPreference: UnitPreference) {
    _unitPreference = UnitPreference
  }
}

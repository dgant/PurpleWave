package Traits

import Strategies.UnitMatchers.{UnitMatchAnything, UnitMatcher}

trait TraitSettableUnitMatcher {
  
  var _unitMatcher:UnitMatcher = UnitMatchAnything
  
  def getUnitMatcher:UnitMatcher = {
    _unitMatcher
  }
  
  def setUnitMatcher(UnitMatcher: UnitMatcher) {
    _unitMatcher = UnitMatcher
  }
}

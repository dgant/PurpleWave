package Strategies.UnitPreferences

import Traits.TraitSettablePositionFinder
import bwapi.Unit

class UnitPreferClose
  extends UnitPreference
  with TraitSettablePositionFinder{
  
  override def preference(unit: Unit): Double = {
    getPositionFinder.find
      .map(position => position.toPosition.getDistance(unit.getPosition))
      .getOrElse(0)
  }
}

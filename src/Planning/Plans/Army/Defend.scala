package Planning.Plans.Army

import Planning.Composition.PositionFinders.Tactics.PositionChoke
import Planning.Composition.UnitMatchers.UnitMatchWarriors

class Defend extends ControlPosition {
  
  description.set("Defend a position")
  
  units.get.unitMatcher.set(UnitMatchWarriors)
  positionToControl.set(new PositionChoke)
}

package Plans.Army

import Strategies.PositionFinders.PositionChoke
import Strategies.UnitCountEverything
import Strategies.UnitMatchers.UnitMatchWarriors

class DefendChoke extends ControlPosition {
  position.set(new PositionChoke)
  units.get.unitMatcher.set(UnitMatchWarriors)
  units.get.unitCounter.set(UnitCountEverything)
}

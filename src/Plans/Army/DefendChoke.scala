package Plans.Army

import Strategies.PositionFinders.PositionChoke
import Strategies.UnitCounters.UnitCountAll
import Strategies.UnitMatchers.UnitMatchWarriors

class DefendChoke extends ControlPosition {
  position.set(PositionChoke)
  units.get.unitMatcher.set(UnitMatchWarriors)
  units.get.unitCounter.set(UnitCountAll)
}

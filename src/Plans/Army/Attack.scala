package Plans.Army

import Strategies.PositionFinders.PositionEnemyBase
import Strategies.UnitCounters.UnitCountAll
import Strategies.UnitMatchers.UnitMatchWarriors

class Attack extends ControlPosition {
  position.set(new PositionEnemyBase)
  units.get.unitCounter.set(UnitCountAll)
  units.get.unitMatcher.set(UnitMatchWarriors)
}

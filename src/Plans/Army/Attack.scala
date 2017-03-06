package Plans.Army

import Strategies.PositionFinders.PositionEnemyBase
import Strategies.UnitCountEverything
import Strategies.UnitMatchers.UnitMatchWarriors

class Attack extends ControlPosition {
  position.set(new PositionEnemyBase)
  units.get.unitCounter.set(UnitCountEverything)
  units.get.unitMatcher.set(UnitMatchWarriors)
}

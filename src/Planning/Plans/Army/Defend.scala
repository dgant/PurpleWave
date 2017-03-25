package Planning.Plans.Army

import Planning.Composition.PositionFinders.PositionChoke
import Planning.Composition.UnitMatchers.UnitMatchWarriors

class Defend extends ControlPosition {
  units.get.unitMatcher.set(UnitMatchWarriors)
  position.set(new PositionChoke)
}

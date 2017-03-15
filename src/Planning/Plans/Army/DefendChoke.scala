package Planning.Plans.Army

import Planning.Composition.PositionFinders.PositionChoke
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitMatchers.UnitMatchWarriors

class DefendChoke extends ControlPosition {
  position.set(new PositionChoke)
  units.get.unitMatcher.set(UnitMatchWarriors)
  units.get.unitCounter.set(UnitCountEverything)
}

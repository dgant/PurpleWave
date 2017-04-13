package Planning.Plans.Army

import Planning.Composition.PixelFinders.Tactics.TileChoke
import Planning.Composition.UnitMatchers.UnitMatchWarriors

class Defend extends ControlPixel {
  
  description.set("Defend a position")
  
  units.get.unitMatcher.set(UnitMatchWarriors)
  positionToControl.set(new TileChoke)
}

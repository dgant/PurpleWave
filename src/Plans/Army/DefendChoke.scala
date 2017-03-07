package Plans.Army

import Startup.With
import Strategies.PositionFinders.PositionChoke
import Strategies.UnitCountEverything
import Strategies.UnitMatchers.UnitMatchWarriors
import bwapi.Color
import Utilities.Enrichment.EnrichPosition._

class DefendChoke extends ControlPosition {
  position.set(new PositionChoke)
  units.get.unitMatcher.set(UnitMatchWarriors)
  units.get.unitCounter.set(UnitCountEverything)
  
  override def drawOverlay() =position.get.find.map(p => With.game.drawCircleMap(p.centerPixel, 64, Color.Green))
}

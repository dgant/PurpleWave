package Plans.Generic.Macro.UnitAtLocation

import Plans.Generic.Compound.AbstractPlanFulfillRequirements
import Startup.With
import Strategies.PositionFinders.{PositionCenter, PositionFinder}
import Strategies.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Traits.Property

class RequireUnitAtLocation extends AbstractPlanFulfillRequirements {
  
  val me = this
  val positionFinder  = new Property[PositionFinder]  (new PositionCenter)
  val unitMatcher     = new Property[UnitMatcher]     (UnitMatchAnything)
  val range           = new Property[Integer]         (32)
  
  checker.set(new PlanCheckUnitAtLocation {
    positionFinder.inherit(me.positionFinder)
    unitMatcher.inherit(me.unitMatcher)
    range.inherit(me.range)
  })
  
  fulfiller.set(new PlanFulfillUnitAtLocation {
    positionFinder.inherit(me.positionFinder)
    unitMatcher.inherit(me.unitMatcher)
  })
  
  override def drawOverlay() = {
    positionFinder.get.find.foreach(position => {
      With.game.drawCircleMap(
        position.toPosition,
        range.get,
        bwapi.Color.Green)
      With.game.drawTextMap(
        position.toPosition,
        "Requiring units")})
  }
}

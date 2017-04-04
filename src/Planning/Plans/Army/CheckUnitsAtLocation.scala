package Planning.Plans.Army

import Planning.Composition.PositionFinders.Generic.PositionMiddle
import Planning.Plan
import Lifecycle.With
import Planning.Composition.PositionFinders.PositionFinder
import Planning.Composition.Property
import Planning.Composition.UnitMatchers.{UnitMatchAnything, UnitMatcher}

class CheckUnitsAtLocation extends Plan {
  
  val quantity        = new Property[Int](1)
  val positionFinder  = new Property[PositionFinder](PositionMiddle)
  val unitMatcher     = new Property[UnitMatcher](UnitMatchAnything)
  val range           = new Property[Int](32)
  
  override def isComplete: Boolean = {
    With.units.ours
      .filter(unitMatcher.get.accept)
      .filter(unit =>
        positionFinder.get.find.exists(position =>
          position.toPosition.getDistance(unit.pixelCenter) < range.get))
      .size >= quantity.get
  }
}

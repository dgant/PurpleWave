package Plans.Information

import Plans.Generic.Allocation.{LockUnits, LockUnitsExactly}
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.{PositionCenter, PositionFinder, PositionSpecific}
import Strategies.UnitMatchers.{UnitMatchMobile, UnitMatcher}
import Strategies.UnitPreferences.{UnitPreferClose, UnitPreference}
import Types.Property
import bwapi.Position

class KnowEnemyBaseLocationFulfiller extends Plan {
  
  description.set(Some("Discover an enemy base"))
  
  val meKEBLF = this
  val positionFinder = new Property[PositionFinder](new PositionCenter)
  val unitPreference = new Property[UnitPreference](new UnitPreferClose  { positionFinder.inherit(meKEBLF.positionFinder) })
  val unitMatcher    = new Property[UnitMatcher]   (new UnitMatchMobile)
  val unitPlan       = new Property[LockUnits]     (new LockUnitsExactly {
    this.unitPreference.inherit(meKEBLF.unitPreference)
    this.unitMatcher.inherit(meKEBLF.unitMatcher)
  })
  
  override def getChildren: Iterable[Plan] = { List(unitPlan.get) }
  
  override def onFrame() {
    positionFinder.set(new PositionSpecific(_getNextScoutingPosition.toTilePosition))
    unitPlan.get.onFrame()
    unitPlan.get.units.foreach(_orderScout)
  }
  
  def _orderScout(scout:bwapi.Unit) {
    val nextBase = With.scout.unscoutedBases.head
    /*
    if (scout.canGather) {
      val nearestMineral = BWTA.getNearestBaseLocation(_getNextScoutingPosition)
        .getStaticMinerals
        .asScala
        .headOption
      
      if (nearestMineral.isDefined &&
        nextBase.getRegion.getPolygon.isInside(nearestMineral.get.getPosition)) {
          scout.rightClick(nearestMineral.get)
          return
      }
    }
    */
  
    scout.move(nextBase.getPosition)
  }
  
  def _getNextScoutingPosition:Position = {
    With.scout.unscoutedBases.head.getPosition
  }
}

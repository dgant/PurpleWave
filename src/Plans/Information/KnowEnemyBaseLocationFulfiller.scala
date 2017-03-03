package Plans.Information

import Global.Combat.Commands.Control
import Plans.Allocation.{LockUnits, LockUnitsExactly}
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.{PositionCenter, PositionFinder, PositionSpecific}
import Strategies.UnitMatchers.{UnitMatchMobile, UnitMatcher}
import Strategies.UnitPreferences.{UnitPreferClose, UnitPreference}
import Types.Intents.Intention
import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Property
import bwapi.TilePosition

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
    positionFinder.set(new PositionSpecific(_getNextScoutingPosition))
    unitPlan.get.onFrame()
    unitPlan.get.units.foreach(_orderScout)
  }
  
  def _orderScout(scout:FriendlyUnitInfo) {
    With.commander.intend(new Intention(scout, Control, _getNextScoutingPosition))
  }
  
  def _getNextScoutingPosition:TilePosition = {
    With.intelligence.mostUnscoutedBases
      .filter(base => With.paths.exists(With.geography.home, base)) //BWTA.isConnected could also help
      .head
  }
}

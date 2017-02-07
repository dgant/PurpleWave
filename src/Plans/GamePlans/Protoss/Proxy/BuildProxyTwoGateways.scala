package Plans.GamePlans.Protoss.Proxy

import Plans.Generic.Allocation.{LockUnits, LockUnitsExactly}
import Plans.Generic.Compound.AllSerial
import Plans.Generic.Macro.BuildBuilding
import Plans.Generic.Macro.UnitAtLocation.RequireUnitAtLocation
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.{PositionCenter, PositionFinder, PositionProxyArea}
import Strategies.UnitMatchers.{UnitMatchWorker, UnitMatcher}
import Strategies.UnitPreferences.{UnitPreferClose, UnitPreference}
import Traits.Property
import bwapi.UnitType

class BuildProxyTwoGateways extends Plan {
  
  description.set(Some("Build two proxy Gateways"))
  
  val meBPTG = this
  val proxySearchTileRadius = new Property[Integer](Math.max(With.game.mapWidth, With.game.mapHeight) * 3 / 8)
  val positionFinder = new Property[PositionFinder](new PositionCenter)
  val unitMatcher = new Property[UnitMatcher](UnitMatchWorker)
  val unitPreference = new Property[UnitPreference](new UnitPreferClose {
    this.positionFinder.inherit(meBPTG.positionFinder)
  })
  
  val builderPlan = new Property[LockUnits](new LockUnitsExactly {
    this.description.set(Some("Lock a proxy builder"))
    this.unitMatcher.inherit(meBPTG.unitMatcher)
    this.unitPreference.inherit(meBPTG.unitPreference)
  })
  val sendBuilderPlan = new Property[Plan](new RequireUnitAtLocation {
    this.description.set(Some("Send builder to proxy"))
    this.unitPlan.inherit(meBPTG.builderPlan);
    this.positionFinder.set(PositionProxyArea);
    this.range.inherit(proxySearchTileRadius);
  })
  val pylonPlan = new Property[Plan](new BuildBuilding(UnitType.Protoss_Pylon) {
    this.description.set(Some("Build proxy Pylon"))
    this.builderPlan.inherit(meBPTG.builderPlan)
  })
  val gateway1Plan = new Property[Plan](new BuildBuilding(UnitType.Protoss_Gateway) {
    this.description.set(Some("Build first proxy Gateway"))
    this.builderPlan.inherit(meBPTG.builderPlan)
  })
  val gateway2Plan = new Property[Plan](new BuildBuilding(UnitType.Protoss_Gateway) {
    this.description.set(Some("Build second proxy Gateway"))
    this.builderPlan.inherit(meBPTG.builderPlan)
  })
  
  val _child = new AllSerial
  
  override def isComplete: Boolean = {
    _updateChildren()
    _child.isComplete
  }
  
  override def getChildren:Iterable[Plan] = {
    _updateChildren()
    List(_child)
  }
  
  override def onFrame() {
    _updateChildren()
    _child.onFrame()
  }
  
  def _updateChildren() {
    _child.children.set(List(
      builderPlan.get,
      sendBuilderPlan.get,
      pylonPlan.get,
      gateway1Plan.get,
      gateway2Plan.get
    ))
  }
}

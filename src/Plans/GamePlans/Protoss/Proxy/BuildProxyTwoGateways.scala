package Plans.GamePlans.Protoss.Proxy

import Utilities.Property
import Plans.Allocation.{LockUnits, LockUnitsExactly}
import Plans.Army.UnitAtLocation.RequireUnitAtLocation
import Plans.Compound.{AllSerial, CompleteOnce}
import Plans.Macro.Build.BuildBuilding
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.{PositionCenter, PositionFinder, PositionSpecific}
import Strategies.UnitMatchers.{UnitMatchWorker, UnitMatcher}
import Strategies.UnitPreferences.{UnitPreferClose, UnitPreference}
import Utilities.Caching.Cache
import bwapi.{TilePosition, UnitType}

class BuildProxyTwoGateways extends Plan {
  
  description.set(Some("Build two proxy Gateways"))
  
  val _proxyPositionCache = new Cache[Iterable[TilePosition]](24 * 15, () =>  _proxyPositions)
  val meBPTG = this
  val proxyWorkerSpotRadius = new Property[Int](32 * 6)
  val proxySearchTileRadius = new Property[Int](Math.max(With.game.mapWidth, With.game.mapHeight) * 3 / 8)
  val positionFinder        = new Property[PositionFinder](new PositionCenter)
  val unitMatcher           = new Property[UnitMatcher](UnitMatchWorker)
  val unitPreference        = new Property[UnitPreference](new UnitPreferClose {
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
    this.positionFinder.set(new PositionSpecific(_proxyPositionCache.get.head))
    this.range.inherit(proxyWorkerSpotRadius);
  })
  val pylonPlan = new Property[Plan](new BuildBuilding(UnitType.Protoss_Pylon) {
    this.description.set(Some("Build proxy Pylon"))
    this.builderPlan.inherit(meBPTG.builderPlan)
    this.positionFinder.set(new PositionSpecific(_proxyPositionCache.get.head))
  })
  val gateway1Plan = new Property[Plan](new BuildBuilding(UnitType.Protoss_Gateway) {
    this.description.set(Some("Build first proxy Gateway"))
    this.builderPlan.inherit(meBPTG.builderPlan)
    this.positionFinder.set(new PositionSpecific(_proxyPositionCache.get.drop(1).head))
  })
  val gateway2Plan = new Property[BuildBuilding](new BuildBuilding(UnitType.Protoss_Gateway) {
    this.description.set(Some("Build second proxy Gateway"))
    this.builderPlan.inherit(meBPTG.builderPlan)
    this.positionFinder.set(new PositionSpecific(_proxyPositionCache.get.drop(2).head))
  })
  
  val _completeSendBuilderOnce = new CompleteOnce {
    child.inherit(sendBuilderPlan)
  }
  
  val _child = new AllSerial
  
  override def isComplete: Boolean = {
    _updateGrandchildren()
    _child.isComplete
  }
  
  override def getChildren:Iterable[Plan] = {
    _updateGrandchildren()
    List(_child)
  }
  
  override def onFrame() {
    _updateGrandchildren()
    if ( ! _child.isComplete) {
      _child.onFrame()
    }
  }
  
  def _updateGrandchildren() {
    _child.children.set(List(
      builderPlan.get,
      _completeSendBuilderOnce,
      sendBuilderPlan.get,
      pylonPlan.get,
      gateway1Plan.get,
      gateway2Plan.get
    ))
  }
  
  def _proxyPositions:Iterable[TilePosition] = {
  
    val buildings = List(UnitType.Protoss_Pylon, UnitType.Protoss_Gateway, UnitType.Protoss_Gateway)
    val center = new PositionCenter().find.get
    
    With.logger.debug("Going to try to place multiple buildings. Good luck.")
    With.logger.debug("Geography: " + With.game.mapName + "(" + With.game.mapFileName() + ")")
    With.logger.debug("Building types: " + buildings.map(_.toString).mkString(", "))
    With.logger.debug("Centerpoint: " + center.toString)
    
    With.architect.placeBuildings(
      buildings,
      center,
      0,
      50)
    .get
  }
}

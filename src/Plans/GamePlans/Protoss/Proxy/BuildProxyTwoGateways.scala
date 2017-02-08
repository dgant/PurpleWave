package Plans.GamePlans.Protoss.Proxy

import Caching.Cache
import Development.Logger
import Plans.Generic.Allocation.{LockUnits, LockUnitsExactly}
import Plans.Generic.Compound.{AllSerial, CompleteOnce}
import Plans.Generic.Macro.BuildBuilding
import Plans.Generic.Macro.UnitAtLocation.RequireUnitAtLocation
import Plans.Information.RequireEnemyBaseLocation
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.{PositionCenter, PositionFinder, PositionSpecific}
import Strategies.UnitMatchers.{UnitMatchWorker, UnitMatcher}
import Strategies.UnitPreferences.{UnitPreferClose, UnitPreference}
import Types.Property
import bwapi.{TilePosition, UnitType}

class BuildProxyTwoGateways extends Plan {
  
  description.set(Some("Build two proxy Gateways"))
  
  val _proxyPositions = new Cache[Iterable[TilePosition]] {
    duration = 24 * 15
    override def recalculate: Iterable[TilePosition] = _getProxyPositions
  }
  
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
    this.positionFinder.set(new PositionSpecific(_proxyPositions.get.head))
    this.range.inherit(proxySearchTileRadius);
  })
  val pylonPlan = new Property[Plan](new BuildBuilding(UnitType.Protoss_Pylon) {
    this.description.set(Some("Build proxy Pylon"))
    this.builderPlan.inherit(meBPTG.builderPlan)
    this.positionFinder.set(new PositionSpecific(_proxyPositions.get.head))
  })
  val gateway1Plan = new Property[Plan](new BuildBuilding(UnitType.Protoss_Gateway) {
    this.description.set(Some("Build first proxy Gateway"))
    this.builderPlan.inherit(meBPTG.builderPlan)
    this.positionFinder.set(new PositionSpecific(_proxyPositions.get.drop(1).head))
  })
  val gateway2Plan = new Property[BuildBuilding](new BuildBuilding(UnitType.Protoss_Gateway) {
    this.description.set(Some("Build second proxy Gateway"))
    this.builderPlan.inherit(meBPTG.builderPlan)
    this.positionFinder.set(new PositionSpecific(_proxyPositions.get.drop(2).head))
  })
  val scoutingPlan = new Property[Plan](new RequireEnemyBaseLocation {
    this.scoutPlan.inherit(meBPTG.builderPlan)
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
    List(_child, scoutingPlan.get)
  }
  
  override def onFrame() {
    _updateGrandchildren()
    if ( ! _child.isComplete) {
      _child.onFrame()
    }
    if (gateway2Plan.get.startedBuilding) {
      scoutingPlan.get.onFrame()
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
  
  def _getProxyPositions:Iterable[TilePosition] = {
  
    val buildings = List(UnitType.Protoss_Pylon, UnitType.Protoss_Gateway, UnitType.Protoss_Gateway)
    val center = new PositionCenter().find.get
    
    Logger.debug("Going to try to place multiple buildings. Good luck.")
    Logger.debug("Map: " + With.game.mapName + "(" + With.game.mapFileName() + ")")
    Logger.debug("Building types: " + buildings.map(_.toString).mkString(", "))
    Logger.debug("Centerpoint: " + center.toString)
    
    With.architect.placeBuildings(
      buildings,
      center,
      0,
      50)
    .get
  }
}

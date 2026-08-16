package Gameplans.Protoss.PvE

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Placement.Access.PlaceLabels.{Proxy2Gate, Proxy4Gate}
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvEProxy2Gate
import Utilities.Time.Minutes

class PvEProxy2Gate extends GameplanImperative {

  override def activated: Boolean = PvEProxy2Gate()
  override def completed: Boolean = With.frame > Minutes(8)()

  override def executeBuild(): Unit = {
    once(8, Protoss.Probe)
    val enemyMains = With.geography.mains.filterNot(_.isOurMain)
    get(1, Protoss.Pylon, new PlacementQuery(Protoss.Pylon)
      .requireLabelYes(Proxy2Gate)
      .requireZone(With.geography.zones.filter(_.heart.groundTiles(With.geography.home) > 40): _*)
      .preferBase(enemyMains: _*)
      .preferZone(With.geography.mains.filterNot(_.isOurMain).map(_.zone): _*))
    once(9, Protoss.Probe)
    get(2, Protoss.Gateway)
  }

  override def doWorkers(): Unit = {}

  doAutosupply = false

  override def executeMain(): Unit = {
    if (unitsEver(Protoss.Gateway) < 2) {
      return
    }
    attack()
    if (armySupply200 >= 20) {
      allIn()
    }
    if (have(Protoss.CyberneticsCore)) {
      gasLimitCeiling(200)
      pumpWorkers()
      requireMiningBases(1)
      get(Protoss.DragoonRange)
      pump(Protoss.Dragoon)
      pump(Protoss.Zealot)
    } else {
      gasWorkerCeiling(1)
      pump(Protoss.Zealot)
      autosupply()
      pumpWorkers()
    }
    if ( ! With.tactics.workerScout.scoutingAbandoned) {
      //microSlush(100)
    }
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
    get(4, Protoss.Gateway, new PlacementQuery(Protoss.Gateway).preferLabelYes(Proxy4Gate))
  }
}

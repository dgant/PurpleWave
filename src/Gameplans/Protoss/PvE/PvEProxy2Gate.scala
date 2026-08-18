package Gameplans.Protoss.PvE

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Placement.Access.PlaceLabels.{Proxy2Gate, Proxy4Gate}
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvEProxy2Gate
import Utilities.?
import Utilities.Time.Minutes

class PvEProxy2Gate extends GameplanImperative {

  override def activated: Boolean = PvEProxy2Gate()
  override def completed: Boolean = With.frame > Minutes(8)()

  override def executeBuild(): Unit = {
    scoutOn(Protoss.Gateway, quantity = 2)

    once(8, Protoss.Probe)
    val enemyMains = With.geography.mains.filterNot(_.isOurMain)
    get(1, Protoss.Pylon, new PlacementQuery(Protoss.Pylon)
      .requireLabelYes(Proxy2Gate)
      .requireZone(With.geography.zones.filter(_.heart.groundTiles(With.geography.home) > 40): _*)
      .preferBase(enemyMains: _*)
      .preferZone(With.geography.mains.filterNot(_.isOurMain).map(_.zone): _*))
    once(9, Protoss.Probe)
    get(2, Protoss.Gateway)
    once(12, Protoss.Probe)

    if (unitsEver(Protoss.Gateway) < 2) {
      return
    }
    once(1, Protoss.Zealot)
    get(2, Protoss.Pylon)
    once(3, Protoss.Zealot)
  }

  doAutosupply = false

  override def doWorkers(): Unit = {}

  override def executeMain(): Unit = {
    attack()
    if (With.units.existsEverOurs(u => Protoss.Assimilator(u) && u.base.exists(_.isOurs))) {
      gasWorkerCeiling(?(units(Protoss.Gateway) >= 4, 3, 2))
      gasLimitCeiling(200)
      requireMiningBases(1)
      slushOrPylons()
      pumpWorkers()
      get(Protoss.CyberneticsCore)
      get(Protoss.DragoonRange)
      pump(Protoss.Dragoon)
      get(4, Protoss.Gateway, new PlacementQuery(Protoss.Gateway).preferLabelYes(Proxy4Gate))
      pump(Protoss.Zealot)
      doAutosupply = true
    } else {
      once(7, Protoss.Zealot)
      pumpWorkers()
      slushOrPylons()
    }

    get(1, Protoss.Assimilator, new PlacementQuery(Protoss.Assimilator).requireBase(With.geography.ourMain))
    get(Protoss.CyberneticsCore)
  }

  def slushOrPylons(): Unit = {
    if ((enemyIsTerran || enemyIsProtoss)
      && ! With.tactics.workerScout.scoutingAbandoned
      && With.tactics.workerScout.scouts.exists(_.base.exists(_.isEnemy)) && units(Protoss.Gateway) >= 2) {
      microSlush(100)
    } else {
      autosupply()
    }
  }
}

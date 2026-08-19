package Gameplans.Protoss.PvE

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Placement.Access.PlaceLabels.{DefendEntrance, DefendGround, Defensive, Proxy2Gate, Proxy4Gate}
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
    once(11, Protoss.Probe)
    once(1, Protoss.Zealot)
    once(2, Protoss.Zealot)
    get(2, Protoss.Pylon)
    once(3, Protoss.Zealot)
    once(12, Protoss.Probe)
  }

  doAutosupply = false

  override def doWorkers(): Unit = {}

  override def executeMain(): Unit = {
    attack()
    gasWorkerCeiling(?(units(Protoss.Gateway) >= 4, 3, 2))
    gasLimitCeiling(200)
    requireMiningBases(1)
    if (have(Protoss.CyberneticsCore)) {
      slushOrPylons()
      pumpWorkers()
      get(Protoss.DragoonRange)
      pump(Protoss.Dragoon)
      pump(Protoss.Zealot)
      get(4, Protoss.Gateway, new PlacementQuery(Protoss.Gateway).preferLabelYes(Proxy4Gate))
    } else {
      pump(Protoss.Zealot)
      pumpWorkers()
      slushOrPylons()
    }
    if (enemyIsZerg) {
      get(5, Protoss.Gateway)
    } else {
      get(1, Protoss.Assimilator, new PlacementQuery(Protoss.Assimilator).requireBase(With.geography.ourMain))
      get(Protoss.CyberneticsCore)
    }
  }

  def slushOrPylons(): Unit = {
    if (With.tactics.workerScout.scoutingAbandoned || ( ! enemyIsProtoss && ! enemyIsTerran)) {
      autosupply()
      return
    }
    if (foundEnemyBase && ! With.tactics.workerScout.scoutingAbandoned) {
      val bases = With.tactics.workerScout.units.flatMap(_.base)
      (1 to 2).foreach(n => {
        get(1, Protoss.Pylon,         new PlacementQuery(Protoss.Pylon)         .requireBase(bases: _*) .preferLabelYes(Defensive, DefendGround, DefendEntrance))
        get(n, Protoss.ShieldBattery, new PlacementQuery(Protoss.ShieldBattery) .requireBase(bases: _*) .preferLabelYes(Defensive, DefendGround, DefendEntrance))
      })
    }
    if (With.tactics.workerScout.scouts.exists(_.base.exists(_.isEnemy))) {
      microSlush(100)
    } else {
      autosupply()
    }
  }
}

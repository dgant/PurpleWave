package Gameplans.Protoss.PvE

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvECannonRushMaximum
import Utilities.?
import Utilities.Time.Minutes
import Utilities.UnitFilters.{IsBuilding, IsWarrior}

class PvECannonRushMaximum extends GameplanImperative {

  override def activated: Boolean = PvECannonRushMaximum()
  override def completed: Boolean = With.frame > Minutes(?(canRush, 10, 4))()

  var canRush: Boolean = false

  override def executeBuild(): Unit = {

    canRush = have(Protoss.Forge) && foundEnemyBase && ! enemyHasTech(Terran.SiegeMode) && ! enemiesHave(Protoss.Reaver)

    once(8, Protoss.Probe)
    get(1, Protoss.Pylon)
    once(11, Protoss.Probe)
    get(1, Protoss.Forge)
    once(12, Protoss.Probe)

    if ((enemiesHave(IsBuilding) || foundEnemyBase) && ! haveEver(u => Protoss.PhotonCannon(u) && u.proxied)) {
      microSlush(300)
    }
  }

  override def doWorkers(): Unit = pumpWorkers(oversaturate = true)

  override def executeMain(): Unit = {
    if (confidenceAttacking11 > 0) {
      attack()
    }
    doAutosupply = false

    pump(Protoss.Dragoon)
    pump(Protoss.Zealot)
    buildCannonsAtOpenings(Math.min(6, With.units.enemy.count(u => IsWarrior(u) && ! u.base.exists(_.isEnemy) && ( ! u.visible || u.proximity > 0.6))))

    // MicroSlush comes here so we keep pumping workers
    if (canRush) {
      With.tactics.workerScout.lock.setInterruptible(true)
      With.blackboard.cannonRushMaximum.set(true)
      microSlush(600)
    } else {
      if (With.geography.startLocations.length == 2) {
        scoutOn(Protoss.Pylon)
        scoutOn(Protoss.Forge, scouts = 2)
      } else if (With.geography.startLocations.length == 2) {
        scoutOn(Protoss.Pylon, scouts = 2)
      } else {
        scoutAt(6)
        scoutOn(Protoss.Probe, 9, scouts = 2)
      }
      microSlush(100)
    }

    val gatewayPlacement =
      ?(canRush,
        new PlacementQuery(Protoss.Gateway).preferZone(With.scouting.enemyHome.zone).preferBase(With.geography.enemyBases: _*),
        new PlacementQuery(Protoss.Gateway))

    get(Protoss.Gateway)
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
    get(Protoss.DragoonRange)
    get(3 * miningBases, Protoss.Gateway, gatewayPlacement)
    expandOnce()
    get(2, Protoss.Pylon, new PlacementQuery(Protoss.Pylon).requireBase(With.geography.ourBases.filter(_.isMain): _*))
    expandOnce()
    pumpGasPumps()
  }
}

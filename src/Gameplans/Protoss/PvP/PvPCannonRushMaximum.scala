package Gameplans.Protoss.PvP

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPCannonRushMaximum
import Utilities.Time.Minutes
import Utilities.UnitFilters.IsWarrior

class PvPCannonRushMaximum extends GameplanImperative {

  override def activated: Boolean = PvPCannonRushMaximum()
  override def completed: Boolean = With.frame > Minutes(12)()

  override def executeBuild(): Unit = {

    once(8, Protoss.Probe)
    get(1, Protoss.Pylon)
    once(11, Protoss.Probe)
    get(1, Protoss.Forge)
    if (foundEnemyBase && ! haveEver(u => Protoss.PhotonCannon(u) && u.proxied)) {
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
    buildCannonsAtOpenings(Math.min(3, With.units.enemy.count(u => IsWarrior(u) && ! u.base.exists(_.isEnemy) && ( ! u.visible || u.proximity > 0.6))))

    // MicroSlush comes here so we keep pumping workers
    if (foundEnemyBase) {
      With.blackboard.cannonRushMaximum.set(true)
      microSlush(600)
    } else {
      scoutOn(Protoss.Pylon)
      if ( ! foundEnemyBase && With.geography.startLocations.length >= 4) {
        scoutOn(Protoss.Forge, 1, scouts = 2)
      }
      microSlush(100)
    }

    With.geography.ourBases.foreach(base => get(1, Protoss.Pylon, new PlacementQuery(Protoss.Pylon).requireBase(base)))
    val gatewayPlacement = new PlacementQuery(Protoss.Gateway).preferZone(With.scouting.enemyHome.zone).preferBase(With.geography.enemyBases: _*)


    get(3, Protoss.Gateway, gatewayPlacement)
    expandOnce()
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
    get(Protoss.DragoonRange)
    get(3 * miningBases, Protoss.Gateway, gatewayPlacement)
    expandOnce()
    pumpGasPumps()
  }
}

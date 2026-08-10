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
  override def completed: Boolean = With.frame > Minutes(10)()

  override def executeBuild(): Unit = {

    once(8, Protoss.Probe)
    get(1, Protoss.Pylon)
    once(11, Protoss.Probe)
    get(1, Protoss.Forge)
    once(12, Protoss.Probe)
  }

  override def executeMain(): Unit = {
    if (confidenceAttacking11 > 0) {
      attack()
    }
    doAutosupply = false

    buildCannonsAtOpenings(Math.min(3, With.units.enemy.count(u => IsWarrior(u) && ! u.base.exists(_.isEnemy) && ( ! u.visible || u.proximity > 0.6))))

    // MicroSlush comes here so we keep pumping workers
    once(13, Protoss.Probe)
    if (foundEnemyBase) {
      With.blackboard.cannonRushMaximum.set(true)
      microSlush(550)
    } else {
      scoutOn(Protoss.Pylon)
      if ( ! foundEnemyBase && With.geography.startLocations.length >= 4) {
        scoutOn(Protoss.Forge, 1, scouts = 2)
      }
      microSlush(100)
    }

    With.geography.ourBases.foreach(base => get(1, Protoss.Pylon, new PlacementQuery(Protoss.Pylon).requireBase(base)))
    get(Protoss.Gateway)
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
    pump(Protoss.Dragoon)
    get(Protoss.DragoonRange)
    get(3 * miningBases, Protoss.Gateway)
    expandOnce()
    pumpGasPumps()
  }
}

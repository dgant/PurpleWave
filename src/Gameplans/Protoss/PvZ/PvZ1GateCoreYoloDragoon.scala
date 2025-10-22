package Gameplans.Protoss.PvZ

import Gameplans.All.GameplanImperative
import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.UnitFilters.IsWarrior

class PvZ1GateCoreYoloDragoon extends GameplanImperative {

  override def executeBuild(): Unit = {
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(9, Protoss.Probe)
    once(Protoss.Gateway)
    once(11, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(12, Protoss.Probe)
    once(Protoss.Zealot)
    once(13, Protoss.Probe)
    once(Protoss.Assimilator)
    once(14, Protoss.Probe)
    once(2, Protoss.Zealot)
    once(15, Protoss.Probe)
    once(1, Protoss.CyberneticsCore)
    once(16, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(17, Protoss.Probe)
    once(3, Protoss.Pylon)
    once(Protoss.Dragoon)

    scoutOn(Protoss.Pylon)
  }

  override def executeMain(): Unit = {

    //////////
    // Army //
    //////////

    var shouldAttack: Boolean = false
    shouldAttack ||= enemyLurkersLikely
    shouldAttack ||= safePushing
    attack(shouldAttack)
    With.blackboard.acePilots.set( ! enemyHasShown(Zerg.Mutalisk))
    if (enemyHasShown(Zerg.Mutalisk)) {
      allIn()
    }

    /////////////
    // Economy //
    /////////////

    maintainMiningBases(1)
    requireMiningBases(Math.min(3, unitsComplete(IsWarrior) / 40))

    ///////////////////
    // High priority //
    ///////////////////

    if (enemyLurkersLikely) {
      buildCannonsAtOpenings(1)
      get(Protoss.RoboticsFacility, Protoss.Observatory)
      pump(Protoss.Observer, 2)
    }
    get(Protoss.DragoonRange)
    pump(Protoss.Dragoon)
    pumpGasPumps()
    pump(Protoss.Zealot)

    //////////////////
    // Low priority //
    //////////////////

    get(Protoss.DragoonRange)
    get(4 * miningBases, Protoss.Gateway)
    expandOnce()
  }
}

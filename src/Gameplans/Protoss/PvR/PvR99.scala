package Gameplans.Protoss.PvR

import Gameplans.All.GameplanImperative
import Placement.Access.PlaceLabels.{DefendEntrance, DefendGround}
import ProxyBwapi.Races.{Protoss, Zerg}

class PvR99 extends GameplanImperative {

  override def executeBuild(): Unit = {
    once(9, Protoss.Probe)
    once(Protoss.Gateway)
    once(10, Protoss.Probe)
    once(2, Protoss.Gateway)
    once(11, Protoss.Probe)
    once(Protoss.Zealot)
    once(2, Protoss.Pylon)
    once(2, Protoss.Zealot)
    once(12, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(13, Protoss.Probe)
    once(4, Protoss.Zealot)
    once(14, Protoss.Probe)
    once(3, Protoss.Pylon)
    once(15, Protoss.Probe)
    once(5, Protoss.Zealot)

    if ( ! foundEnemyBase) {
      scoutOn(Protoss.Zealot, 3)
    }

    if (enemyShownCloakedThreat) {
      get(Protoss.RoboticsFacility, Protoss.Observatory, Protoss.Observer)
      buildCannonsAtOpenings(2, DefendEntrance, DefendGround)
      pump(Protoss.Observer, 2)
    }
  }
  override def executeMain(): Unit = {

    if (safePushing
      && armySupply200 >= 6
      && (foundEnemyBase || armySupply200 >= 10)
      && ( ! enemiesHave(Protoss.DarkTemplar, Zerg.Lurker) || haveComplete(Protoss.Observer))) {
      attack()
    }

    maintainMiningBases(armySupply200 / 12)

    once(2, Protoss.DarkTemplar)
    pump(Protoss.DarkTemplar, 1)
    pump(Protoss.Observer, 1)
    get(Protoss.DragoonRange)
    if (upgradeComplete(Protoss.ZealotSpeed, 1, Protoss.Zealot.buildFrames)) {
      pump(Protoss.Zealot, units(Protoss.Dragoon) / 3)
    }
    if ( ! enemyMutalisksLikely || units(Protoss.Dragoon) > 12) {
      pumpShuttleAndReavers(4)
    }
    pump(Protoss.Dragoon)
    pump(Protoss.Zealot)
    get(Protoss.CyberneticsCore)
    get(Protoss.Assimilator)
    get(Protoss.CitadelOfAdun)
    get(Protoss.TemplarArchives)
    get(3, Protoss.Gateway)
    get(Protoss.ZealotSpeed)
    requireMiningBases(2)
    get(5, Protoss.Gateway)
    get(2, Protoss.Assimilator)
    get(Protoss.RoboticsFacility)
    get(Protoss.Observatory)
    get(Protoss.RoboticsSupportBay)
    get(Protoss.ShuttleSpeed)
    requireMiningBases(3)
    get(Protoss.Forge)
    upgradeContinuously(Protoss.GroundDamage) && upgradeContinuously(Protoss.Shields)
    get(7, Protoss.Gateway)
    get(2, Protoss.RoboticsFacility)
    requireMiningBases(4)
    get(20, Protoss.Gateway)
  }
}

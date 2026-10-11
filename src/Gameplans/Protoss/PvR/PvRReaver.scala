package Gameplans.Protoss.PvR

import Gameplans.All.GameplanImperative
import Gameplans.Protoss.PvP.PvPLateGame
import Lifecycle.With
import Placement.Access.PlaceLabels
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.?
import Utilities.Time.Minutes
import Utilities.UnitFilters.IsWarrior

class PvRReaver extends GameplanImperative {

  override def executeBuild(): Unit = {
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    once(11, Protoss.Probe)
    once(Protoss.Assimilator)
    once(13, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    scoutOn(Protoss.CyberneticsCore)
    once(14, Protoss.Probe)
    once(Protoss.Zealot)
    once(2,  Protoss.Pylon)
    once(15, Protoss.Probe)
    once(Protoss.Dragoon)
    once(16, Protoss.Probe)
    if (enemyIsZerg)  {
      once(Protoss.Forge)
      once(18, Protoss.Probe)
      buildCannonsAtMain(2, PlaceLabels.DefendEntrance)
      once(20, Protoss.Probe)
      if ( ! haveEver(Protoss.Stargate)) {
        cancel(Protoss.RoboticsFacility)
      }
      get(Protoss.Stargate)
      once(22, Protoss.Probe)
      once(Protoss.Corsair)
      once(2, Protoss.Zealot)
      once(3, Protoss.Pylon)
      once(23, Protoss.Probe)
      once(Protoss.RoboticsFacility)
      once(25, Protoss.Probe)
      once(3, Protoss.Zealot)
      once(Protoss.RoboticsSupportBay)
      once(Protoss.Shuttle)
      once(4, Protoss.Pylon)
      once(27, Protoss.Probe)
      get(Protoss.ShuttleSpeed)
      once(Protoss.Reaver)
      once(4, Protoss.Zealot)
    } else {
      once(Protoss.RoboticsFacility)
      once(18, Protoss.Probe)
      once(2, Protoss.Dragoon)
      once(3, Protoss.Pylon)
      once(20, Protoss.Probe)
      if (enemyIsTerran) {
        once(2, Protoss.Zealot)
      } else {
        once(3, Protoss.Dragoon)
      }
      once(Protoss.RoboticsSupportBay)
      once(Protoss.Shuttle)
      once(4, Protoss.Pylon)
      once(22, Protoss.Probe)
      once(Protoss.ShuttleSpeed)
      once(Protoss.Reaver)
      once(4, Protoss.Dragoon)
      once(23, Protoss.Probe)
      get(Protoss.DragoonRange)
    }
  }

  override def doWorkers(): Unit = {
    pumpWorkers(maximumConcurrently = 2, oversaturate = true)
  }

  var shouldHarass: Boolean = false
  override def executeMain(): Unit = {
    if (enemyIsTerran) terranMain()
    if (enemyIsProtoss) protossMain()
    if (enemyIsZerg) zergMain()
    With.blackboard.reaverDrop.set(With.blackboard.wantToHarass())
    With.blackboard.reaverDropMaxReavers.set(Math.max(1, unitsComplete(Protoss.Reaver) / 2))
  }

  def terranMain(): Unit = {
    val needDetection = enemyHasTech(Terran.SpiderMinePlant, Terran.WraithCloak)
    harass(enemyBases > 1 || With.scouting.ourProximity < 0.6 || (unitsComplete(Protoss.Reaver) >= 3 && unitsComplete(Protoss.Shuttle) >= 2))
    attack(safeSkirmishing && Protoss.DragoonRange())
    pump(Protoss.Observer, ?(needDetection, 2, 1))
    pumpShuttleAndReavers(shuttleFirst = true)
    pump(Protoss.Dragoon, enemies(Terran.Wraith))
    requireMiningBases(enemyBases + unitsComplete(IsWarrior) / 12)
    if (needDetection && unitsEver(Protoss.Reaver) >= 3 && unitsEver(Protoss.Shuttle) >= 2) {
      get(Protoss.Observatory)
    }
    pump(Protoss.Dragoon)
    get(Protoss.DragoonRange)
    get(Protoss.RoboticsSupportBay)
    get(Protoss.ShuttleSpeed)
    get(miningBases, Protoss.RoboticsFacility)
    pumpGasPumps()
    get(miningBases * 3, Protoss.Gateway)
    get(Protoss.Observatory)
    pump(Protoss.Zealot)
  }


  lazy val pvpLateGame = new PvPLateGame
  def protossMain(): Unit = {
    if (With.frame > Minutes(10)()) {
      harass(true)
      pvpLateGame.executeMain()
      return
    }
    var shouldAttack: Boolean =false
    shouldAttack ||= enemyIsZerg && haveEverComplete(Protoss.Reaver)
    shouldAttack ||= Protoss.DragoonRange() && unitsComplete(IsWarrior) >= 8
    shouldAttack &&= ! (enemiesHave(Protoss.DarkTemplar, Zerg.Lurker) || enemyHasShown(Terran.SpiderMine)) || haveComplete(Protoss.Observer)
    shouldAttack &&= safePushing
    shouldHarass = true
    harass(shouldHarass)
    attack(shouldAttack)
    maintainMiningBases(unitsComplete(IsWarrior) / 6)
    pump(Protoss.Observer, 1)
    pumpShuttleAndReavers(shuttleFirst = false)
    pumpGasPumps()
    if (safePushing && upgradeComplete(Protoss.DragoonRange)) {
      requireMiningBases(2)
    }
    upgradeContinuously(Protoss.GroundArmor) && upgradeContinuously(Protoss.GroundDamage)
    pump(Protoss.Dragoon)
    pump(Protoss.Zealot)
    get(3, Protoss.Gateway)
    requireMiningBases(2)
    get(2, Protoss.RoboticsFacility)
    get(Protoss.Forge)
    get(Protoss.Observatory)
    get(Protoss.ScarabDamage)
    get(Protoss.GroundArmor)
    get(Protoss.GroundDamage)
    get(Protoss.CitadelOfAdun)
    get(Protoss.ZealotSpeed)
    get(Protoss.TemplarArchives)
    get(6, Protoss.Gateway)
    pump(Protoss.Zealot)
  }

  def zergMain(): Unit = {
    harass(true)
    attack(safePushing && (unitsComplete(Protoss.Reaver) >= 2 || enemyMutalisksLikely))
    once(2, Protoss.Reaver)
    pump(Protoss.Dragoon, 3 * enemies(Zerg.Mutalisk) / 2)
    pumpShuttleAndReavers(shuttleFirst = false)
    if (haveComplete(Protoss.Reaver)) {
      pump(Protoss.Dragoon)
    }
    pump(Protoss.Zealot, 3)
    pump(Protoss.Dragoon)
    get(Protoss.RoboticsFacility, Protoss.RoboticsSupportBay)
    get(Protoss.DragoonRange)
    get(3, Protoss.Gateway)
    get(Protoss.ShuttleSpeed)
    pumpGasPumps()
  }
}

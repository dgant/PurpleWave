package Gameplans.Protoss.PvR

import Gameplans.All.GameplanImperative
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Utilities.UnitFilters.IsWarrior

class PvRReaver extends GameplanImperative{

  override def executeBuild(): Unit = {
    once(8,  Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    once(12, Protoss.Probe)
    once(Protoss.Assimilator)
    once(13, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    scoutOn(Protoss.CyberneticsCore)
    once(14, Protoss.Probe)
    once(Protoss.Zealot)
    once(2,  Protoss.Pylon)
    once(16, Protoss.Probe)
    once(Protoss.Dragoon)
    once(17, Protoss.Probe)
    once(Protoss.RoboticsFacility)
    once(18, Protoss.Probe)
    once(2,  Protoss.Dragoon)
    once(3,  Protoss.Pylon)
    once(19, Protoss.Probe)
    once(3,  Protoss.Dragoon)
    once(Protoss.Shuttle)
    once(Protoss.RoboticsSupportBay)
    once(21, Protoss.Probe)
    once(Protoss.Reaver)
    once(Protoss.ShuttleSpeed)
    once(Protoss.DragoonRange)
    once(2, Protoss.Reaver)
  }
  var shouldHarass: Boolean = false
  override def executeMain(): Unit = {
    With.blackboard.takeUnownedBases.set(false)
    var shouldAttack: Boolean = unitsEverComplete(IsWarrior) >= 12 && unitsEverComplete(Protoss.Observer) > 0 && safePushing
    shouldHarass = true
    With.blackboard.reaverDrop.set(shouldHarass)
    With.blackboard.reaverDropMaxReavers.set(Math.max(2, unitsComplete(Protoss.Reaver) / 2))
    harass(shouldHarass)
    attack(shouldAttack)
    maintainMiningBases(unitsComplete(IsWarrior) / 6)
    pump(Protoss.Observer, 1)
    pumpShuttleAndReavers()
    if (safePushing && upgradeComplete(Protoss.DragoonRange)) {
      requireMiningBases(2)
    }
    upgradeContinuously(Protoss.GroundArmor) && upgradeContinuously(Protoss.GroundDamage)
    pump(Protoss.Dragoon)
    pump(Protoss.Zealot)
    if (With.geography.ourNatural.isOurs) {
      buildInWall(1, Protoss.Pylon)
      buildInWall(1, Protoss.Gateway)
      buildInWall(1, Protoss.Forge)
      buildInWall(6, Protoss.PhotonCannon)
    }
    get(3, Protoss.Gateway)
    requireMiningBases(2)
    get(2, Protoss.RoboticsFacility)
    pumpGasPumps()
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
}

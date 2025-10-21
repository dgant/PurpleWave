package Gameplans.Protoss.PvZ

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Mathematics.Maff
import Placement.Access.PlaceLabels.DefendAir
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.?

class PvZ1GateCore extends GameplanImperative {

  private def wallPlacement (unitClass: UnitClass)                : PlacementQuery  = new PlacementQuery(unitClass).preferLabelYes().preferLabelNo().preferZone(With.geography.ourFoyer.edges.flatMap(_.zones).distinct: _*).preferBase().preferTile()
  private def requireWall   (quantity: Int, unitClass: UnitClass) : Unit            = { get(quantity, unitClass, wallPlacement(unitClass)  .requireLabelYes(PlaceLabels.Wall)) }
  private def preferWall    (quantity: Int, unitClass: UnitClass) : Unit            = { get(quantity, unitClass, wallPlacement(unitClass)  .preferLabelYes(PlaceLabels.Wall))  }

  override def executeBuild(): Unit = {
    once(8, Protoss.Probe)
    preferWall(1, Protoss.Pylon)
    once(9, Protoss.Probe)
    preferWall(1, Protoss.Gateway)
    once(11, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(12, Protoss.Probe)
    once(Protoss.Zealot)
    once(13, Protoss.Probe)
    once(Protoss.Assimilator)
    once(14, Protoss.Probe)
    once(2, Protoss.Zealot)
    once(15, Protoss.Probe)
    preferWall(1, Protoss.CyberneticsCore)
    once(16, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(17, Protoss.Probe)
    once(3, Protoss.Pylon)

    scoutOn(Protoss.Pylon)
  }

  override def executeMain(): Unit = {

    var shouldAttack: Boolean = false
    if (enemyLurkersLikely) {
      shouldAttack = true
      buildCannonsAtOpenings(1)
      get(Protoss.RoboticsFacility, Protoss.Observatory)
      pump(Protoss.Observer, 2)
    }
    shouldAttack ||= enemyMutalisksLikely &&  ! enemiesHave(Zerg.Mutalisk)
    shouldAttack ||= safePushing && confidenceAttacking01 > 0.6
    shouldAttack ||= miningBases < 1
    attack(shouldAttack)
    With.blackboard.acePilots.set(true)

    maintainMiningBases(1)

    if ( ! haveEver(Protoss.CyberneticsCore)) {
      gasWorkerCeiling(1)
    }

    if (enemyLurkersLikely) {
      buildCannonsAtOpenings(1)
      get(Protoss.RoboticsFacility, Protoss.Observatory)
      pump(Protoss.Observer, 2)
      if (safeDefending) {
        get(Protoss.ObserverSpeed)
        get(Protoss.ObserverVisionRange)
      }
    }
    pump(Protoss.Corsair, enemies(Zerg.Mutalisk))
    if (have(Protoss.Dragoon)) {
      get(Protoss.DragoonRange)
    }
    pump(Protoss.Dragoon, Math.max(0, units(Protoss.Zealot) - 12) + ?(enemyMutalisksLikely, 6 + enemies(Zerg.Mutalisk) - units(Protoss.Corsair), 0))
    if (have(Protoss.Corsair) && upgradeStarted(Protoss.DragoonRange)) {
      get(Protoss.AirArmor)
      get(Protoss.AirDamage)
    }
    pump(Protoss.DarkTemplar, 1)
    val capArchons = units(Protoss.Archon) >= 4
    makeArchons(?(capArchons, 49, 300))
    if (capArchons) {
      get(Protoss.PsionicStorm)
      get(Protoss.HighTemplarEnergy)
      pump(Protoss.Shuttle, 1)
    }
    pump(Protoss.HighTemplar)
    if (enemyMutalisksLikely) {
      buildCannonsAtBases(Maff.clamp(2 + enemies(Zerg.Mutalisk) / 5, 2, 4), DefendAir)
    }
    pump(Protoss.Zealot)

    get(Protoss.Forge)
    get(Protoss.CitadelOfAdun)
    get(Protoss.GroundDamage)
    get(Protoss.ZealotSpeed)
    get(3, Protoss.Gateway)
    requireMiningBases(2)
    get(Protoss.TemplarArchives)
    get(5, Protoss.Gateway)
    pumpGasPumps()
    if (enemyMutalisksLikely) {
      get(Maff.clamp(1 + enemies(Zerg.Mutalisk) / 6, 1, 3), Protoss.Stargate)
    }
    get(2, Protoss.Forge)
    upgradeContinuously(Protoss.GroundArmor)
    upgradeContinuously(Protoss.GroundDamage) && upgradeContinuously(Protoss.Shields)
    get(7, Protoss.Gateway)
    requireMiningBases(3)
    get(14, Protoss.Gateway)
  }
}

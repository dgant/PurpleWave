package Gameplans.Protoss.PvZ

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Macro.Actions.BuildDefense
import Mathematics.Maff
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.?
import Utilities.UnitFilters.IsWarrior

class PvZ1GateCoreExpand extends GameplanImperative {

  private def wallPlacement (unitClass: UnitClass)                : PlacementQuery  = new PlacementQuery(unitClass).preferLabelYes().preferLabelNo().preferZone(With.geography.ourFoyer.edges.flatMap(_.zones).distinct: _*).preferBase().preferTile()
  private def requireWall   (quantity: Int, unitClass: UnitClass) : Unit            = { get(quantity, unitClass, wallPlacement(unitClass)  .requireLabelYes(PlaceLabels.Wall)) }
  private def preferWall    (quantity: Int, unitClass: UnitClass) : Unit            = { get(quantity, unitClass, wallPlacement(unitClass)  .preferLabelYes(PlaceLabels.Wall))  }

  override def executeBuild(): Unit = {
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    //preferWall(1, Protoss.Pylon)
    //preferWall(1, Protoss.Gateway)
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
    With.blackboard.acePilots.set( ! enemyHasShown(Zerg.Mutalisk))

    /////////////
    // Economy //
    /////////////

    maintainMiningBases(1)
    requireMiningBases(Math.min(3, unitsComplete(IsWarrior) / 20))
    if ( ! haveEver(Protoss.CyberneticsCore)) {
      gasWorkerCeiling(1)
    }
    gasLimitCeiling(600)

    ///////////////////
    // High priority //
    ///////////////////

    if (enemyLurkersLikely) {
      buildCannonsAtOpenings(1)
      get(Protoss.RoboticsFacility, Protoss.Observatory)
      pump(Protoss.Observer, 2)
      if (safeDefending) {
        get(Protoss.ObserverSpeed)
        get(Protoss.ObserverVisionRange)
      }
    }
    pump(Protoss.Corsair, Math.max(?(enemyHydralisksLikely, 1, 3), enemies(Zerg.Mutalisk)))
    if (enemyMutalisksLikely) {
      get(Maff.clamp(1 + enemies(Zerg.Mutalisk) / 6, 1, miningBases), Protoss.Stargate)
    }
    if (units(Protoss.Dragoon) > 1) {
      get(Protoss.DragoonRange)
    }
    if (safeDefending && units(IsWarrior) >= 24) {
      get(Protoss.GroundDamage)
      upgradeContinuously(Protoss.GroundArmor)
      upgradeContinuously(Protoss.GroundDamage) && upgradeContinuously(Protoss.Shields)
    }
    pump(Protoss.Dragoon, 2 * units(Protoss.Archon) + units(Protoss.Zealot) / 2 - 6)
    if (have(Protoss.Corsair) && upgradeStarted(Protoss.DragoonRange)) {
      get(Protoss.AirArmor)
      get(Protoss.AirDamage)
    }
    pump(Protoss.DarkTemplar, 1)
    val capArchons = units(Protoss.Archon) >= 3
    makeArchons(?(capArchons, 49, 300))
    if (capArchons) {
      get(Protoss.PsionicStorm)
      get(Protoss.HighTemplarEnergy)
      pump(Protoss.Shuttle, 1)
    }
    pump(Protoss.HighTemplar)
    if (enemyMutalisksLikely) {
      With.geography.ourMiningBases.foreach(base =>
        BuildDefense(
          Maff.clamp(2 + enemies(Zerg.Mutalisk) / 5, 2, 4),
          Protoss.PhotonCannon,
          new PlacementQuery(_)
          .requireBase(base)
          .requireLabelYes(PlaceLabels.DefendHall)
          .preferLabelYes(PlaceLabels.DefendAir)))
    }
    buildCannonsAtExpansions(2)
    pumpGasPumps()
    pump(Protoss.Zealot)

    //////////////////
    // Low priority //
    //////////////////

    get(Protoss.Stargate)
    once(Protoss.Corsair)
    get(Protoss.Forge)
    get(Protoss.CitadelOfAdun)
    get(Protoss.GroundDamage)
    get(Protoss.ZealotSpeed)
    get(Protoss.TemplarArchives)

    requireMiningBases(2)
    get(5, Protoss.Gateway)
    get(2, Protoss.Forge)
    get(7, Protoss.Gateway)
    requireMiningBases(3)
    get(Protoss.RoboticsFacility, Protoss.Observatory)
    get(18, Protoss.Gateway)
  }
}

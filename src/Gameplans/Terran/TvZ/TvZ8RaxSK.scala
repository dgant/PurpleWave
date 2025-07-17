package Gameplans.Terran.TvZ

import Gameplans.Terran.TvE.{BunkerRush, TerranGameplan}
import Lifecycle.With
import Macro.Actions.{Enemy, Friendly}
import Mathematics.Maff
import Placement.Access.PlaceLabels
import ProxyBwapi.Races.{Terran, Zerg}
import Utilities.Time.Minutes
import Utilities.UnitFilters.IsWarrior
import Utilities.{?, SwapIf}

class TvZ8RaxSK extends TerranGameplan {

  override def executeBuild(): Unit = {
    emergencyReactions()

    once(8, Terran.SCV)
    get(Terran.Barracks)
    once(9, Terran.SCV)
    get(Terran.SupplyDepot)
    once(10, Terran.SCV)
    scoutAt(10)
  }

  override def doWorkers(): Unit = {
    if (enemyLurkersLikely) {
      get(Terran.Academy)
    }
    pump(Terran.Comsat)
    pumpWorkers(oversaturate = true, 38)
    pumpWorkers(oversaturate = false)
  }

  def directToVessels(): Unit = {
    get(Terran.Factory, Terran.Starport)
    pumpGasPumps()
    get(2, Terran.Starport)
    get(Terran.ScienceFacility)
    get(2, Terran.ControlTower)
    once(2, Terran.ScienceVessel)
    get(Terran.Irradiate)
    once(4, Terran.ScienceVessel)
  }

  override def executeMain(): Unit = {
    maintainMiningBases(armySupply200 / 30)
    requireMiningBases(armySupply200 / 40)
    With.blackboard.floatableBuildings.set(Vector(Terran.Factory))

    if (units(Terran.Barracks) >= 3 && safeDefending) {
      directToVessels()
    }

    once(2, Terran.Medic)
    once(2, Terran.Firebat)
    once(1, Terran.Wraith)
    SwapIf(
      safeDefending, {
        pump(Terran.Wraith, 1)
        pump(Terran.ScienceVessel, 3)
        pump(Terran.Battlecruiser)
        pumpRatio(Terran.Firebat, 0, 4, Seq(Enemy(Zerg.Zergling, 0.125), Friendly(Terran.Dropship, 1.0)))
        pumpRatio(Terran.Medic, 0, 12, Seq(Friendly(Terran.Marine, 0.25), Friendly(Terran.Firebat, 0.25)))
        pump(Terran.ScienceVessel)
        pump(Terran.Marine)
      }, {
        upgradeContinuously(Terran.BioDamage)
        upgradeContinuously(Terran.BioArmor)
        get(Terran.Irradiate)
        get(Terran.ScienceVesselEnergy)
      })

    BunkerRush()
    if (With.fingerprints.ninePool() || With.fingerprints.overpool() || miningBases > 1) {
      buildBunkersAtFoyer(1, PlaceLabels.DefendEntrance)
    }
    if (safeDefending && ! have(Terran.Refinery)) {
      requireMiningBases(2)
    }
    get(2, Terran.Barracks)
    get(Terran.Refinery)
    if (miningBases < 2) {
      gasWorkerCeiling(2)
    }
    get(Terran.Academy)
    get(Terran.Stim)
    get(Terran.EngineeringBay)
    requireMiningBases(2)
    get(Terran.MarineRange)
    buildTurretsAtFoyer(?(enemyLurkersLikely, 2, 1), PlaceLabels.DefendEntrance)
    buildTurretsAtBases(?(enemyMutalisksLikely, 4, ?(enemyLurkersLikely, 2, 3)))
    get(3, Terran.Barracks)
    directToVessels()
    get(2, Terran.EngineeringBay)
    get(7, Terran.Barracks)
    requireMiningBases(3)

    get(Terran.PhysicsLab)
    get(gasPumps, Terran.Starport)
    get(gasPumps, Terran.ControlTower)
    get(4 * miningBases, Terran.Barracks)

    if (safePushing
      && unitsComplete(Terran.Marine) >= 5
      && (enemiesComplete(Zerg.SunkenColony) < 2 || enemyBases > 2)
      && ! enemyHasUpgrade(Zerg.ZerglingSpeed)
      && ! enemyHasShown(Zerg.Lurker)
      && miningBases < 2
      && With.frame < Minutes(6)()) {
      attack()
    }

    if (safePushing
      && Terran.Stim()
      && unitsComplete(IsWarrior) >= 12
        + 4 * Maff.fromBoolean(enemyHasUpgrade(Zerg.ZerglingSpeed))
        + 6 * Maff.fromBoolean(enemyHasShown(Zerg.Mutalisk))
        + 6 * Maff.fromBoolean(enemyHasShown(Zerg.Lurker, Zerg.LurkerEgg))
      && (enemiesComplete(Zerg.SunkenColony) < 4 || enemyBases > 2)
      && With.frame < Minutes(10)()) {
      attack()
    }

    if (safePushing
      && haveComplete(Terran.ScienceVessel)
      && unitsComplete(IsWarrior) >= 24) {
      attack()
    }
  }
}
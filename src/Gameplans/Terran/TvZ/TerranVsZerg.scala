package Gameplans.Terran.TvZ

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Macro.Actions.{Enemy, Friendly}
import Placement.Access.PlaceLabels
import ProxyBwapi.Races.{Terran, Zerg}
import Utilities.{?, SwapIf}
import Utilities.Time.Minutes
import Utilities.UnitFilters.IsWarrior

class TerranVsZerg extends GameplanImperative {

  override def executeBuild(): Unit = {
    once(8, Terran.SCV)
    get(Terran.Barracks)
    once(9, Terran.SCV)
    get(Terran.SupplyDepot)
    once(10, Terran.SCV)
    scoutAt(10)
  }

  override def doWorkers(): Unit = {
    pumpWorkers(oversaturate = true, 38)
    pumpWorkers(oversaturate = false)
  }

  override def executeMain(): Unit = {
    maintainMiningBases(armySupply200 / 30)
    requireMiningBases(armySupply200 / 40)

    once(2, Terran.Medic)
    once(2, Terran.Firebat)
    once(1, Terran.Wraith)
    once(2, Terran.ScienceVessel)
    SwapIf(
      safeDefending, {
        pump(Terran.Wraith, 1)
        pump(Terran.ScienceVessel, 3)
        pump(Terran.Battlecruiser)
        pump(Terran.ScienceVessel)
        pumpRatio(Terran.Firebat, 0, 4, Seq(Enemy(Zerg.Zergling, 0.125), Friendly(Terran.Dropship, 1.0)))
        pumpRatio(Terran.Medic, 0, 12, Seq(Friendly(Terran.Marine, 0.25), Friendly(Terran.Firebat, 0.25)))
        pump(Terran.Marine)
      }, {
        upgradeContinuously(Terran.BioDamage)
        upgradeContinuously(Terran.BioArmor)
        get(Terran.Irradiate)
        get(Terran.ScienceVesselEnergy)
      })

    if (With.fingerprints.fourPool()) {
      buildBunkersAtMain(1)
    }
    if (With.fingerprints.ninePool() || With.fingerprints.overpool() || miningBases > 1) {
      buildBunkersAtNatural(1)
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
    pump(Terran.Comsat, unitsComplete(Terran.CommandCenter))
    get(Terran.MarineRange)
    buildTurretsAtFoyer(?(enemyLurkersLikely, 2, 1), PlaceLabels.DefendEntrance)
    buildTurretsAtBases(?(enemyMutalisksLikely, 4, ?(enemyLurkersLikely, 2, 3)))
    get(4, Terran.Barracks)
    get(Terran.Factory)
    get(Terran.Starport)
    pumpGasPumps()
    get(Terran.ScienceFacility)
    get(2, Terran.EngineeringBay)
    get(2, Terran.Starport)
    get(2, Terran.ControlTower)
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
      && ! enemyHasShown(Zerg.Mutalisk)
      && ! enemyHasShown(Zerg.Lurker)
      && ! enemyHasUpgrade(Zerg.ZerglingSpeed)
      && (enemiesComplete(Zerg.SunkenColony) < 3 || enemyBases > 2)
      && With.frame < Minutes(8)()) {
      attack()
    }

    if (safePushing
      && haveComplete(Terran.ScienceVessel)
      && unitsComplete(IsWarrior) >= 24) {
      attack()
    }
  }
}
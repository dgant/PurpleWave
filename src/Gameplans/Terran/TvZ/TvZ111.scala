package Gameplans.Terran.TvZ

import Gameplans.Terran.TvE.{BunkerRush, TerranGameplan}
import Lifecycle.With
import Macro.Actions.{Enemy, Friendly}
import Mathematics.Maff
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.{Terran, Zerg}
import Utilities.?
import Utilities.UnitFilters.{IsTank, IsWarrior}

class TvZ111 extends TerranGameplan {

  override def executeBuild(): Unit = {
    emergencyReactions()

    once(9, Terran.SCV)
    get(Terran.SupplyDepot, new PlacementQuery(Terran.SupplyDepot).preferLabelYes(PlaceLabels.Wall))
    once(11, Terran.SCV)
    get(Terran.Barracks, new PlacementQuery(Terran.SupplyDepot).preferLabelYes(PlaceLabels.Wall))
    once(12, Terran.SCV)
    get(Terran.Refinery)
    once(14, Terran.SCV)
    once(3, Terran.Marine)
    get(2, Terran.SupplyDepot)
    //get(2, Terran.SupplyDepot, new PlacementQuery(Terran.SupplyDepot).preferLabelYes(PlaceLabels.Wall))
    once(15, Terran.SCV)
    get(Terran.Factory)
    once(Terran.Vulture)
    if (safeDefending) {
      buildBunkersAtFoyer(1, PlaceLabels.DefendEntrance)
      requireMiningBases(2)
    }

    if (haveGasForUnit(Terran.Factory) && ! haveEver(Terran.Starport)) {
      gasWorkerCeiling(?(miningBases > 1, 2, 0))
    }

    scoutOn(Terran.Barracks)
  }

  override def doWorkers(): Unit = {
    if (enemyLurkersLikely) {
      get(Terran.Academy)
    }
    pump(Terran.Comsat)
    pumpWorkers(oversaturate = true)
  }

  override def executeMain(): Unit = {
    maintainMiningBases(armySupply200 / 30)
    requireMiningBases(armySupply200 / 40)

    get(Terran.MachineShop)
    get(Terran.VultureSpeed)
    get(Terran.SpiderMinePlant)
    pump(Terran.ScienceVessel, 1)
    pump(Terran.Valkyrie, 2 * Maff.fromBoolean(enemyMutalisksLikely))
    pump(Terran.Battlecruiser, 4)
    pump(Terran.ScienceVessel, 24)
    if (haveEver(Terran.ScienceVessel) && techStarted(Terran.Irradiate))
    pump(Terran.SiegeTankUnsieged,
        1 * Maff.fromBoolean(haveEver(Terran.ScienceVessel))
      + 2 * Maff.fromBoolean(enemyLurkersLikely)
      + enemies(Zerg.SunkenColony))
    pump(Terran.Vulture, 3)
    pumpRatio(Terran.Firebat, 0, 4, Seq(Enemy(Zerg.Zergling, 0.125)))
    pumpRatio(Terran.Medic, 0, 12, Seq(Friendly(Terran.Marine, 0.2), Friendly(Terran.Firebat, 0.2)))
    pump(Terran.Marine, 4)

    buildBunkersAtFoyer(1, PlaceLabels.DefendEntrance)
    requireMiningBases(2)
    pump(Terran.Vulture)
    pump(Terran.Marine)

    get(Terran.Starport)
    once(Terran.Wraith)
    get(Terran.ScienceFacility)
    get(Terran.ControlTower)
    once(Terran.ScienceVessel)
    get(Terran.EngineeringBay)
    buildTurretsAtFoyer(?(enemyLurkersLikely, 2, 1), PlaceLabels.DefendEntrance)
    buildTurretsAtBases(?(enemyMutalisksLikely, 4, ?(enemyLurkersLikely, 2, 3)))
    get(Terran.BioDamage)
    get(Terran.Irradiate)
    pumpGasPumps()
    get(2, Terran.Barracks)
    get(Terran.Academy)
    upgradeContinuously(Terran.BioDamage)
    if (enemyMutalisksLikely) {
      upgradeContinuously(Terran.AirDamage)
    }
    get(Terran.MarineRange)
    get(Terran.Stim)
    get(Terran.ScienceVesselEnergy)
    get(2, Terran.EngineeringBay)
    upgradeContinuously(Terran.BioArmor)
    get(Terran.SiegeMode)
    get(2, Terran.Starport)
    get(2, Terran.ControlTower)

    get(2, Terran.Barracks)
    get(Terran.EngineeringBay)
    get(Terran.BioDamage)
    get(Terran.Academy)
    get(5, Terran.Barracks)
    requireMiningBases(3)

    get(Terran.PhysicsLab)
    get(gasPumps, Terran.Starport)
    get(gasPumps, Terran.ControlTower)
    get(4 * miningBases, Terran.Barracks)
    requireMiningBases(4)


    if (haveComplete(Terran.Vulture) && ! haveComplete(IsTank) && ! enemyHasUpgrade(Zerg.ZerglingSpeed) && safeSkirmishing && ! enemyMutalisksLikely) {
      attack()
    }
    if (safePushing
      && haveComplete(Terran.ScienceVessel)
      && unitsComplete(IsWarrior) >= 24) {
      attack()
    }
  }
}
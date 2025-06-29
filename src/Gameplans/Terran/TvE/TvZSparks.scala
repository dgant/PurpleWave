package Gameplans.Terran.TvE

import Lifecycle.With
import Macro.Actions.Friendly
import Placement.Access.PlaceLabels
import ProxyBwapi.Races.Terran
import Utilities.SwapIf
import Utilities.Time.Minutes

class TvZSparks extends TerranGameplan {

  override def executeBuild(): Unit = {
    emergencyReactions()

    once(9, Terran.SCV)
    get(Terran.SupplyDepot)
    once(11, Terran.SCV)
    get(Terran.Barracks)
    once(13, Terran.SCV)
    get(2, Terran.Barracks)
    once(15, Terran.SCV)
    get(2, Terran.SupplyDepot)
    once(Terran.Marine)

    if (With.frame < Minutes(4)() && enemyStrategy(With.fingerprints.fiveRax, With.fingerprints.proxyGateway, With.fingerprints.fourPool)) {
      buildBunkersAtMain(1, PlaceLabels.DefendHall)
      pump(Terran.Marine, 4)
    }

    once(16, Terran.SCV)
    once(2, Terran.Marine)
    if (gas < 200) {
      pumpGasPumps(unitsComplete(Terran.Refinery) + 1)
    }
    once(17, Terran.SCV)
    once(4, Terran.Marine)

    scoutOn(Terran.Academy)
  }

  override def doWorkers(): Unit = {
    if (enemyLurkersLikely || enemyDarkTemplarLikely || enemyHasTech(Terran.WraithCloak)) {
      get(Terran.Comsat)
    }
    pumpWorkers(oversaturate = false)
    pump(Terran.Comsat)
  }

  override def executeMain(): Unit = {
    if ( ! haveComplete(Terran.CovertOps)) {
      gasWorkerCeiling(2)
    }

    SwapIf(
      safeDefending,
      {
        once(2, Terran.Medic)
        if (enemyIsZerg) {
          once(2, Terran.Firebat)
        }
        pumpRatio(Terran.Medic, 0, 12, Seq(Friendly(Terran.Marine, 0.2)))
        pump(Terran.Ghost)
        pump(Terran.Marine)
      }, {
        get(Terran.Refinery)
        get(Terran.Academy)
        SwapIf(
          enemyIsZerg,
          get(Terran.MarineRange),
          get(Terran.Stim))
      })

    if (miningBases > 1) {
      get(2, Terran.EngineeringBay)
      upgradeContinuously(Terran.BioDamage)
      upgradeContinuously(Terran.BioArmor)
      get(Terran.Factory)
      get(Terran.ScienceFacility)
      get(Terran.CovertOps)
      if (enemyIsProtoss || enemyIsTerran) {
        get(Terran.Lockdown)
      }
    }
    get(5 * miningBases, Terran.Barracks)
    requireMiningBases(8)

    if (Terran.Stim()) {
      attack()
    }
    With.blackboard.floatableBuildings.set(Vector(Terran.Factory, Terran.Starport))
  }
}
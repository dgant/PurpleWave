package Gameplans.Terran.TvE

import Lifecycle.With
import Macro.Actions.Enemy
import Macro.Requests.RequestUnit
import Placement.Access.PlaceLabels
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.UnitFilters.IsWarrior
import Utilities.{?, SwapIf}

class TvE3Fac extends TerranGameplan {

  override def executeBuild(): Unit = {
    emergencyReactions()

    once(9, Terran.SCV)
    get(Terran.SupplyDepot)
    once(11, Terran.SCV)
    get(Terran.Barracks)
    once(12, Terran.SCV)
    pumpGasPumps()

    scoutAt(13)

    once(14, Terran.SCV)
    once(Terran.Marine)

    if (With.fingerprints.fourPool() || With.fingerprints.proxyGateway() || With.fingerprints.proxyRax()) {
      buildBunkersAtMain(1, PlaceLabels.DefendHall)
      pump(Terran.Marine, 4)
      pumpWorkers(oversaturate = false)
    }
    once(15, Terran.SCV)
    get(2, Terran.SupplyDepot)
    once(16, Terran.SCV)
    once(Terran.Factory)
    once(18, Terran.SCV)
    once(2, Terran.Factory)

    pump(Terran.Comsat)
  }

  override def executeMain(): Unit = {
    if (units(Terran.Factory) < 3 && minerals < 300) {
      gasWorkerCeiling(2)
    }
    if (haveComplete(Terran.Factory)) {
      With.blackboard.floatableBuildings.set(Vector(Terran.Barracks))
    }
    if (enemyHasTech(Terran.WraithCloak)) {
      requireBases(2)
      get(Terran.Academy)
      buildTurretsAtOpenings(1)
    }

    gasLimitCeiling(300)

    once(Terran.Vulture)

    SwapIf(
      With.fingerprints.twoGate(),
      get(Terran.MachineShop),
      once(3, Terran.Vulture))

    if (enemyLurkersLikely || enemyDarkTemplarLikely || enemyHasTech(Terran.WraithCloak)) {
      get(Terran.Academy)
    }

    if (enemyIsZerg) {
      get(RequestUnit(Terran.Armory, 1, With.scouting.earliestArrival(Zerg.Mutalisk) - Terran.Armory.buildFrames - 2 * Terran.Goliath.buildFrames))
      once(6, Terran.Goliath)
      if (enemyMutalisksLikely) {
        get(Terran.GoliathAirRange)
        buildTurretsAtMain(2)
      }
    } else if (enemyMutalisksLikely || enemyHasShown(Protoss.Scout, Protoss.Carrier, Terran.Wraith)) {
      get(Terran.Armory)
      get(Terran.GoliathAirRange)
    }
    pumpRatio(Terran.Goliath, ?(enemyIsZerg, 8, 1), 18, Seq(Enemy(Zerg.Mutalisk, 2.0), Enemy(Protoss.Scout, 2.0), Enemy(Terran.Wraith, 1.0), Enemy(Protoss.Carrier, 4.0)))

    if (enemyHasShown(Terran.Bunker, Protoss.PhotonCannon, Zerg.SunkenColony)) {
      get(Terran.SiegeMode)
      get(2, Terran.MachineShop)
      once(2, Terran.SiegeTankUnsieged)
    }

    SwapIf(
      With.fingerprints.oneGateCore(),
      get(Terran.VultureSpeed),
      get(Terran.SpiderMinePlant))

    once(7, Terran.Vulture)
    get(3, Terran.Factory)
    get(Terran.SiegeMode)
    pump(Terran.SiegeTankUnsieged, maximumConcurrently = Math.max(1, gas / 200 + units(Terran.Factory) / 4))
    pump(Terran.Vulture)
    get(4, Terran.Factory)
    get(2, Terran.MachineShop)
    requireMiningBases(2)
    pumpGasPumps()
    get(4 * miningBases, Terran.Factory)
    get(2 * miningBases, Terran.MachineShop)
    requireMiningBases(5)

    if (unitsComplete(Terran.Vulture) > 0 && (safePushing || unitsComplete(IsWarrior) >= 5)) {
      attack()
    }
  }
}

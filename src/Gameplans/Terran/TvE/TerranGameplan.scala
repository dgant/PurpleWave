package Gameplans.Terran.TvE

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Macro.Actions.Enemy
import Placement.Access.PlaceLabels
import Placement.Access.PlaceLabels.{DefendEntrance, DefendHall}
import ProxyBwapi.Races.{Protoss, Terran}
import Utilities.Time.{GameTime, Minutes}
import Utilities.UnitFilters.{IsTank, IsWorker}

abstract class TerranGameplan extends GameplanImperative {

  def emergencyReactions(): Unit = {
    lazy val suspiciousScout = With.frame < GameTime(2, 0)() && With.geography.ourMetros.exists(_.enemies.exists(IsWorker))
    if (suspiciousScout)                                                                        reactTo4PoolScout()
    if (With.fingerprints.fourPool())                                                           reactTo4Pool()
    if (With.fingerprints.workerRush())                                                         reactToWorkerRush()
    if (With.fingerprints.bunkerRush())                                                         reactToBunkerRush()
    if (With.fingerprints.bbs() || With.fingerprints.proxyRax() || With.fingerprints.fiveRax()) reactToBarracksCheese()
    if (With.fingerprints.twoGate99() || With.fingerprints.proxyGateway())                      reactToGatewayCheese()
    if (With.fingerprints.cannonRush())                                                         reactToCannonRush()
    if (enemyDarkTemplarLikely)                                                                 reactToDarkTemplar()
    if (enemyLurkersLikely)                                                                     reactToLurkers()
    if (enemyArbitersLikely)                                                                    reactToArbiters()
  }

  def reactTo4PoolScout(): Unit = {
    if (With.frame > Minutes(5)()) return
    once(8, Terran.SCV)
    once(Terran.Barracks)
    once(9, Terran.SCV)
    once(Terran.SupplyDepot)
    once(11, Terran.SCV)
    once(Terran.Marine)
    buildBunkersAtMain(1, DefendHall)
  }

  def reactTo4Pool(): Unit = {
    if (With.frame > Minutes(5)()) return
    once(8, Terran.SCV)
    once(Terran.Barracks)
    once(9, Terran.SCV)
    once(Terran.SupplyDepot)
    once(11, Terran.SCV)
    pump(Terran.Marine, 2)
    buildBunkersAtMain(1, DefendHall)
    pump(Terran.SCV, 8)
    pump(Terran.Marine)
    pump(Terran.Vulture)
    autosupply()
    pumpWorkers(oversaturate = false, maximumTotal = 23)
    if (unitsComplete(Terran.Marine) < 4 || ! haveComplete(Terran.Bunker)) {
      gasWorkerCeiling(0)
    }
    gasLimitCeiling(150)
  }

  def reactToWorkerRush(): Unit = {
    if (With.frame > Minutes(5)()) return
    once(8, Terran.SCV)
    get(Terran.Barracks)
    once(9, Terran.SCV)
    get(Terran.SupplyDepot)
    once(11, Terran.SCV)
    pump(Terran.Marine, 2)
    buildBunkersAtMain(1, DefendHall)
    pumpWorkers(oversaturate = true)
    pump(Terran.Marine)
    autosupply()
    pump(Terran.Vulture)
    if (unitsComplete(Terran.Marine) < 2 || ! haveComplete(Terran.Bunker)) {
      gasWorkerCeiling(0)
    }
    get(Terran.Refinery)
    get(Terran.Factory)
  }

  def reactToBunkerRush(): Unit = {
    if (With.frame > Minutes(5)()) return
    once(9, Terran.SCV)
    get(Terran.SupplyDepot)
    once(11, Terran.SCV)
    get(Terran.Barracks)
    once(12, Terran.SCV)
    get(Terran.Refinery)
    once(14, Terran.SCV)
    once(Terran.Marine)
    get(2, Terran.SupplyDepot)
    once(16, Terran.SCV)
    get(Terran.Factory)
    once(2, Terran.Marine)
    once(18, Terran.SCV)
    autosupply()
    pumpWorkers(oversaturate = true)
    get(Terran.MachineShop)
    pump(Terran.SiegeTankUnsieged)
    get(Terran.SiegeMode)
    pump(Terran.Marine, 4)
    get(2, Terran.Factory)
    pump(Terran.Vulture)
    get(Terran.VultureSpeed)
  }

  def reactToBarracksCheese(): Unit = {
    if (With.frame > Minutes(5)()) return
    once(9, Terran.SCV)
    get(Terran.SupplyDepot)
    once(10, Terran.SCV)
    get(Terran.Barracks)
    once(13, Terran.SCV)
    once(Terran.Marine)
    buildBunkersAtMain(1, DefendEntrance)
    once(14, Terran.SCV)
    once(2, Terran.Marine)
    autosupply()
    pumpWorkers(oversaturate = true)
    pump(Terran.Vulture, 3)
    get(Terran.Factory)
    get(Terran.MachineShop)
    get(Terran.VultureSpeed)
    pump(Terran.Vulture, 8)
    get(Terran.SpiderMinePlant)
  }

  def reactToGatewayCheese(): Unit = {
    if (With.frame > Minutes(5)()) return
    once(9, Terran.SCV)
    get(Terran.SupplyDepot)
    once(10, Terran.SCV)
    get(Terran.Barracks)
    once(13, Terran.SCV)
    once(Terran.Marine)
    buildBunkersAtMain(1, DefendEntrance)
    once(14, Terran.SCV)
    once(2, Terran.Marine)
    autosupply()
    pumpWorkers(oversaturate = true)
    pump(Terran.Vulture, 3)
    get(Terran.Factory)
    get(Terran.MachineShop)
    get(Terran.VultureSpeed)
    pump(Terran.Vulture, 8)
    get(Terran.SpiderMinePlant)
  }

  def reactToCannonRush(): Unit = {
    if (With.frame > Minutes(7)()) return
    once(9, Terran.SCV)
    get(Terran.SupplyDepot)
    once(11, Terran.SCV)
    get(Terran.Barracks)
    once(12, Terran.SCV)
    get(Terran.Refinery)
    once(14, Terran.SCV)
    once(Terran.Marine)
    get(2, Terran.SupplyDepot)
    once(16, Terran.SCV)
    once(Terran.Factory)
    autosupply()
    get(Terran.SiegeMode)
    pump(Terran.SiegeTankUnsieged, 3)
    get(Terran.MachineShop)
    pumpWorkers(oversaturate = true)
    once(4, Terran.Marine)
    get(Terran.SpiderMinePlant)
    pump(Terran.Vulture)
    get(2, Terran.Factory)
    if (enemiesComplete(Protoss.PhotonCannon) == 0) {
      attack()
    }
    if (Terran.SiegeMode() && haveComplete(IsTank)) {
      attack()
    }
  }

  def reactToDarkTemplar(): Unit = {
    buildTurretsAtFoyer(1, PlaceLabels.DefendEntrance)
    if (have(Terran.Factory)) {
      get(Terran.MachineShop)
      get(Terran.SpiderMinePlant)
      once(4, Terran.Vulture)
      pumpRatio(Terran.Vulture, 0, 8, Seq(Enemy(Protoss.DarkTemplar, 2.0)))
    }
    if (safeDefending) {
      get(Terran.Academy)
      pump(Terran.Comsat)
    }
  }

  def reactToLurkers(): Unit = {
    buildTurretsAtFoyer(1, PlaceLabels.DefendEntrance)
    if (have(Terran.MachineShop)) {
      pump(Terran.SiegeTankUnsieged, 1)
    } else {
      buildBunkersAtFoyer(1, PlaceLabels.DefendEntrance)
      if (With.frame < Minutes(6)()) {
        pump(Terran.Marine, 4)
      }
    }
    if (safeDefending) {
      get(Terran.Academy)
      pump(Terran.Comsat)
    }
  }

  def reactToArbiters(): Unit = {
    buildTurretsAtOpenings(1)
    if (safeDefending) {
      get(Terran.Academy)
      pump(Terran.Comsat)
      get(Terran.Factory)
      get(Terran.Starport)
      get(Terran.ScienceFacility)
      get(Terran.ControlTower)
      pump(Terran.ScienceVessel, 1)
    }
  }
}

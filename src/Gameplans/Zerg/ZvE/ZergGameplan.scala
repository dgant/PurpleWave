package Gameplans.Zerg.ZvE

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Placement.Access.PlaceLabels
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.Time.{GameTime, Minutes}
import Utilities.UnitFilters.IsWorker

abstract class ZergGameplan extends GameplanImperative {

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
  }

  def reactTo4Pool(): Unit = {
    if (With.frame > Minutes(5)()) return
    once(9, Zerg.Drone)
    get(Zerg.SpawningPool)
    once(10, Zerg.Drone)
    once(2, Zerg.Overlord)
    once(11, Zerg.Drone)
    once(10, Zerg.Zergling)
    buildSunkensAtMain(1, PlaceLabels.DefendHall)
    pump(Zerg.Mutalisk)
    pump(Zerg.Zergling, enemies(Zerg.Zergling) + 4)
    pump(Zerg.Drone, 8)
    if (unitsEver(Zerg.CreepColony) == 0 && unitsEver(Zerg.Zergling) < 12) {
      gasLimitCeiling(0)
    }
    get(Zerg.Extractor, Zerg.Lair, Zerg.Spire)
  }

  def reactToWorkerRush(): Unit = {
    if (With.frame > Minutes(5)()) return
    reactTo4Pool()
  }

  def reactToBunkerRush(): Unit = {
    if (With.frame > Minutes(5)()) return
  }

  def reactToBarracksCheese(): Unit = {
    if (With.frame > Minutes(5)()) return
  }

  def reactToGatewayCheese(): Unit = {
    if (With.frame > Minutes(5)()) return
  }

  def reactToCannonRush(): Unit = {
    if (With.frame > Minutes(7)()) return
    once(9, Zerg.Drone)
    get(Zerg.SpawningPool)
    once(10, Zerg.Drone)
    once(2, Zerg.Overlord)
    once(11, Zerg.Drone)
    once(12, Zerg.Zergling)
    autosupply()
    pump(Zerg.Mutalisk)
    pump(Zerg.Drone, 10)
    pump(Zerg.Zergling)
    get(Zerg.Extractor, Zerg.Lair, Zerg.Spire)
    if (unitsComplete(Zerg.Mutalisk) > 0) {
      attack()
      harass()
    } else if (enemiesComplete(Protoss.PhotonCannon) < 1 + unitsComplete(Zerg.Zergling) / 6) {
      attack()
    }
  }

  def reactToDarkTemplar(): Unit = {
  }

  def reactToLurkers(): Unit = {
  }

  def reactToArbiters(): Unit = {
  }
}

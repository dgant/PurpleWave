package Gameplans.All

import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.?
import Utilities.UnitFilters.{IsFlyingBuilding, IsWorker}

class WorkerRush extends GameplanImperative {

  private var timeToAttack    = false
  private var seenFiveWorkers = false
  private var rushEnded       = false
  doAutosupply = false

  override def executeBuild(): Unit = {
    rushEnded ||= enemiesHaveComplete(Protoss.PhotonCannon, Zerg.SunkenColony)
    rushEnded ||= With.units.enemy.count(IsFlyingBuilding) > 0 && With.units.enemy.count( ! IsFlyingBuilding(_)) == 0
    rushEnded ||= With.geography.enemyBases.forall(_.zone.walledIn)
    doAutosupply = rushEnded
    doBasics     = rushEnded
    once(10, With.self.workerClass)

    if ( ! rushEnded) {
      With.blackboard.maxBuilderTravelFrames.set(0)

      seenFiveWorkers ||= unitsComplete(IsWorker) >= 5
      timeToAttack ||= units(IsWorker) + minerals / 50 >= ?(With.self.isTerran, 10, 9)

      val completeWorkers = unitsEver(u => IsWorker(u) && u.complete)

      if (timeToAttack) {
        //val workersToKeep = Maff.clamp(completeWorkers - 9, 0, 3)
        With.blackboard.workerRushers.set(unitsComplete(IsWorker))
        //With.blackboard.workerRushers.set(unitsComplete(IsWorker) - workersToKeep)
        //if (workersToKeep >= 3 && supplyBlocked && minerals >= 150) {
        //  autosupply()
        //}
      } else if (With.geography.startLocations.length > 2) {
        scoutAt(6, scouts = ?(With.geography.startLocations.length > 3, 2, 1))
      }
      pump(With.self.workerClass)
    }
  }

  override def executeMain(): Unit = {
    if (rushEnded) {
      attack(confidenceAttacking11 > 0)
      if (With.self.isTerran) {
        get(Terran.SiegeMode)
        pump(Terran.SiegeTankUnsieged, 5)
        pump(Terran.Wraith, 5)
        pump(Terran.SiegeTankUnsieged)
        get(Terran.Barracks, Terran.Refinery, Terran.Factory, Terran.MachineShop, Terran.Starport)
        pump(Terran.Marine)
        get(4, Terran.Barracks)
      } else if (With.self.isProtoss) {
        pump(Protoss.Scout)
        pump(Protoss.Dragoon)
        get(Protoss.Gateway, Protoss.Assimilator, Protoss.CyberneticsCore)
        get(Protoss.DragoonRange)
        get(Protoss.Stargate)
        pump(Protoss.Zealot)
        get(4, Protoss.Gateway)
      } else {
        pump(Zerg.Drone, 10)
        pump(Zerg.Mutalisk)
        pump(Zerg.Zergling)
        get(Zerg.SpawningPool, Zerg.Extractor, Zerg.Lair, Zerg.Spire)
      }
    }
  }
}

package Gameplans.All

import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Strategery.Strategies.AllRaces.WorkerRushContinuousProduction
import Utilities.?
import Utilities.UnitFilters.{IsFlyingBuilding, IsWorker}

class WorkerRush extends GameplanImperative {

  private var timeToAttack    = false
  private var seenFiveWorkers = false
  private var rushEnded       = false
  doAutosupply = false

  override def executeBuild(): Unit = {
    rushEnded ||= enemies(IsWorker) <= 1 && enemiesHaveComplete(Protoss.PhotonCannon, Zerg.SunkenColony)
    rushEnded ||= With.units.enemy.count(IsFlyingBuilding) > 0 && With.units.enemy.count( ! IsFlyingBuilding(_)) == 0
    doAutosupply = rushEnded
    doBasics     = rushEnded
    once(10, With.self.workerClass)

    if ( ! rushEnded) {
      With.blackboard.maxBuilderTravelFrames.set(0)

      seenFiveWorkers ||= unitsComplete(IsWorker) >= 5
      timeToAttack ||= foundEnemyBase && unitsComplete(IsWorker) >= 9


      if (timeToAttack) {
        val workersToKeep =
          if (WorkerRushContinuousProduction()) 3
          else if (seenFiveWorkers)             1
          else                                  0

        With.blackboard.workerRushers.set(unitsComplete(IsWorker) - workersToKeep)
        if (workersToKeep >= 3 && supplyBlocked && minerals >= 150) {
          autosupply()
        }
      } else {
        scoutAt(?(With.geography.startLocations.length > 3, 5, 6))
      }
      pump(With.self.workerClass)
    }
  }

  override def executeMain(): Unit = {
    if (rushEnded) {
      attack(confidenceAttacking11 > 0)
      if (With.self.isTerran) {
        pump(Terran.SiegeTankUnsieged, 5)
        pump(Terran.Wraith, 5)
        pump(Terran.SiegeTankUnsieged)
        get(Terran.Barracks, Terran.Refinery, Terran.Factory, Terran.MachineShop)
        get(Terran.SiegeMode)
        get(Terran.Starport)
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

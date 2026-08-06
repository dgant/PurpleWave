package Gameplans.Protoss.PvP

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPCannonRush

class PvPCannonRush extends GameplanImperative {

  override def activated: Boolean = PvPCannonRush()
  override def completed: Boolean = unitsEver(Protoss.DarkTemplar) >= 2

  var rushEnded: Boolean = false

  override def executeBuild(): Unit = {
    scoutOn(Protoss.Pylon)
    if ( ! foundEnemyBase && With.geography.startLocations.length >= 4) {
      scoutOn(Protoss.Forge, 1, scouts = 2)
    }

    once(8, Protoss.Probe)
    tryBuildInWall(1, Protoss.Pylon)
    once(11, Protoss.Probe)
    tryBuildInWall(1, Protoss.Forge)
    once(13, Protoss.Probe)
  }

  override def doWorkers(): Unit = {}

  override def executeMain(): Unit = {
    rushEnded ||= With.units.existsEverOurs(Protoss.PhotonCannon)
    rushEnded ||= With.tactics.workerScout.scoutingAbandoned
    doAutosupply = unitsEver(Protoss.Nexus) >= 2

    if (With.fingerprints.proxyGateway() && unitsEver(Protoss.PhotonCannon) < 3) {
      tryBuildInWall(3, Protoss.PhotonCannon)
    }
    else if ( ! rushEnded) {
      With.blackboard.cannonRush.set(With.scouting.enemyMain)
      return
    }

    once(13, Protoss.Probe)
    tryBuildInWall(2, Protoss.PhotonCannon)
    once(14, Protoss.Probe)
    tryBuildInWall(1, Protoss.Gateway)
    once(15, Protoss.Probe)
    get(Protoss.Pylon, new PlacementQuery(Protoss.Pylon).requireBase(With.geography.ourMain)) // Needed for building placements
    once(16, Protoss.Probe)
    once(Protoss.Assimilator)
    once(17, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    once(18, Protoss.Probe)
    requireMiningBases(2)
    once(19, Protoss.Probe)
    once(Protoss.Dragoon)
    get(Protoss.CitadelOfAdun)
    once(20, Protoss.Probe)
    get(Protoss.TemplarArchives)
    get(Protoss.DragoonRange)
    once(2, Protoss.DarkTemplar)
    tryBuildInWall(4, Protoss.PhotonCannon)
    once(38, Protoss.Probe)
    pump(Protoss.Dragoon)
    get(5, Protoss.Gateway, new PlacementQuery(Protoss.Gateway).requireBase(With.geography.ourMain))
    get(3, Protoss.Pylon,   new PlacementQuery(Protoss.Pylon).requireBase(With.geography.ourMain))
    pumpGasPumps()
    get(8, Protoss.Gateway, new PlacementQuery(Protoss.Gateway).requireBase(With.geography.ourMain))
  }
}

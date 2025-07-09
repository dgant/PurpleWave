package Gameplans.Zerg.ZvT

import Gameplans.Zerg.ZvE.ZergGameplan
import Placement.Access.PlaceLabels
import ProxyBwapi.Races.{Terran, Zerg}

class ZvT13PoolMuta extends ZergGameplan {

  override def executeBuild(): Unit = {
    emergencyReactions()

    once(9, Zerg.Drone)
    get(2, Zerg.Overlord)
    once(13, Zerg.Drone)
    get(Zerg.SpawningPool)
    get(Zerg.Extractor)
    once(14, Zerg.Drone)
    requireMiningBases(2)
    get(Zerg.Lair)
    once(4, Zerg.Zergling)
    once(Zerg.Spire)
    once(16, Zerg.Drone)
    buildSunkensAtFoyer(1, PlaceLabels.DefendEntrance)
  }
  override def executeMain(): Unit = {
    if (haveComplete(Zerg.Mutalisk)) {
      attack()
    }
    if (enemyHasTech(Terran.WraithCloak)) {
      get(Zerg.OverlordSpeed)
    }
    if (have(Zerg.Spire)) {
      get(2, Zerg.Extractor)
      pumpGasPumps(units(Zerg.Drone) / 7)
    }
    once(8, Zerg.Mutalisk)
    pump(Zerg.Mutalisk)
    pump(Zerg.Drone, miningBases * 11)

    requireMiningBases(6)
  }
}

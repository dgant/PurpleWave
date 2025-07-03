package Gameplans.Zerg.ZvT

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.{Terran, Zerg}
import Utilities.Time.Seconds

// Based on Effort vs. Flash's 1-1-1:
// https://www.youtube.com/watch?v=3sb47YGI7l8&feature=youtu.be&t=2280
// https://docs.google.com/spreadsheets/d/1m6nU6FewJBC2LGQX_DPuo4PqzxH8hF3bazp8T6QlqRs/edit#gid=1166229923
class ZvTEffortBust extends GameplanImperative {

  override def executeBuild(): Unit = {
    once(9, Zerg.Drone)
    get(2, Zerg.Overlord)
    once(12, Zerg.Drone)
    requireMiningBases(2)
    get(Zerg.Extractor, new PlacementQuery(Zerg.Extractor).requireBase(With.geography.ourMain))
    get(Zerg.SpawningPool)
    once(17, Zerg.Drone)
    get(Zerg.Lair)
    get(Zerg.ZerglingSpeed)
    once(4, Zerg.Zergling)
    get(3, Zerg.Overlord)
    once(18, Zerg.Zergling)
    get(Zerg.Spire)
    once(26, Zerg.Zergling)
  }
  override def executeMain(): Unit = {
    var timingAttack = upgradeComplete(Zerg.ZerglingSpeed, 1, Seconds(10)())
    var shouldAttack = false
    shouldAttack ||= With.fingerprints.fourteenCC()
    shouldAttack ||= With.fingerprints.oneRaxFE() && safePushing
    shouldAttack ||= timingAttack
    if (shouldAttack) {
      attack()
      if (timingAttack && enemiesHave(Terran.Vulture) && unitsComplete(Zerg.Mutalisk) == 0) {
        aggression(1e6)
      } else if (unitsCompleteFor(20, Zerg.Mutalisk) >= 6) {
        aggression(2.0)
      }
    }

    pump(Zerg.Mutalisk)
    pump(Zerg.Drone, 20)
    requireMiningBases(3)
    pump(Zerg.Drone, 24)
    pumpGasPumps()
    pump(Zerg.Zergling)
    get(4, Zerg.Hatchery, PlaceLabels.MacroHatch)
  }
}

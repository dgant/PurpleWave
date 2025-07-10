package Gameplans.Zerg.ZvT

import Gameplans.Zerg.ZvE.ZergGameplan
import Lifecycle.With
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.{Terran, Zerg}
import Utilities.Time.Seconds

// Based on Effort vs. Flash's 1-1-1:
// https://www.youtube.com/watch?v=3sb47YGI7l8&feature=youtu.be&t=2280
// https://docs.google.com/spreadsheets/d/1m6nU6FewJBC2LGQX_DPuo4PqzxH8hF3bazp8T6QlqRs/edit#gid=1166229923
class ZvTEffortBust extends ZergGameplan {

  override def executeBuild(): Unit = {
    emergencyReactions()

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

    upgradeContinuously(Zerg.GroundArmor)
    upgradeContinuously(Zerg.GroundMeleeDamage)
    get(Zerg.UltraliskArmor)
    get(Zerg.UltraliskSpeed)
    pump(Zerg.Ultralisk)
    pump(Zerg.Mutalisk, 12)
    pump(Zerg.Mutalisk, 24)
    get(Zerg.QueensNest)
    get(Zerg.Hive)
    if (have(Zerg.Hive)) {
    get(Zerg.ZerglingAttackSpeed)
      get(Zerg.EvolutionChamber)
      get(Zerg.UltraliskCavern)
      get(2, Zerg.EvolutionChamber)
    }
    pump(Zerg.Mutalisk)
    pump(Zerg.Drone, miningBases * 13)
    pump(Zerg.Zergling)
    if (gas < 250) {
      pumpGasPumps(units(Zerg.Drone) / 10)
    }
    requireMiningBases(6)
    fillMacroHatches(18)
  }
}

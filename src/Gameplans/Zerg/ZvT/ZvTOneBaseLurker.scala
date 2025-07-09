package Gameplans.Zerg.ZvT

import Gameplans.Zerg.ZvE.ZergGameplan
import Lifecycle.With
import ProxyBwapi.Races.Zerg

// 11 Gas 10 Pool 12 Lair + 6 Lings 15 Drone 16 Overlord Hydra Den Drone 17 Lurker Aspect Metabolic Boost 4 Hydras
// https://bmnielsen.github.io/openbw-replay-viewer/?rep=https://data.basil-ladder.net/bots/NLPRbot/NLPRbot%20vs%20PurpleSpirit%20Tau%20Cross%20CTR_BDF3EE7.rep
class ZvTOneBaseLurker extends ZergGameplan {

  override def executeBuild(): Unit = {
    emergencyReactions()

    once(9, Zerg.Drone)
    get(2, Zerg.Overlord)
    once(11, Zerg.Drone)
    get(Zerg.Extractor)
    get(Zerg.SpawningPool)
    once(13, Zerg.Drone)
    get(Zerg.Lair)
    once(6, Zerg.Zergling)
    if (With.fingerprints.bbs()) {
      once(10, Zerg.Zergling)
    } else {
      once(15, Zerg.Drone)
    }
    once(2, Zerg.Overlord)
    get(Zerg.HydraliskDen)
    get(Zerg.LurkerMorph)
    get(Zerg.ZerglingSpeed)

  }
  override def executeMain(): Unit = {
    once(3, Zerg.Hydralisk)
    once(3, Zerg.Lurker)
    val maxLurkers = 10
    pump(Zerg.Lurker)
    pump(Zerg.Hydralisk, maxLurkers - units(Zerg.Lurker))
    pump(Zerg.Guardian, 8)
    pump(Zerg.Mutalisk, 12)
    upgradeContinuously(Zerg.AirArmor) && upgradeContinuously(Zerg.AirDamage)
    pump(Zerg.Mutalisk, 24)
    pump(Zerg.Drone, miningBases * 11)
    pump(Zerg.Zergling)
    requireBases(2)
    if (gas < 250) {
      pumpGasPumps(units(Zerg.Drone) / 10)
    }
    get(Zerg.Spire, Zerg.QueensNest, Zerg.Hive)
    get(Zerg.ZerglingAttackSpeed)
    get(2, Zerg.EvolutionChamber)
    upgradeContinuously(Zerg.GroundArmor)
    upgradeContinuously(Zerg.GroundMeleeDamage)
    get(Zerg.GreaterSpire)
    pump(Zerg.Mutalisk)
    requireMiningBases(6)
    fillMacroHatches(24)
    attack()
  }
}

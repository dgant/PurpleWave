package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.Get
import Planning.Plans.GamePlans.GameplanImperative
import Planning.Plans.Macro.Automatic.{Enemy, Friendly}
import Planning.UnitMatchers.MatchWarriors
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.GameTime

class PvZ2Gate4Gate extends GameplanImperative{

  override def executeBuild(): Unit = {
    buildOrder(
      Get(8, Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10, Protoss.Probe),
      Get(Protoss.Gateway),
      Get(12, Protoss.Probe),
      Get(2, Protoss.Gateway),
      Get(13, Protoss.Probe),
      Get(Protoss.Zealot),
      Get(2, Protoss.Pylon),
      Get(15, Protoss.Probe),
      Get(3, Protoss.Zealot),
      Get(16, Protoss.Probe),
      Get(3, Protoss.Pylon),
      Get(17, Protoss.Probe),
      Get(5, Protoss.Zealot),
      Get(18, Protoss.Probe),
      Get(4, Protoss.Pylon),
      Get(Protoss.Assimilator),
      Get(19, Protoss.Probe),
      Get(Protoss.CyberneticsCore),
      Get(7, Protoss.Zealot),
      Get(21, Protoss.Probe),
      Get(3, Protoss.Gateway),
      Get(2, Protoss.Dragoon),
      Get(Protoss.DragoonRange))
  }
  override def executeMain(): Unit = {
    if (enemyHydralisksLikely) status("Hydras")
    if (enemyMutalisksLikely) status("Mutas")
    if (enemyLurkersLikely) status("Lurkers")
    if (bases < 2) {
      earlyGame()
    } else {
      restOfGame()
    }
  }

  def earlyGame(): Unit = {
    // Goal is to be home before any Mutalisks can pop.
    // Optimization: Detect 2 vs 3 hatch muta and stay out the extra 30s vs 3hatch
    scoutOn(Protoss.Gateway, quantity = 2)
    if ((frame < GameTime(6, 10)() || enemyHydralisksLikely) && safeToMoveOut) { attack() }

    val shouldExpand = (safeAtHome || unitsComplete(MatchWarriors) >= 9) && (enemiesComplete(Zerg.SunkenColony) > 2 || frame > GameTime(5, 40)())
    if (shouldExpand) {
      status("Expand")
      requireMiningBases(2)
    }
    pump(Protoss.Dragoon)
    get(4, Protoss.Gateway)
  }

  def restOfGame(): Unit = {
    val safeFromMutalisks = ! enemyMutalisksLikely || unitsComplete(Protoss.Corsair) >= 5
    val safeFromGround = upgradeComplete(Protoss.ZealotSpeed) && upgradeComplete(Protoss.GroundDamage)
    if (safeFromGround && safeFromMutalisks && safeToMoveOut) {
      attack()
      harass()
      requireMiningBases(2)
    }

    // We want this up ASAP in case of Mutalisks
    get(Protoss.Stargate)
    buildGasPumps()

    trainCorsairs()

    upgradeContinuously(Protoss.GroundDamage)
    if (unitsComplete(Protoss.Forge) > 1 || upgradeComplete(Protoss.GroundDamage, 3)) {
      upgradeContinuously(Protoss.GroundArmor)
    }
    requireMiningBases(2 + Math.min(unitsComplete(MatchWarriors) / 20, 2))

    trainLateArmy()


    if (frame > GameTime(10, 0)() || enemyLurkersLikely) {
      techRobo()
    }
    twoBaseTech()

    if (unitsComplete(Protoss.Gateway) > 7) {
      lategame()
    }
  }

  def trainCorsairs(): Unit = {
    buildOrder(Get(Protoss.Corsair))
    if (enemyMutalisksLikely) {
      pumpRatio(Protoss.Corsair, 5, 10, Seq(Enemy(Zerg.Mutalisk, 1.0)))
      get(2, Protoss.Stargate)
      get(Protoss.AirDamage)
      if (upgradeComplete(Protoss.AirDamage)) {
        get(Protoss.AirArmor)
      }
    } else if (safeAtHome && unitsComplete(MatchWarriors) >= 8) {
      pump(Protoss.Corsair, if (enemyHydralisksLikely) 1 else 3)
    }
  }

  def trainLateArmy(): Unit = {
    if (upgradeComplete(Protoss.ZealotSpeed, 1, 2 * Protoss.Zealot.buildFrames)) {
      pumpRatio(Protoss.Dragoon, 1, 24, Seq(Friendly(Protoss.Zealot, 1.0), Enemy(Zerg.Mutalisk, 1.0), Enemy(Zerg.Lurker, 1.5)))
      pump(Protoss.DarkTemplar, 1)
      get(Protoss.PsionicStorm)
      pump(Protoss.HighTemplar)
      pump(Protoss.Zealot)
    } else {
      pump(Protoss.Dragoon, 16)
    }
  }

  def techRobo(): Unit = {
    get(Protoss.RoboticsFacility)
    get(Protoss.Observatory)
    pump(Protoss.Observer, if (enemyLurkersLikely) 2 else 1)
    if (enemyLurkersLikely) upgradeContinuously(Protoss.ObserverSpeed)
  }

  def twoBaseTech(): Unit = {
    get(Protoss.Forge)
    get(Protoss.CitadelOfAdun)
    get(Protoss.ZealotSpeed)
    get(Protoss.TemplarArchives)
    get(8, Protoss.Gateway)
  }

  def lategame(): Unit = {
    requireBases(3)
    get(Protoss.HighTemplarEnergy)
    requireMiningBases(3)
    get(Protoss.RoboticsSupportBay)
    get(Protoss.ShuttleSpeed)
    pump(Protoss.Shuttle, 1)
    get(14, Protoss.Gateway)
    requireBases(4)
  }
}

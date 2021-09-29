package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.GamePlans.GameplanImperative
import Planning.Plans.Macro.Automatic.{Enemy, Friendly}
import Planning.UnitMatchers.MatchWarriors
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.Time.{GameTime, Minutes}
import Utilities.DoQueue

class PvZ2GateFlex extends GameplanImperative{

  var massZealot: Boolean = false

  override def executeBuild(): Unit = {
    buildOrder(
      Get(8, Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10, Protoss.Probe),
      Get(Protoss.Gateway))
    if (enemyStrategy(With.fingerprints.fourPool) && unitsComplete(MatchWarriors) < 5) {
      pumpSupply()
      pumpWorkers()
      pump(Protoss.Zealot)
    }
    buildOrder(
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
      Get(4, Protoss.Pylon))
    if (massZealot) {
    } else {
      buildOrder(
        Get(Protoss.Assimilator),
        Get(19, Protoss.Probe),
        Get(Protoss.CyberneticsCore),
        Get(7, Protoss.Zealot),
        Get(21, Protoss.Probe),
        Get(8, Protoss.Zealot),
        Get(1, Protoss.Dragoon))
    }
  }
  override def executeMain(): Unit = {
    if (enemyHydralisksLikely) status("Hydras")
    if (enemyMutalisksLikely) status("Mutas")
    if (enemyLurkersLikely) status("Lurkers")
    if (enemyHasUpgrade(Zerg.ZerglingSpeed)) status("LingSpeed")
    if (bases < 2) {
      status("Early")
      earlyGame()
    } else {
      status("Late")
      restOfGame()
    }
  }

  def earlyGame(): Unit = {
    // Goal is to be home before any Mutalisks can pop.
    // Optimization: Detect 2 vs 3 hatch muta and stay out the extra 30s vs 3hatch
    scoutOn(Protoss.Gateway, quantity = 2)

    val shouldExpand = (safeAtHome || unitsComplete(MatchWarriors) >= 9) && (enemiesComplete(Zerg.SunkenColony) > 2 || frame > GameTime(5, 40)())
    if (shouldExpand) {
      status("Expand")
      requireMiningBases(2)
    }

    var shouldAttack = frame < GameTime(6, 10)()
    shouldAttack || enemyHydralisksLikely
    shouldAttack &&= safeToMoveOut
    shouldAttack ||= shouldExpand
    if (shouldAttack) { attack() }

    if (enemyMutalisksLikely) {
      get(Protoss.Stargate)
      pump(Protoss.Corsair, 5)
    } else if ( ! enemyHydralisksLikely) {
      get(Protoss.Stargate)
      pump(Protoss.Corsair, 1)
    }

    pump(Protoss.Dragoon)
    get(Protoss.DragoonRange)
    get(3, Protoss.Gateway)
    if (enemyHasUpgrade(Zerg.ZerglingSpeed)) {
      get(4, Protoss.Gateway)
    }
    pump(Protoss.Zealot)
    requireMiningBases(2)
  }

  def restOfGame(): Unit = {
    val safeFromMutalisks = ! enemyMutalisksLikely || unitsComplete(Protoss.Corsair) >= 5
    val safeFromGround = (upgradeComplete(Protoss.ZealotSpeed) && upgradeComplete(Protoss.GroundDamage)) || enemyMutalisksLikely
    if (safeFromGround && safeFromMutalisks && safeToMoveOut) {
      attack()
      harass()
      requireMiningBases(2)
    }

    val trainCorsairs = new DoQueue(doTrainCorsairs)
    val trainMainArmy = new DoQueue(doTrainMainArmy)
    val upgrades      = new DoQueue(doUpgrades)
    val tech2Base     = new DoQueue(doTech2Base)
    val techRobo      = new DoQueue(doTechRobo)
    val endgame       = new DoQueue(doEndgame)
    val expand        = new DoQueue(doExpand)

    get(Protoss.Gateway); get(Protoss.Assimilator); get(3, Protoss.Gateway); get(Protoss.Stargate)
    if (saturated || (gas < 200 && units(Protoss.Gateway, Protoss.Stargate) >= 4)) buildGasPumps()

    var shouldConsiderExpanding = upgradeComplete(Protoss.ZealotSpeed)
    shouldConsiderExpanding ||= miningBases < 2 && safeAtHome
    if (shouldConsiderExpanding) {
      status("ExpandUnlocked")
      expand()
    }
    upgrades()
    trainCorsairs()
    if (safeAtHome && unitsComplete(MatchWarriors) >= 10) {
      tech2Base()
    }
    if (enemyLurkersLikely || after(Minutes(9))) {
      techRobo()
    }
    trainMainArmy()
    tech2Base()
    get(6, Protoss.Gateway)
    techRobo()
    get(8, Protoss.Gateway)
    if (unitsComplete(Protoss.Gateway) >= 6) {
      endgame()
    }
  }

  def doTrainCorsairs(): Unit = {
    buildOrder(Get(Protoss.Corsair))
    if (enemyMutalisksLikely) {
      pumpRatio(Protoss.Corsair, 7, 12, Seq(Enemy(Zerg.Mutalisk, 1.0), Enemy(Zerg.Scourge, 0.5)))
      get(2, Protoss.Stargate)
      get(Protoss.AirDamage)
      if (upgradeComplete(Protoss.AirDamage)) {
        get(Protoss.AirArmor)
      }
    } else if (safeAtHome || unitsComplete(MatchWarriors) >= 8) {
      pumpRatio(Protoss.Corsair, 1, 4, Seq(Friendly(MatchWarriors, 0.1)))
    }
  }

  def doTrainMainArmy(): Unit = {
    if (minerals > 800 && gas < 50) {
      pump(Protoss.Zealot)
    }
    if (upgradeComplete(Protoss.ZealotSpeed, 1, 2 * Protoss.Zealot.buildFrames)) {
      pumpRatio(Protoss.Dragoon, 1, 24, Seq(Friendly(Protoss.Zealot, 1.0), Friendly(Protoss.Corsair, -1.5), Enemy(Zerg.Mutalisk, 1.0), Enemy(Zerg.Lurker, 1.5)))
      pump(Protoss.DarkTemplar, 1)
      get(Protoss.PsionicStorm)
      pumpRatio(Protoss.HighTemplar, 2, 12, Seq(Friendly(MatchWarriors, 0.15)))
      if (upgradeComplete(Protoss.ShuttleSpeed, 1, Protoss.Shuttle.buildFrames)) {
        pump(Protoss.Shuttle, 1)
      }
      pump(Protoss.Zealot, 24)
      pump(Protoss.Dragoon)
      pump(Protoss.Zealot)
    } else {
      pump(Protoss.Dragoon, 16)
      pump(Protoss.Zealot)
    }
  }

  def doUpgrades(): Unit = {
    upgradeContinuously(Protoss.GroundDamage)
    if (unitsComplete(Protoss.Forge) > 1 || upgradeComplete(Protoss.GroundDamage, 3)) {
      upgradeContinuously(Protoss.GroundArmor)
    }
  }

  def doTech2Base(): Unit = {
    get(Protoss.DragoonRange)
    get(Protoss.Forge)
    get(Protoss.CitadelOfAdun)
    get(Protoss.GroundDamage)
    get(Protoss.ZealotSpeed)
    get(Protoss.TemplarArchives)
    get(Protoss.PsionicStorm)
  }

  def doTechRobo(): Unit = {
    get(Protoss.RoboticsFacility)
    get(Protoss.Observatory)
    pump(Protoss.Observer, if (enemies(Zerg.Lurker) > 1) 3 else if (enemyLurkersLikely) 2 else 1)
    if (enemyLurkersLikely && safeAtHome) {
      if (upgradeComplete(Protoss.ObserverSpeed)) upgradeContinuously(Protoss.ObserverVisionRange) else upgradeContinuously(Protoss.ObserverSpeed)
    }
  }

  def doEndgame(): Unit = {
    get(8, Protoss.Gateway)
    requireBases(3)
    get(Protoss.RoboticsSupportBay)
    get(Protoss.ShuttleSpeed)
    get(Protoss.HighTemplarEnergy)
    requireMiningBases(3)
    get(12, Protoss.Gateway)
    requireBases(4)
    get(14, Protoss.Gateway)
  }

  def doExpand(): Unit = {
    if (With.units.ours.filter(Protoss.Nexus).forall(_.complete)) {
      requireMiningBases(
        Math.min(
          miningBases + 1,
          2 + Math.min(unitsComplete(MatchWarriors) / 20, 2)))
    }
  }
}

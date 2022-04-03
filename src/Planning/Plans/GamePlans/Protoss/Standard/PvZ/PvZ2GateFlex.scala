package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plans.GamePlans.GameplanImperative
import Planning.Plans.Macro.Automatic.{Enemy, Flat, Friendly}
import Planning.Plans.Macro.Protoss.MeldArchons
import Utilities.UnitMatchers.MatchWarriors
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.DoQueue
import Utilities.Time.{GameTime, Minutes}

class PvZ2GateFlex extends GameplanImperative {

  var anticipateSpeedlings: Boolean = false
  var goSpeedlots: Boolean = false
  var goCorsair: Boolean = true
  var goObserver: Boolean = false

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
      Get(4, Protoss.Pylon),
      Get(Protoss.Assimilator),
      Get(19, Protoss.Probe))
    if (goCorsair) {
      buildOrder(
        Get(Protoss.CyberneticsCore),
        Get(7, Protoss.Zealot),
        Get(21, Protoss.Probe),
        Get(8, Protoss.Zealot),
        Get(1, Protoss.Dragoon))
    } else if (goSpeedlots) {
      buildOrder(
        Get(Protoss.Forge),
        Get(7, Protoss.Zealot),
        Get(21, Protoss.Probe),
        Get(Protoss.GroundDamage),
        Get(9, Protoss.Zealot))
    }
  }
  override def executeMain(): Unit = {
    if (enemyHydralisksLikely) status("Hydras")
    if (enemyMutalisksLikely) status("Mutas")
    if (enemyLurkersLikely) status("Lurkers")
    if (bases < 2) {
      status("Early")
      earlyGame()
    } else {
      status("Late")
      restOfGame()
    }
  }

  private val meldAllArchons = new MeldArchons
  private val meldSomeArchons = new MeldArchons(49)
  def earlyGame(): Unit = {
    // Goal is to be home before any Mutalisks can pop.
    // Optimization: Detect 2 vs 3 hatch muta and stay out the extra 30s vs 3hatch
    scoutOn(Protoss.Gateway, quantity = 2)

    anticipateSpeedlings = enemyStrategy(With.fingerprints.fourPool, With.fingerprints.ninePoolGas, With.fingerprints.ninePoolHatch, With.fingerprints.overpoolGas, With.fingerprints.tenHatchPoolGas, With.fingerprints.twoHatchMain, With.fingerprints.oneHatchGas)
    anticipateSpeedlings ||= enemiesShown(Zerg.Zergling) > 10 && With.frame < GameTime(4, 0)()
    anticipateSpeedlings ||= enemiesShown(Zerg.Zergling) > 12 && With.frame < GameTime(4, 30)()
    anticipateSpeedlings &&= ! enemyHydralisksLikely
    anticipateSpeedlings &&= ! enemyMutalisksLikely
    anticipateSpeedlings &&= ! enemyLurkersLikely
    anticipateSpeedlings ||= enemyHasUpgrade(Zerg.ZerglingSpeed)
    goCorsair = ! anticipateSpeedlings
    goCorsair &&= ! enemyHydralisksLikely
    goCorsair ||= enemyMutalisksLikely
    goSpeedlots = ! goCorsair || enemyBases > 2
    goObserver = With.frame > GameTime(8, 30)()
    goObserver &&= With.units.ours.exists(u => u.intent.toBuild.contains(Protoss.Nexus))
    goObserver &&= safeAtHome
    goObserver ||= enemyLurkersLikely

    val mutaFrame = (if (enemyStrategy(With.fingerprints.threeHatchGas)) GameTime(6, 10) else GameTime(5, 40))()

    var safeOutside = safeToMoveOut
    safeOutside &&= ! goSpeedlots || upgradeComplete(Protoss.GroundDamage, 10) || unitsComplete(Protoss.Zealot) >= 11
    var safeToExpand = safeOutside
    safeToExpand &&= unitsComplete(MatchWarriors) + 2 * unitsComplete(Protoss.Archon) >= 9
    safeToExpand ||= unitsComplete(Protoss.DarkTemplar) > 0 && unitsComplete(Protoss.Corsair) > 0 && enemies(Zerg.Hydralisk) == 0 && enemies(Zerg.Mutalisk) == 0
    var shouldExpand = frame > mutaFrame
    shouldExpand ||= enemiesComplete(Zerg.SunkenColony) > 1
    shouldExpand &&= safeToExpand
    var shouldAttack = safeOutside

    if (anticipateSpeedlings) status("Speedlings")
    if (goSpeedlots) status("Speedlots")
    if (goCorsair) status("Corsair")
    if (goObserver) status("Obs")
    if (safeOutside) status("SafeOutside")
    if (safeToExpand) status("SafeToExpand")
    if (shouldExpand) {
      status("ShouldExpand")
      requireMiningBases(2)
    }
    if (shouldAttack) {
      status("Attack")
      attack()
    }

    if (goCorsair) {
      get(Protoss.Assimilator)
      get(Protoss.CyberneticsCore)
      get(Protoss.Stargate)
      pump(Protoss.Corsair, if (enemyMutalisksLikely) 8 else if (enemyHydralisksLikely) 1 else 5)
    }
    if (goObserver) {
      get(Protoss.Assimilator)
      get(Protoss.CyberneticsCore)
      get(Protoss.RoboticsFacility)
      get(Protoss.Observatory)
      pump(Protoss.Observer, 1)
    }
    if (enemyMutalisksLikely) {
      get(Protoss.DragoonRange)
      pump(Protoss.Dragoon)
    }
    meldAllArchons.update()
    if (units(Protoss.TemplarArchives) > 0) {
      buildOrder(Get(2, Protoss.HighTemplar))
    }
    pump(Protoss.DarkTemplar, 1)
    pump(Protoss.HighTemplar)
    pump(Protoss.Zealot, 12)
    pump(Protoss.Corsair, 1)
    if (goSpeedlots) {
      get(Protoss.Assimilator)
      get(Protoss.Forge)
      get(Protoss.CyberneticsCore)
      get(Protoss.GroundDamage)
      get(Protoss.CitadelOfAdun)
      get(Protoss.ZealotSpeed)
      get(Protoss.TemplarArchives)
    }
    pump(Protoss.Zealot)
    get(Protoss.CitadelOfAdun)
    get(Protoss.TemplarArchives)
    get(Protoss.ZealotSpeed)
    pumpWorkers(oversaturate = true)
    get(4, Protoss.Gateway)
    if (units(Protoss.TemplarArchives) > 0) {
      get(5, Protoss.Gateway)
    }
    requireMiningBases(2)
  }

  def restOfGame(): Unit = {
    val safeFromMutalisks = ! enemyMutalisksLikely || unitsComplete(Protoss.Corsair) >= 5
    val safeFromGround = (upgradeComplete(Protoss.ZealotSpeed) && upgradeComplete(Protoss.GroundDamage)) || enemyMutalisksLikely
    if (safeFromGround && safeFromMutalisks && safeToMoveOut) {
      attack()
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

    var shouldConsiderExpanding = upgradeComplete(Protoss.ZealotSpeed) && unitsComplete(Protoss.Gateway) >= 6 && unitsComplete(Protoss.Observer) > 0
    shouldConsiderExpanding ||= miningBases < 2 && safeAtHome
    if (shouldConsiderExpanding) {
      status("ExpandUnlocked")
      expand()
    }
    if (enemyLurkersLikely) {
      techRobo()
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
    if ( ! techStarted(Protoss.PsionicStorm)) {
      meldAllArchons.update()
    } else {
      meldSomeArchons.update()
    }
    if (units(Protoss.Dragoon) > 1) {
      get(Protoss.DragoonRange)
    }
    if (unitsComplete(Protoss.Corsair) > 5 || (enemies(Zerg.Scourge, Zerg.Mutalisk) == 0 && ! enemyHasUpgrade(Zerg.OverlordSpeed))) {
      pumpRatio(Protoss.DarkTemplar, 1, 3, Seq(Friendly(MatchWarriors, 0.1)))
    }
    if (minerals > 500 && gas < 50) {
      pump(Protoss.Zealot)
    }
    pumpRatio(Protoss.Dragoon, 1, 24, Seq(Friendly(Protoss.Corsair, -1.5), Enemy(Zerg.Mutalisk, 1.0), Enemy(Zerg.Lurker, 1.5)))
    pumpRatio(Protoss.Dragoon, 0, 24, Seq(Flat(-12), Friendly(Protoss.Zealot, 2.0)))
    pumpRatio(Protoss.HighTemplar, 4, 12, Seq(Friendly(MatchWarriors, 0.15)))
    if (upgradeComplete(Protoss.ShuttleSpeed, 1, Protoss.Shuttle.buildFrames)) {
      pump(Protoss.Shuttle, 1)
    }
    pump(Protoss.Zealot, 24)
    pump(Protoss.Dragoon)
    pump(Protoss.Zealot)
  }

  def doUpgrades(): Unit = {
    upgradeContinuously(Protoss.GroundDamage)
    if (unitsComplete(Protoss.Forge) > 1 || upgradeComplete(Protoss.GroundDamage, 3)) {
      upgradeContinuously(Protoss.GroundArmor)
    }
  }

  def doTech2Base(): Unit = {
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

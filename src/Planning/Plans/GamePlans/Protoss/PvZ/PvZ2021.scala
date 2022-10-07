package Planning.Plans.GamePlans.Protoss.PvZ

import Lifecycle.With
import Mathematics.Maff
import Placement.Access.PlaceLabels.{DefendEntrance, DefendHall, Defensive, Wall}
import Placement.Access.{PlaceLabels, PlacementQuery}
import Planning.Plans.GamePlans.All.GameplanImperative
import Planning.Plans.Macro.Automatic.{Enemy, Flat, Friendly}
import Planning.Plans.Macro.Protoss.MeldArchons
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss._
import Utilities.Time.{GameTime, Minutes}
import Utilities.UnitFilters.{IsAll, IsAntiAir, IsWarrior}
import Utilities.{?, DoQueue}

class PvZ2021 extends GameplanImperative {

  var anticipateSpeedlings: Boolean = false
  var goGoons     : Boolean = false
  var goSpeedlots : Boolean = false
  var goCorsair   : Boolean = true
  var goObserver  : Boolean = false

  private def reactVs4Pool(): Unit = {
    if (enemyStrategy(With.fingerprints.fourPool) && unitsComplete(IsWarrior) < 5) {
      once(8, Protoss.Probe)
      once(Protoss.Pylon)
      once(9, Protoss.Probe)
      if (units(Protoss.Forge) > 0) {
        get(3, Protoss.PhotonCannon)
      }
      once(Protoss.Gateway)
      pumpSupply()
      pumpWorkers()
      pump(Protoss.Zealot)
      cancel(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.Nexus)
    }
  }
  private def open910(): Unit = {
    once(8,  Protoss.Probe)
    once(Protoss.Pylon)
    once(9, Protoss.Probe)
    once(Protoss.Gateway)
    once(10, Protoss.Probe)
    once(2, Protoss.Gateway)
    once(11, Protoss.Probe)
    once(Protoss.Zealot)
    once(2, Protoss.Pylon)
    once(2, Protoss.Zealot)
    once(12, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(13, Protoss.Probe)
    once(4, Protoss.Zealot)
    once(14, Protoss.Probe)
    reactVs4Pool()
    once(3, Protoss.Pylon)
    once(15, Protoss.Probe)
    once(5, Protoss.Zealot)
    once(16, Protoss.Probe)
    once(4, Protoss.Pylon)
    once(17, Protoss.Probe)
    once(7, Protoss.Zealot)
    once(18, Protoss.Probe)
    once(5, Protoss.Pylon)
    once(19, Protoss.Probe)
  }
  private def open1012(): Unit = {
    reactVs4Pool()
    once(8,  Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    once(12, Protoss.Probe)
    once(2, Protoss.Gateway)
    scoutOn(Protoss.Gateway, quantity = 2)
    once(13, Protoss.Probe)
    once(Protoss.Zealot)
    once(2, Protoss.Pylon)
    once(15, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(16, Protoss.Probe)
    once(3, Protoss.Pylon)
    once(17, Protoss.Probe)
    once(5, Protoss.Zealot)
    once(18, Protoss.Probe)
  }
  private def openZZCoreZ(): Unit = {
    reactVs4Pool()
    once(8,  Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    scoutOn(Protoss.Gateway)
    once(12, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(13, Protoss.Probe)
    once(Protoss.Zealot)
    once(14, Protoss.Probe)
    once(Protoss.Assimilator)
    once(15, Protoss.Probe)
    once(2, Protoss.Zealot)
    once(16, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    once(17, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(18, Protoss.Probe)
    once(3, Protoss.Pylon)
  }
  private def openGateNexus(): Unit = {
    reactVs4Pool()
    once(8,  Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    once(13, Protoss.Probe)
    once(2,  Protoss.Nexus)
    scoutOn(Protoss.Nexus, quantity = 2)
    once(Protoss.Zealot)
    once(15, Protoss.Probe)
    once(2,  Protoss.Gateway)
  }
  private def ffeQuery: PlacementQuery = {
    new PlacementQuery(Protoss.Pylon)
      .preferBase(With.geography.ourNatural)
      .preferLabelYes(Wall, Defensive, DefendEntrance)
      .preferTile(With.geography.ourNatural.zone.exitNowOrHeart)
  }
  private def openCruddyFFE(): Unit = {
    val o12hh = With.fingerprints.twelveHatchHatch()
    val o12h = With.fingerprints.twelveHatch() || o12hh
    val o10h = With.fingerprints.tenHatch() || o12h
    val op12 = With.fingerprints.twelvePool() || o10h
    val op9 = With.fingerprints.overpool() || op12
    val p9 = With.fingerprints.ninePool() || op9
    once(8,  Protoss.Probe)
    get(Protoss.Pylon, ffeQuery)
    scoutOn(Protoss.Pylon)
    once(11, Protoss.Probe)
    if (o12h) {
      once(13, Protoss.Probe)
      once(2, Protoss.Nexus)
      once(14, Protoss.Probe)
      if (o12hh) {
        get(Protoss.Gateway, ffeQuery)
        once(15, Protoss.Probe)
        once(2, Protoss.Pylon)
        once(16, Protoss.Probe)
        once(Protoss.Assimilator)
        once(17, Protoss.Probe)
        once(Protoss.CyberneticsCore)
        once(18, Protoss.Probe)
        once(Protoss.Zealot)
        once(19, Protoss.Probe)
        once(Protoss.Forge)
      } else {
        once(15, Protoss.Probe)
        get(2, Protoss.Pylon)
        once(16, Protoss.Probe)
        get(Protoss.Forge, ffeQuery)
        once(17, Protoss.Probe)
        get(2, Protoss.PhotonCannon, ffeQuery)
        once(18, Protoss.Probe)
        get(Protoss.Gateway, ffeQuery)
        once(19, Protoss.Probe)
      }
    } else {
      once(Protoss.Forge)
      once(13, Protoss.Probe)
      get(?(anticipateSpeedlings, 5, ?(op9, 1, 2)), Protoss.PhotonCannon, ffeQuery)
      once(2, Protoss.Nexus)
      if (op12) {
        once(15, Protoss.Probe)
      }
      get(2, Protoss.PhotonCannon, ffeQuery)
    }
    once(15, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(16, Protoss.Probe)
    get(Protoss.Gateway, ffeQuery)
    once(17, Protoss.Probe)
    once(Protoss.Assimilator)
    once(18, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    once(19, Protoss.Probe)
    once(Protoss.Zealot)
  }

  override def executeBuild(): Unit = {
    if (PvZ910()) open910()
    if (PvZ1012()) open1012()
    if (PvZZZCoreZ()) openZZCoreZ()
    if (PvZGateNexus()) openGateNexus()
    if (PvZCruddyFFE()) openCruddyFFE()
    if (anticipateSpeedlings && PvZ1BaseCorsair()) {
      get(Protoss.Forge)
      once(19, Protoss.Probe)
      get(2, Protoss.PhotonCannon, new PlacementQuery(Protoss.PhotonCannon).requireLabelYes(PlaceLabels.DefendEntrance))
      once(20, Protoss.Probe)
      once(4, Protoss.Pylon)
      once(7, Protoss.Zealot)
      get(3, Protoss.PhotonCannon, new PlacementQuery(Protoss.PhotonCannon).requireLabelYes(PlaceLabels.DefendEntrance))
    }
    once(Protoss.Assimilator)
    once(19, Protoss.Probe)
  }

  override def executeMain(): Unit = {
    if (enemyHydralisksLikely) status("Hydras")
    if (enemyMutalisksLikely) status("Mutas")
    if (enemyLurkersLikely) status("Lurkers")
    if (bases < 2) { status("Early"); earlyGame() }
    else { status("Late"); restOfGame() }
  }

  private val meldAllArchons = new MeldArchons
  private val meldMostArchons = new MeldArchons(60)
  private val meldSomeArchons = new MeldArchons(49)
  private val speedlingStrategies = Vector(With.fingerprints.fourPool, With.fingerprints.ninePoolGas, With.fingerprints.ninePoolHatch, With.fingerprints.overpoolGas, With.fingerprints.tenHatchPoolGas, With.fingerprints.twoHatchMain, With.fingerprints.oneHatchGas)
  def earlyGame(): Unit = {
    anticipateSpeedlings = enemyStrategy(speedlingStrategies: _*)
    anticipateSpeedlings ||= enemyRecentStrategy(speedlingStrategies: _*) && ! With.scouting.enemyMainFullyScouted
    anticipateSpeedlings ||= enemiesShown(Zerg.Zergling) > 10 && With.frame < GameTime(4, 0)()
    anticipateSpeedlings ||= enemiesShown(Zerg.Zergling) > 12 && With.frame < GameTime(4, 30)()
    anticipateSpeedlings ||= enemyHasUpgrade(Zerg.ZerglingSpeed)
    anticipateSpeedlings &&= ! enemyHydralisksLikely
    anticipateSpeedlings &&= ! enemyMutalisksLikely
    anticipateSpeedlings &&= ! enemyLurkersLikely

    goGoons       = PvZ4GateGoon()
    goGoons     ||= enemiesComplete(Zerg.Mutalisk, Zerg.Spire) > 0 && units(Protoss.Stargate) == 0
    goCorsair     = ! anticipateSpeedlings
    goCorsair   &&= ! enemyHydralisksLikely
    goCorsair   ||= enemyMutalisksLikely
    goCorsair   &&= PvZ1BaseCorsair()
    goSpeedlots   = ! goCorsair || enemyBases > 2
    goSpeedlots &&= ! goGoons
    goObserver    = With.frame > GameTime(8, 30)()
    goObserver  &&= With.units.ours.exists(u => u.intent.toBuild.contains(Protoss.Nexus))
    goObserver  &&= safeAtHome
    goObserver  ||= enemyLurkersLikely

    // Goal is to be home before any Mutalisks can pop.
    // Optimization: Detect 2 vs 3 hatch muta and stay out the extra 30s vs 3hatch
    val armySize        = unitsComplete(IsWarrior) + unitsComplete(Protoss.Archon)
    var safeOutside     = safeToMoveOut
    safeOutside       &&= ! anticipateSpeedlings || upgradeComplete(Protoss.GroundDamage) || upgradeComplete(Protoss.DragoonRange) || armySize >= 12
    var safeToExpand    = safeOutside
    safeToExpand      &&= unitsComplete(IsWarrior) + 2 * unitsComplete(Protoss.Archon) >= 9
    safeToExpand      ||= unitsComplete(Protoss.DarkTemplar) > 0 && unitsComplete(Protoss.Corsair) > 0 && enemies(Zerg.Hydralisk) == 0 && enemies(Zerg.Mutalisk) == 0
    safeToExpand      &&= ! anticipateSpeedlings || (unitsComplete(Protoss.DarkTemplar) > 0 && unitsComplete(Protoss.Archon) > 0)
    val mutaFrame       = (if (enemyStrategy(With.fingerprints.threeHatchGas)) GameTime(6, 10) else GameTime(5, 40))()
    var shouldExpand    = frame > mutaFrame
    shouldExpand      ||= enemiesComplete(Zerg.SunkenColony) > 1
    shouldExpand      ||= PvZFE()
    shouldExpand      &&= safeToExpand
    val shouldAttack    = safeOutside

    if (anticipateSpeedlings) status("Speedlings")
    if (goSpeedlots) status("Speedlots")
    if (goCorsair) status("Corsair")
    if (goObserver) status("Obs")
    if (safeOutside) status("SafeOutside")
    if (safeToExpand) status("SafeToExpand")
    if (shouldExpand) { status("ShouldExpand"); requireMiningBases(2) }
    if (shouldAttack) { status("Attack"); attack() }

    if (goGoons || goCorsair) {
      once(Protoss.CyberneticsCore)
      once(7, Protoss.Zealot)
      once(21, Protoss.Probe)
      if (goGoons) {
        once(2, Protoss.Dragoon)
        once(Protoss.DragoonRange)
        once(4, Protoss.Dragoon)
        once(4, Protoss.Gateway)
      } else {
        once(8, Protoss.Zealot)
        once(1, Protoss.Dragoon)
      }
    } else if (goSpeedlots) {
      once(Protoss.Forge)
      once(7, Protoss.Zealot)
      once(21, Protoss.Probe)
      once(Protoss.GroundDamage)
      once(9, Protoss.Zealot)
    }

    if (goCorsair) {
      get(Protoss.Stargate)
      pump(Protoss.Corsair, ?(enemyMutalisksLikely, 8, ?(enemyHydralisksLikely, 1, 5)))
    }
    if (goObserver) {
      get(Protoss.RoboticsFacility, Protoss.Observatory)
      pump(Protoss.Observer, 1)
    }
    if (enemyMutalisksLikely) {
      get(Protoss.DragoonRange)
      pump(Protoss.Dragoon)
    }
    meldAllArchons.update()
    once(2 * units(Protoss.TemplarArchives), Protoss.HighTemplar)
    pump(Protoss.DarkTemplar, 1)
    pump(Protoss.HighTemplar)
    pump(Protoss.Zealot, 12)
    pump(Protoss.Corsair, 1)
    if (goSpeedlots) {
      get(Protoss.Assimilator, Protoss.Forge, Protoss.CyberneticsCore)
      get(Protoss.GroundDamage)
      get(Protoss.CitadelOfAdun)
      get(Protoss.ZealotSpeed)
      get(Protoss.TemplarArchives)
    }
    if (units(Protoss.Dragoon) > 1) {
      get(Protoss.DragoonRange)
    }
    pump(Protoss.Zealot, 12)
    pump(Protoss.Dragoon)
    get(Protoss.CitadelOfAdun, Protoss.TemplarArchives)
    get(Protoss.ZealotSpeed)
    pumpWorkers(oversaturate = true)
    get(5, Protoss.Gateway)
    requireMiningBases(2)
  }

  def restOfGame(): Unit = {
    val safeFromMutalisks = ! enemyMutalisksLikely || unitsComplete(IsAll(IsWarrior, IsAntiAir)) >= 6
    val safeFromGround = (upgradeComplete(Protoss.ZealotSpeed) && upgradeComplete(Protoss.GroundDamage)) || enemyMutalisksLikely
    // This unitsComplete() check is from observing excess passivity in COG2022 testing
    if (safeFromGround && safeFromMutalisks && (safeToMoveOut || unitsComplete(IsWarrior) >= 24)) {
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

    if (With.fingerprints.threeHatchHydra() && unitsComplete(IsWarrior) < 12) {
      buildCannonsAtNatural(5, PlaceLabels.DefendEntrance)
      trainMainArmy()
    }

    get(Protoss.Gateway, Protoss.Assimilator); get(3, Protoss.Gateway); get(Protoss.Stargate)
    if (saturated || (gas < 200 && units(Protoss.Gateway, Protoss.Stargate) >= 4)) buildGasPumps()

    var shouldConsiderExpanding = upgradeComplete(Protoss.ZealotSpeed) && unitsComplete(Protoss.Gateway) >= 6 && unitsComplete(Protoss.Observer) > 0
    shouldConsiderExpanding ||= miningBases < 2 && safeAtHome
    if (shouldConsiderExpanding) {
      status("ExpandUnlocked")
      expand()
    }
    maintainMiningBases(3)
    recordRequestedBases()
    if (enemyLurkersLikely) {
      techRobo()
    }
    upgrades()
    trainCorsairs()
    val minCannons = Maff.fromBoolean(With.fingerprints.twoHatchGas()) + enemiesComplete(Zerg.Spire)
    val mutaCannons = Maff.clamp(enemies(Zerg.Mutalisk) / 3, minCannons, 3)
    if (mutaCannons > 0) {
      buildCannonsAtBases(mutaCannons, DefendHall)
    }
    if (With.blackboard.wantToAttack()) {
      buildCannonsAtNatural(2, DefendHall)
    }

    if (safeAtHome && unitsComplete(IsWarrior) >= 10) {
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
    once(Protoss.Corsair)
    if (enemyMutalisksLikely) {
      pumpRatio(Protoss.Corsair, 7, 12, Seq(Enemy(Zerg.Mutalisk, 1.0), Enemy(Zerg.Scourge, 0.5)))
      get(2, Protoss.Stargate)
      get(Protoss.AirDamage)
      if (upgradeComplete(Protoss.AirDamage)) {
        get(Protoss.AirArmor)
      }
    } else if (safeAtHome || unitsComplete(IsWarrior) >= 8) {
      pumpRatio(Protoss.Corsair, 1, 4, Seq(Friendly(IsWarrior, 0.1)))
    }
  }

  def doTrainMainArmy(): Unit = {
    if ( ! techStarted(Protoss.PsionicStorm)) {
      meldAllArchons.update()
    } else if (unitsComplete(IsWarrior) < 12) {
      meldMostArchons.update()
    } else {
      meldSomeArchons.update()
    }
    if (units(Protoss.Dragoon) > 1) {
      get(Protoss.DragoonRange)
    }
    if (unitsComplete(Protoss.Corsair) > 5 || (enemies(Zerg.Scourge, Zerg.Mutalisk) == 0 && ! enemyHasUpgrade(Zerg.OverlordSpeed))) {
      pumpRatio(Protoss.DarkTemplar, 1, 3, Seq(Friendly(IsWarrior, 0.1)))
    }
    if (minerals > 500 && gas < 50) {
      pump(Protoss.Zealot)
    }
    pumpRatio(Protoss.HighTemplar, 4, 12, Seq(Friendly(IsWarrior, 0.15)))
    pumpRatio(Protoss.Dragoon, 1, 24, Seq(Friendly(Protoss.Corsair, -1.5), Enemy(Zerg.Mutalisk, 1.0), Enemy(Zerg.Lurker, 1.5)))
    pumpRatio(Protoss.Dragoon, 0, 24, Seq(Flat(-12), Friendly(Protoss.Zealot, 2.0)))
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
    approachMiningBases(2 + Math.min(unitsComplete(IsWarrior) / 20, 2))
  }
}

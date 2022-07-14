package Planning.Plans.GamePlans.Protoss.PvP

import Debugging.SimpleString
import Lifecycle.With
import Macro.Requests.Get
import Mathematics.Maff
import Mathematics.Points.Pixel
import Placement.Access.PlaceLabels
import Planning.Plans.GamePlans.All.GameplanImperative
import Planning.Plans.Macro.Automatic.{Enemy, Flat, Friendly}
import ProxyBwapi.Races.Protoss
import Utilities.Time.{Forever, Frames, GameTime, Minutes, Seconds}
import Utilities.UnitFilters.IsWarrior
import Utilities._

class PvPLateGame extends GameplanImperative {
  trait PrimaryTech extends SimpleString
  object RoboTech extends PrimaryTech
  object TemplarTech extends PrimaryTech

  var fearDeath           : Boolean = _
  var fearDT              : Boolean = _
  var fearMacro           : Boolean = _
  var fearContain         : Boolean = _
  var dtBraveryAbroad     : Boolean = _
  var dtBraveryHome       : Boolean = _
  var expectCarriers      : Boolean = _
  var shouldDetect        : Boolean = _
  var shouldSecondaryTech : Boolean = _
  var shouldHarass        : Boolean = _
  var shouldAttack        : Boolean = _
  var shouldExpand        : Boolean = _
  var shouldMindControl   : Boolean = _
  var dtDebut             : Int     = Forever()
  var obsDebut            : Int     = Forever()
  var obsArrival          : Int     = Forever()
  var primaryTech         : Option[PrimaryTech] = None

  def primaryTemplar      : Boolean = primaryTech.contains(TemplarTech)
  def primaryRobo         : Boolean = primaryTech.contains(RoboTech)

  override def executeBuild(): Unit = {
    updateDTBravery()
    fearDeath   = ! dtBraveryAbroad && ! enemyStrategy(With.fingerprints.dtRush, With.fingerprints.robo) && ( ! safeAtHome || unitsComplete(IsWarrior) < 8 || (PvPIdeas.recentlyExpandedFirst && unitsComplete(Protoss.Shuttle) * unitsComplete(Protoss.Reaver) < 2))
    fearMacro   = miningBases < Math.max(2, enemyBases)
    fearDT      = enemyDarkTemplarLikely && unitsComplete(Protoss.Observer) == 0 && (enemies(Protoss.DarkTemplar) > 0 && unitsComplete(Protoss.PhotonCannon) == 0)
    fearContain = With.scouting.enemyProximity > 0.6 && ! dtBraveryHome

    expectCarriers = enemyCarriersLikely || (With.fingerprints.forgeFe() && enemies(IsWarrior) == 0 && (With.frame > Minutes(8)() || enemies(Protoss.PhotonCannon) > 3))
    shouldMindControl = false // expectCarriers
    shouldDetect = enemyDarkTemplarLikely
    shouldDetect ||= enemyHasShown(Protoss.ArbiterTribunal, Protoss.Arbiter)
    shouldExpand = fearMacro || ! fearDeath
    shouldExpand &&= ! fearContain
    shouldExpand &&= ! fearDT
    shouldExpand &&= (
      miningBases < 2
      || enemyBases > miningBases
      || (expectCarriers && With.frame > GameTime(3, 30)() * miningBases)
      || Math.min(unitsComplete(IsWarrior) / 16, unitsComplete(Protoss.Gateway) / 3) >= miningBases)
    shouldExpand ||= miningBases < 1
    shouldHarass = fearMacro || fearContain || upgradeComplete(Protoss.ShuttleSpeed)
    shouldHarass = Protoss.PsionicStorm() && Protoss.ShuttleSpeed()
    shouldAttack = PvPIdeas.shouldAttack
    shouldAttack ||= unitsComplete(Protoss.Gateway) >= targetGateways
    shouldAttack ||= (dtBraveryAbroad && enemiesComplete(Protoss.PhotonCannon) == 0)
    shouldAttack ||= shouldHarass
    shouldAttack &&= ! fearDeath
    shouldAttack &&= ! fearDT
    shouldAttack ||= shouldExpand
    shouldSecondaryTech = miningBases > 2
    shouldSecondaryTech ||= (unitsComplete(Protoss.Reaver) > 2 && unitsComplete(Protoss.Shuttle) > 0)
    shouldSecondaryTech ||= fearDT && primaryTech.contains(TemplarTech)
    shouldSecondaryTech &&= unitsComplete(Protoss.Gateway) >= Math.min(7, targetGateways)
    shouldSecondaryTech &&= With.units.ours.filter(Protoss.Nexus).forall(_.complete)
    shouldSecondaryTech &&= gasPumps > 1
    shouldSecondaryTech &&= miningBases > 1
    shouldSecondaryTech &&= ! fearDeath
    shouldSecondaryTech ||= units(Protoss.RoboticsSupportBay) > 0 && units(Protoss.TemplarArchives) > 0
    oversaturate = shouldExpand && ! fearDeath && ! fearContain

    lazy val commitToTech = unitsComplete(Protoss.Gateway) >= 5 && saturated
    primaryTech = primaryTech
      .orElse(Some(RoboTech).filter(x => units(Protoss.RoboticsFacility) > 0))
      .orElse(Some(TemplarTech).filter(x => units(Protoss.CitadelOfAdun, Protoss.TemplarArchives, Protoss.PhotonCannon) > 0))
      .orElse(Some(RoboTech).filter(x => enemyDarkTemplarLikely))
      .orElse(Some(RoboTech).filter(x => With.fingerprints.cannonRush()))
      .orElse(Some(TemplarTech).filter(x => commitToTech && shouldMindControl))
      .orElse(Some(TemplarTech).filter(x => commitToTech && bases > 2))
      .orElse(Some(TemplarTech).filter(x => commitToTech && ! enemyRobo && roll("PrimaryTechTemplar", if (With.fingerprints.fourGateGoon()) 0.5 else 0.25)))
      .orElse(Some(TemplarTech).filter(x => commitToTech && units(Protoss.Zealot) >= 5))
      .orElse(Some(RoboTech).filter(x => commitToTech))

    if (PvPIdeas.recentlyExpandedFirst) status("Pioneer")
    if (obsDebut < Forever()) status(f"ObsDebut@${Frames(obsDebut)}")
    if (obsArrival < Forever()) status(f"ObsArrival@${Frames(obsArrival)}")
    if (dtDebut < Forever()) status(f"DTDebut@${Frames(dtDebut)}")
    if (dtBraveryHome) status("DTBraveryHome")
    if (dtBraveryAbroad) status("DTBraveryAbroad")
    if (fearDeath) status("FearDeath")
    if (fearDT) status("FearDT")
    if (fearMacro) status("FearMacro")
    if (fearContain) status("FearContain")
    if (expectCarriers) status("ExpectCarriers")
    if (shouldMindControl) status("MindControl")
    if (shouldDetect) status("Detect")
    if (shouldSecondaryTech) status("2ndTech")
    if (shouldExpand) status("ShouldExpand")
    if (shouldAttack) { status("Attack"); attack() }
    if (shouldHarass) { status("Harass"); harass() }
    primaryTech.map(_.toString).foreach(status)
    PvPTools.requireTimelyDetection()
  }

  override def executeMain(): Unit = {
    val trainArmy     = new DoQueue(doTrainArmy)
    val addGates      = new DoQueue(doAddProduction)
    val primaryTech   = new DoQueue(doPrimaryTech)
    val secondaryTech = new DoQueue(doSecondaryTech)
    val expand        = new DoQueue(doExpand)
    val cannons       = new DoQueue(doCannons)

    if (shouldMindControl) { makeDarkArchons() }

    get(Protoss.Gateway)
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
    get(Protoss.DragoonRange)

    if (fearDeath) {
      trainArmy()
      addGates()
    }
    cannons()
    get(3, Protoss.Gateway)
    if (fearContain) {
      primaryTech()
      trainArmy()
      addGates()
    }
    if (shouldExpand) {
      if (enemyBases < 2 && ! enemyHasShown(Protoss.Shuttle, Protoss.Reaver, Protoss.DarkTemplar, Protoss.HighTemplar, Protoss.TemplarArchives) && ! enemyHasUpgrade(Protoss.ZealotSpeed) && ! enemyHasUpgrade(Protoss.ShuttleSpeed)) {
        addGates()
      }
      expand()
    }
    if (minerals > 400 || gas < 100) buildGasPumps()
    primaryTech()
    if (shouldSecondaryTech) {
      secondaryTech()
    }
    trainArmy()
    addGates()
    expand()
    secondaryTech()
  }

  def doTrainArmy(): Unit = {
    // Pump 2 Observer vs. DT. This is important because we refuse to attack without DT backstab protection
    if (enemyDarkTemplarLikely || enemyShownCloakedThreat) pump(Protoss.Observer, 2)
    if (expectCarriers) {
      pumpRatio(Protoss.Dragoon, 16, 64, Seq(Enemy(Protoss.Carrier, 6.0)))
    }
    if (enemies(Protoss.Observer) == 0 || (With.geography.enemyBases.size > 2 && With.geography.enemyBases.exists(b => ! b.units.exists(u => u.player == b.owner && u.unitClass.isDetector)))) {
      pump(Protoss.DarkTemplar, 1)
      buildOrder(Get(2, Protoss.DarkTemplar))
    }
    pumpRatio(Protoss.Dragoon, 0, 100, Seq(Enemy(Protoss.Scout, 2.0), Enemy(Protoss.Shuttle, 2.0), Friendly(Protoss.Zealot, 1.0), Friendly(Protoss.Archon, 3.0)))
    if ( ! expectCarriers && ! fearDeath && ( ! fearContain || ! enemyRobo)) pump(Protoss.Observer, 1)

    if (techStarted(Protoss.PsionicStorm) && upgradeStarted(Protoss.ZealotSpeed)) {
      pumpRatio(Protoss.Dragoon, 8, 24, Seq(Friendly(Protoss.Zealot, 2.0)))
      pumpRatio(Protoss.HighTemplar, 0, 8, Seq(Flat(-1), Friendly(IsWarrior, 1.0 / 4.0)))
      if (shouldHarass) {
        pump(Protoss.Shuttle, 1)
      }
    } else {
      pumpShuttleAndReavers(6, shuttleFirst = unitsComplete(Protoss.RoboticsSupportBay) == 0)
    }
    if (upgradeComplete(Protoss.ZealotSpeed, 1, withinFrames = Protoss.Zealot.buildFrames + Seconds(10)())) {
      pumpRatio(Protoss.Zealot, 2, 16, Seq(Friendly(Protoss.Dragoon, 0.5)))
    }
    pump(Protoss.Dragoon)
    if (techStarted(Protoss.PsionicStorm)) {
      pump(Protoss.HighTemplar)
    }
    pump(Protoss.Zealot)
  }

  def targetGateways: Int = miningBases * 5 - 2 * units(Protoss.RoboticsFacility)

  def doAddProduction(): Unit = {
    if (units(Protoss.RoboticsFacility) > 0) { get(Protoss.RoboticsSupportBay) }
    if (units(Protoss.CitadelOfAdun) > 0 && ! enemyRobo) { get(Protoss.TemplarArchives) }
    get(5, Protoss.Gateway)
    get(2, Protoss.Assimilator)
    if (gas < 400) { buildGasPumps() }
    get(targetGateways, Protoss.Gateway)
  }

  def doPrimaryTech(): Unit = {
    if (primaryTech.contains(RoboTech)) {
      doRobo()
    } else if (primaryTech.contains(TemplarTech)) {
      doTemplar()
    }
  }

  def doSecondaryTech(): Unit = {
    if (primaryTech.contains(RoboTech)) {
      doTemplar()
    } else if (primaryTech.contains(TemplarTech)) {
      doRobo()
    }
  }

  def doExpand(): Unit = {
    get(unitsComplete(Protoss.Nexus) + 1, Protoss.Nexus)
  }

  def doRobo(): Unit = {
    if (unitsComplete(Protoss.Gateway) >= 4 && gas < 400 && unitsComplete(Protoss.Nexus) > 1) {
      buildGasPumps()
    }
    get(Protoss.RoboticsFacility)
    buildOrder(Get(Protoss.Shuttle))
    val getObservers = shouldDetect || ( ! fearDeath && ! fearContain)
    if (getObservers) {
      get(Protoss.Observatory)
      buildOrder(Get(Protoss.Observer))
      if (enemyDarkTemplarLikely || enemyShownCloakedThreat) {
        upgradeContinuously(Protoss.ObserverSpeed)
        if (enemyHasShown(Protoss.Arbiter) && upgradeComplete(Protoss.ObserverSpeed)) {
          get(Protoss.ObserverVisionRange)
        }
      }
    }
    get(Protoss.RoboticsSupportBay)
    if ( ! fearDeath && units(Protoss.Shuttle) > 0 && (units(Protoss.Reaver) > 1 || Protoss.PsionicStorm())) {
      get(Protoss.ShuttleSpeed)
    }
    pump(Protoss.Reaver, 1)
    if (upgradeComplete(Protoss.ShuttleSpeed) && units(Protoss.Reaver) >= 4) {
      get(Protoss.ScarabDamage)
    }
  }

  def doTemplar(): Unit = {
    lazy val readyForStorm = (
      unitsComplete(Protoss.Nexus) > 1
      && unitsComplete(Protoss.Gateway) >= 5
      && unitsComplete(Protoss.Assimilator) > 1
      && unitsComplete(IsWarrior) >= 12
      && (unitsComplete(IsWarrior) >= 24 || enemyStrategy(With.fingerprints.robo, With.fingerprints.dtRush) || techStarted(Protoss.PsionicStorm)))
    if (shouldDetect) { get(Protoss.Forge) }
    get(Protoss.CitadelOfAdun)
    get(Protoss.TemplarArchives)
    if (shouldMindControl) {
      if (upgradeComplete(Protoss.DarkArchonEnergy)) get(Protoss.MindControl) else get(Protoss.DarkArchonEnergy)
    } else if (readyForStorm) {
      get(Protoss.Forge)
      get(Protoss.PsionicStorm)
      if (techStarted(Protoss.PsionicStorm)) {
        buildOrder(Get(2, Protoss.HighTemplar))
      }
      upgradeContinuously(Protoss.GroundDamage)
      get(Protoss.ZealotSpeed)
      if (gasPumps > 2 && techComplete(Protoss.PsionicStorm, withinFrames = Seconds(10)())) {
        get(Protoss.HighTemplarEnergy)
      }
      if (upgradeComplete(Protoss.GroundDamage, 3)) { upgradeContinuously(Protoss.GroundArmor) }
      if (upgradeComplete(Protoss.GroundArmor,  3)) { upgradeContinuously(Protoss.Shields) }
    }
  }

  def doCannons(): Unit = {
    if (units(Protoss.Forge) > 0) {
      if (enemyHasShown(Protoss.Shuttle)) {
        buildCannonsAtBases(1, PlaceLabels.DefendHall)
      } else {
        buildCannonsAtOpenings(1, PlaceLabels.DefendHall)
      }
    }
  }

  def obsTravelFrames(from: Pixel): Int = (from.pixelDistance(With.geography.ourNatural.zone.downtown.center) / Protoss.Observer.topSpeed).toInt
  def newObsDebut(value: Int, origin: Pixel): Unit = {
    obsDebut = Math.min(obsDebut, value)
    obsArrival = Math.min(obsArrival, obsDebut + obsTravelFrames(origin))
  }
  def updateDTBravery(): Unit = {
    if (units(Protoss.TemplarArchives, Protoss.DarkTemplar) == 0) {
      dtBraveryAbroad = false
      dtBraveryHome = false
      return
    }
    val roboFrames = Protoss.RoboticsFacility.buildFrames
    val toryFrames = Protoss.Observatory.buildFrames
    val obsFrames = Protoss.Observer.buildFrames
    lazy val roboOrigin = Maff.minBy(With.units.enemy.filter(Protoss.RoboticsFacility))(_.completionFrame).map(_.pixel).getOrElse(With.scouting.enemyHome.center)

    With.units.enemy.filter(_.isAny(Protoss.Shuttle, Protoss.Reaver, Protoss.RoboticsSupportBay))
                                                      .foreach(r => newObsDebut(r.frameDiscovered           + toryFrames  + obsFrames,  roboOrigin))
    With.units.enemy.filter(Protoss.RoboticsFacility) .foreach(r => newObsDebut(r.remainingCompletionFrames + toryFrames  + obsFrames,  r.pixel))
    With.units.enemy.filter(Protoss.Observatory)      .foreach(o => newObsDebut(o.remainingCompletionFrames               + obsFrames,  roboOrigin))
    With.units.enemy.filter(Protoss.Observer)         .foreach(o => newObsDebut(o.frameDiscovered,                                      o.pixel))

    With.units.ours.filter(_.isAny(Protoss.DarkTemplar, Protoss.TemplarArchives))
      .filter(_.knownToOpponents)
      .map(_.frameKnownToOpponents)
      .foreach(frame => dtDebut = Math.min(dtDebut, frame))

    newObsDebut(dtDebut + roboFrames + toryFrames + obsFrames, roboOrigin)

    dtBraveryAbroad = With.frame < obsDebut
    dtBraveryHome   = With.frame < obsArrival
  }
}

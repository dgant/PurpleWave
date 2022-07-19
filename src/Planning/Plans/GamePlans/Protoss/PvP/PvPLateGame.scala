package Planning.Plans.GamePlans.Protoss.PvP

import Debugging.SimpleString
import Lifecycle.With
import Placement.Access.PlaceLabels
import Planning.Plans.GamePlans.All.GameplanImperative
import Planning.Plans.GamePlans.Protoss.PvP.PvPIdeas._
import Planning.Plans.Macro.Automatic.{Enemy, Flat, Friendly}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvP3GateGoon, PvP4GateGoon}
import Utilities.Time.{GameTime, Minutes, Seconds}
import Utilities.UnitFilters.{IsDetector, IsWarrior}
import Utilities._

class PvPLateGame extends GameplanImperative {
  trait PrimaryTech extends SimpleString
  object RoboTech extends PrimaryTech
  object TemplarTech extends PrimaryTech

  var fearDeath           : Boolean = _
  var fearMacro           : Boolean = _
  var fearContain         : Boolean = _
  var expectCarriers      : Boolean = _
  var shouldSecondaryTech : Boolean = _
  var shouldHarass        : Boolean = _
  var shouldAttack        : Boolean = _
  var shouldExpand        : Boolean = _
  var shouldUpgrade       : Boolean = _
  var shouldReaver        : Boolean = _
  var shouldContain       : Boolean = _
  var primaryTech         : Option[PrimaryTech] = None

  private def productionCapacity        : Int = unitsComplete(Protoss.Gateway) + ?(shouldReaver, 2 * unitsComplete(Protoss.RoboticsFacility) * Math.min(1, unitsComplete(Protoss.RoboticsSupportBay)), 0)
  private def targetProductionCapacity  : Int = 1 + miningBases * 3
  private def targetGateways            : Int = targetProductionCapacity - ?(shouldReaver, 2 * units(Protoss.RoboticsFacility), 0)

  override def executeBuild(): Unit = {
    fearDeath   = ! dtBraveryHome && ! enemyStrategy(With.fingerprints.dtRush, With.fingerprints.robo) && ( ! safeAtHome || unitsComplete(IsWarrior) < 8 || (recentlyExpandedFirst && unitsComplete(Protoss.Shuttle) * unitsComplete(Protoss.Reaver) < 2))
    fearMacro   = miningBases < Math.max(2, enemyBases)
    fearContain = With.scouting.enemyProximity > 0.6 && ! dtBraveryAbroad
    fearContain ||= enemyDarkTemplarLikely && unitsComplete(Protoss.Observer) == 0

    expectCarriers = enemyCarriersLikely || (With.fingerprints.forgeFe() && enemies(IsWarrior) == 0 && (With.frame > Minutes(8)() || enemies(Protoss.PhotonCannon) > 3))
    shouldExpand = fearMacro || ! fearDeath
    shouldExpand &&= ( ! fearContain || With.geography.safeExpansions.nonEmpty)
    shouldExpand &&= (fearMacro || (expectCarriers && With.frame > GameTime(3, 30)() * miningBases) || miningBases <= Math.min(unitsComplete(IsWarrior) / 14, productionCapacity / 3))
    shouldExpand ||= miningBases < 1
    shouldHarass = Protoss.PsionicStorm()
    shouldHarass ||= enemyBases > 2
    shouldHarass ||= fearContain && ! fearDeath
    shouldContain = (PvP3GateGoon() || PvP4GateGoon()) && ! With.fingerprints.threeGateGoon() && ! With.fingerprints.fourGateGoon()
    shouldContain &&= With.geography.enemyBases.filter(_.isStartLocation).exists(_.natural.exists(With.scouting.weControl))
    shouldContain &&= ! fearDeath
    shouldAttack = PvPIdeas.shouldAttack
    shouldAttack ||= dtBraveryAbroad && enemiesComplete(Protoss.PhotonCannon) == 0
    shouldAttack &&= ! fearDeath
    shouldAttack ||= shouldExpand
    shouldAttack ||= shouldContain
    if (units(Protoss.RoboticsFacility) * units(Protoss.TemplarArchives) == 0) {
      shouldSecondaryTech = miningBases > 2
      shouldSecondaryTech ||= (unitsComplete(Protoss.Reaver) > 2 && unitsComplete(Protoss.Shuttle) > 0)
      shouldSecondaryTech ||= enemyDarkTemplarLikely && primaryTech.contains(TemplarTech)
      shouldSecondaryTech ||= unitsComplete(IsWarrior) >= 30
      shouldSecondaryTech &&= productionCapacity >= 7
      shouldSecondaryTech &&= With.units.ours.filter(Protoss.Nexus).forall(_.complete)
      shouldSecondaryTech &&= gasPumps > 1
      shouldSecondaryTech &&= miningBases > 1
      shouldSecondaryTech &&= ! fearDeath
      shouldSecondaryTech ||= units(Protoss.RoboticsSupportBay) > 0 && units(Protoss.TemplarArchives) > 0
      shouldSecondaryTech ||= primaryTech.contains(RoboTech) && gas > 800
      shouldSecondaryTech ||= unitsComplete(IsWarrior) >= 40
    }
    shouldReaver = primaryTech.contains(RoboTech) || upgradeStarted(Protoss.ScarabDamage)
    shouldReaver &&= units(Protoss.TemplarArchives) == 0 || enemies(Protoss.PhotonCannon) > 4
    oversaturate = fearContain && ! fearDeath

    lazy val commitToTech = productionCapacity >= 5 && saturated
    primaryTech = primaryTech
      .orElse(Some(RoboTech)    .filter(x => units(Protoss.RoboticsFacility) > 0))
      .orElse(Some(TemplarTech) .filter(x => units(Protoss.CitadelOfAdun, Protoss.TemplarArchives, Protoss.PhotonCannon) > 0))
      .orElse(Some(RoboTech)    .filter(x => enemyDarkTemplarLikely))
      .orElse(Some(RoboTech)    .filter(x => With.fingerprints.cannonRush()))
      .orElse(Some(RoboTech)    .filter(x => enemies(Protoss.PhotonCannon) > 2 && roll("RoboVsCannon", 0.7)))
      .orElse(Some(TemplarTech) .filter(x => commitToTech && bases > 2))
      .orElse(Some(TemplarTech) .filter(x => commitToTech && ! enemyRobo && roll("PrimaryTechTemplar", 0.7)))
      .orElse(Some(TemplarTech) .filter(x => commitToTech && units(Protoss.Zealot) >= 5))
      .orElse(Some(TemplarTech) .filter(x => commitToTech && upgradeStarted(Protoss.GroundDamage)))
      .orElse(Some(RoboTech)    .filter(x => commitToTech))

    if (PvPIdeas.recentlyExpandedFirst) status("Pioneer")
    if (dtBraveryHome) status("DTBraveHome")
    if (dtBraveryAbroad) status("DTBraveAbroad")
    if (fearDeath) status("FearDeath")
    if (fearMacro) status("FearMacro")
    if (fearContain) status("FearContain")
    if (expectCarriers) status("ExpectCarriers")
    if (shouldReaver) status("Reaver")
    if (shouldSecondaryTech) status("2Tech")
    if (shouldExpand) status("Expand")
    if (shouldAttack) { status("Attack"); attack() }
    if (shouldHarass) { status("Harass"); harass() }
    primaryTech.map(_.toString.replaceAll("Tech", "")).foreach(status)
    PvPIdeas.requireTimelyDetection()
  }

  override def executeMain(): Unit = {
    val trainArmy     = new DoQueue(doTrainArmy)
    val addGates      = new DoQueue(doAddProduction)
    val primaryTech   = new DoQueue(doPrimaryTech)
    val secondaryTech = new DoQueue(doSecondaryTech)
    val expand        = new DoQueue(doExpand)
    val cannons       = new DoQueue(doCannons)

    get(Protoss.Gateway, Protoss.Assimilator, Protoss.CyberneticsCore)
    get(Protoss.DragoonRange)

    if (shouldExpand) {
      expand()
    }
    if (recentlyExpandedFirst && dtBraveryAbroad) {
      addGates()
    }
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

  private def doTrainArmy(): Unit = {
    if (expectCarriers) {
      pumpRatio(Protoss.Dragoon, 16, 64, Seq(Enemy(Protoss.Carrier, 6.0)))
    }
    if (enemies(Protoss.Observer) == 0 || (With.geography.enemyBases.size > 2 && With.geography.enemyBases.exists( ! _.enemies.exists(IsDetector)))) {
      pump(Protoss.DarkTemplar, 1)
      once(2, Protoss.DarkTemplar)
    }
    pumpRatio(Protoss.Dragoon, 0, 100, Seq(Enemy(Protoss.Scout, 2.0), Enemy(Protoss.Shuttle, 2.0), Friendly(Protoss.Zealot, 1.0), Friendly(Protoss.Archon, 3.0)))
    if ( ! expectCarriers && ! fearDeath && ( ! fearContain || ! enemyRobo)) {
      pump(Protoss.Observer, 1)
    }

    if (techStarted(Protoss.PsionicStorm) && upgradeStarted(Protoss.ZealotSpeed)) {
      pumpRatio(Protoss.Dragoon, 8, 24, Seq(Friendly(Protoss.Zealot, 2.0)))
      pumpRatio(Protoss.HighTemplar, 0, 8, Seq(Flat(-1), Friendly(IsWarrior, 1.0 / 4.0)))
      if (shouldHarass) {
        pump(Protoss.Shuttle, 1)
      }
    }
    if (shouldReaver) {
      pumpShuttleAndReavers(6, shuttleFirst = unitsComplete(Protoss.RoboticsSupportBay) == 0)
    }
    pump(Protoss.Observer, 2)

    if (upgradeComplete(Protoss.ZealotSpeed, 1, withinFrames = Protoss.Zealot.buildFrames + Seconds(10)())) {
      pumpRatio(Protoss.Zealot, 2, 16, Seq(Friendly(Protoss.Dragoon, 0.5)))
    }
    pump(Protoss.Dragoon)
    if (techStarted(Protoss.PsionicStorm)) {
      pump(Protoss.HighTemplar)
    }
    pump(Protoss.Zealot)
  }

  private def doAddProduction(): Unit = {
    if (units(Protoss.RoboticsFacility) > 0 && shouldReaver) {
      get(Protoss.RoboticsSupportBay)
    }
    if (units(Protoss.CitadelOfAdun) > 0 && ! enemyRobo) {
      get(Protoss.TemplarArchives)
    }
    get(6, Protoss.Gateway)
    if (gas < 300) { buildGasPumps() }
    get(targetGateways + miningBases, Protoss.Gateway)
  }

  private def doPrimaryTech(): Unit = {
    if (primaryTech.contains(RoboTech)) {
      doRobo()
    } else if (primaryTech.contains(TemplarTech)) {
      doTemplar()
    }
  }

  private def doSecondaryTech(): Unit = {
    if (primaryTech.contains(RoboTech)) {
      doTemplar()
    } else if (primaryTech.contains(TemplarTech)) {
      doRobo()
    }
  }

  private def doExpand(): Unit = {
    get(unitsComplete(Protoss.Nexus) + 1, Protoss.Nexus)
  }

  private def doRobo(): Unit = {
    if (productionCapacity >= 5 && gas < 300) {
      buildGasPumps()
    }
    get(Protoss.RoboticsFacility)
    if (shouldReaver || techStarted(Protoss.PsionicStorm)) {
      once(Protoss.Shuttle)
    }

    if ( ! fearDeath || enemyHasShown(Protoss.Arbiter)) {
      get(Protoss.Observatory)
      once(Protoss.Observer)
      if (enemyDarkTemplarLikely || enemyShownCloakedThreat) {
        upgradeContinuously(Protoss.ObserverSpeed)
        if (enemyHasShown(Protoss.Arbiter) && Protoss.ObserverSpeed()) {
          get(Protoss.ObserverVisionRange)
        }
      }
    }
    if (shouldReaver || Protoss.PsionicStorm()) {
      get(Protoss.RoboticsSupportBay)
    }
    if ( ! fearDeath && units(Protoss.Shuttle) > 0 && (units(Protoss.Reaver) > 1 || Protoss.PsionicStorm())) {
      get(Protoss.ShuttleSpeed)
    }
    if (shouldReaver) {
      pump(Protoss.Reaver, 1)
      if (Protoss.ShuttleSpeed() && units(Protoss.Reaver) >= 4) {
        get(Protoss.ScarabDamage)
      }
    }
  }

  private def doTemplar(): Unit = {
    lazy val readyForStorm = (
      unitsComplete(Protoss.Nexus) > 1
      && unitsComplete(Protoss.Gateway) >= 5
      && unitsComplete(Protoss.Assimilator) > 1
      && unitsComplete(IsWarrior) >= 12
      && (unitsComplete(IsWarrior) >= 24 || enemyStrategy(With.fingerprints.robo, With.fingerprints.dtRush) || techStarted(Protoss.PsionicStorm)))
    get(Protoss.CitadelOfAdun, Protoss.TemplarArchives, Protoss.Forge)
    if (readyForStorm) {
      status("Ready4Storm")
      get(Protoss.PsionicStorm)
      get(2, Protoss.HighTemplar)
    }
    upgradeContinuously(Protoss.GroundDamage)
    get(Protoss.ZealotSpeed)
    if (readyForStorm) {
      if (gasPumps > 2) get(Protoss.HighTemplarEnergy)
      upgradeContinuously(Protoss.GroundArmor)
    }
  }

  private def doCannons(): Unit = {
    if (units(Protoss.Forge) > 0) {
      buildCannonsAtBases(1, PlaceLabels.DefendHall)
    }
  }
}

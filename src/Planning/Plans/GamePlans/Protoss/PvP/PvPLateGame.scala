package Planning.Plans.GamePlans.Protoss.PvP

import Debugging.SimpleString
import Lifecycle.With
import Placement.Access.PlaceLabels
import Planning.Plans.GamePlans.All.GameplanImperative
import Planning.Plans.GamePlans.Protoss.PvP.PvPIdeas._
import Planning.Plans.Macro.Automatic.{Enemy, Flat, Friendly}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvP3GateGoon, PvP4GateGoon, PvPCoreExpand, PvPDT}
import Utilities.Time.{Minutes, Seconds}
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
  var primaryTech         : Option[PrimaryTech] = None

  private def productionCapacity        : Int = unitsComplete(Protoss.Gateway) + ?(shouldReaver, 2 * unitsComplete(Protoss.RoboticsFacility) * Math.min(1, unitsComplete(Protoss.RoboticsSupportBay)), 0)
  private def targetProductionCapacity  : Int = miningBases * 4
  private def targetGateways            : Int = targetProductionCapacity - ?(shouldReaver, 2 * units(Protoss.RoboticsFacility), 0)

  override def executeBuild(): Unit = {
    expectCarriers = enemyCarriersLikely || (With.fingerprints.forgeFe() && enemies(IsWarrior) == 0 && (With.frame > Minutes(8)() || enemies(Protoss.PhotonCannon) > 3))

    lazy val commitToTech = productionCapacity >= 5 && saturated
    primaryTech = primaryTech
      .orElse(Some(RoboTech)    .filter(x => units(Protoss.RoboticsFacility) > 0))
      .orElse(Some(TemplarTech) .filter(x => units(Protoss.TemplarArchives) > 0))
      .orElse(Some(TemplarTech) .filter(x => With.units.ours.filter(Protoss.CitadelOfAdun).exists( ! _.knownToOpponents)))
      .orElse(Some(RoboTech)    .filter(x => enemyDarkTemplarLikely))
      .orElse(Some(RoboTech)    .filter(x => With.fingerprints.cannonRush()))
      .orElse(Some(RoboTech)    .filter(x => enemies(Protoss.PhotonCannon) > 2 && roll("RoboVsCannon", 0.7)))
      .orElse(Some(TemplarTech) .filter(x => commitToTech && bases > 2))
      .orElse(Some(TemplarTech) .filter(x => commitToTech && ! enemyRobo && roll("PrimaryTechTemplar", 0.4)))
      .orElse(Some(TemplarTech) .filter(x => commitToTech && units(Protoss.Zealot) >= 5))
      .orElse(Some(TemplarTech) .filter(x => commitToTech && upgradeStarted(Protoss.GroundDamage)))
      .orElse(Some(RoboTech)    .filter(x => commitToTech))
    shouldReaver = primaryTech.contains(RoboTech) || upgradeStarted(Protoss.ScarabDamage)
    //shouldReaver &&= units(Protoss.TemplarArchives) == 0 || enemies(Protoss.PhotonCannon) > 4

    fearDeath   = ! safeAtHome
    fearDeath   ||= unitsComplete(IsWarrior) < 8
    fearDeath   ||= recentlyExpandedFirst && ! PvP4GateGoon() && ( ! PvP3GateGoon() || With.fingerprints.fourGateGoon()) && unitsComplete(Protoss.Reaver) * unitsComplete(Protoss.Shuttle) < 2
    fearDeath   &&= ! dtBraveryHome
    fearMacro   = miningBases < Math.max(2, enemyBases)
    fearContain = With.scouting.enemyProximity > 0.6 && ! dtBraveryAbroad
    fearContain ||= enemyDarkTemplarLikely && unitsComplete(Protoss.Observer) == 0
    shouldExpand = productionCapacity >= targetProductionCapacity
    shouldExpand &&= ! fearDeath || (fearMacro && miningBases < 3)
    shouldExpand &&= ! fearContain || With.geography.safeExpansions.nonEmpty
    shouldExpand &&= With.geography.ourBases.forall(With.scouting.weControl)
    shouldExpand ||= unitsComplete(IsWarrior) >= miningBases * 20
    shouldHarass = Protoss.PsionicStorm()
    shouldHarass ||= enemyBases > 2
    shouldHarass ||= fearContain && ! fearDeath
    shouldAttack = enemyMiningBases > miningBases
    shouldAttack ||= bases > 2
    shouldAttack ||= bases > miningBases
    shouldAttack ||= ! recentlyExpandedFirst
    shouldAttack ||= dtBraveryAbroad && enemiesComplete(Protoss.PhotonCannon) == 0
    shouldAttack ||= With.geography.enemyBases.exists(_.natural.exists(With.scouting.weControl)) && employing(PvP3GateGoon, PvP4GateGoon) && ! enemyStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)
    shouldAttack &&= PvPIdeas.pvpSafeToMoveOut
    shouldAttack &&= ! fearDeath
    shouldAttack ||= shouldExpand && With.geography.safeExpansions.isEmpty
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

    ///////////////////////////
    // High-priority builds! //
    ///////////////////////////

    PvPIdeas.requireTimelyDetection()
    if (recentlyExpandedFirst) {
      if (PvPCoreExpand()) {
        if (units(Protoss.Gateway) < 3) {
          pumpSupply()
          doTrainArmy()
          get(3, Protoss.Gateway)
        }
      } else if (PvPDT()) {
        pump(Protoss.DarkTemplar, 1)
        pumpSupply()
        get(5, Protoss.Gateway)
        shouldAttack = false
      } else {
        get(units(Protoss.RoboticsSupportBay), Protoss.RoboticsFacility)
        get(4 - 2 * units(Protoss.RoboticsSupportBay), Protoss.Gateway)
      }
      if ( ! With.scouting.enemyNaturalPossiblyMining) {
        pumpSupply()
        doTrainArmy()
        get(6, Protoss.Gateway)
      }
    }

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
  }

  override def executeMain(): Unit = {
    val trainArmy     = new DoQueue(doTrainArmy)
    val addProduction = new DoQueue(doAddProduction)
    val primaryTech   = new DoQueue(doPrimaryTech)
    val secondaryTech = new DoQueue(doSecondaryTech)
    val expand        = new DoQueue(doExpand)
    val cannons       = new DoQueue(doCannons)

    get(Protoss.Gateway, Protoss.Assimilator, Protoss.CyberneticsCore)
    get(Protoss.DragoonRange)

    if (shouldExpand) {
      expand()
    }
    if (fearDeath) {
      trainArmy()
      addProduction()
    }
    get(3, Protoss.Gateway)
    cannons()
    primaryTech()
    if (shouldSecondaryTech) {
      secondaryTech()
    }
    trainArmy()
    addProduction()
    expand()
    secondaryTech()
  }

  private def doTrainArmy(): Unit = {
    if (expectCarriers) {
      pumpRatio(Protoss.Dragoon, 16, 64, Seq(Enemy(Protoss.Carrier, 6.0)))
    }
    if (enemies(Protoss.Observer) == 0 || (With.geography.enemyBases.size > 2 && With.geography.enemyBases.exists( ! _.enemies.exists(IsDetector)))) {
      pump(Protoss.DarkTemplar, 1)
      if (unitsComplete(Protoss.DarkTemplar) == 0) {
        once(2, Protoss.DarkTemplar)
      }
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
    get(?(units(Protoss.RoboticsSupportBay) > 0, 4, 6), Protoss.Gateway)
    doUpgrades()
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
    if (productionCapacity >= 5 && gas < 250) {
      buildGasPumps()
    }
    get(Protoss.RoboticsFacility)
    if (shouldReaver || techStarted(Protoss.PsionicStorm)) {
      once(Protoss.Shuttle)
    }

    if ( ! fearDeath || enemyHasShown(Protoss.Arbiter)) {
      get(Protoss.Observatory)
      once(Protoss.Observer)
      if (units(IsWarrior) >= 12 && (enemyDarkTemplarLikely || enemyShownCloakedThreat)) {
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
    get(Protoss.CitadelOfAdun, Protoss.TemplarArchives, Protoss.Forge)
    if (productionCapacity >= 6 && units(IsWarrior) >= 16) {
      doUpgrades()
      if (gas < 250) {
        buildGasPumps()
      }
      val readyForStorm = techStarted(Protoss.PsionicStorm) || (gasPumps > 1 && (unitsComplete(IsWarrior) >= 24 || enemyStrategy(With.fingerprints.robo, With.fingerprints.dtRush)))
      if (readyForStorm) {
        status("Ready4Storm")
        get(Protoss.PsionicStorm)
        get(2, Protoss.HighTemplar)
        get(Protoss.HighTemplarEnergy)
      }
    }
  }

  private def doUpgrades(): Unit = {
    get(Protoss.Forge)
    if (units(Protoss.TemplarArchives) > 0) {
      upgradeContinuously(Protoss.GroundDamage)
      upgradeContinuously(Protoss.GroundArmor)
    } else {
      get(Protoss.GroundDamage)
      get(Protoss.GroundArmor)
    }
    if (gas < 300) {
      get(Protoss.CitadelOfAdun)
      get(Protoss.ZealotSpeed)
    }
  }

  private def doCannons(): Unit = {
    if (units(Protoss.Forge) > 0) {
      if (units(Protoss.Observer) == 0) {
        buildCannonsAtMain(1, PlaceLabels.DefendHall)
        buildCannonsAtNatural(1, PlaceLabels.DefendEntrance)
      }
      With.geography.ourBasesAndSettlements.view
        .filterNot(_.isOurMain)
        .filterNot(_.isOurNatural)
        .foreach(base => {
          val coverHall = base.zone.edges.exists(_.pixelCenter.pixelDistance(base.townHallArea.center) > 32 * 15)
          buildDefensesAt(1, Protoss.PhotonCannon, Seq(if (coverHall) PlaceLabels.DefendHall else PlaceLabels.DefendEntrance), Seq(base))
        })
    }
  }
}

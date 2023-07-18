package Planning.Plans.GamePlans.Protoss.PvP

import Debugging.SimpleString
import Lifecycle.With
import Placement.Access.PlaceLabels
import Planning.Plans.GamePlans.All.GameplanImperative
import Planning.Plans.GamePlans.Protoss.PvP.PvPIdeas._
import Planning.Plans.Macro.Automatic.{Enemy, Flat, Friendly}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
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

  private def unitsNotStrengthening(player: PlayerInfo): Iterable[UnitInfo] = With.units.ever
    .filter(_.player == player)
    .filterNot(Protoss.Scarab)
    .filter(u =>
      ! u.alive
      || u.isAny(Protoss.Nexus, Protoss.Forge, Protoss.PhotonCannon, Protoss.CitadelOfAdun, Protoss.TemplarArchives, Protoss.RoboticsFacility, Protoss.Observatory, Protoss.Observer))
    .filterNot(u => Protoss.Nexus(u) && u.base.exists(_.isStartLocation))

  private def skimEquivalent(unit: UnitInfo): Double = unit.subjectiveValue * Protoss.Zealot.skimulationValue / Protoss.Zealot.subjectiveValue

  override def executeBuild(): Unit = {
    expectCarriers = enemyCarriersLikely || (With.fingerprints.forgeFe() && ! enemiesHave(IsWarrior) && (With.frame > Minutes(8)() || enemies(Protoss.PhotonCannon) > 3))

    lazy val nonStrengthUs              = unitsNotStrengthening(With.self).map(skimEquivalent).sum
    lazy val nonStrengthEnemy           = unitsNotStrengthening(With.enemy).map(skimEquivalent).sum
    lazy val estimatedArmyDifferential  = With.battles.globalAttack.differential + nonStrengthEnemy - nonStrengthUs
    lazy val commitToTech               = productionCapacity >= 5 && saturated
    primaryTech = primaryTech
      .orElse(Some(RoboTech)    .filter(x => have(Protoss.RoboticsFacility)))
      .orElse(Some(TemplarTech) .filter(x => have(Protoss.TemplarArchives)))
      .orElse(Some(TemplarTech) .filter(x => With.units.ours.filter(Protoss.CitadelOfAdun).exists( ! _.knownToOpponents)))
      .orElse(Some(RoboTech)    .filter(x => enemyDarkTemplarLikely))
      .orElse(Some(RoboTech)    .filter(x => With.fingerprints.cannonRush()))
      .orElse(Some(RoboTech)    .filter(x => enemies(Protoss.PhotonCannon) >= 5))
      .orElse(Some(RoboTech)    .filter(x => enemies(Protoss.PhotonCannon) >= 2 && roll("RoboVsCannon", 0.7)))
      .orElse(Some(TemplarTech) .filter(x => commitToTech && bases > 2))
      .orElse(Some(TemplarTech) .filter(x => commitToTech && ! enemyRobo && roll("PrimaryTechTemplar", 0.4)))
      .orElse(Some(TemplarTech) .filter(x => commitToTech && units(Protoss.Zealot) >= 5))
      .orElse(Some(TemplarTech) .filter(x => commitToTech && upgradeStarted(Protoss.GroundDamage)))
      .orElse(Some(RoboTech)    .filter(x => commitToTech))
    shouldReaver = primaryTech.contains(RoboTech) || upgradeStarted(Protoss.ScarabDamage) || enemies(Protoss.PhotonCannon) >= 5

    val reaverShuttleCombo = unitsComplete(Protoss.Reaver) >= 2 && unitsComplete(Protoss.Shuttle) >= 1
    fearDeath   = ! safeDefending
    fearDeath   ||= unitsComplete(IsWarrior) < 8
    fearDeath   ||= recentlyExpandedFirst && estimatedArmyDifferential < 0 && ! With.fingerprints.robo() && ! PvP4GateGoon() && ( ! PvP3GateGoon() || With.fingerprints.fourGateGoon()) && ! reaverShuttleCombo
    fearDeath   &&= ! dtBraveryHome
    fearDeath   &&= ! (With.fingerprints.cannonRush() && enemies(IsWarrior) < 8)
    fearMacro   = miningBases < Math.max(2, enemyBases)
    fearContain = With.scouting.enemyProximity > 0.6 && ! dtBraveryAbroad
    fearContain ||= enemyDarkTemplarLikely && ! haveComplete(Protoss.Observer)
    shouldExpand = productionCapacity >= targetProductionCapacity
    shouldExpand ||= unitsComplete(Protoss.Reaver) >= 4  && unitsComplete(Protoss.Shuttle) >= 2
    shouldExpand &&= ! fearDeath || (fearMacro && miningBases < 3)
    shouldExpand &&= ! fearContain || With.geography.safeExpansions.nonEmpty
    shouldExpand &&= With.geography.ourBases.forall(With.scouting.weControl)
    shouldExpand ||= unitsComplete(IsWarrior) + unitsComplete(Protoss.Reaver) >= miningBases * 18
    shouldExpand ||= miningBases < 2 && frame > Minutes(10)()
    shouldHarass = Protoss.PsionicStorm()
    shouldHarass ||= enemyBases > 2
    shouldHarass ||= fearContain && ! fearDeath
    shouldAttack = enemyMiningBases > miningBases
    shouldAttack ||= bases > 2
    shouldAttack ||= bases > miningBases
    shouldAttack ||= ! recentlyExpandedFirst
    shouldAttack ||= reaverShuttleCombo
    shouldAttack ||= With.fingerprints.cannonRush() || With.fingerprints.forgeFe()
    shouldAttack ||= dtBraveryAbroad && ! enemiesHaveComplete(Protoss.PhotonCannon)
    shouldAttack ||= With.geography.enemyBases.exists(_.natural.exists(With.scouting.weControl)) && employing(PvP3GateGoon, PvP4GateGoon) && ! enemyStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)
    shouldAttack ||= estimatedArmyDifferential > 3 * Protoss.Dragoon.skimulationValue
    shouldAttack &&= PvPIdeas.pvpSafeToMoveOut
    shouldAttack &&= ! fearDeath
    shouldAttack ||= shouldExpand && With.geography.safeExpansions.isEmpty
    if ( ! have(Protoss.RoboticsFacility) || ! have(Protoss.TemplarArchives)) {
      shouldSecondaryTech = miningBases > 2
      shouldSecondaryTech ||= (unitsComplete(Protoss.Reaver) > 2 && haveComplete(Protoss.Shuttle))
      shouldSecondaryTech ||= enemyDarkTemplarLikely && primaryTech.contains(TemplarTech)
      shouldSecondaryTech ||= unitsComplete(IsWarrior) >= 30
      shouldSecondaryTech &&= productionCapacity >= 7
      shouldSecondaryTech &&= With.units.ours.filter(Protoss.Nexus).forall(_.complete)
      shouldSecondaryTech &&= gasPumps > 1
      shouldSecondaryTech &&= miningBases > 1
      shouldSecondaryTech &&= ! fearDeath
      shouldSecondaryTech ||= have(Protoss.RoboticsSupportBay) && have(Protoss.TemplarArchives)
      shouldSecondaryTech ||= primaryTech.contains(RoboTech) && gas > 800
      shouldSecondaryTech ||= unitsComplete(IsWarrior) >= 40
    }
    if (primaryTech.contains(TemplarTech) && have(Protoss.TemplarArchives) && enemyDarkTemplarLikely && ! fearDeath) {
      primaryTech = Some(RoboTech)
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
      } else if (PvPDT() && ! With.fingerprints.dtRush()) {
        pump(Protoss.DarkTemplar, 1)
        pumpSupply()
        get(Protoss.DragoonRange)
        get(5, Protoss.Gateway)
        shouldAttack = false
      } else {
        get(units(Protoss.RoboticsSupportBay), Protoss.RoboticsFacility)
        get(4 - 2 * units(Protoss.RoboticsSupportBay), Protoss.Gateway)
      }
      if ( ! With.scouting.enemyNaturalPossiblyMining && ! With.fingerprints.robo()) {
        pumpSupply()
        doTrainArmy()
        get(3, Protoss.Gateway)
        doCannons()
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
    PvPIdeas.considerMonitoring()
  }

  override def executeMain(): Unit = {
    val trainArmy     = new DoQueue(doTrainArmy)
    val addProduction = new DoQueue(doAddProduction)
    val tech1         = new DoQueue(doPrimaryTech)
    val tech2         = new DoQueue(doSecondaryTech)
    val cannons       = new DoQueue(doCannons)

    get(Protoss.Gateway, Protoss.Assimilator, Protoss.CyberneticsCore)
    get(3, Protoss.Gateway)
    get(Protoss.DragoonRange)

    if (shouldExpand) {
      expandOnce()
    }
    if (With.scouting.enemyProximity < 0.65) {
      maintainMiningBases()
    }
    recordRequestedBases()
    if (saturated && gas < 500) {
      buildGasPumps()
    }
    if (fearDeath) {
      trainArmy()
      addProduction()
    }
    cannons()
    if (productionCapacity >= 6) {
      doUpgrades()
    }
    tech1()
    if (shouldSecondaryTech) {
      tech2()
    }
    trainArmy()
    addProduction()
    expandOnce()
    tech2()
  }

  private def doTrainArmy(): Unit = {
    if (expectCarriers) {
      pumpRatio(Protoss.Dragoon, 16, 64, Seq(Enemy(Protoss.Carrier, 6.0)))
    }
    if ( ! enemiesHave(Protoss.Observer) || (With.geography.enemyBases.size > 2 && With.geography.enemyBases.exists( ! _.enemies.exists(IsDetector)))) {
      pump(Protoss.DarkTemplar, 1)
      if ( ! haveComplete(Protoss.DarkTemplar)) {
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
    pump(Protoss.Observer, Math.min(3, enemies(Protoss.DarkTemplar)))
    if (shouldReaver) {
      // The combined pump will go Reaver-Shuttle-Reaver, but on defense we want Reaver-Reaver-Shuttle
      if (recentlyExpandedFirst || ! shouldAttack) {
        pump(Protoss.Reaver, 2)
      }
      pumpShuttleAndReavers(2, shuttleFirst = ! haveComplete(Protoss.RoboticsSupportBay))
    }
    pump(Protoss.Observer, 2)
    if (shouldReaver) {
      pumpShuttleAndReavers(6, shuttleFirst = ! haveComplete(Protoss.RoboticsSupportBay))
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

  private def doAddProduction(): Unit = {
    if (have(Protoss.RoboticsFacility) && shouldReaver) {
      get(Protoss.RoboticsSupportBay)
    }
    if (have(Protoss.CitadelOfAdun) && ! enemyRobo) {
      get(Protoss.TemplarArchives)
    }
    get(?(have(Protoss.RoboticsSupportBay), 4, 6), Protoss.Gateway)
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

  private def doRobo(): Unit = {
    if (productionCapacity >= 6 && gas < 250) {
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
    if (units(Protoss.Shuttle) >= ?(fearDeath, 1, 2) && (units(Protoss.Reaver) >= ?(fearDeath, 2, 3) || Protoss.PsionicStorm())) {
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
    if ( ! have(Protoss.Observer, Protoss.Observatory)) {
      get(Protoss.Forge)
    }
    get(Protoss.CitadelOfAdun, Protoss.TemplarArchives)
    val gasThreshold = 250
    if (gas < gasThreshold) {
      buildGasPumps()
    }
    if (productionCapacity >= 6 && units(IsWarrior) >= 16) {
      doUpgrades()
      val readyForStorm = techStarted(Protoss.PsionicStorm) || ((gasPumps > 1 || gas >= gasThreshold) && (unitsComplete(IsWarrior) >= 24 || enemyStrategy(With.fingerprints.robo, With.fingerprints.dtRush)))
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
    if (have(Protoss.TemplarArchives)) {
      upgradeContinuously(Protoss.GroundDamage)
      upgradeContinuously(Protoss.GroundArmor)
    } else {
      get(Protoss.GroundDamage)
      get(Protoss.GroundArmor)
    }
    if (units(Protoss.Zealot) > 4 || gas < 200) {
      get(Protoss.CitadelOfAdun)
      get(Protoss.ZealotSpeed)
    }
  }

  private def doCannons(): Unit = {
    val addForge = ! primaryTech.contains(RoboTech)
    if (addForge) {
      get(Protoss.Forge)
    }
    if (addForge || have(Protoss.Forge)) {
      if ( ! have(Protoss.Observer)) {
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

package Planning.Plans.GamePlans.Protoss.PvP

import Debugging.SimpleString
import Lifecycle.With
import Macro.Requests.Get
import Placement.Access.PlaceLabels
import Planning.Plans.GamePlans.All.GameplanImperative
import Planning.Plans.Macro.Automatic.{Enemy, Flat, Friendly}
import ProxyBwapi.Races.Protoss
import Utilities.Time.{GameTime, Minutes, Seconds}
import Utilities.UnitFilters.IsWarrior
import Utilities._
import PvPIdeas._

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
  var primaryTech         : Option[PrimaryTech] = None

  def primaryTemplar      : Boolean = primaryTech.contains(TemplarTech)
  def primaryRobo         : Boolean = primaryTech.contains(RoboTech)

  private def productionCapacity        : Int = unitsComplete(Protoss.Gateway) + 2 * unitsComplete(Protoss.RoboticsFacility) * Math.min(1, unitsComplete(Protoss.RoboticsSupportBay))
  private def targetProductionCapacity  : Int = miningBases * 4
  private def targetGateways            : Int = targetProductionCapacity - 2 * units(Protoss.RoboticsFacility)

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
    shouldHarass &&= Protoss.ShuttleSpeed()
    shouldAttack = PvPIdeas.shouldAttack
    shouldAttack ||= productionCapacity >= targetProductionCapacity
    shouldAttack ||= dtBraveryAbroad && enemiesComplete(Protoss.PhotonCannon) == 0
    shouldAttack &&= ! fearDeath
    shouldAttack ||= shouldExpand
    shouldSecondaryTech = miningBases > 2
    shouldSecondaryTech ||= (unitsComplete(Protoss.Reaver) > 2 && unitsComplete(Protoss.Shuttle) > 0)
    shouldSecondaryTech ||= enemyDarkTemplarLikely && primaryTech.contains(TemplarTech)
    shouldSecondaryTech &&= productionCapacity >= Math.min(7, targetProductionCapacity)
    shouldSecondaryTech &&= With.units.ours.filter(Protoss.Nexus).forall(_.complete)
    shouldSecondaryTech &&= gasPumps > 1
    shouldSecondaryTech &&= miningBases > 1
    shouldSecondaryTech &&= ! fearDeath
    shouldSecondaryTech ||= units(Protoss.RoboticsSupportBay) > 0 && units(Protoss.TemplarArchives) > 0
    shouldSecondaryTech ||= primaryTech.contains(RoboTech) && gas > 800
    oversaturate = shouldExpand && ! fearDeath && ! fearContain

    lazy val commitToTech = productionCapacity >= 5 && saturated
    primaryTech = primaryTech
      .orElse(Some(RoboTech).filter(x => units(Protoss.RoboticsFacility) > 0))
      .orElse(Some(TemplarTech).filter(x => units(Protoss.CitadelOfAdun, Protoss.TemplarArchives, Protoss.PhotonCannon) > 0))
      .orElse(Some(RoboTech).filter(x => enemyDarkTemplarLikely))
      .orElse(Some(RoboTech).filter(x => With.fingerprints.cannonRush()))
      .orElse(Some(TemplarTech).filter(x => commitToTech && bases > 2))
      .orElse(Some(TemplarTech).filter(x => commitToTech && ! enemyRobo && roll("PrimaryTechTemplar", if (With.fingerprints.fourGateGoon()) 0.5 else 0.25)))
      .orElse(Some(TemplarTech).filter(x => commitToTech && units(Protoss.Zealot) >= 5))
      .orElse(Some(RoboTech).filter(x => commitToTech))

    if (PvPIdeas.recentlyExpandedFirst) status("Pioneer")
    if (dtBraveryHome) status("DTBraveHome")
    if (dtBraveryAbroad) status("DTBraveAbroad")
    if (fearDeath) status("FearDeath")
    if (fearMacro) status("FearMacro")
    if (fearContain) status("FearContain")
    if (expectCarriers) status("ExpectCarriers")
    if (shouldSecondaryTech) status("2ndTech")
    if (shouldExpand) status("ShouldExpand")
    if (shouldAttack) { status("Attack"); attack() }
    if (shouldHarass) { status("Harass"); harass() }
    primaryTech.map(_.toString).foreach(status)
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

  private def doTrainArmy(): Unit = {
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

  private def doAddProduction(): Unit = {
    if (units(Protoss.RoboticsFacility) > 0) { get(Protoss.RoboticsSupportBay) }
    if (units(Protoss.CitadelOfAdun) > 0 && ! enemyRobo) { get(Protoss.TemplarArchives) }
    get(5, Protoss.Gateway)
    get(2, Protoss.Assimilator)
    if (gas < 400) { buildGasPumps() }
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
    if (productionCapacity >= 4 && gas < 400 && unitsComplete(Protoss.Nexus) > 1) {
      buildGasPumps()
    }
    get(Protoss.RoboticsFacility)
    buildOrder(Get(Protoss.Shuttle))
    val getObservers = ! fearDeath && ! fearContain
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

  private def doTemplar(): Unit = {
    lazy val readyForStorm = (
      unitsComplete(Protoss.Nexus) > 1
      && unitsComplete(Protoss.Gateway) >= 5
      && unitsComplete(Protoss.Assimilator) > 1
      && unitsComplete(IsWarrior) >= 12
      && (unitsComplete(IsWarrior) >= 24 || enemyStrategy(With.fingerprints.robo, With.fingerprints.dtRush) || techStarted(Protoss.PsionicStorm)))
    get(Protoss.CitadelOfAdun)
    get(Protoss.TemplarArchives)
    if (readyForStorm) {
      status("Ready4Storm")
      get(Protoss.Forge)
      get(Protoss.PsionicStorm)
      get(2, Protoss.HighTemplar)
      upgradeContinuously(Protoss.GroundDamage)
      get(Protoss.ZealotSpeed)
      if (gasPumps > 2) get(Protoss.HighTemplarEnergy)
      if (upgradeComplete(Protoss.GroundDamage, 3)) { upgradeContinuously(Protoss.GroundArmor) }
      if (upgradeComplete(Protoss.GroundArmor,  3)) { upgradeContinuously(Protoss.Shields) }
    }
  }

  private def doCannons(): Unit = {
    if (units(Protoss.Forge) > 0) {
      if (enemyHasShown(Protoss.Shuttle)) {
        buildCannonsAtBases(1, PlaceLabels.DefendHall)
      } else {
        buildCannonsAtOpenings(1, PlaceLabels.DefendHall)
      }
    }
  }
}

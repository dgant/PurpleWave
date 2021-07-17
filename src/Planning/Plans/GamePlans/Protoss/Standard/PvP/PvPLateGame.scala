package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Debugging.SimpleString
import Lifecycle.With
import Macro.BuildRequests.Get
import Mathematics.Maff
import Planning.Plans.GamePlans.GameplanImperative
import Planning.Plans.Macro.Automatic.{Enemy, Flat, Friendly}
import Planning.Plans.Placement.{BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.UnitMatchers.MatchWarriors
import ProxyBwapi.Races.Protoss
import Utilities._

class PvPLateGame extends GameplanImperative {
  trait PrimaryTech extends SimpleString
  object RoboTech extends PrimaryTech
  object TemplarTech extends PrimaryTech

  var fearDeath: Boolean = _
  var fearDT: Boolean = _
  var fearMacro: Boolean = _
  var fearContain: Boolean = _
  var dtBravery: Boolean = _
  var expectCarriers: Boolean = _
  var shouldDetect: Boolean = _
  var shouldSecondaryTech: Boolean = _
  var shouldHarass: Boolean = _
  var shouldAttack: Boolean = _
  var shouldExpand: Boolean = _
  var primaryTech: Option[PrimaryTech] = None

  def primaryTemplar: Boolean = primaryTech.contains(TemplarTech)
  def primaryRobo: Boolean = primaryTech.contains(RoboTech)

  var firstDTFrame: Int = Forever()

  val buildCannonsAtNatural = new BuildCannonsAtNatural(1)
  val buildCannonsAtExpansions = new BuildCannonsAtExpansions(1)
  override def executeBuild(): Unit = {
    fearDeath   = ! safeAtHome || unitsComplete(MatchWarriors) < 8
    fearMacro   = miningBases < Math.max(2, enemyBases)
    fearDT      = enemyDarkTemplarLikely
    fearContain = With.scouting.enemyProgress > 0.6

    // Don't fear death or contain for a couple of minutes after getting DT if they have no mobile detection or evidence of Robo.
    // It takes 1:34 to complete an Observer and another ~35 seconds to float it across the map,
    // so making one DT buys about 2:10 of safety, plus the time it takes to actually make the DT
    Maff.min(With.units.ours.filter(Protoss.DarkTemplar).map(_.frameDiscovered)).foreach(f => firstDTFrame = Math.min(firstDTFrame, f))
    dtBravery = ! enemyRobo && unitsComplete(Protoss.DarkTemplar) > 0 && With.frame < firstDTFrame + GameTime(2, 42)()
    if (dtBravery) {
      fearDeath = false
      fearContain = false
    }

    expectCarriers = enemyCarriersLikely || (enemyStrategy(With.fingerprints.forgeFe) && enemies(MatchWarriors) == 0 && (With.frame > Minutes(8)() || enemies(Protoss.PhotonCannon) > 3))
    shouldDetect = enemyDarkTemplarLikely
    shouldExpand = fearMacro || ! fearDeath
    shouldExpand &&= ! fearContain
    shouldExpand &&= ! fearDT
    shouldExpand &&= (
      miningBases < 2
      || enemyBases > miningBases
      || (expectCarriers && With.frame > GameTime(3, 30)() * miningBases)
      || Math.min(unitsComplete(MatchWarriors) / 16, unitsComplete(Protoss.Gateway) / 3) >= miningBases)
    shouldHarass = fearMacro || fearContain || upgradeComplete(Protoss.ShuttleSpeed)
    shouldAttack = PvPIdeas.shouldAttack
    shouldAttack ||= unitsComplete(Protoss.Gateway) >= targetGateways
    shouldAttack ||= dtBravery
    shouldAttack ||= shouldHarass
    shouldAttack &&= ! fearDeath
    shouldAttack &&= ! fearDT
    shouldAttack ||= shouldExpand
    shouldSecondaryTech = gasPumps > 2 || (unitsComplete(Protoss.Reaver) > 2 && unitsComplete(Protoss.Shuttle) > 0) || techComplete(Protoss.PsionicStorm)
    shouldSecondaryTech &&= unitsComplete(Protoss.Gateway) >= targetGateways
    shouldSecondaryTech &&= With.units.ours.filter(Protoss.Nexus).forall(_.complete)
    shouldSecondaryTech &&= primaryTech.contains(RoboTech) || (gasPumps > 2 && miningBases > 2)
    shouldSecondaryTech &&= miningBases > 1
    oversaturate = shouldExpand && ! fearDeath && ! fearContain

    lazy val commitToTech = unitsComplete(Protoss.Gateway) >= 5
    primaryTech = primaryTech
      .orElse(Some(RoboTech).filter(x => units(Protoss.RoboticsFacility) > 0))
      .orElse(Some(TemplarTech).filter(x => units(Protoss.CitadelOfAdun, Protoss.TemplarArchives, Protoss.PhotonCannon) > 0))
      .orElse(Some(RoboTech).filter(x => enemyDarkTemplarLikely))
      .orElse(Some(RoboTech).filter(x => enemyStrategy(With.fingerprints.cannonRush)))
      .orElse(Some(TemplarTech).filter(x => commitToTech && expectCarriers))
      .orElse(Some(TemplarTech).filter(x => commitToTech && bases > 2))
      .orElse(Some(TemplarTech).filter(x => commitToTech && ! enemyRobo && roll("PrimaryTechTemplar", if (enemyStrategy(With.fingerprints.fourGateGoon)) 0.5 else 0.25)))
      .orElse(Some(RoboTech).filter(x => commitToTech))

    if (dtBravery) status("DTBravery")
    if (fearDeath) status("FearDeath")
    if (fearDT) status("FearDT")
    if (fearMacro) status("FearMacro")
    if (fearContain) status("FearContain")
    if (expectCarriers) status("ExpectCarriers")
    if (shouldDetect) status("Detect")
    if (shouldSecondaryTech) status("2ndTech")
    if (shouldAttack) attack()
    if (shouldHarass) harass()
    primaryTech.map(_.toString).foreach(status)

    // Emergency reactions
    new OldPvPIdeas.ReactToDarkTemplarEmergencies().update()
    new OldPvPIdeas.ReactToCannonRush().update()
    new OldPvPIdeas.ReactToArbiters().update()
  }

  override def executeMain(): Unit = {
    val trainArmy     = new DoQueue(doTrainArmy)
    val fillerArmy    = new DoQueue(doFillerArmy)
    val addGates      = new DoQueue(doAddProduction)
    val primaryTech   = new DoQueue(doPrimaryTech)
    val secondaryTech = new DoQueue(doSecondaryTech)
    val expand        = new DoQueue(doExpand)
    val cannons       = new DoQueue(doCannons)

    // TODO: Emergency/Proactive detection
    // TODO: Gas pumps when?

    if (expectCarriers) { makeDarkArchons() }

    get(Protoss.Gateway)
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
    get(Protoss.DragoonRange)

    if (fearDeath) {
      trainArmy()
      fillerArmy()
      addGates()
    }
    cannons()
    get(3, Protoss.Gateway)
    if (fearContain) {
      primaryTech()
      trainArmy()
      addGates()
      fillerArmy()
    }
    if (shouldExpand) {
      expand()
    }
    if (minerals > 400 || gas < 100) buildGasPumps()
    primaryTech()
    if (shouldSecondaryTech) {
      secondaryTech()
    }
    trainArmy()
    addGates()
    fillerArmy()
    secondaryTech()
    expand()
  }

  def doTrainArmy(): Unit = {
    // Pump 2 Observer vs. DT. This is important because we refuse to attack without DT backstab protection
    if (enemyDarkTemplarLikely || enemyShownCloakedThreat) pump(Protoss.Observer, 2)
    if (expectCarriers && units(Protoss.DarkArchon) + units(Protoss.DarkTemplar) / 2 < Maff.clamp(enemies(Protoss.Carrier), 8, 16)) {
      pump(Protoss.DarkTemplar)
      pumpRatio(Protoss.Dragoon, 16, 64, Seq(Enemy(Protoss.Carrier, 6.0)))
    }
    if (enemies(Protoss.Observer) == 0) {
      pump(Protoss.DarkTemplar, 1)
    }
    pumpRatio(Protoss.Dragoon, 0, 100, Seq(Enemy(Protoss.Scout, 2.0), Enemy(Protoss.Shuttle, 2.0), Friendly(Protoss.Zealot, 1.0), Friendly(Protoss.Archon, 3.0)))
    if ( ! expectCarriers && ! fearDeath && ( ! fearContain || ! enemyRobo)) pump(Protoss.Observer, 1)

    if (techStarted(Protoss.PsionicStorm) && upgradeStarted(Protoss.ZealotSpeed)) {
      pumpRatio(Protoss.Dragoon, 8, 24, Seq(Friendly(Protoss.Zealot, 2.0)))
      pumpRatio(Protoss.HighTemplar, 0, 8, Seq(Flat(-1), Friendly(MatchWarriors, 1.0 / 4.0)))
      if (shouldHarass) {
        pump(Protoss.Shuttle, 1)
      }
    } else {
      pumpShuttleAndReavers(6, shuttleFirst = unitsComplete(Protoss.RoboticsSupportBay) == 0)
    }
    if (upgradeComplete(Protoss.ZealotSpeed, withinFrames = Protoss.Zealot.buildFrames + Seconds(10)())) {
      pumpRatio(Protoss.Zealot, 2, 16, Seq(Friendly(Protoss.Dragoon, 0.5)))
    }
    pump(Protoss.Dragoon)
  }

  def doFillerArmy(): Unit = {
    if (gas > 200 && techStarted(Protoss.PsionicStorm)) {
      pump(Protoss.HighTemplar)
    }
    if (gas < 42 || minerals > 400) {
      pump(Protoss.Zealot)
    }
  }

  def targetGateways: Int = miningBases * 5 - 2 * units(Protoss.RoboticsFacility)

  def doAddProduction(): Unit = {
    if (units(Protoss.RoboticsFacility) > 0) { get(Protoss.RoboticsSupportBay) }
    if (units(Protoss.CitadelOfAdun) > 0 && ! enemyRobo) { get(Protoss.TemplarArchives) }
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
    if (With.units.ours.filter(Protoss.Nexus).forall(_.complete)) {
      requireMiningBases(Math.min(4, unitsComplete(Protoss.Nexus) + 1))
    }
  }

  def doRobo(): Unit = {
    if (units(Protoss.Gateway) >= 4 && gas < 150 && unitsComplete(Protoss.Nexus) > 1) {
      get(2, Protoss.Assimilator)
    }
    get(Protoss.RoboticsFacility)
    buildOrder(Get(Protoss.Shuttle))
    if ( ! fearDeath) {
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
    buildOrder(Get(Protoss.Reaver))
    if (units(Protoss.Reaver) > 0 || techStarted(Protoss.PsionicStorm)) {
      get(Protoss.ShuttleSpeed)
    }
    if (upgradeComplete(Protoss.ShuttleSpeed) && units(Protoss.Reaver) >= 4) {
      get(Protoss.ScarabDamage)
    }
  }

  def doTemplar(): Unit = {
    if ( ! enemyStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)) {
      get(Protoss.Forge)
    }
    get(Protoss.CitadelOfAdun)
    get(Protoss.TemplarArchives)
    if (expectCarriers) {
      get(Protoss.DarkArchonEnergy)
      if (upgradeComplete(Protoss.DarkArchonEnergy)) {
        get(Protoss.MindControl)
      }
    } else {
      if (enemies(Protoss.Observer) == 0) {
        buildOrder(Get(Protoss.DarkTemplar))
      }
      get(5, Protoss.Gateway)
      get(2, Protoss.Assimilator)
      // We don't want this tech until we have a ton of gas available to us
      if (unitsComplete(Protoss.Gateway) >= 5 && unitsComplete(Protoss.Nexus) > 1 && unitsComplete(Protoss.Assimilator) > 1) {
        if (unitsComplete(MatchWarriors) > 15) {
          get(Protoss.PsionicStorm)
        }
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
  }

  def doCannons(): Unit = {
    if (units(Protoss.Forge) > 0) {
      buildCannonsAtNatural.update()
      if (bases > 2) {
        buildCannonsAtExpansions.update()
      }
    }
  }
}

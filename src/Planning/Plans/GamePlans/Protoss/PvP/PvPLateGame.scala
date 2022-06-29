package Planning.Plans.GamePlans.Protoss.PvP

import Debugging.SimpleString
import Lifecycle.With
import Macro.Requests.Get
import Mathematics.Maff
import Placement.Access.PlaceLabels
import Planning.Plans.GamePlans.All.GameplanImperative
import Planning.Plans.Macro.Automatic.{Enemy, Flat, Friendly}
import ProxyBwapi.Races.Protoss
import Utilities.Time.{Forever, GameTime, Minutes, Seconds}
import Utilities.UnitFilters.IsWarrior
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
  var shouldMindControl: Boolean = _
  var primaryTech: Option[PrimaryTech] = None

  def primaryTemplar: Boolean = primaryTech.contains(TemplarTech)
  def primaryRobo: Boolean = primaryTech.contains(RoboTech)

  var firstDTFrame: Int = Forever()

  override def executeBuild(): Unit = {
    fearDeath   = ! enemyStrategy(With.fingerprints.dtRush, With.fingerprints.robo) && ( ! safeAtHome || unitsComplete(IsWarrior) < 8 || (PvPIdeas.recentlyExpandedFirst && unitsComplete(Protoss.Shuttle) * unitsComplete(Protoss.Reaver) < 2))
    fearMacro   = miningBases < Math.max(2, enemyBases)
    fearDT      = enemyDarkTemplarLikely && unitsComplete(Protoss.Observer) == 0 && (enemies(Protoss.DarkTemplar) > 0 && unitsComplete(Protoss.PhotonCannon) == 0)
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
    shouldAttack = PvPIdeas.shouldAttack
    shouldAttack ||= unitsComplete(Protoss.Gateway) >= targetGateways
    shouldAttack ||= (dtBravery && enemiesComplete(Protoss.PhotonCannon) == 0)
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
      .orElse(Some(RoboTech).filter(x => commitToTech))

    if (PvPIdeas.recentlyExpandedFirst) status("Pioneer")
    if (dtBravery) status("DTBravery")
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
    PvPDTDefense.reactToDarkTemplarEmergencies()
  }

  override def executeMain(): Unit = {
    val trainArmy     = new DoQueue(doTrainArmy)
    val addGates      = new DoQueue(doAddProduction)
    val primaryTech   = new DoQueue(doPrimaryTech)
    val secondaryTech = new DoQueue(doSecondaryTech)
    val expand        = new DoQueue(doExpand)
    val cannons       = new DoQueue(doCannons)

    // TODO: Emergency/Proactive detection

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
    if (fearContain && ! fearDeath) {
      get(Protoss.ShuttleSpeed)
    }
    pump(Protoss.Reaver, 1)
    get(Protoss.ShuttleSpeed)
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
}

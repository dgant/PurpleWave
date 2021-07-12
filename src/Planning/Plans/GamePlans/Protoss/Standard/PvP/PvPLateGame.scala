package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Debugging.SimpleString
import Lifecycle.With
import Macro.BuildRequests.Get
import Mathematics.Maff
import Planning.Plans.GamePlans.GameplanImperative
import Planning.Plans.Macro.Automatic.{Enemy, Flat, Friendly}
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
  var shouldDetect: Boolean = _
  var shouldSecondaryTech: Boolean = _
  var shouldHarass: Boolean = _
  var shouldAttack: Boolean = _
  var shouldExpand: Boolean = _
  var expectCarriers: Boolean = _
  var primaryTech: Option[PrimaryTech] = None

  def primaryTemplar: Boolean = primaryTech.contains(TemplarTech)
  def primaryRobo: Boolean = primaryTech.contains(RoboTech)

  var firstDTFrame: Int = Forever()

  override def executeBuild(): Unit = {
    fearDeath   = ! safeAtHome
    fearMacro   = bases < enemyBases
    fearDT      = enemyDarkTemplarLikely
    fearContain =
      With.scouting.threatOrigin.nearestWalkableTile.tileDistanceGroundManhattan(With.geography.home.nearestWalkableTile) <
      With.scouting.threatOrigin.nearestWalkableTile.tileDistanceGroundManhattan(With.scouting.mostBaselikeEnemyTile.nearestWalkableTile)

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
    shouldExpand = (fearMacro || ! fearDeath) || (miningBases < 2 && With.frame > Minutes(13)())
    shouldExpand &&= ! fearContain
    shouldExpand &&= ! fearDT
    shouldExpand &&= (miningBases < 2 || enemyBases > 2 || unitsComplete(Protoss.Gateway) > 3 / miningBases || (expectCarriers && With.frame > GameTime(3, 30)() * miningBases))
    shouldHarass = fearMacro || fearContain
    shouldAttack = shouldHarass && ! fearDeath && ! fearDT
    shouldAttack ||= shouldExpand
    shouldSecondaryTech = gasPumps > 2 || (unitsComplete(Protoss.Reaver) > 2 && unitsComplete(Protoss.Shuttle) > 0) || techComplete(Protoss.PsionicStorm)
    oversaturate = shouldExpand && ! fearDeath

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
    if (shouldDetect) status("Detect")
    if (shouldSecondaryTech) status("2ndTech")
    if (shouldAttack) attack()
    if (shouldHarass) harass()
    primaryTech.map(_.toString).foreach(status)

    // Emergency reactions
    new PvPIdeas.ReactToDarkTemplarEmergencies().update()
    new PvPIdeas.ReactToCannonRush().update()
    new PvPIdeas.ReactToArbiters().update()
  }

  override def execute(): Unit = {
    val trainArmy     = new DoQueue(doTrainArmy)
    val fillerArmy    = new DoQueue(doFillerArmy)
    val addGates      = new DoQueue(doAddProduction)
    val primaryTech   = new DoQueue(doPrimaryTech)
    val secondaryTech = new DoQueue(doSecondaryTech)
    val expand        = new DoQueue(doExpand)

    // TODO: Emergency/Proactive detection
    // TODO: Gas pumps when?

    if (expectCarriers) { makeDarkArchons() }

    get(Protoss.Gateway)
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
    get(Protoss.DragoonRange)

    if (fearDeath) {
      trainArmy()
      addGates()
      fillerArmy()
    }
    if (fearContain) {
      primaryTech()
      trainArmy()
      addGates()
    }
    get(3, Protoss.Gateway)
    if (shouldExpand) {
      expand()
    }
    get(2, Protoss.Assimilator)
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
    if (enemyDarkTemplarLikely || enemyShownCloakedThreat) pump(Protoss.Observer, 2)
    if (expectCarriers && units(Protoss.DarkArchon) + 2 * units(Protoss.DarkTemplar) < Maff.clamp(enemies(Protoss.Carrier), 8, 16)) {
      pump(Protoss.DarkTemplar)
      pumpRatio(Protoss.Dragoon, 16, 64, Seq(Enemy(Protoss.Carrier, 6.0)))
    }
    if (enemies(Protoss.Observer) == 0) {
      pump(Protoss.DarkTemplar, 1)
    }
    pumpRatio(Protoss.Dragoon, 0, 100, Seq(Enemy(Protoss.Scout, 2.0), Enemy(Protoss.Shuttle, 2.0), Friendly(Protoss.Zealot, 1.0), Friendly(Protoss.Archon, 3.0)))
    if ( ! expectCarriers && ! fearDeath && ( ! fearContain || ! enemyRobo)) pump(Protoss.Observer, 1)

    if (techStarted(Protoss.PsionicStorm)) {
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

  def doAddProduction(): Unit = {
    if (units(Protoss.RoboticsFacility) > 0) { get(Protoss.RoboticsSupportBay) }
    if (units(Protoss.CitadelOfAdun) > 0 && ! enemyRobo) { get(Protoss.TemplarArchives) }
    get(miningBases * 4 - 2 * units(Protoss.RoboticsFacility), Protoss.Gateway)
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
    requireMiningBases(Math.min(4, miningBases + 1))
  }

  def doRobo(): Unit = {
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
    get(Protoss.Forge)
    get(Protoss.CitadelOfAdun)
    get(Protoss.TemplarArchives)
    if (expectCarriers) {
      get(Protoss.DarkArchonEnergy)
      if (upgradeComplete(Protoss.DarkArchonEnergy)) {
        get(Protoss.MindControl)
      }
    } else {
      if ( ! enemyRobo) {
        buildOrder(Get(Protoss.DarkTemplar))
      }
      get(Protoss.PsionicStorm)
      buildOrder(Get(2, Protoss.HighTemplar))
      get(Protoss.GroundDamage)
      get(Protoss.ZealotSpeed)
      if (techComplete(Protoss.PsionicStorm, withinFrames = Seconds(10)())) {
        get(Protoss.HighTemplarEnergy)
      }
      if (upgradeComplete(Protoss.GroundDamage, 1)) { get(Protoss.GroundArmor,  1) }
      if (upgradeComplete(Protoss.GroundArmor,  1)) { get(Protoss.GroundDamage, 2) }
      if (upgradeComplete(Protoss.GroundDamage, 2)) { get(Protoss.GroundArmor,  2) }
      if (upgradeComplete(Protoss.GroundArmor,  2)) { get(Protoss.GroundDamage, 3) }
      if (gasPumps >= 3 & miningBases >= 3) {
        if (upgradeComplete(Protoss.GroundDamage, 3)) { get(Protoss.GroundArmor, 3) }
        if (upgradeComplete(Protoss.GroundArmor,  3)) { upgradeContinuously(Protoss.Shields) }
      }
    }
  }
}

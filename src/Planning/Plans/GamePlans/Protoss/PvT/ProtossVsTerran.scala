
package Planning.Plans.GamePlans.Protoss.PvT

import Debugging.SimpleString
import Lifecycle.With
import Mathematics.Maff
import Planning.Plans.Macro.Automatic.{Enemy, Friendly}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss._
import Utilities.Time.Minutes
import Utilities.UnitFilters._
import Utilities.{?, DoQueue}

import scala.collection.mutable.ArrayBuffer

class ProtossVsTerran extends PvTOpeners {

  val workerGoal = 65

  override def doWorkers(): Unit = pumpWorkers(oversaturate = true, maximumTotal = workerGoal)

  override def executeBuild(): Unit = {
    // TODO: Gas steal. Pylon scout on 2/3 player. See https://www.youtube.com/watch?v=Cl2MHjmGBLk for ideal build and 2-rax reaction
    // TODO: Khala says, if you confirm expansion, you can cancel Range for faster Robo
    // TODO: Expansion is informative
    // TODO: Cross-spawn informs Nexus-first choices
    // TODO: 10-12 is informative
    // TODO: 14 CC is informative
    // TODO: 1 Rax FE is informative -- if we're not trying to pressure it (eg 1-zealot core builds) we can always expand off gate
    // TODO: Fac is informative
    // TODO: 2 Fac is informative
    // TODO: 3 Fac is informative
    // TODO: 3rd CC is informative
    // TODO: Early turret is informative
    // TODO: Factory count is informative
    // TODO: Armory count is informative
    // TODO: Bio is informative
    // TODO: Early Vulture is informative
    // TODO: Early Vulture Speed is informative
    // TODO: Early Vulture Mines is informative
    // TODO: Early Siege is informative
    // TODO: Goliath is informative
    // TODO: Dropship is informative

    // TODO: Take/Retake natural when?
    // TODO: 2/3 Gateway when?
    // TODO: If we take mineral 3rd, get speed+ups

    // Early vulture -> Prefer obs
    // 14CC -> ???
    // 1 Rax -> Reaver, Fast 3rd
    // Siege expand -> Fast 3rd
    // Early Vulture -> Obs, Reaver
    // 2/3 Fac -> Prefer muscle
    // 3rd CC -> Fast 4th
    // Bio -> Reaver, 3rd, upgrades, 4th, double upgrades

    // Against one-base Siege push, 3-Gate goon: https://youtu.be/djZt3n1Po6s?t=6681
    // TODO: Go Reaver if siege tech and no Vulture tech or minimal Vultures (suggesting bio)
    // TODO: Lock into doing one of these two things first, then getting the other

    open()
  }

  val techs = new ArrayBuffer[TechTransition]
  def doTech(n: Int): Unit = techs.view.drop(n).headOption.foreach(_.perform())
  def doNextTech(): Unit = {
    techs.foreach(t => { t.perform(); if ( ! t.profited) return })
  }
  trait TechTransition extends SimpleString {
    def apply(predicate: Boolean): Boolean = if (predicate) { apply(); true } else false
    def apply(): Unit = if ( ! queued) techs += this
    def started: Boolean // Have we invested anything significant into this tech?
    def profited: Boolean // Have we finished the up-front investment to gain value out of it
    def perform(): Unit
    def order: Int = if (techs.contains(this)) techs.indexOf(this) else techs.length
    def queued: Boolean = techs.contains(this)
    override def toString: String = f"${super.toString}${if (profited) "(Done)" else if (started) "(Start)" else ""}"
  }
  object TechObservers extends TechTransition {
    def started: Boolean = units(Protoss.Observatory) > 0 // TODO: Consider Robo, not being used for earlier tech
    def profited: Boolean = unitsEver(Protoss.Observer) > 0
    def perform(): Unit = {
      once(Protoss.RoboticsFacility, Protoss.Observatory, Protoss.Observer)
      get(Protoss.DragoonRange)
      requireGas()
      val observersForVultures  = enemyHasShown(Terran.SpiderMine) || gasPumps > 2
      val observersForWraiths   = enemyHasTech(Terran.WraithCloak) || enemies(Terran.Wraith) > 1
      val observersForSomething = observersForVultures || observersForWraiths
      val safeForUpgrades       = unitsComplete(IsWarrior) > 12 && safeAtHome
      if ((observersForWraiths && TechCarrier.profited) || (observersForSomething && safeForUpgrades)) {
        get(?(upgradeStarted(Protoss.ObserverSpeed), Protoss.ObserverVisionRange, Protoss.ObserverSpeed))
      }
    }
  }
  object TechReavers extends TechTransition {
    def started: Boolean = unitsEver(Protoss.Reaver) > 0 // TODO: Consider Robo+Bay, not being used for earlier tech
    def profited: Boolean = unitsEver(Protoss.Reaver) > 1
    def perform(): Unit = {
      get(Protoss.RoboticsFacility)
      once(Protoss.Shuttle)
      get(Protoss.RoboticsSupportBay)
      once(2, Protoss.Reaver)
      get(Protoss.ShuttleSpeed)
      get(Protoss.DragoonRange)
      requireGas()
      if (units(Protoss.Reaver) > 2) get(Protoss.ScarabDamage)
    }
  }
  object TechUpgrades extends TechTransition {
    def started: Boolean = upgradeStarted(Protoss.ZealotSpeed)
    def profited: Boolean = started && upgradeStarted(Protoss.GroundDamage)
    def perform(): Unit = {
      get(Protoss.DragoonRange)
      get(Protoss.CitadelOfAdun)
      requireGas()
      get(Protoss.ZealotSpeed)
      get(?(counterBio, 2, 1), Protoss.Forge)
      get(Protoss.TemplarArchives)
      get(Protoss.GroundDamage)
      if (counterBio) get(Protoss.GroundArmor)
      buildCannonsAtExpansions(1)
      upgradeContinuously(Protoss.GroundDamage)
      if (counterBio || upgradeStarted(Protoss.GroundDamage, 3)) upgradeContinuously(Protoss.GroundArmor)
    }
  }
  object TechStorm extends TechTransition {
    def started: Boolean = profited || unitsEver(Protoss.HighTemplar) > 0
    def profited: Boolean = unitsEver(Protoss.HighTemplar) >= 2 && techStarted(Protoss.PsionicStorm) && upgradeStarted(Protoss.ShuttleSpeed)
    def perform(): Unit = {
      get(Protoss.DragoonRange)
      get(Protoss.CitadelOfAdun)
      requireGas()
      get(Protoss.ZealotSpeed)
      get(Protoss.TemplarArchives)
      once(2, Protoss.HighTemplar)
      get(Protoss.PsionicStorm)
      get(Protoss.RoboticsFacility, Protoss.RoboticsSupportBay)
      get(Protoss.ShuttleSpeed)
      once(Protoss.Shuttle)
      get(Protoss.HighTemplarEnergy)
    }
  }
  object TechCarrier extends TechTransition {
    def started: Boolean = profited || units(Protoss.FleetBeacon) > 0 || (units(Protoss.Stargate) > 0 && units(Protoss.TemplarArchives, Protoss.ArbiterTribunal) == 0)
    def profited: Boolean = unitsEver(Protoss.Carrier) >= 4 && upgradeStarted(Protoss.CarrierCapacity)
    def perform(): Unit = {
      get(Math.min(4, miningBases), Protoss.Stargate)
      requireGas()
      get(Protoss.FleetBeacon)
      once(2, Protoss.Carrier)
      get(Protoss.CarrierCapacity)
      once(4, Protoss.Carrier)
      get(Protoss.DragoonRange) // Make sure we get it before we start air upgrades
      if (enemyStrategy(With.fingerprints.bio)) upgradeContinuously(Protoss.AirArmor) else upgradeContinuously(Protoss.AirDamage)
      if (upgradeComplete(Protoss.AirArmor, 3))  upgradeContinuously(Protoss.AirDamage)
      if (upgradeComplete(Protoss.AirDamage, 3))  upgradeContinuously(Protoss.AirArmor)
      buildCannonsAtExpansions(2)
    }
  }
  object TechArbiter extends TechTransition {
    def started: Boolean = profited || units(Protoss.ArbiterTribunal) > 0
    def profited: Boolean = unitsEver(Protoss.Arbiter) > 0 && techStarted(Protoss.Stasis)
    def perform(): Unit = {
      get(Protoss.DragoonRange)
      get(Protoss.CitadelOfAdun)
      requireGas()
      get(Protoss.TemplarArchives, Protoss.ArbiterTribunal, Protoss.Stargate)
      get(Protoss.ArbiterEnergy)
      once(2, Protoss.Arbiter)
      get(Protoss.Stasis)
      if (gasPumps > 3) get(2, Protoss.Stargate)
    }
  }

  def counterBio: Boolean = With.fingerprints.bio() && enemies(Terran.Marine, Terran.Firebat, Terran.Medic) >= enemies(Terran.Vulture) * 1.5
  def executeMain(): Unit = {
    val reaverVsBio = counterBio && ! PvTDT()
    val stormVsBio  = counterBio && ! reaverVsBio
    techs.clear()
    TechObservers       (With.fingerprints.twoFacVultures() || With.fingerprints.threeFacVultures())
    TechReavers         (TechReavers.started    || reaverVsBio)
    TechObservers       (TechObservers.started  || ! stormVsBio || enemyHasShown(Terran.SpiderMine))
    TechStorm           (PvTMidgameStorm()      || stormVsBio)
    TechCarrier         (PvTMidgameCarrier()    && ( ! counterBio || TechCarrier.started))
    TechUpgrades        (counterBio)
    TechObservers       ()
    TechReavers         (PvTMidgameReaver())
    TechUpgrades        ()
    TechCarrier         (PvTEndgameCarrier()  && ( ! counterBio || TechCarrier.started))
    TechStorm           (PvTMidgameStorm() || PvTEndgameStorm() || TechCarrier.queued || counterBio)
    TechArbiter         ()
    PvTMidgameOpen.activate()
    PvTEndgameArbiter.activate()

    val techsComplete   = techs.count(_.profited)
    val gatewayEquivs   = ?(TechCarrier.queued, miningBases, 0) + ?(TechReavers.queued, 2, 0)
    val gatewayWant     = (3.0 * miningBases).toInt - gatewayEquivs
    var gatewayNeed     = (2.0 * miningBases).toInt - gatewayEquivs - 1
    gatewayNeed         = Math.max(gatewayNeed, 1.5 * enemies(Terran.Factory) + Math.max(0, enemies(Terran.Barracks) - 1)).toInt
    gatewayNeed         = Math.max(gatewayNeed, ?(enemyStrategy(With.fingerprints.twoFac, With.fingerprints.threeFac), 3, 0))
    gatewayNeed         = Math.min(gatewayNeed, gatewayWant)
    val vulturesEnemy   = With.units.enemy.count(Terran.Vulture)
    val antiVulture     = With.units.ours.filter(_.isAny(Protoss.Dragoon, Protoss.Reaver, Protoss.Scout, Protoss.Carrier)).map(_.unitClass.supplyRequired / 2.0).sum
    val armySizeEnemy   = With.units.enemy.filter(IsWarrior).map(u => ?(Terran.Vulture(u), 1.5, ?(IsTank(u), 2.5, u.unitClass.supplyRequired / 4.0))).sum
    val armySizeUd      = With.units.ours.filterNot(IsWorker).map(_.unitClass.supplyRequired / 4.0).sum
    var armySizeMinimum = 1.2 * armySizeEnemy
    armySizeMinimum     = Math.max(armySizeMinimum, 12 + 6 * techsComplete)
    armySizeMinimum     = Math.max(armySizeMinimum, 6 * Math.max(1, miningBases - 1))
    armySizeMinimum     = Math.max(armySizeMinimum, ?(With.fingerprints.siegeExpand(),  4, 0))
    armySizeMinimum     = Math.max(armySizeMinimum, ?(With.fingerprints.twoRaxAcad(),   8, 0))
    armySizeMinimum     = Math.max(armySizeMinimum, ?(With.fingerprints.twoFac(),       10, 0))
    armySizeMinimum     = Math.max(armySizeMinimum, ?(With.fingerprints.threeFac(),     12, 0))
    armySizeMinimum     = Math.min(armySizeMinimum, 200 - workerGoal)
    var armySizeLow     = armySizeUd < armySizeMinimum || With.battles.globalHome.judgement.exists(_.confidence01Total < With.scouting.enemyProximity)
    armySizeLow       &&= unitsComplete(Protoss.DarkTemplar) == 0 || enemyHasShown(Terran.SpellScannerSweep, Terran.SpiderMine, Terran.ScienceVessel)
    val zealotAggro     = frame < Minutes(5)() && unitsComplete(Protoss.Zealot) > 0
    val pushMarines     = barracksCheese && ! With.strategy.isRamped
    val mineContain     = enemyHasShown(Terran.SpiderMine) && unitsComplete(Protoss.Observer) == 0
    val vultureContain  = vulturesEnemy > antiVulture * ?(enemyHasUpgrade(Terran.VultureSpeed), 1.25, 1.5)
    val vultureRush     = frame < Minutes(8)() && enemyStrategy(With.fingerprints.twoFacVultures, With.fingerprints.threeFac, With.fingerprints.threeFacVultures) && (armySizeUd < 12 || unitsComplete(Protoss.Observer) == 0)
    val consolidatingFE = frame < Minutes(7)() && PvT13Nexus() && ! With.fingerprints.fourteenCC()
    val nascentCarriers = TechCarrier.started && unitsEver(IsAll(Protoss.Carrier, IsComplete)) < 4
    val encroaching     = With.scouting.enemyProximity > 0.65
    var shouldAttack    = unitsComplete(IsWarrior) >= 7
    shouldAttack      ||= ! barracksCheese
    shouldAttack      &&= ! mineContain
    shouldAttack      &&= ! vultureContain
    shouldAttack      &&= ! vultureRush
    shouldAttack      &&= ! consolidatingFE
    shouldAttack      ||= zealotAggro
    shouldAttack      ||= bases > 2
    shouldAttack      ||= enemyMiningBases > miningBases
    shouldAttack      ||= frame > Minutes(10)()
    shouldAttack      ||= pushMarines

    status(f"${gatewayNeed}-${gatewayWant}gate")
    status(techs.mkString("-").replaceAll("Tech", "") + f":${techsComplete}/${techs.length}")
    if (armySizeLow) status("ArmyLow")
    if (zealotAggro) status("ZealotAggro")
    if (pushMarines) status("PushMarines")
    if (mineContain) status("MineContain")
    if (vultureContain) status("VultureContain")
    if (vultureRush) status("VultureRush")
    if (consolidatingFE) status("ConsolidatingFE")
    if (nascentCarriers) status("NascentCarriers")
    if (encroaching) status("Encroaching")

    if (shouldAttack) attack()
    harass()
    gasLimitCeiling(Math.max(1, miningBases) * 300)
    With.blackboard.monitorBases.set(unitsComplete(Protoss.Observer) > 1 || ! enemyHasShown(Terran.SpiderMine) || ! shouldAttack)

    val army = new DoQueue(doArmyNormalPriority)
    val tech = new DoQueue(doNextTech)

    ////////////////
    // Transition //
    ////////////////

    get(Protoss.Pylon, Protoss.Gateway, Protoss.Assimilator, Protoss.CyberneticsCore)
    doArmyHighPriority()

    if (armySizeLow) {
      if (miningBases > 1 && gasPumps > 1 && techs.exists(t => t.started && ! t.profited)) {
        tech()
      }
      army()
      get(Protoss.DragoonRange)
    }
    get(gatewayNeed, Protoss.Gateway)

    ////////////
    // Expand //
    ////////////

    if (With.scouting.weControlOurNatural) {
      approachMiningBases(2)
    }
    if ( ! encroaching && shouldAttack) {
      approachMiningBases(1 + enemyMiningBases)
      approachMiningBases(Maff.clamp(2 + techsComplete, 2, 4))
    }

    ///////////
    // Spend //
    ///////////

    maintainMiningBases(4)
    recordRequestedBases()
    tech()
    army()
    get(?(armySizeLow, gatewayWant, gatewayNeed), Protoss.Gateway)
    approachMiningBases(3)
    requireGas()
    get(gatewayWant, Protoss.Gateway)
    buildCannonsAtExpansions(2)
    if (isMiningBase(With.geography.ourNatural)) {
      buildCannonsAtNatural(2)
    }
    approachMiningBases(4)
    techs.foreach(_.perform())
    approachMiningBases(5)
    get(6 * miningBases, Protoss.Gateway)
  }

  def requireGas(): Unit = {
    if (With.self.gas < 500) buildGasPumps()
  }

  def doArmyHighPriority(): Unit = {
    if (units(Protoss.TemplarArchives) > 0
      && ( ! With.fingerprints.bio() || ! enemyHasShown(Terran.Comsat, Terran.SpellScannerSweep)) // Vs. mech we can spam DT and slow a push; bio can just walk over us
      && 0 == units(Protoss.FleetBeacon, Protoss.Arbiter) + unitsComplete(Protoss.ArbiterTribunal) + enemies(Terran.ScienceVessel)
      && ( ! enemyHasShown(Terran.SpiderMine) || With.scouting.enemyProximity > 0.7))  {
      once(2, Protoss.DarkTemplar)
      pump(Protoss.DarkTemplar, 1)
    }
    pump(Protoss.Observer, 1)
    if (TechReavers.queued) pumpShuttleAndReavers(?(With.fingerprints.bio(), 2, 6), shuttleFirst = TechReavers.order < TechObservers.order)
    pump(Protoss.Carrier, 4)
  }

  def doArmyNormalPriority(): Unit = {
    pumpRatio(Protoss.Dragoon, ?(counterBio, 6, 12), 24, Seq(Enemy(Terran.Vulture, .6), Enemy(Terran.Wraith, 0.5), Enemy(Terran.Battlecruiser, 4.0), Friendly(Protoss.Zealot, 0.5)))
    pumpRatio(Protoss.Observer, ?(enemyHasShown(Terran.SpiderMine), 1, 2), 4, Seq(Friendly(IsWarrior, 1.0 / 12.0)))
    if (TechCarrier.started && (enemyHasTech(Terran.WraithCloak) || enemies(Terran.Wraith) > 1)) {
      pump(Protoss.Observer, 8)
    }
    if (techStarted(Protoss.PsionicStorm)) {
      pumpRatio(Protoss.HighTemplar, 2, 8, Seq(Friendly(IsWarrior, 1.0 / 10.0)))
    }
    if (unitsComplete(Protoss.FleetBeacon) == 0 && With.scouting.enemyProximity > 0.4 && enemies(Terran.Goliath) + enemies(Terran.Marine) / 5 == 0) {
      pump(Protoss.Scout, 2)
    }
    pump(Protoss.Carrier, 12)
    pumpRatio(Protoss.Arbiter, 2, 8, Seq(Enemy(IsTank, 0.5)))
    if (units(Protoss.HighTemplar) > 0) {
      pumpRatio(Protoss.Shuttle, 1, 3, Seq(Friendly(Protoss.Reaver, 0.5), Friendly(Protoss.HighTemplar, 1.0 / 3.0)))
    }
    if ( ! upgradeStarted(Protoss.ZealotSpeed)) {
      pump(Protoss.Dragoon, 24)
    }
    pump(Protoss.Zealot)
  }
}

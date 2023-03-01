
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

  val workerGoal = 75

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
  def techsSorted: Seq[TechTransition] = techs.toVector.sortBy( ! _.started)
  def doNextTech(maxInProgress: Int = 1): Unit = {
    var inProgress = 0
    techsSorted.foreach(t =>
      if (inProgress < maxInProgress) {
        t.perform()
        inProgress += Maff.fromBoolean(t.inProgress)
      })
  }
  trait TechTransition extends SimpleString {
    final def apply(predicate: Boolean): Boolean = if (predicate) { apply(); true } else false
    final def apply(): Unit = if ( ! queued) techs += this
    def started: Boolean // Have we invested anything significant into this tech?
    def profited: Boolean // Have we finished the up-front investment to gain value out of it
    def perform(): Unit
    final def inProgress: Boolean = started && ! profited
    final def order: Int = if (techs.contains(this)) techsSorted.indexOf(this) else techs.length
    final def queued: Boolean = techs.contains(this)
    override def toString: String = f"${super.toString}${if (profited) "(Done)" else if (started) "(Start)" else ""}"
  }
  object TechDarkTemplar extends TechTransition {
    override def started: Boolean = have(Protoss.CitadelOfAdun)
    override def profited: Boolean = unitsEver(Protoss.DarkTemplar) > 0
    override def perform(): Unit = {
      get(Protoss.CitadelOfAdun, Protoss.TemplarArchives)
      once(?(PvT29Arbiter(), 1, 2), Protoss.DarkTemplar)
    }
  }
  object TechDarkTemplarDrop extends TechTransition {
    override def started: Boolean = TechDarkTemplar.started && have(Protoss.RoboticsFacility)
    override def profited: Boolean = unitsEver(Protoss.DarkTemplar) > 0 && unitsEver(Protoss.Shuttle) > 0
    override def perform(): Unit = {
      once(Protoss.CitadelOfAdun, Protoss.RoboticsFacility, Protoss.TemplarArchives, Protoss.Shuttle)
      once(2, Protoss.DarkTemplar)
    }
  }
  object TechObservers extends TechTransition {
    def started: Boolean = have(Protoss.Observatory) // TODO: Consider Robo, not being used for earlier tech
    def profited: Boolean = unitsEver(Protoss.Observer) > 0
    def perform(): Unit = {
      once(Protoss.RoboticsFacility, Protoss.Observatory, Protoss.Observer)
      get(Protoss.DragoonRange)
      val observersForVultures  = enemyHasShown(Terran.SpiderMine) || gasPumps > 2
      val observersForWraiths   = enemyHasTech(Terran.WraithCloak) || enemies(Terran.Wraith) > 1
      val observersForSomething = observersForVultures || observersForWraiths
      val safeForUpgrades       = unitsComplete(IsWarrior) > 12 && safeDefending
      if ((observersForWraiths && TechCarrier.profited) || (observersForSomething && safeForUpgrades)) {
        get(?(upgradeStarted(Protoss.ObserverSpeed), Protoss.ObserverVisionRange, Protoss.ObserverSpeed))
      }
    }
  }
  object TechReavers extends TechTransition {
    def started: Boolean = unitsEver(Protoss.Reaver) > 0 || (have(Protoss.RoboticsSupportBay) && ! have(Protoss.TemplarArchives))
    def profited: Boolean = unitsEver(Protoss.Reaver) > 0 && unitsEver(Protoss.Shuttle) > 0
    def perform(): Unit = {
      get(Protoss.RoboticsFacility)
      once(Protoss.Shuttle)
      get(Protoss.RoboticsSupportBay)
      once(2, Protoss.Reaver)
      get(Protoss.ShuttleSpeed)
      get(Protoss.DragoonRange)
      if (units(Protoss.Reaver) > 2) get(Protoss.ScarabDamage)
    }
  }
  object TechUpgrades extends TechTransition {
    def started: Boolean = upgradeStarted(Protoss.ZealotSpeed) && unitsEver(Protoss.Forge) > 0
    def profited: Boolean = started && upgradeStarted(Protoss.GroundDamage)
    def perform(): Unit = {
      get(Protoss.DragoonRange)
      get(Protoss.CitadelOfAdun)
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
    def profited: Boolean = unitsEver(Protoss.HighTemplar) > 0 && techStarted(Protoss.PsionicStorm)
    def perform(): Unit = {
      get(Protoss.DragoonRange)
      get(Protoss.CitadelOfAdun)
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
    def started: Boolean = profited || have(Protoss.FleetBeacon) || (have(Protoss.Stargate) && ! have(Protoss.TemplarArchives, Protoss.ArbiterTribunal))
    def profited: Boolean = unitsEver(Protoss.Interceptor) >= 24 && upgradeStarted(Protoss.CarrierCapacity)
    def perform(): Unit = {
      // We're leaning heavily on MacroSim to sequence this for us
      val stargates = Math.min(4, miningBases)
      get(2, Protoss.Stargate)
      get(Protoss.FleetBeacon)
      get(stargates, Protoss.Stargate)
      once(stargates, Protoss.Carrier)
      get(Protoss.CarrierCapacity)
      once(4, Protoss.Carrier)
      get(Protoss.DragoonRange) // Make sure we get it before we start air upgrades
      if (enemyStrategy(With.fingerprints.bio)) upgradeContinuously(Protoss.AirArmor) else upgradeContinuously(Protoss.AirDamage)
      if (upgradeComplete(Protoss.AirArmor, 3))  upgradeContinuously(Protoss.AirDamage)
      if (upgradeComplete(Protoss.AirDamage, 3))  upgradeContinuously(Protoss.AirArmor)
      requireGas()
      buildCannonsAtExpansions(2)
    }
  }
  object TechArbiter extends TechTransition {
    def started: Boolean = profited || units(Protoss.ArbiterTribunal) > 0
    def profited: Boolean = unitsEver(Protoss.Arbiter) > 0
    def perform(): Unit = {
      get(Protoss.DragoonRange)
      get(Protoss.CitadelOfAdun, Protoss.TemplarArchives, Protoss.ArbiterTribunal, Protoss.Stargate)
      if ( ! have(Protoss.Arbiter)) {
        get(Protoss.ArbiterEnergy)
      }
      once(2, Protoss.Arbiter)
      get(Protoss.Stasis)
      get(Protoss.ArbiterEnergy)
      if (gasPumps > 3) get(2, Protoss.Stargate)
    }
  }

  def counterBio: Boolean = With.fingerprints.bio() && enemies(Terran.Marine, Terran.Firebat, Terran.Medic) >= enemies(Terran.Vulture) * 1.5
  def executeMain(): Unit = {
    var scoredEnemy   = false
    def scoreEnemy(value: Boolean): Int = {
      scoredEnemy ||= value
      Maff.fromBoolean(value)
    }
    var ecoScoreUs    = 0
    var ecoScoreFoe   = 0
    ecoScoreUs  +=  4 * Maff.fromBoolean(PvT13Nexus())
    ecoScoreUs  +=  2 * Maff.fromBoolean(PvTZealotExpand())
    ecoScoreUs  +=  1 * Maff.fromBoolean(PvTRangeless())
    ecoScoreUs  +=  0 * Maff.fromBoolean(PvT28Nexus())
    ecoScoreUs  += -1 * Maff.fromBoolean(PvTZZCoreZ())
    ecoScoreUs  += -1 * Maff.fromBoolean(PvTDT())
    ecoScoreUs  += -2 * Maff.fromBoolean(PvT1BaseReaver())
    ecoScoreUs  += -2 * Maff.fromBoolean(PvT29Arbiter())
    ecoScoreUs  += -3 * Maff.fromBoolean(PvT1015())
    ecoScoreUs  += -4 * Maff.fromBoolean(PvT4Gate())
    ecoScoreUs  += -5 * Maff.fromBoolean(PvT910())
    ecoScoreFoe +=  4 * scoreEnemy(With.fingerprints.fourteenCC())
    ecoScoreFoe +=  2 * scoreEnemy(With.fingerprints.oneRaxFE())
    ecoScoreFoe +=  0 * scoreEnemy(With.fingerprints.oneFac() && ! With.fingerprints.fd()) // Includes siege expand
    ecoScoreFoe += -1 * scoreEnemy(With.fingerprints.fd())
    ecoScoreFoe += -2 * scoreEnemy(With.fingerprints.twoFac())
    ecoScoreFoe += -2 * scoreEnemy(With.fingerprints.twoRax1113())
    ecoScoreFoe += -3 * scoreEnemy(With.fingerprints.threeFac())
    ecoScoreFoe += -4 * scoreEnemy(With.fingerprints.twoRaxAcad())
    ecoScoreFoe += -6 * scoreEnemy(With.fingerprints.bbs())
    val ecoEdge       = ?(scoredEnemy, ecoScoreUs - ecoScoreFoe, 0)
    val terran23Fac   = enemyStrategy(With.fingerprints.twoFac, With.fingerprints.threeFac)
    val terranOneBase = terran23Fac || enemyStrategy(With.fingerprints.bbs, With.fingerprints.twoRax1113, With.fingerprints.twoRaxAcad, With.fingerprints.oneBaseBioMech)
    val turretsShown  = enemyHasShown(Terran.EngineeringBay, Terran.MissileTurret)
    val detectorShown = turretsShown || enemyHasShown(Terran.Comsat, Terran.SpellScannerSweep, Terran.SpiderMine)
    val goDT          = PvTDT() || PvT29Arbiter() || (ecoEdge <= -3 && ! detectorShown && roll("DT", 0.6))
    val goDTDrop      = ! PvT4Gate() && ! goDT && ecoEdge <= -1 && ! turretsShown && roll("DTDrop", 0.4)
    val goFastReaver  = PvT1BaseReaver() || ( ! goDT && (terranOneBase || (counterBio && have(Protoss.RoboticsFacility)) || ecoScoreFoe >= 2))
    val goFastCarrier = ecoEdge >= 2 && ecoScoreFoe >= -1 && With.scouting.enemyProximity < 0.5 && ! counterBio && (enemyBases > 1 || enemies(Terran.CommandCenter) > 1)
    val goReaver      = goFastCarrier && ! have(Protoss.TemplarArchives)
    val goCarrier     = ! TechArbiter.started && miningBases >= 3 && safeSkirmishing && safeDefending && ! With.fingerprints.bio.recently && roll("Carrier", 0.5)

    techs.clear()
    TechDarkTemplar     (goDT && ! PvT29Arbiter())
    TechDarkTemplarDrop (goDTDrop)
    TechArbiter         (PvT29Arbiter())
    TechReavers         (goFastReaver)
    TechObservers       (terran23Fac)
    TechStorm           (unitsEver(Protoss.Carrier) >= 6 && gasPumps >= 4)
    TechCarrier         (goFastCarrier)
    TechObservers       ()
    TechCarrier         (goCarrier)
    TechReavers         (goReaver)
    TechStorm           (TechCarrier.started && ! TechReavers.started)
    TechUpgrades        (TechCarrier.started || miningBases >= 3)
    TechStorm           (safeSkirmishing && safeDefending && gasPumps >= 3 + Maff.fromBoolean(TechReavers.started))
    TechArbiter         ( ! TechCarrier.started)
    TechUpgrades        ()
    TechStorm           ()
    TechCarrier         (TechCarrier.started)

    val techsComplete   = techs.count(_.profited)
    val gatewayEquivs   = ?(TechCarrier.queued, miningBases, 0) + ?(TechReavers.queued, 2, 0)
    val gatewaysMax     = (3.0 * miningBases).toInt - gatewayEquivs
    var gatewaysMin     = (1.0 * miningBases).toInt - gatewayEquivs
    gatewaysMin         = Math.max(gatewaysMin, 1.5 * enemies(Terran.Factory) + Math.max(0, enemies(Terran.Barracks) - 1)).toInt
    gatewaysMin         = Math.max(gatewaysMin, ?(terranOneBase, 3, 0))
    gatewaysMin         = Math.min(gatewaysMin, gatewaysMax)
    val armySizeEnemy   = With.units.enemy.filter(IsWarrior).map(u => ?(Terran.Vulture(u), 1.5, ?(IsTank(u), 2.5, u.unitClass.supplyRequired / 4.0))).sum
    val armySizeUs      = With.units.ours.filterNot(IsWorker).map(_.unitClass.supplyRequired / 4.0).sum
    var armySizeMinimum = 1.2 * armySizeEnemy
    armySizeMinimum     = Math.max(armySizeMinimum, 12 + 6 * techsComplete)
    armySizeMinimum     = Math.max(armySizeMinimum, 6 * Math.max(1, miningBases - 1))
    armySizeMinimum     = Math.max(armySizeMinimum, ?(With.fingerprints.oneFac(),       4, 0))
    armySizeMinimum     = Math.max(armySizeMinimum, ?(With.fingerprints.twoRaxAcad(),   8, 0))
    armySizeMinimum     = Math.max(armySizeMinimum, ?(With.fingerprints.twoFac(),       10, 0))
    armySizeMinimum     = Math.max(armySizeMinimum, ?(With.fingerprints.threeFac(),     12, 0))
    armySizeMinimum     = Math.min(armySizeMinimum, 200 - workerGoal)
    var armySizeLow     = armySizeUs < armySizeMinimum || confidenceDefending01 < With.scouting.enemyProximity
    armySizeLow       &&= unitsComplete(Protoss.DarkTemplar) == 0 || enemyHasShown(Terran.SpellScannerSweep, Terran.SpiderMine, Terran.ScienceVessel)
    val zealotAggro     = frame < Minutes(5)() && unitsComplete(Protoss.Zealot) > 0 && ! (With.fingerprints.eightRax() && With.fingerprints.oneFac())
    val pushMarines     = barracksCheese && ! With.strategy.isRamped
    val mineContain     = enemyHasShown(Terran.SpiderMine) && unitsComplete(Protoss.Observer) == 0
    val vultureRush     = frame < Minutes(8)() && enemyStrategy(With.fingerprints.twoFacVultures, With.fingerprints.threeFacVultures) && (armySizeUs < 12 || unitsComplete(Protoss.Observer) == 0)
    val consolidatingFE = frame < Minutes(7)() && PvT13Nexus() && ! With.fingerprints.fourteenCC()
    val nascentCarriers = TechCarrier.started && unitsEver(IsAll(Protoss.Carrier, IsComplete)) < 4
    val encroaching     = With.scouting.enemyProximity > 0.65
    var shouldAttack    = unitsComplete(IsWarrior) >= 7
    shouldAttack  ||= ! barracksCheese
    shouldAttack  &&= safeSkirmishing
    shouldAttack  &&= ! mineContain
    shouldAttack  &&= ! vultureRush
    shouldAttack  &&= ! consolidatingFE
    shouldAttack  ||= zealotAggro
    shouldAttack  ||= enemyHasShown(Terran.SiegeTankUnsieged, Terran.SiegeTankSieged)
    shouldAttack  ||= bases > 2
    shouldAttack  ||= enemyMiningBases > miningBases
    shouldAttack  ||= frame > Minutes(10)()
    shouldAttack  ||= pushMarines

    status(f"Eco$ecoScoreUs,$ecoScoreFoe=$ecoEdge")
    status(f"${gatewaysMin}-${gatewaysMax}gate")
    status(techs.mkString("-").replaceAll("Tech", "") + f":${techsComplete}/${techs.length}")
    if (armySizeLow) status("ArmyLow")
    if (zealotAggro) status("ZealotAggro")
    if (pushMarines) status("PushMarines")
    if (mineContain) status("MineContain")
    if (vultureRush) status("VultureRush")
    if (consolidatingFE) status("ConsolidatingFE")
    if (nascentCarriers) status("NascentCarriers")
    if (encroaching) status("Encroaching")

    if (shouldAttack) attack()
    harass()
    gasLimitCeiling(Math.max(1, miningBases) * 300)
    With.blackboard.monitorBases.set(unitsComplete(Protoss.Observer) > 1 || ! enemyHasShown(Terran.SpiderMine) || ! shouldAttack)

    val army = new DoQueue(doArmyNormalPriority)
    val techOnce = new DoQueue(() => doNextTech(1))
    val techTwice = new DoQueue(() => doNextTech(2))

    ////////////////
    // Transition //
    ////////////////

    get(Protoss.Pylon, Protoss.Gateway, Protoss.Assimilator, Protoss.CyberneticsCore)
    doArmyHighPriority()

    if (armySizeLow) {
      if (miningBases > 1) {
        techOnce()
      }
      army()
    }
    if ( ! enemyStrategy(With.fingerprints.fourteenCC, With.fingerprints.oneRaxFE) || With.fingerprints.bunkerRush()) {
      get(Protoss.DragoonRange)
    }
    get(gatewaysMin, Protoss.Gateway)

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
    techOnce()
    army()
    get(?(terranOneBase, gatewaysMax, gatewaysMin), Protoss.Gateway)
    // Crummy gas formula; in general we're counting on MacroSim to bump up the later invocation of requireGas if we really need it
    requireGas(units(Protoss.Gateway, Protoss.RoboticsSupportBay, Protoss.Stargate, Protoss.CitadelOfAdun) / 4)
    approachMiningBases(3)
    if ( ! armySizeLow) {
      techTwice()
    }
    get(gatewaysMax, Protoss.Gateway)
    buildCannonsAtExpansions(2)
    if (isMiningBase(With.geography.ourNatural)) {
      buildCannonsAtNatural(2)
    }
    techTwice()
    approachMiningBases(4)
    requireGas()
    techs.foreach(_.perform())
    approachMiningBases(5)
    get(6 * miningBases, Protoss.Gateway)
  }

  def requireGas(quantity: Int = Int.MaxValue): Unit = {
    if (With.self.gas < 500) buildGasPumps(quantity)
  }

  def doArmyHighPriority(): Unit = {
    if (have(Protoss.TemplarArchives)
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
    pumpRatio(Protoss.Observer, ?(enemyHasShown(Terran.SpiderMine), 2, 3), 4, Seq(Friendly(IsWarrior, 1.0 / 12.0)))
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
    if (have(Protoss.HighTemplar)) {
      pumpRatio(Protoss.Shuttle, 1, 3, Seq(Friendly(Protoss.Reaver, 0.5), Friendly(Protoss.HighTemplar, 1.0 / 3.0)))
    }
    if ( ! upgradeStarted(Protoss.ZealotSpeed)) {
      pump(Protoss.Dragoon, 24)
    }
    pump(Protoss.Zealot)
  }
}

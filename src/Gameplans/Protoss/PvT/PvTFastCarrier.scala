package Gameplans.Protoss.PvT

import Lifecycle.With
import Macro.Actions.{Enemy, Flat, Friendly}
import Performance.Cache
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.{PvT13Nexus, PvTFastCarrier}
import Utilities.Time.Minutes
import Utilities.UnitFilters.{IsWarrior, IsWorker}
import Utilities.{?, DoQueue}

class PvTFastCarrier extends PvTOpeners {

  override def activated: Boolean = PvTFastCarrier()

  override def doWorkers(): Unit = pumpWorkers(oversaturate = true, maximumTotal = 70)

  private val armySupply200 = new Cache(() => With.units.ours.filter(u => IsWarrior(u) && u.complete).map(_.unitClass.supplyRequired).sum / 2)

  override def executeMain(): Unit = {
    val armyNormalPriority = new DoQueue(doArmyNormalPriority)
    val gasIfNeeded = new DoQueue(() => pumpGasPumps(miningBases - gas / 400))

    get(Protoss.Pylon, Protoss.Gateway, Protoss.Assimilator, Protoss.CyberneticsCore)

    doArmyHighPriority()
    if (armySupply200() < 24) {
      armyNormalPriority()
    }
    get(Protoss.DragoonRange)
    if (With.fingerprints.twoFac()) {
      get(2, Protoss.Gateway)
    }

    //////////////
    // Two Base //
    //////////////

    maintainMiningBases(3)
    requireMiningBases(2)
    requireMiningBases(enemyMiningBases)
    requireMiningBases(supplyTotal200 / 40)

    if (armySupply200() < 32) {
      armyNormalPriority()
    }
    if (counterBio) {
      get(Protoss.RoboticsSupportBay)
      once(2, Protoss.Reaver)
      requireMiningBases(3)
    }

    get(Protoss.Stargate)
    get(Protoss.FleetBeacon)
    get(2, Protoss.Stargate)
    once(4, Protoss.Carrier)
    get(?(counterBio, Protoss.AirArmor,   Protoss.AirDamage))
    get(Protoss.CarrierCapacity)
    get(?(counterBio, Protoss.AirDamage,  Protoss.AirArmor))
    get(2, Protoss.Assimilator)
    get(2, Protoss.Gateway)
    requireMiningBases(3)

    ////////////////
    // Three Base //
    ////////////////

    once(8, Protoss.Carrier)
    buildCannonsAtExpansions(3, PlaceLabels.DefendHall)
    buildCannonsAtNatural(1, PlaceLabels.DefendHall)
    upgradeContinuously(Protoss.GroundDamage)
    if ( ! upgradeComplete(Protoss.ZealotSpeed) || ! have(Protoss.TemplarArchives)) {
      get(Protoss.CitadelOfAdun)
    }
    gasIfNeeded()
    get(Protoss.ZealotSpeed)
    get(Protoss.RoboticsFacility)
    get(Protoss.Observatory)
    once(2, Protoss.Observer)
    get(Protoss.TemplarArchives)
    get(Protoss.ObserverSpeed)
    get(4, Protoss.Gateway)
    requireMiningBases(4)

    ////////////////
    // Four Base //
    ////////////////

    get(2, Protoss.Forge)
    upgradeContinuously(?(counterBio, Protoss.AirArmor, Protoss.AirDamage)) && upgradeContinuously(?(counterBio, Protoss.AirDamage, Protoss.AirArmor))
    upgradeContinuously(Protoss.Shields)
    get(2, Protoss.Gateway)

    if (With.fingerprints.twoFac()) {
      get(3, Protoss.Gateway)
      once(Protoss.Shuttle)
      get(Protoss.RoboticsSupportBay)
    }
    if ( ! safeDefending) {
      get(Protoss.RoboticsSupportBay)
      get(3, Protoss.Gateway)
    }
    if (armySupply200() < 48) {
      armyNormalPriority()
    }

    requireMiningBases(3)
    get(Protoss.RoboticsSupportBay)
    once(Protoss.Shuttle)
    get(Protoss.ShuttleSpeed)
    get(2, Protoss.RoboticsFacility)
    once(4, Protoss.Reaver)
    once(2, Protoss.Shuttle)
    gasIfNeeded()

    armyNormalPriority()

    get(Protoss.Forge)
    get(Protoss.GroundDamage)
    if ( ! upgradeComplete(Protoss.ZealotSpeed) || ! have(Protoss.TemplarArchives)) {
      get(Protoss.CitadelOfAdun)
    }
    get(Protoss.ZealotSpeed)
    get(Math.max(3, (1.5 * enemies(Terran.Factory)).toInt), Protoss.Gateway)

    get(Protoss.ObserverSpeed)
    get(Protoss.TemplarArchives)
    get(Protoss.PsionicStorm)
    get(Protoss.GroundArmor)
    get(Protoss.HighTemplarEnergy)
    get(6, Protoss.Gateway)
    requireMiningBases(4)

    get(3, Protoss.Stargate)
    get(8, Protoss.Gateway)
    get(2, Protoss.Forge)
    upgradeContinuously(Protoss.GroundDamage) && upgradeContinuously(Protoss.Shields)
    upgradeContinuously(Protoss.GroundArmor)
    get(Protoss.ObserverVisionRange)
    requireMiningBases(5)

    ///////////////
    // Five Base //
    ///////////////

    upgradeContinuously(Protoss.GroundArmor)
    pumpGasPumps()
    (1 to 3).foreach(count =>
      With.geography.ourMiningBases
        .sortBy(-_.tiles.size)
        .foreach(base => {
          get(    count, Protoss.Pylon,   new PlacementQuery(Protoss.Pylon).requireBase(base))
          get(3 * count, Protoss.Gateway, new PlacementQuery(Protoss.Gateway).requireBase(base))
        }))

    ///////////////
    // Attacking //
    ///////////////

    val zealotAggro     = frame < Minutes(5)() && unitsComplete(Protoss.Zealot) > 0 && ! (With.fingerprints.eightRax() && With.fingerprints.oneFac())
    val pushMarines     = barracksCheese && ! With.strategy.isRamped
    val mineContain     = enemyHasShown(Terran.SpiderMine) && unitsComplete(Protoss.Observer) == 0 && unitsComplete(Protoss.Interceptor) < 24
    val armySizeUs      = With.units.ours.filterNot(IsWorker).map(_.unitClass.supplyRequired / 4.0).sum
    val vultureRush     = frame < Minutes(8)() && enemyStrategy(With.fingerprints.twoFacVultures, With.fingerprints.threeFacVultures) && (armySizeUs < 12 || unitsComplete(Protoss.Observer) == 0)
    val consolidatingFE = frame < Minutes(7)() && PvT13Nexus() && ! With.fingerprints.fourteenCC()
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

    if (zealotAggro)      status("ZealotAggro")
    if (pushMarines)      status("PushMarines")
    if (mineContain)      status("MineContain")
    if (vultureRush)      status("VultureRush")
    if (consolidatingFE)  status("ConsolidatingFE")

    if (shouldAttack) {
      attack()
    }
    gasLimitCeiling(Math.max(1, miningBases) * 300)
    With.blackboard.monitorBases.set(unitsComplete(Protoss.Observer) > 1 || ! enemyHasShown(Terran.SpiderMine) || ! shouldAttack)
  }

  def counterBio: Boolean = With.fingerprints.bio() && enemies(Terran.Marine, Terran.Firebat, Terran.Medic) >= enemies(Terran.Vulture) * 1.5

  def doArmyHighPriority(): Unit = {
    pump(Protoss.Observer, ?(enemyHasShown(Terran.SpiderMine), 2, 1))
    pumpRatio(Protoss.Dragoon, 6, 16, Seq(Flat(6), Enemy(Terran.Vulture, .75), Enemy(Terran.Wraith, 1.0))) // Don't get caught with pants totally down against harassment
    if (have(Protoss.RoboticsSupportBay)) {
      pumpShuttleAndReavers(?(counterBio, 6, 4), shuttleFirst = ! counterBio)
    }
    pump(Protoss.Carrier)
  }

  def doArmyNormalPriority(): Unit = {
    pumpRatio(Protoss.Dragoon, ?(counterBio, 6, 12), 24, Seq(Enemy(Terran.Vulture, .75), Enemy(Terran.Wraith, 1.0), Enemy(Terran.Battlecruiser, 4.0), Friendly(Protoss.Zealot, 0.5)))
    pumpRatio(Protoss.Observer, ?(enemyHasShown(Terran.SpiderMine), 2, 3), 4, Seq(Friendly(IsWarrior, 1.0 / 12.0)))
    if (have(Protoss.Stargate) && (enemyHasTech(Terran.WraithCloak) || enemies(Terran.Wraith) > 2)) {
      pump(Protoss.Observer, 8)
    }
    once(Protoss.DarkTemplar)
    pumpRatio(Protoss.HighTemplar, 2, 4, Seq(Friendly(IsWarrior, 0.2)))
    if (upgradeStarted(Protoss.ZealotSpeed)) {
      pump(Protoss.Zealot, units(Protoss.Dragoon))
    }
    pump(Protoss.Dragoon)
    pump(Protoss.Zealot)
  }
}

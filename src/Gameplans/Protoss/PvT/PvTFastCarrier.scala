package Gameplans.Protoss.PvT

import Lifecycle.With
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.{PvT13Nexus, PvTFastCarrier}
import Utilities.Time.Minutes
import Utilities.UnitFilters.{IsWarrior, IsWorker}
import Utilities.{?, DoQueue}

class PvTFastCarrier extends PvTOpeners {

  override def activated: Boolean = PvTFastCarrier()

  override def executeMain(): Unit = {
    val armyNormalPriority = new DoQueue(PvTArmy.normalPriority)

    get(Protoss.Pylon, Protoss.Gateway, Protoss.Assimilator, Protoss.CyberneticsCore)

    PvTArmy.highPriority()
    if (armySupply200 < 24) {
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

    if (armySupply200 < 32) {
      armyNormalPriority()
    }
    if (PvTArmy.counterBio) {
      get(Protoss.RoboticsSupportBay)
      once(2, Protoss.Reaver)
      requireMiningBases(3)
    }

    get(2, Protoss.Stargate)
    get(Protoss.FleetBeacon)
    once(4, Protoss.Carrier)
    get(?(PvTArmy.counterBio, Protoss.AirArmor, Protoss.AirDamage))
    get(Protoss.CarrierCapacity)
    get(2, Protoss.Assimilator)
    get(?(PvTArmy.counterBio, Protoss.AirDamage, Protoss.AirArmor))
    get(2, Protoss.Gateway)
    requireMiningBases(3)

    ////////////////
    // Three Base //
    ////////////////

    pump(Protoss.Carrier, 8)
    buildCannonsAtExpansions(3, PlaceLabels.DefendHall)
    buildCannonsAtNatural(1, PlaceLabels.DefendHall)
    pumpGasPumps(miningBases - gas / 400)
    get(Protoss.RoboticsFacility)
    once(Protoss.Shuttle)
    get(Protoss.RoboticsSupportBay)
    once(2, Protoss.Reaver)
    get(Protoss.ShuttleSpeed)
    get(Protoss.Shields)
    get(3, Protoss.Gateway)
    get(Protoss.Observatory)
    once(Protoss.Observer)
    requireMiningBases(4)

    ///////////////
    // Four Base //
    ///////////////

    armyNormalPriority()
    get(2, Protoss.RoboticsFacility)
    if ( ! upgradeComplete(Protoss.ZealotSpeed) || ! have(Protoss.TemplarArchives)) {
      get(Protoss.CitadelOfAdun)
    }
    get(Protoss.TemplarArchives)
    upgradeContinuously(?(PvTArmy.counterBio, Protoss.AirArmor, Protoss.AirDamage)) && upgradeContinuously(?(PvTArmy.counterBio, Protoss.AirDamage, Protoss.AirArmor))
    upgradeContinuously(Protoss.Shields)
    get(Protoss.ZealotSpeed)
    get(Protoss.ScarabDamage)
    get(Protoss.PsionicStorm)
    requireMiningBases(5)

    ///////////////
    // Five Base //
    ///////////////

    get(3, Protoss.Stargate)
    get(4, Protoss.Gateway)
    pumpGasPumps()
    get(2, Protoss.CyberneticsCore)
    upgradeContinuously(Protoss.AirDamage)
    upgradeContinuously(Protoss.AirArmor)
    get(2, Protoss.Forge)
    upgradeContinuously(Protoss.GroundDamage)
    upgradeContinuously(Protoss.GroundArmor)
    get(Protoss.HighTemplarEnergy)
    get(Protoss.ObserverSpeed)
    get(Protoss.ObserverVisionRange)

    pump(Protoss.Zealot)
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
    val nascentCarriers = existsEver(Protoss.FleetBeacon) && unitsEver(Protoss.Carrier) < 4 && unitsEver(Protoss.Interceptor) < 24
    var shouldAttack    = unitsComplete(IsWarrior) >= 7
    shouldAttack  ||= ! barracksCheese
    shouldAttack  &&=   safeSkirmishing
    shouldAttack  &&= ! mineContain
    shouldAttack  &&= ! vultureRush
    shouldAttack  &&= ! consolidatingFE
    shouldAttack  &&= ! nascentCarriers
    shouldAttack  ||= zealotAggro
    shouldAttack  ||= pushMarines
    shouldAttack  ||= enemyMiningBases > miningBases
    shouldAttack  ||= unitsComplete(Protoss.Interceptor) > 24

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
}

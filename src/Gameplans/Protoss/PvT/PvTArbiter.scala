package Gameplans.Protoss.PvT

import Lifecycle.With
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.{PvT13Nexus, PvTArbiter}
import Utilities.DoQueue
import Utilities.Time.Minutes
import Utilities.UnitFilters.{IsWarrior, IsWorker}

class PvTArbiter extends PvTOpeners {

  override def activated: Boolean = PvTArbiter()

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
    requireMiningBases(armySupply200 / 30)
    get(2, Protoss.Gateway)
    get(Protoss.CitadelOfAdun)
    pumpGasPumps()
    get(Protoss.TemplarArchives)
    get(Protoss.Stargate)
    once(2, Protoss.DarkTemplar)
    get(Protoss.ArbiterTribunal)
    once(2, Protoss.Arbiter)
    get(Protoss.ZealotSpeed)
    get(Protoss.Stasis)
    if (armySupply200 < 40) {
      armyNormalPriority()
    }
    get(4, Protoss.Gateway)
    requireMiningBases(3)

    ////////////////
    // Three Base //
    ////////////////

    get(Protoss.RoboticsFacility)
    once(Protoss.Shuttle)
    get(Protoss.Observatory)
    once(Protoss.Observer)
    get(2, Protoss.Forge)
    upgradeContinuously(Protoss.GroundDamage) && upgradeContinuously(Protoss.Shields)
    upgradeContinuously(Protoss.GroundArmor)
    get(Protoss.RoboticsSupportBay)
    once(2, Protoss.Reaver)
    get(Protoss.ShuttleSpeed)
    get(Protoss.PsionicStorm)
    get(Protoss.HighTemplarEnergy)
    get(Protoss.ObserverSpeed)
    get(Protoss.ObserverVisionRange)
    get(Protoss.ArbiterEnergy)
    get(2, Protoss.RoboticsFacility)
    get(6, Protoss.Gateway)
    requireMiningBases(4)

    ///////////////
    // Four Base //
    ///////////////

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
    val mineContain     = enemyHasShown(Terran.SpiderMine) && unitsComplete(Protoss.Observer) == 0
    val armySizeUs      = With.units.ours.filterNot(IsWorker).map(_.unitClass.supplyRequired / 4.0).sum
    val vultureRush     = frame < Minutes(8)() && enemyStrategy(With.fingerprints.twoFacVultures, With.fingerprints.threeFacVultures) && (armySizeUs < 12 || unitsComplete(Protoss.Observer) == 0)
    val consolidatingFE = frame < Minutes(7)() && PvT13Nexus() && ! With.fingerprints.fourteenCC()
    var shouldAttack    = unitsComplete(IsWarrior) >= 7
    shouldAttack  ||= ! barracksCheese
    shouldAttack  &&=   safeSkirmishing
    shouldAttack  &&= ! mineContain
    shouldAttack  &&= ! vultureRush
    shouldAttack  &&= ! consolidatingFE
    shouldAttack  ||= zealotAggro
    shouldAttack  ||= pushMarines
    shouldAttack  ||= enemyHasShown(Terran.SiegeTankUnsieged, Terran.SiegeTankSieged)
    shouldAttack  ||= haveComplete(Protoss.Reaver) && haveComplete(Protoss.Shuttle) && safePushing
    shouldAttack  ||= bases > 2
    shouldAttack  ||= enemyMiningBases > miningBases
    shouldAttack  ||= frame > Minutes(12)()

    if (zealotAggro)      status("ZealotAggro")
    if (pushMarines)      status("PushMarines")
    if (mineContain)      status("MineContain")
    if (vultureRush)      status("VultureRush")
    if (consolidatingFE)  status("ConsolidatingFE")

    if (shouldAttack) {
      attack()
    }
    if (Protoss.PsionicStorm() && unitsComplete(Protoss.HighTemplar) > 2) {
      harass()
    }
    gasLimitCeiling(Math.max(1, miningBases) * 300)
    With.blackboard.monitorBases.set(unitsComplete(Protoss.Observer) > 1 || ! enemyHasShown(Terran.SpiderMine) || ! shouldAttack)
  }
}

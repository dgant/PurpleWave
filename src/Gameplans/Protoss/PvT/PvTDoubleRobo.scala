package Gameplans.Protoss.PvT

import Lifecycle.With
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.{PvT13Nexus, PvTDoubleRobo}
import Utilities.DoQueue
import Utilities.Time.Minutes
import Utilities.UnitFilters.{IsWarrior, IsWorker}

class PvTDoubleRobo extends PvTOpeners {

  // Don't set an activation requirement because
  // this is our PvT late game fallback build in case of strategy snafu.

  override def executeMain(): Unit = {
    PvTDoubleRobo.activate()
    val armyNormalPriority = new DoQueue(PvTArmy.normalPriority)
    val gasIfNeeded = new DoQueue(() => pumpGasPumps(miningBases - gas / 400))

    get(Protoss.Pylon, Protoss.Gateway, Protoss.Assimilator, Protoss.CyberneticsCore)

    PvTArmy.highPriority()
    if (armySupply200 < 24) {
      armyNormalPriority()
    }
    if ( PvT13Nexus() || ! enemyStrategy(With.fingerprints.fourteenCC, With.fingerprints.oneRaxFE) || With.fingerprints.bunkerRush() || With.fingerprints.workerRush()) {
      get(Protoss.DragoonRange)
    }
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
    get(Protoss.RoboticsFacility)
    get(Protoss.DragoonRange)
    if (PvTArmy.counterBio) {
      get(Protoss.RoboticsSupportBay)
      once(2, Protoss.Reaver)
      get(3, Protoss.Gateway)
    }
    get(Protoss.Observatory)
    get(2, Protoss.Gateway)
    once(Protoss.Observer)
    if (With.fingerprints.twoFac()) {
      get(3, Protoss.Gateway)
      once(Protoss.Shuttle)
      get(Protoss.RoboticsSupportBay)
    }
    if ( ! safeDefending) {
      get(Protoss.RoboticsSupportBay)
      get(3, Protoss.Gateway)
    }
    if (armySupply200 < 48) {
      armyNormalPriority()
    }

    ////////////////
    // Three Base //
    ////////////////

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
    get(Protoss.ScarabDamage)
    get(Math.max(3, (1.5 * enemies(Terran.Factory)).toInt), Protoss.Gateway)

    get(Protoss.ObserverSpeed)
    get(Protoss.TemplarArchives)
    get(Protoss.PsionicStorm)
    get(Protoss.GroundArmor)
    get(Protoss.HighTemplarEnergy)
    get(6, Protoss.Gateway)
    requireMiningBases(4)

    ////////////////
    // Four Base //
    ////////////////

    buildCannonsAtExpansions(1, PlaceLabels.DefendHall)
    get(2, Protoss.Forge)
    upgradeContinuously(Protoss.GroundDamage) && upgradeContinuously(Protoss.Shields)
    upgradeContinuously(Protoss.GroundArmor)
    get(Protoss.ObserverVisionRange)
    requireMiningBases(5)

    ///////////////
    // Five Base //
    ///////////////

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
    val mineContain     = enemyHasShown(Terran.SpiderMine) && unitsComplete(Protoss.Observer) == 0
    val armySizeUs      = With.units.ours.filterNot(IsWorker).map(_.unitClass.supplyRequired400 / 4.0).sum
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

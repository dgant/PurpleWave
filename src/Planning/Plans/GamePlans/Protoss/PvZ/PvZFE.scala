package Planning.Plans.GamePlans.Protoss.PvZ

import Lifecycle.With
import Macro.Requests.RequestUnit
import Mathematics.Maff
import Placement.Access.PlaceLabels.DefendAir
import Placement.Access.{PlaceLabels, PlacementQuery}
import Placement.Walls.Wall
import Planning.Plans.GamePlans.All.GameplanImperative
import Planning.Plans.Macro.Automatic.{Enemy, Friendly}
import Planning.Plans.Macro.Protoss.MeldArchons
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import Strategery.Python
import Strategery.Strategies.Protoss.{PvZ1BaseReactive, PvZFFE, PvZGatewayFE}
import Utilities.Time.{GameTime, Minutes}
import Utilities.UnitFilters.IsWarrior
import Utilities.{?, DoQueue}

class PvZFE extends GameplanImperative {

  override def activated: Boolean = PvZFFE() || PvZGatewayFE()

  lazy val wallOption: Option[Wall] = With.placement.wall
  private def wall = wallOption.get
  private val oneBase = new PvZ1BaseReactive

  private def wallPlacement   (unitClass: UnitClass)                : PlacementQuery  = new PlacementQuery(unitClass).preferLabelYes().preferLabelNo().preferZone(With.geography.ourFoyer.edges.flatMap(_.zones).distinct: _*).preferBase().preferTile()
  private def buildInWall     (quantity: Int, unitClass: UnitClass) : Unit            = { get(quantity, unitClass, wallPlacement(unitClass)  .requireLabelYes(PlaceLabels.Wall)) }
  private def tryBuildInWall  (quantity: Int, unitClass: UnitClass) : Unit            = { get(quantity, unitClass, wallPlacement(unitClass)  .preferLabelYes(PlaceLabels.Wall))  }

  private def naturalNexus(): Unit = {
    get(Protoss.Nexus, new PlacementQuery(Protoss.Nexus).requireBase(With.geography.ourNatural))
  }

  override def executeBuild(): Unit = {
    if (wallOption.isEmpty) {
      // We shouldn't even pick this strategy in the first place, but if we do here's an escape valve
      once(5, Protoss.Probe)
      With.strategy.swapEverything(Seq(PvZ1BaseReactive), Seq(PvZFFE, PvZGatewayFE))
      return
    }

    // FFE: Scout on pylon
    // GFE: Scout with Zealot or just on gate
    // GFE: Can attack with first Zealot unless Zerg made 6 Zerglings
    scoutOn(Protoss.Pylon)

    // Via https://www.youtube.com/watch?v=OgLBP6y_CQU
    // - GFE: Vs. 6 lings: 3 Zealot 23 Nexus
    // - GFE: Vs. cross spawn, few lings: 1 Zealot Nexus
    // - GFE: 12+ Ling: Defend at natural
    // On maps where you can completely block with forge-forge-gateway:
    // FFE: Khala: "When you fail to scout Zerg at first try do 12 forge because you can block 9 pool with sim city)" Not sure if map-specific: https://youtu.be/Zm-t_mpHWG0?t=102
    // FFE: Khala: Can send second scout if first one misses to catch 4/5 pool

    if (With.fingerprints.fourPool() && units(IsWarrior) < 9) {
      cancel(Protoss.Nexus, Protoss.Assimilator, Protoss.CyberneticsCore)
      once(Protoss.Forge)
      pump(Protoss.Probe,  3)
      pump(Protoss.Zealot, 2)
      pump(Protoss.Probe)
      pump(Protoss.Zealot)
      get(1, Protoss.Pylon,         new PlacementQuery(Protoss.Pylon)       .requireBase(With.geography.ourMain).requireLabelYes(PlaceLabels.DefendHall))
      get(3, Protoss.PhotonCannon,  new PlacementQuery(Protoss.PhotonCannon).requireBase(With.geography.ourMain).preferLabelYes(PlaceLabels.DefendHall, PlaceLabels.DefendAir))
      get(1, Protoss.Forge,         new PlacementQuery(Protoss.Forge)       .requireBase(With.geography.ourMain))
      get(2, Protoss.Gateway,       new PlacementQuery(Protoss.Gateway)     .requireBase(With.geography.ourMain))
      get(1, Protoss.Pylon,         new PlacementQuery(Protoss.Pylon)       .requireBase(With.geography.ourMain).preferTile(With.geography.home).preferLabelYes(PlaceLabels.GroundProduction))
      oneBase.executeBuild()
      oneBase.executeMain()
      return
    }

    get(8, Protoss.Probe)
    buildInWall(1, Protoss.Pylon)
    if (With.fingerprints.hatchFirst()) {
      once(12, Protoss.Probe)
      naturalNexus()
      once(13, Protoss.Probe)
      if (With.fingerprints.tenHatch() || With.fingerprints.twoHatchMain() || Python()) {
        once(15, Protoss.Probe)
        buildInWall(1, Protoss.Gateway)
        buildInWall(1, Protoss.Forge)
        once(Protoss.Zealot)
        buildInWall(2, Protoss.PhotonCannon)
      } else {
        if (units(Protoss.Gateway, Protoss.Nexus) < 3) {
          cancel(Protoss.Forge, Protoss.PhotonCannon)
        }
        once(15, Protoss.Probe)
        buildInWall(1, Protoss.Gateway)
        buildInWall(1, Protoss.Forge)
        once(Protoss.Zealot)
        once(16, Protoss.Probe)
      }
    } else if (With.fingerprints.twelvePool() || With.fingerprints.overpool()) {
      once(13, Protoss.Probe)
      naturalNexus()
      buildInWall(1, Protoss.Forge)
    } else {
      val no4PoolDelay = ?(With.fingerprints.fourPool.recently, 0, 2)
      //get(10 + no4PoolDelay, Protoss.Probe)
      once(12, Protoss.Probe)
      buildInWall(1, Protoss.Forge)
      //once(12 + no4PoolDelay, Protoss.Probe)
      once(14, Protoss.Probe)
      buildInWall(2, Protoss.PhotonCannon)
    }

    once(13, Protoss.Probe)
    if (With.frame < Minutes(8)()) {
      buildInWall(
        Maff.clamp(
          1 + Seq(
            With.fingerprints.ninePool(),
            With.tactics.scoutWithWorkers.scouts.isEmpty || ! With.scouting.enemyMainFullyScouted,
            With.fingerprints.twoHatchMain).count(_ == true),
          (3 + enemies(Zerg.Zergling)) / 3,
          6),
        Protoss.PhotonCannon)
    }
    naturalNexus()
  }

  override def executeMain(): Unit = {
    if (wallOption.isEmpty) return

    // Cannons vs. ling/hydra bust
    // Cannons vs. muta
    // +1/+1 air vs. muta
    // 2nd Stargate vs. committed muta
    // Archon, delaying storm, vs. committed muta
    // Maelstrom vs. committed muta
    // Obs vs. Lurker
    // Reaver composition vs. 3HH/mass sunken?
    // Main composition: Zealots-Weapons-Speed-Corsair-Templar/Storm-Amulet-Dragoon-Range-Observer-Speed shuttle-Reaver

    tryBuildInWall(1, Protoss.Gateway)
    get(Protoss.Pylon, new PlacementQuery(Protoss.Pylon).requireBase(With.geography.ourMain))

    ///////////////////
    // High priority //
    ///////////////////

    if (enemyMutalisksLikely) {
      pumpRatio(Protoss.Corsair, 6, 24, Seq(Enemy(Zerg.Mutalisk, 1.0)))
      val mutaliskCannons = Maff.clamp(1 + enemies(Zerg.Mutalisk) / 3, 2, 5)
      val cannonTime      = Math.min(With.scouting.expectedArrival(Zerg.Mutalisk), GameTime(7, 0)())
      val pylonTime       = cannonTime - Protoss.Pylon.buildFramesFull
      tryBuildInWall(1, Protoss.Forge)
      With.geography.ourBases.foreach(base => {
        tryBuildInWall(1, Protoss.Forge)
        get(RequestUnit(Protoss.Pylon,        1,                pylonTime,  Some(new PlacementQuery(Protoss.Pylon)       .requireBase(base).requireLabelYes(DefendAir))))
        get(RequestUnit(Protoss.PhotonCannon, mutaliskCannons,  cannonTime, Some(new PlacementQuery(Protoss.Pylon)       .requireBase(base).requireLabelYes(DefendAir))))
      })
      pumpRatio(Protoss.Dragoon, 6, 24, Seq(Enemy(Zerg.Mutalisk, 2.0), Friendly(Protoss.Corsair, -2.0)))
      get(Protoss.Gateway, Protoss.Assimilator, Protoss.CyberneticsCore)
      get(Maff.clamp(enemies(Zerg.Mutalisk) / 8, 1, 2), Protoss.Stargate)
      get(Protoss.DragoonRange)
      get(Protoss.AirDamage)
      buildGasPumps()
    }

    if (enemyHydralisksLikely || enemyLurkersLikely) {
      if (With.frame < Minutes(8)()) {
        buildInWall(Maff.clamp(enemies(Zerg.Hydralisk, Zerg.Lurker, Zerg.LurkerEgg), 1, 6 - unitsComplete(Protoss.Gateway)), Protoss.PhotonCannon)
      }
    }
    if (enemyLurkersLikely || enemyHasTech(Zerg.Burrow)) {
      get(Protoss.Gateway, Protoss.Assimilator, Protoss.CyberneticsCore)
      get(Protoss.DragoonRange)
      once(Protoss.RoboticsFacility, Protoss.Observatory, Protoss.Observer)
      buildGasPumps()
    }

    maintainMiningBases(3)
    buildCannonsAtExpansions(4)
    if (unitsComplete(IsWarrior) >= 16
      && unitsComplete(Protoss.Gateway) >= 6
      && safePushing
      && upgradeComplete(Protoss.DragoonRange)
      && upgradeComplete(Protoss.ZealotSpeed)
      && haveComplete(Protoss.Observer)) {
      requireMiningBases(3)
    }
    val doTech = new DoQueue(getTech)
    if (unitsComplete(IsWarrior) >= ?(safeDefending, 20, 30)) {
      doTech()
    }

    //////////
    // Army //
    //////////

    new MeldArchons()()
    if (upgradeStarted(Protoss.DragoonRange)) {
      pumpRatio(Protoss.Dragoon, 1, 24, Seq(Friendly(Protoss.Zealot, 0.5), Enemy(Zerg.Lurker, 1.0), Enemy(Zerg.Mutalisk, 2.0), Friendly(Protoss.Corsair, -2.0)))
    }
    pumpRatio(Protoss.Observer, 1, 3, Seq(Enemy(Zerg.Lurker, 0.5)))
    pumpShuttleAndReavers()
    pump(Protoss.DarkTemplar, 2)
    pump(Protoss.HighTemplar)
    pump(Protoss.Zealot)

    once(Protoss.Gateway, Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.Dragoon)
    get(5, Protoss.Gateway)
    doTech()
    get(15, Protoss.Gateway)

    if (bases > 2) {
      attack()
    }
    if (safePushing && ! enemyHasUpgrade(Zerg.ZerglingSpeed) && ! enemyHasUpgrade(Zerg.HydraliskSpeed) && enemiesComplete(Zerg.SunkenColony) < 2) {
      attack()
    }
    if (safePushing && upgradeComplete(Protoss.ZealotSpeed) && upgradeComplete(Protoss.DragoonRange) && (haveComplete(Protoss.Observer) || ! enemiesHave(Zerg.Lurker))) {
      attack()
    }
  }

  def getTech(): Unit = {
    buildGasPumps()
    get(Protoss.GroundDamage)
    get(Protoss.CitadelOfAdun)
    get(Protoss.ZealotSpeed)
    get(Protoss.DragoonRange)
    get(Protoss.GroundArmor)
    once(Protoss.TemplarArchives, Protoss.RoboticsFacility, Protoss.Observatory, Protoss.RoboticsSupportBay)
    get(Protoss.ObserverSpeed)
    upgradeContinuously(Protoss.GroundDamage) && upgradeContinuously(Protoss.GroundArmor)
    get(Protoss.ShuttleSpeed)
  }
}

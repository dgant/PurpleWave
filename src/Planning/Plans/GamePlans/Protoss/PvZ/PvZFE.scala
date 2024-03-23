package Planning.Plans.GamePlans.Protoss.PvZ

import Lifecycle.With
import Mathematics.Maff
import Placement.Access.PlaceLabels.DefendAir
import Placement.Access.{PlaceLabels, PlacementQuery}
import Placement.Walls.Wall
import Planning.Plans.GamePlans.All.GameplanImperative
import Planning.Plans.Macro.Automatic.{Enemy, Friendly}
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import Strategery.Strategies.Protoss.PvZFFE
import Utilities.?
import Utilities.UnitFilters.IsWarrior

class PvZFE extends GameplanImperative {

  override def activated: Boolean = PvZFFE()

  lazy val wallOption: Option[Wall] = With.placement.wall
  private def wall = wallOption.get
  private val oneBase = new PvZ1BaseReactive

  var haveTakenNatural: Boolean = false

  private def wallPlacement   (unitClass: UnitClass)                : PlacementQuery  = new PlacementQuery(unitClass).preferLabelYes().preferLabelNo().preferZone().preferBase().preferTile()
  private def buildInWall     (quantity: Int, unitClass: UnitClass) : Unit            = { get(quantity, unitClass, wallPlacement(unitClass)  .requireLabelYes(PlaceLabels.Wall)) }
  private def tryBuildInWall  (quantity: Int, unitClass: UnitClass) : Unit            = { get(quantity, unitClass, wallPlacement(unitClass)  .preferLabelYes(PlaceLabels.Wall))  }

  private def naturalNexus(): Unit = {
    get(Protoss.Nexus, new PlacementQuery(Protoss.Nexus).requireBase(With.geography.ourNatural))
  }

  override def executeBuild(): Unit = {
    if (wallOption.isEmpty) { oneBase.executeBuild(); return; }

    get(8, Protoss.Probe)
    buildInWall(1, Protoss.Pylon)
    scoutOn(Protoss.Pylon)

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
    if (With.fingerprints.hatchFirst()) {
      once(12, Protoss.Probe)
      naturalNexus()
      once(13, Protoss.Probe)
      buildInWall(1, Protoss.Gateway)
      once(13, Protoss.Probe)
    } else if (With.fingerprints.twelvePool() || With.fingerprints.overpool()) {
      once(13, Protoss.Probe)
      naturalNexus()
      buildInWall(1, Protoss.Forge)
    } else {
      get(11, Protoss.Probe)
      buildInWall(1, Protoss.Forge)
    }

    once(13, Protoss.Probe)
    buildInWall(Math.max(?(With.fingerprints.ninePool(), 2, 1), (3 + enemies(Zerg.Zergling)) / 4), Protoss.PhotonCannon)
    naturalNexus()
  }

  override def executeMain(): Unit = {
    if (wallOption.isEmpty) { oneBase.executeMain(); return; }
    // FFE: Scout on pylon
    // GFE: Scout with Zealot or just on gate
    // GFE: Can attack with first Zealot unless Zerg made 6 Zerglings
    // Via https://www.youtube.com/watch?v=OgLBP6y_CQU
    // - GFE: Vs. 6 lings: 3 Zealot 23 Nexus
    // - GFE: Vs. cross spawn, few lings: 1 Zealot Nexus
    // - GFE: 12+ Ling: Defend at natural
    // On maps where you can completely block with forge-forge-gateway:
      // FFE: Khala: "When you fail to scout Zerg at first try do 12 forge because you can block 9 pool with sim city)" Not sure if map-specific: https://youtu.be/Zm-t_mpHWG0?t=102
      // FFE: Khala: Can send second scout if first one misses to catch 4/5 pool

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

    if (enemyMutalisksLikely) {
      pumpRatio(Protoss.Corsair, 6, 12, Seq(Enemy(Zerg.Mutalisk, 1.0)))
      upgradeContinuously(Protoss.AirDamage)
      upgradeContinuously(Protoss.AirArmor)
      val mutaliskCannons = Maff.clamp(1 + enemies(Zerg.Mutalisk) / 3, 2, 5)
      With.geography.ourBases.foreach(base => {
        get(1,                Protoss.Pylon,        new PlacementQuery(Protoss.Pylon)       .requireBase(base).requireLabelYes(DefendAir))
        get(mutaliskCannons,  Protoss.PhotonCannon, new PlacementQuery(Protoss.PhotonCannon).requireBase(base).requireLabelYes(DefendAir))
      })
      pumpRatio(Protoss.Dragoon, 6, 24, Seq(Enemy(Zerg.Mutalisk, 2.0), Friendly(Protoss.Corsair, -2.0)))
      upgradeContinuously(Protoss.DragoonRange)
      tryBuildInWall(1, Protoss.Forge)
      get(Protoss.CyberneticsCore)
    }

    if (enemyHydralisksLikely) {
      buildInWall(Math.min(enemies(Zerg.Hydralisk), 6 - unitsComplete(Protoss.Gateway)), Protoss.PhotonCannon)
    }

    if ( ! haveComplete(Protoss.CyberneticsCore, Protoss.TemplarArchives, Protoss.RoboticsFacility, Protoss.RoboticsSupportBay)) {
      pump(Protoss.Zealot)
    }
    get(Protoss.Gateway, Protoss.Assimilator, Protoss.CyberneticsCore)

    haveTakenNatural ||= With.geography.ourNatural.townHall.exists(h => h.isOurs && h.complete)
    if (haveTakenNatural) {
      oneBase.executeBuild()
      oneBase.executeMain()
    }
  }
}

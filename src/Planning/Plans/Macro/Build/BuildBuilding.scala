package Planning.Plans.Macro.Build

import Debugging.Visualization.DrawMap
import Micro.Intentions.Intention
import Planning.Composition.PositionFinders.PositionSimpleBuilding
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import Planning.Plans.Allocation.{LockCurrencyForUnit, LockUnits}
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._
import bwapi.TilePosition

class BuildBuilding(val buildingType:UnitClass) extends Plan {
  
  val buildingPlacer = new PositionSimpleBuilding(buildingType)
  val currencyLock = new LockCurrencyForUnit(buildingType)
  val builderLock = new LockUnits {
    description.set("Get a builder")
    unitCounter.set(UnitCountOne)
    unitMatcher.set(new UnitMatchType(buildingType.whatBuilds._1))
    unitPreference.set(new UnitPreferClose { positionFinder.set(buildingPlacer)})
  }
  
  private var builder:Option[FriendlyUnitInfo] = None
  private var building:Option[FriendlyUnitInfo] = None
  private var tile:Option[TilePosition] = None
    
  description.set("Build a " + buildingType)
  
  override def getChildren: Iterable[Plan] = List(currencyLock, builderLock)
  override def isComplete: Boolean = building.exists(building => building.complete)
  
  def startedBuilding:Boolean = building.isDefined
  
  override def onFrame() {
    if (isComplete) return
    
    // Building dead? Forget we had one.
    // Have a position but no building? Check for buildings.
    // Started building? Don't change position.
    // No building? Update the position.
    
    building = building.filter(_.alive)
    
    if (building.isEmpty && tile.isDefined)
      building = With.units.ours
        .filter(unit => unit.unitClass == buildingType && unit.tileTopLeft == tile.get)
        .headOption
    
    tile = if (building.isDefined) tile else buildingPlacer.find
    
    if (tile.isEmpty) return
  
    //TODO: Terran: Complete incomplete buildings
    //TODO: Protoss: Don't onFrame the builder if it's a warping Protoss building
    //TODO: Zerg: the builder becomes the building!
  
    currencyLock.isSpent = building.isDefined
    currencyLock.onFrame()
    if (currencyLock.isComplete) {
      builderLock.onFrame()
      if (building.isEmpty) {
        builderLock.units.foreach(
          unit => With.executor.intend(
            new Intention(this, unit) {
              toBuild = Some(buildingType)
              destination = tile
            }))
      }
    }
  }
  
  override def drawOverlay() {
    if (isComplete) return
    if (tile.isEmpty) return
    DrawMap.box(
      buildingType.area.startInclusive.add(tile.get).topLeftPixel,
      buildingType.area.endExclusive.add(tile.get).topLeftPixel,
      DrawMap.playerColor(With.self))
    DrawMap.label(
      "Building a " + buildingType.toString,
      tile.get.toPosition,
      drawBackground = true,
      DrawMap.playerColor(With.self))
  }
}

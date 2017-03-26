package Planning.Plans.Macro.Build

import Debugging.Visualization.Rendering.DrawMap
import Micro.Intentions.Intention
import Planning.Composition.PositionFinders.PositionSimpleBuilding
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import Planning.Plans.Allocation.{LockArea, LockCurrencyForUnit, LockUnits}
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Startup.With
import Utilities.EnrichPosition._
import bwapi.{Race, TilePosition}

class BuildBuilding(val buildingClass:UnitClass) extends Plan {
  
  val buildingPlacer = new PositionSimpleBuilding(buildingClass)
  val areaLock = new LockArea
  val currencyLock = new LockCurrencyForUnit(buildingClass)
  val builderLock = new LockUnits {
    description.set("Get a builder")
    unitCounter.set(UnitCountOne)
    unitMatcher.set(new UnitMatchType(buildingClass.whatBuilds._1))
    unitPreference.set(new UnitPreferClose { positionFinder.set(buildingPlacer)})
  }
  
  private var builder:Option[FriendlyUnitInfo] = None
  private var building:Option[FriendlyUnitInfo] = None
  private var tile:Option[TilePosition] = None
    
  description.set("Build a " + buildingClass)
  
  override def getChildren: Iterable[Plan] = List(currencyLock, areaLock, builderLock)
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
        .filter(unit => unit.unitClass == buildingClass && unit.tileTopLeft == tile.get)
        .headOption
  
    currencyLock.isSpent = building.isDefined
    currencyLock.onFrame()
    
    tile = if (building.isDefined) tile else buildingPlacer.find
    if (tile.isEmpty) return
  
    areaLock.area = buildingClass.tileArea.add(tile.get)
  
    //TODO: Terran: Complete incomplete buildings
    
    if (currencyLock.isComplete) {
      areaLock.onFrame()
      if (areaLock.isComplete) {
        
      if (building.isEmpty || buildingClass.getRace == Race.Terran) {
        builderLock.onFrame()
      }
      
      if (building.isEmpty) {
        builderLock.units.foreach(
          unit => With.executor.intend(
            new Intention(this, unit) {
              toBuild = Some(buildingClass)
              destination = tile
            }))
        }
      }
    }
  }
  
  override def drawOverlay() {
    if (isComplete) return
    if ( ! tile.isDefined) return
    if ( ! areaLock.isComplete) return
    DrawMap.box(
      buildingClass.tileArea.startInclusive.add(tile.get).topLeftPixel,
      buildingClass.tileArea.endExclusive.add(tile.get).topLeftPixel,
      DrawMap.playerColorDark(With.self))
    DrawMap.label(
      "Building a " + buildingClass.toString,
      tile.get.toPosition,
      drawBackground = true,
      DrawMap.playerColorDark(With.self))
  }
}

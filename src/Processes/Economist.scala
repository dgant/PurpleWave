package Processes

import Geometry.TileRectangle
import Startup.With
import bwapi.{Race, UnitCommandType}

import scala.collection.JavaConverters._

class Economist {
  
  var ourEstimatedTotalMinerals = 50.0
  var ourEstimatedTotalGas = 0.0
  
  def onFrame() = {
    ourEstimatedTotalMinerals += ourMineralIncomePerMinute.toDouble / (24.0 * 60)
    ourEstimatedTotalGas += ourGasIncomePerMinute.toDouble / (24.0 * 60)
  }
  
  def ourActualTotalMinerals:Integer = {
    With.game.self.gatheredMinerals
  }
  
  def ourActualTotalGas:Integer = {
    With.game.self.gatheredGas
  }
  
  def ourActiveHarvesters:Iterable[bwapi.Unit] = {
    With.map.ourHarvestingAreas.flatten(ourActiveHarvesters)
  }
  
  def ourActiveMiners:Iterable[bwapi.Unit] = {
    With.map.ourHarvestingAreas.flatten(ourActiveMiners)
  }
  
  def ourActiveDrillers:Iterable[bwapi.Unit] = {
    With.map.ourHarvestingAreas.flatten(ourActiveDrillers)
  }
  
  def ourActiveHarvesters(harvestingArea:TileRectangle):Iterable[bwapi.Unit] = {
    With.game.getUnitsInRectangle(
      harvestingArea.start.toPosition,
      harvestingArea.end.toPosition).asScala
      //This is a slightly dubious filter.
      // 1. There are several associated UnitCommandTypes, including ReturnCargo
      // 2. We could also check the current ORDER, which cycles between things like "MiningMinerals" and "MoveToMinerals"
      // This will work for now, but will likely break when we start trying to micro harvesters
      .filter(_.getLastCommand.getUnitCommandType == UnitCommandType.Gather)
  }
  
  def ourActiveMiners(miningArea:TileRectangle):Iterable[bwapi.Unit] = {
    ourActiveHarvesters
    //TODO: This fails because getOrderTarget can return invalid targets that NPE on property access
    //ourActiveHarvesters(miningArea).filter(_.getOrderTarget.getType.isMineralField)
  }
  
  def ourActiveDrillers(miningArea:TileRectangle):Iterable[bwapi.Unit] = {
    List.empty
    //ourActiveHarvesters(miningArea).filter(_.getOrderTarget.getType.isRefinery)
  }
  
  def ourMineralIncomePerMinute:Integer = {
    //Source: http://www.teamliquid.net/forum/brood-war/484849-improving-mineral-gathering-rate-in-brood-war
    //See also: http://www.teamliquid.net/forum/brood-war/89939-ideal-mining-thoughts
    //http://wiki.teamliquid.net/starcraft/Mining gives 182, 174, and 154-frame cycles for mining (for each race
    //
    // Also, according to meltYSC:
    // meLtySC : I can tell you how many workers you need to optimally mine
    // meLtySC : for Protoss, it's about 3 per mineral patch, for terran it's 2.5, for zerg it's 2.
    // Supposed explanation: Different worker movement characteristics and pathing logic
    //
    //This also fails to account for:
    // <> 9 patches per base
    // Mining out
    // Gathering optimization
    val racialMultiplier =
      if (With.game.self.getRace == Race.Protoss) 1.18f else
      if (With.game.self.getRace == Race.Zerg)    1.05f else
                                                  1.0f
    Math.round(
      With.map.ourHarvestingAreas
        .map(ourActiveMiners)
        .map(_.size)
        .map(n => Math.min(n, 23)) //9 * 2.5
        .map(n =>
          if(n <= 9 * 1.0) n * 59 else
          if(n <= 9 * 1.3) n * 54 else
          if(n <= 9 * 1.6) n * 50 else
          if(n <= 9 * 1.9) n * 47 else
          if(n <= 9 * 2.2) n * 45 else
          if(n <= 9 * 2.5) n * 43 else
                           n * 41)
        .sum * racialMultiplier)
  }
  
  def ourGasIncomePerMinute:Integer = {
    //Source: http://wiki.teamliquid.net/starcraft/Resources
    //Fastest speed: 42ms per frame, or 24fps
    With.map.ourHarvestingAreas
        .map(ourActiveDrillers)
        .map(_.size * 96)
        .sum
  }
}

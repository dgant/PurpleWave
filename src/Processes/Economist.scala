package Processes

import Geometry.TileRectangle
import Startup.With
import bwapi.Race

import scala.collection.JavaConverters._

class Economist {
  
  //Should be 50, but this offsets the effect of starting workers all being far from minerals
  var ourEstimatedTotalMinerals = 20.0
  var ourEstimatedTotalGas = 0.0
  
  var _lastFrame = 0
  def onFrame() = {
    //Don't change estimate while paused
    if (With.game.getFrameCount > _lastFrame) {
      ourEstimatedTotalMinerals += ourMineralIncomePerMinute.toDouble / (24.0 * 60)
      ourEstimatedTotalGas += ourGasIncomePerMinute.toDouble / (24.0 * 60)
    }
    _lastFrame = With.game.getFrameCount
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
      .filter(worker => worker.isGatheringMinerals || worker.isGatheringGas)
  }
  
  def ourActiveMiners(miningArea:TileRectangle):Iterable[bwapi.Unit] = {
    ourActiveHarvesters.filter(_.isGatheringMinerals)
  }
  
  def ourActiveDrillers(miningArea:TileRectangle):Iterable[bwapi.Unit] = {
    ourActiveHarvesters.filter(_.isGatheringGas)
  }
  
  def ourMineralIncomePerMinute:Integer = {
    // These values are hand tuned.
    //
    // Original source: http://www.teamliquid.net/forum/brood-war/89939-ideal-mining-thoughts
    // See also Martin Rooijackers:  http://www.teamliquid.net/forum/brood-war/484849-improving-mineral-gathering-rate-in-brood-war
    //
    // Dave Churchill via https://pdfs.semanticscholar.org/dfd9/1e739bd979c08485a75fd11c501a6ec05118.pdf
    // gives a flat .045 minerals per frame and .07 gas per frame
    //
    // http://wiki.teamliquid.net/starcraft/Mining gives 182, 174, and 154-frame cycles for mining (for each race, Terran, Zerg, protoss)
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
          if(n <= 9 * 1.0) n * 57 else
          if(n <= 9 * 1.3) n * 53 else
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
  
  def ourMiningBases:Iterable[bwapi.Unit] = {
    //TODO: Don't count macro hatches or bases without minerals
    With.map.ourBaseHalls
  }
}

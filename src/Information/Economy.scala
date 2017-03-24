package Information

import Geometry.TileRectangle
import Startup.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.Race

class Economy {
  
  //Should be 50, but this offsets the effect of starting workers all being far from minerals
  var ourEstimatedTotalMinerals = 20.0
  var ourEstimatedTotalGas = 0.0
  
  val samplingRate = 24
  var _lastFrame = 0
  def onFrame() = {
    val frameDiff = With.frame - _lastFrame
    //Don't change estimate while paused
    if (frameDiff > samplingRate) {
      
      ourEstimatedTotalMinerals += frameDiff * ourMineralIncomePerMinute.toDouble / (24.0 * 60)
      ourEstimatedTotalGas      += frameDiff * ourGasIncomePerMinute.toDouble / (24.0 * 60)
    }
    _lastFrame = With.frame
  }
  
  def ourActualTotalMinerals:Integer = {
    With.self.gatheredMinerals
  }
  
  def ourActualTotalGas:Integer = {
    With.self.gatheredGas
  }
  
  def ourActiveHarvesters:Iterable[FriendlyUnitInfo] = {
    With.geography.ourHarvestingAreas.flatten(ourActiveHarvesters)
  }
  
  def ourActiveMiners:Iterable[FriendlyUnitInfo] = {
    With.geography.ourHarvestingAreas.flatten(ourActiveMiners)
  }
  
  def ourActiveDrillers:Iterable[FriendlyUnitInfo] = {
    With.geography.ourHarvestingAreas.flatten(ourActiveDrillers)
  }
  
  def ourActiveHarvesters(harvestingArea:TileRectangle):Iterable[FriendlyUnitInfo] = {
    With.units.inRectangle(harvestingArea).flatten(_.friendly)
      //This is a slightly dubious filter.
      // 1. There are several associated UnitCommandTypes, including ReturnCargo
      // 2. We could also check the current ORDER, which cycles between things like "MiningMinerals" and "MoveToMinerals"
      // This will work for now, but will likely break when we start trying to micro harvesters
      .filter(worker => worker.gatheringMinerals || worker.gatheringGas)
  }
  
  def ourActiveMiners(miningArea:TileRectangle):Iterable[FriendlyUnitInfo] = {
    ourActiveHarvesters(miningArea).filter(_.gatheringMinerals)
  }
  
  def ourActiveDrillers(miningArea:TileRectangle):Iterable[FriendlyUnitInfo] = {
    ourActiveHarvesters(miningArea).filter(_.gatheringGas)
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
    
    With.geography.ourHarvestingAreas
      .map(ourActiveMiners)
      .map(_.size)
      .map(n => Math.min(n, 23)) //9 * 2.5
      .map(mineralIncomePerMinuteInOneBase)
      .sum
      .toInt
  }
  
  def mineralIncomePerMinute(workers:Int, bases:Int):Double = {
    if (bases < 1) return 0
    mineralIncomePerMinuteInOneBase(workers/bases) * bases
  }
  
  def mineralIncomePerMinuteInOneBase(workers:Int):Double = {
    val racialMultiplier =
      if (With.self.getRace == Race.Protoss) 1.18f else
      if (With.self.getRace == Race.Zerg)    1.05f else
                                                  1.0f
    racialMultiplier * (
      if(workers <= 9 * 1.0) workers * 57 else
      if(workers <= 9 * 1.3) workers * 53 else
      if(workers <= 9 * 1.6) workers * 50 else
      if(workers <= 9 * 1.9) workers * 46 else
      if(workers <= 9 * 2.2) workers * 44 else
      if(workers <= 9 * 2.5) workers * 43 else
                             workers * 41
      ).toInt
  }
  
  def ourGasIncomePerMinute:Integer = {
    //Original source: http://wiki.teamliquid.net/starcraft/Resources
    //Fastest speed: 42ms per frame, or 24fps
    //That would give 96 gas per driller per minute
    //In practice it seems higher, so this number is a fudge.
    //Possible cause: Not detcecting workers inside refineries
    With.geography.ourHarvestingAreas
      .map(ourActiveDrillers)
      .map(_.size)
      .map(gasIncomePerMinute)
      .sum
      .toInt
  }
  
  def gasIncomePerMinute(workers:Int, bases:Int):Double = {
    if (bases < 1) return 0
    gasIncomePerMinute(workers/bases) * bases
  }
  
  def gasIncomePerMinute(workers:Int):Double = {
    workers * 128
  }
  
  def ourMiningBases:Iterable[UnitInfo] = {
    //TODO: Don't count macro hatches or bases without minerals
    With.geography.ourTownHalls
  }
}

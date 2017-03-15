package Planning.Plans.Macro.Automatic

import Startup.With
import bwapi.UnitType

class BuildPylonsContinuously extends TrainContinuously(UnitType.Protoss_Pylon) {
  
  override protected def totalRequiredRecalculate: Int = {
  
    // Remember! BWAPI doubles all supply numbers (so that Zerglings can each cost 1)
    //
    // Back of the envelope assumptions:
    //   1. All our spending is on units
    //   2. 50 minerals/gas of units = 1 supply
    //
    // I think #1 and #2 are both overestimates (#2 definitely is),
    // but it's better to guess too high than too low.
    // Testing should show how this formula performs.
    //
    // Tracking the supply we already have or are building is tricky.
    // We want to count units in all of these categories:
    //   #1 Supply provided by completed buildings
    //   #2 Supply that will be provided by completed units once they pop
    //   #3 Supply that will be provided by incomplete units
    //   #4 Supply that will be provided by our plans once they lead to creation of a unit
    //
    // What a mess. Fortunately, there's a better way to break it down:
    //   #1 Supply that is/will be provided by units that exist
    //   #2 Supply that will be provided by plans for which we haven't started building
  
    val pylon                     = UnitType.Protoss_Pylon
    val supplyPerDepot            = With.self.getRace.getSupplyProvider.supplyProvided
    val currentSupplyOfNexus      = With.units.ours.filter(_.utype != pylon).toSeq.map(_.utype.supplyProvided).sum
    val currentSupplyUsed         = With.self.supplyUsed
    val unitSpendingRatio         = 0.75
    val costPerUnitSupply         = 25.0
    val depotCompletionFrames     = pylon.buildTime + 24 * 8 //Add a few seconds to account for builder transit time
    val incomePerMinute           = With.economy.ourMineralIncomePerMinute + With.economy.ourGasIncomePerMinute
    val incomePerFrame            = incomePerMinute / 60.0 / 24.0
    val supplyUsedPerFrame        = incomePerFrame * unitSpendingRatio / costPerUnitSupply
  
    val supplySpentBeforeDepotCompletion  = supplyUsedPerFrame * depotCompletionFrames
    val supplyUsedWhenDepotWouldFinish    = currentSupplyUsed + supplySpentBeforeDepotCompletion
    val additionalDepotsRequired          = Math.ceil((supplyUsedWhenDepotWouldFinish - currentSupplyOfNexus) / supplyPerDepot).toInt
    val pylonsRequired                    = Math.max(0, additionalDepotsRequired)
  
    return pylonsRequired
  }
}

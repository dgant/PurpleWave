package Plans.Generic.Macro

import Startup.With

class BuildSupplyContinuously extends AbstractBuildContinuously {
  
  override def _buildPlan = {
    new BuildBuilding(With.game.self.getRace.getSupplyProvider)
  }
  
  override def _additionalPlansRequired:Int = {
    
    // Remember!
    //
    // BWAPI doubles all supply numbers (so that Zerglings can each cost 1)
    
    val currentSupplyDeficit = With.game.self.supplyUsed - With.game.self.supplyTotal
    
    // Back of the envelope assumptions:
    //   1. All our spending is on units
    //   2. 50 minerals/gas of units = 1 supply
    //
    // I think #1 and #2 are both overestimates (#2 definitely is),
    // but it's better to guess too high than too low.
    // Testing should show how this formula performs.
  
    //Add a couple seconds to account for builder transit time/Overlord spawn time
    val unitSpendingRatio = 1.0
    val costPerUnitSupply = 50.0
    val depotCompletionFrames = With.game.self.getRace.getSupplyProvider.buildTime + 48
    val incomePerFrame = (With.economist.ourMineralIncomePerMinute + With.economist.ourGasIncomePerMinute) / 60.0 / 24.0
    val supplyUsedPerFrame = incomePerFrame * unitSpendingRatio / costPerUnitSupply
    val supplyProvidedPerDepot = With.game.self.getRace.getSupplyProvider.supplyProvided
    
    // Example:
    //   Supply builds in 20 seconds
    //   Spending 16(/2) supply every 30 seconds
    //
    val supplySpentBeforeDepotCompletion = supplyUsedPerFrame * depotCompletionFrames
    val supplyDeficitWhenDepotWouldFinish = currentSupplyDeficit + supplySpentBeforeDepotCompletion
    val additionalDepotsRequired = Math.ceil(supplyDeficitWhenDepotWouldFinish / supplyProvidedPerDepot).toInt
    
    val plansToAdd = Math.max(0, additionalDepotsRequired - getChildren.size)
    plansToAdd
  }
}

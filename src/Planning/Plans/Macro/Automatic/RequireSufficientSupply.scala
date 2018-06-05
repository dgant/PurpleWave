package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Plan
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitClasses.UnitClasses

class RequireSufficientSupply extends Plan {
  
  description.set("Require enough supply to avoid supply block")
  
  val supplyProvider =  UnitClasses.get(With.self.raceInitial.getSupplyProvider)
  
  override def onUpdate() {
    With.scheduler.request(this, RequestAtLeast(totalRequiredRecalculate, supplyProvider))
  }
  
  def totalRequiredRecalculate: Int = {
  
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
  
    val depotCompletionFrames     = supplyProvider.buildFrames + (if (supplyProvider.isBuilding) 24 * 4 else 0) //Add a few seconds to account for builder transit time (and finishing time)
    val supplyPerProvider         = supplyProvider.supplyProvided
    val currentSupplyOfTownHalls  = With.units.ours.filter(unit => unit.remainingCompletionFrames < depotCompletionFrames && ! unit.is(supplyProvider)).toSeq.map(_.unitClass.supplyProvided).sum
    val currentSupplyUsed         = With.self.supplyUsed
    val unitSpendingRatio         = if (With.geography.ourBases.size < 3) 0.5 else 0.75 //This is the metric that needs the most improvement
    val costPerUnitSupply         = 50.0 / 2.0 //Assume 50 minerals buys 1 supply (then divide by two because 1 supply = 2 BWAPI supply)
    val incomePerFrame            = With.economy.ourIncomePerFrameMinerals + With.economy.ourIncomePerFrameGas
    val supplyUsedPerFrame        = incomePerFrame * unitSpendingRatio / costPerUnitSupply
    val supplyBanked              = unitSpendingRatio * With.self.minerals / costPerUnitSupply
    lazy val larva                = With.units.countOurs(Zerg.Larva)
    lazy val hatcheries           = With.units.countOurs(Zerg.HatcheryLairOrHive)
    lazy val larvaPerFrame        = hatcheries / 342.0
    val supplyAddableBeforeDepotCompletion  = if (With.self.isZerg) larva + larvaPerFrame * depotCompletionFrames else 400
    val supplySpentBeforeDepotCompletion    = supplyBanked + supplyUsedPerFrame * depotCompletionFrames
    val supplyUsedWhenDepotWouldFinish      = Math.min(400, currentSupplyUsed + Math.min(supplySpentBeforeDepotCompletion, supplyAddableBeforeDepotCompletion))
    val totalProvidersRequired              = Math.ceil((supplyUsedWhenDepotWouldFinish - currentSupplyOfTownHalls) / supplyPerProvider).toInt
    val providersRequired                   = Math.max(0, totalProvidersRequired)
  
    providersRequired
  }
}

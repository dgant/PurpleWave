package Planning.Plans.GamePlans.Zerg.ZvE

import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.Latch
import Planning.Plans.Army.{Aggression, AllIn, Attack}
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Macro.Automatic.{ExtractorTrick, Gather, TrainContinuously}
import Planning.Plans.Macro.BuildOrders.{BuildOrder, FollowBuildOrder}
import Planning.Plans.Predicates.Economy.MineralsAtLeast
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.Predicates.StartPositionsAtLeast
import Planning.Plans.Scouting.Scout
import ProxyBwapi.Races.Zerg

class Zerg4Pool extends Parallel {
  
  class FivePool extends StartPositionsAtLeast(4)
  
  children.set(Vector(
    
    new Aggression(4.0),
    
    new If(
      new FivePool,
      new BuildOrder(RequestAtLeast(5, Zerg.Drone))),
    
    new BuildOrder(
      RequestAtLeast(1, Zerg.SpawningPool),
      RequestAtLeast(5, Zerg.Drone)),
  
    new If(
      new And(
        new UnitsAtLeast(1, Zerg.SpawningPool),
        new UnitsAtLeast(4, Zerg.Drone),
        new Or(
          new FivePool,
          new Latch(new MineralsAtLeast(80))),
          new Scout)),
    new ExtractorTrick,
    new TrainContinuously(Zerg.Zergling),
    
    new AllIn(
      new And(
        new UnitsAtLeast(2, Zerg.Larva),
        new UnitsAtLeast(13, Zerg.Zergling, complete = true))),
    
    new Attack,
    new FollowBuildOrder,
    new Gather
  ))
}

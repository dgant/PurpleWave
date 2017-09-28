package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound.{Parallel, Trigger}
import Planning.Plans.GamePlans.Mode
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.Protoss.Situational.PlaceGatewaysProxied
import Planning.Plans.Scouting.ScoutAt
import Planning.ProxyPlanner
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarly1GateProxy

class PvT1GateProxy extends Mode {
  
  override val activationCriteria = new Employing(PvTEarly1GateProxy)
  override val completionCriteria = new MiningBasesAtLeast(2)
  
  children.set(Vector(
    new Aggression(2.0),
    new Trigger(new UnitsAtLeast(1, Protoss.Gateway), initialBefore = new PlaceGatewaysProxied(1, () => ProxyPlanner.proxyAutomaticSneaky)),
    
    new BuildOrder(
      RequestAtLeast(8, Protoss.Probe),
      RequestAtLeast(1, Protoss.Pylon),
      RequestAtLeast(9, Protoss.Probe)),
    
    // Build proxies with only one Probe
    new Trigger(
      new UnitsAtLeast(1, Protoss.Pylon),
      initialAfter = new Parallel(
        new BuildOrder(
          RequestAtLeast(1,   Protoss.Gateway),
          RequestAtLeast(11,  Protoss.Probe),
          RequestAtLeast(1,   Protoss.Assimilator),
          RequestAtLeast(12,  Protoss.Probe),
          RequestAtLeast(1,   Protoss.Zealot),
          RequestAtLeast(13,  Protoss.Probe),
          RequestAtLeast(2,   Protoss.Pylon),
          RequestAtLeast(14,  Protoss.Probe),
          RequestAtLeast(1,   Protoss.CyberneticsCore),
          RequestAtLeast(15,  Protoss.Probe),
          RequestAtLeast(2,   Protoss.Zealot),
          RequestAtLeast(16,  Protoss.Probe)),
        new RequireSufficientSupply,
        new TrainWorkersContinuously,
        new TrainContinuously(Protoss.Dragoon),
        new Build(RequestUpgrade(Protoss.DragoonRange)),
        new Build(RequestAtLeast(2, Protoss.Gateway)),
        new RequireMiningBases(2),
        new ScoutAt(10)
      )),
    
    new FollowBuildOrder,
    new Gather,
    new Attack
  ))
}


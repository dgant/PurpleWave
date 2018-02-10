package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{And, FlipIf, If, Parallel}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding._
import Planning.Plans.Macro.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarly1BaseCarrier

class PvT1BaseCarrierextends extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvTEarly1BaseCarrier)
  override val completionCriteria = new And(new MiningBasesAtLeast(2), new UnitsAtLeast(3, Protoss.Stargate))
  override def priorityAttackPlan = new PvTIdeas.PriorityAttacks
  override def defaultAttackPlan  = new Attack
  
  override val buildOrder = Vector(
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(11,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(2,   Protoss.Pylon))
  
  override def buildPlans = Vector(
    new If(
      new UnitsAtLeast(16, Protoss.Interceptor),
      new RequireMiningBases(2)),
    new FlipIf(
      new UnitsAtLeast(4, UnitMatchWarriors),
      new PvTIdeas.TrainArmy,
      new Parallel(
        new Build(
          RequestUpgrade(Protoss.DragoonRange),
          RequestAtLeast(1, Protoss.Stargate),
          RequestAtLeast(1, Protoss.FleetBeacon),
          RequestUpgrade(Protoss.AirDamage)),
        new RequireMiningBases(2),
        new Build(RequestAtLeast(3, Protoss.Gateway)),
        new PvTIdeas.BuildSecondGasIfWeNeedIt,
        new Build(RequestAtLeast(3, Protoss.Stargate)))))
}
package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding._
import Planning.Plans.Predicates.Milestones.{EnemyHasShownCloakedThreat, MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarly1BaseCarrier

class PvT1BaseCarrier extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvTEarly1BaseCarrier)
  override val completionCriteria = new And(new MiningBasesAtLeast(2), new UnitsAtLeast(2, Protoss.Stargate))
  override def priorityAttackPlan = new PvTIdeas.PriorityAttacks
  override def defaultAttackPlan  = new If(new UnitsAtLeast(2, Protoss.Interceptor), new Attack)
  
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
    new Do(() => {
      With.blackboard.gasLimitCeiling = 500
      With.blackboard.gasTargetRatio = 0.25
    }),
    new If(
      new UnitsAtLeast(1, Protoss.Carrier),
      new RequireMiningBases(2)),
    new If(
      new UnitsAtLeast(2, Protoss.Interceptor),
      new UpgradeContinuously(Protoss.CarrierCapacity)),
    new FlipIf(
      new UnitsAtLeast(12, Protoss.Interceptor),
      new PvTIdeas.TrainArmy,
      new Parallel(
        new BuildOrder(
          RequestUpgrade(Protoss.DragoonRange),
          RequestAtLeast(1, Protoss.Stargate),
          RequestAtLeast(1, Protoss.FleetBeacon)),
        new RequireMiningBases(2),
        new If(
          new EnemyHasShownCloakedThreat,
          new Build(
            RequestAtLeast(1, Protoss.RoboticsFacility),
            RequestAtLeast(1, Protoss.Observatory),
            RequestAtLeast(2, Protoss.Observer))),
        new Build(RequestAtLeast(2, Protoss.Gateway)),
        new BuildGasPumps,
        new Build(
          RequestAtLeast(2, Protoss.Stargate),
          RequestAtLeast(6, Protoss.Gateway),
          RequestAtLeast(6, Protoss.Zealot)),
        new RequireMiningBases(3))))
}
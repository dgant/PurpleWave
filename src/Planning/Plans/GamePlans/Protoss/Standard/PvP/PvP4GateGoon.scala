package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones.{EnemiesAtLeast, MiningBasesAtLeast, UnitsAtLeast}
import Planning.Predicates.Reactive.EnemyRobo
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP4GateGoon

class PvP4GateGoon extends GameplanModeTemplate {
  
  override val activationCriteria : Predicate = new Employing(PvP4GateGoon)
  override val completionCriteria : Predicate = new Latch(new MiningBasesAtLeast(2))
  override def defaultAttackPlan  : Plan      = new PvPIdeas.AttackSafely

  override def defaultScoutPlan   : Plan = new ScoutOn(Protoss.CyberneticsCore)
  override val defaultWorkerPlan  : Plan = NoPlan()
  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactToTwoGate)
  
  override val buildOrder: Seq[BuildRequest] = ProtossBuilds.Opening_4GateDragoon
  
  override val buildPlans = Vector(
    new If(
      new Or(
        new UnitsAtLeast(15, Protoss.Dragoon),
        new UnitsAtLeast(3, Protoss.DarkTemplar),
        new EnemiesAtLeast(1, Protoss.PhotonCannon)),
      new RequireMiningBases(2)),

    new Pump(Protoss.DarkTemplar),
    new If(
      new EnemyStrategy(With.fingerprints.fourGateGoon),
      new Parallel(
        new Build(
          Get(Protoss.CitadelOfAdun),
          Get(Protoss.TemplarArchives)),
        new PvPIdeas.TrainArmy),
      new Pump(Protoss.Dragoon)),

    new If(
      new And(
        new Not(new EnemyStrategy(With.fingerprints.fourGateGoon)),
        new Not(new EnemyRobo)),
      new Build(Get(Protoss.Forge))),
    new RequireMiningBases(2)
  )
}

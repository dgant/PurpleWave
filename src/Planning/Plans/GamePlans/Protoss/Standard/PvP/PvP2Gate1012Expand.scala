package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Placement.{BuildCannonsAtNatural, ProposePlacement}
import Planning.Plans.Scouting.{ScoutForCannonRush, ScoutOn}
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyDarkTemplarLikely
import Planning.Predicates.Strategy.{Employing, EnemyStrategy, StartPositionsAtLeast}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP2Gate1012

class PvP2Gate1012Expand extends GameplanTemplate {
  
  class PylonAtNatural extends ProposePlacement {
    override lazy val blueprints: Seq[Blueprint] = Vector(
      new Blueprint(Protoss.Pylon, requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.defensive)),
      new Blueprint(Protoss.ShieldBattery, requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.defensive))
    )
  }
  
  override val activationCriteria: Predicate = new Employing(PvP2Gate1012)
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(5, Protoss.Gateway))
  override def attackPlan: Plan = new PvPIdeas.AttackSafely
  override val scoutPlan: Plan = new If(new StartPositionsAtLeast(4), new ScoutOn(Protoss.Pylon), new ScoutOn(Protoss.Gateway))
  
  override def placementPlan: Plan = new Trigger(
    new UnitsAtLeast(3, Protoss.Pylon),
    new PylonAtNatural)
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new If(
      new EnemyDarkTemplarLikely,
      new BuildCannonsAtNatural(2)),
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactToFFE,
    new ScoutForCannonRush)

  class FollowUpVsTwoGate extends Parallel(
    new RequireMiningBases(2),
    new PvPIdeas.TrainArmy,
    new Build(
      Get(3, Protoss.Gateway),
      Get(Protoss.ShieldBattery),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.DragoonRange)))

  class FollowUpVsOneGateCore extends Parallel(
    new RequireMiningBases(2),
    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore)),
    new BuildCannonsAtNatural(2),
    new If(
      new UpgradeComplete(Protoss.DragoonRange, 1, Protoss.Dragoon.buildFrames),
      new PvPIdeas.TrainArmy,
      new Pump(Protoss.Zealot)),
    new Build(Get(Protoss.DragoonRange)),
    new If(
      new EnemiesAtLeast(3, Protoss.Dragoon),
      new BuildCannonsAtNatural(5),
      new If(
        new EnemiesAtLeast(2, Protoss.Dragoon),
        new BuildCannonsAtNatural(3))))

  class FollowUpVsFE extends Parallel(
    new PvPIdeas.TrainArmy,
    new RequireMiningBases(2),
    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.DragoonRange),
      Get(3, Protoss.Gateway)))
  
  override val buildOrder: Vector[BuildRequest] = ProtossBuilds.TwoGate1012
  override def buildPlans = Vector(

    new If(
      new EnemyStrategy(With.fingerprints.nexusFirst),
      new FollowUpVsFE,
      new If(
        new And(
          new EnemyStrategy(With.fingerprints.twoGate),
          new Not(new EnemyHasShown(Protoss.Dragoon))),
        new FollowUpVsTwoGate,
        new FollowUpVsOneGateCore)),

    new Build(Get(3, Protoss.Gateway)),
    new BuildGasPumps,
    new Build(
      Get(1, Protoss.RoboticsFacility),
      Get(4, Protoss.Gateway),
      Get(1, Protoss.RoboticsSupportBay),
      Get(1, Protoss.Observatory),
      Get(6, Protoss.Gateway))
  )
}

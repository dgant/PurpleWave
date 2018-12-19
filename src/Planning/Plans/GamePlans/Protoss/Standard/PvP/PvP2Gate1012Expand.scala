package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.BuildCannonsAtNatural
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyDarkTemplarLikely
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP2Gate1012

class PvP2Gate1012Expand extends GameplanModeTemplate {
  
  class PylonAtNatural extends ProposePlacement {
    override lazy val blueprints: Seq[Blueprint] = Vector(
      new Blueprint(this, building = Some(Protoss.Pylon), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.defensive)),
      new Blueprint(this, building = Some(Protoss.ShieldBattery), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.defensive))
    )
  }
  
  override val activationCriteria: Predicate = new Employing(PvP2Gate1012)
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(5, Protoss.Gateway))
  override def defaultAttackPlan: Plan = new PvPIdeas.AttackSafely
  override val defaultScoutPlan: Plan = new ScoutOn(Protoss.Pylon)
  
  override def defaultPlacementPlan: Plan = new Trigger(
    new UnitsAtLeast(3, Protoss.Pylon),
    new PylonAtNatural)
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new If(
      new EnemyDarkTemplarLikely,
      new BuildCannonsAtNatural(2)),
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactToFFE)

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
  
  override val buildOrder = ProtossBuilds.OpeningTwoGate1012
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

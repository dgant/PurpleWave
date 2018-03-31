package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.Latch
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.BuildCannonsAtNatural
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.Reactive.EnemyDarkTemplarPossible
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import Planning.Plans.Scouting.ScoutOn
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen2Gate1012

class PvP2Gate1012 extends GameplanModeTemplate {
  
  class PylonAtNatural extends ProposePlacement {
    override lazy val blueprints: Seq[Blueprint] = Vector(
      new Blueprint(this, building = Some(Protoss.Pylon), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.wallPylon))
    )
  }
  
  override val activationCriteria : Plan  = new Employing(PvPOpen2Gate1012)
  override val completionCriteria : Plan  = new Latch(new UnitsAtLeast(2, Protoss.PhotonCannon))
  override def defaultAttackPlan  : Plan  = new PvPIdeas.AttackSafely
  override val defaultScoutPlan   : Plan  = new ScoutOn(Protoss.Pylon)
  
  override def defaultPlacementPlan: Plan = new Trigger(
    new UnitsAtLeast(3, Protoss.Pylon),
    new PylonAtNatural)
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new If(
      new EnemyDarkTemplarPossible,
      new BuildCannonsAtNatural(2)),
    new PvPIdeas.ReactToExpansion
  )
  
  override val buildOrder = ProtossBuilds.OpeningTwoGate1012Expand
  override def buildPlans = Vector(
    new RequireMiningBases(2),
    new FlipIf(
      new Or(
        new SafeAtHome,
        new EnemyHasShown(Protoss.Dragoon),
        new EnemyHasShown(Protoss.Assimilator),
        new EnemyHasShown(Protoss.CyberneticsCore)),
      new PvPIdeas.TrainArmy,
      new Build(
        RequestAtLeast(1, Protoss.Assimilator),
        RequestAtLeast(1, Protoss.CyberneticsCore))),
    new BuildCannonsAtNatural(2),
    new Build(
      RequestAtLeast(1, Protoss.Forge),
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsFacility))
  )
}

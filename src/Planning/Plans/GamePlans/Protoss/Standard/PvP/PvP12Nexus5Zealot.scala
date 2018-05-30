package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, RequestAtLeast}
import Planning.Composition.Latch
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainWorkersContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.BuildGasPumps
import Planning.Plans.Macro.Protoss.BuildCannonsAtNatural
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.Predicates.Reactive.{EnemyBasesAtLeast, EnemyBasesAtMost}
import Planning.Plans.Scouting.ScoutOn
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen12Nexus5Zealot

class PvP12Nexus5Zealot extends GameplanModeTemplate {
  
  class PylonAtNatural extends ProposePlacement {
    override lazy val blueprints: Seq[Blueprint] = {
      val output = Vector(
        new Blueprint(this, building = Some(Protoss.Pylon)),
        new Blueprint(this, building = Some(Protoss.Pylon), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.wallPylon)))
      output
    }
  }
  
  override val activationCriteria   : Plan = new Employing(PvPOpen12Nexus5Zealot)
  override val completionCriteria   : Plan = new Latch(new Or(new UnitsAtLeast(3, Protoss.PhotonCannon, complete = true), new EnemyBasesAtLeast(2)))
  override def defaultScoutPlan     : Plan = new ScoutOn(Protoss.Gateway, quantity = 2)
  override def defaultSupplyPlan    : Plan = NoPlan()
  override def defaultWorkerPlan    : Plan = NoPlan()
  override def defaultPlacementPlan : Plan = new PylonAtNatural
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToTwoGate
  )
  
  override val buildOrder: Seq[BuildRequest] = Vector(
    // http://wiki.teamliquid.net/starcraft/Fast_Expand_(vs._Protoss)
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),             // 8
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),             // 12
    RequestAtLeast(1,   Protoss.Gateway),           // 12
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Gateway),           // 14
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),             // 15
    RequestAtLeast(2,   Protoss.Zealot),
    RequestAtLeast(1,   Protoss.Forge),             // 19
    RequestAtLeast(4,   Protoss.Zealot),            // 19
    RequestAtLeast(16,  Protoss.Probe),             // 20
    RequestAtLeast(5,   Protoss.Zealot)             // 21
  )
  
  override val buildPlans = Vector(
    new If(
      new EnemyBasesAtMost(1),
      new BuildCannonsAtNatural(3)),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new FlipIf(
      new UnitsAtLeast(5, UnitMatchWarriors),
      new PvPIdeas.TrainArmy,
      new Parallel(
        new Build(
          RequestAtLeast(1, Protoss.Assimilator),
          RequestAtLeast(1, Protoss.CyberneticsCore)),
        new BuildGasPumps))
  )
}

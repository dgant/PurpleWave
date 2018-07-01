package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plans.Compound._
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.{Plan, Predicate}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.BuildCannonsAtNatural
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyDarkTemplarPossible, SafeAtHome}
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen2Gate1012

class PvP2Gate1012 extends GameplanModeTemplate {
  
  class PylonAtNatural extends ProposePlacement {
    override lazy val blueprints: Seq[Blueprint] = Vector(
      new Blueprint(this, building = Some(Protoss.Pylon), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.wallPylon)),
      new Blueprint(this, building = Some(Protoss.ShieldBattery), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.wallPylon))
    )
  }
  
  override val activationCriteria: Predicate = new Employing(PvPOpen2Gate1012)
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(5, Protoss.Gateway))
  override def defaultAttackPlan: Plan = new PvPIdeas.AttackSafely
  override val defaultScoutPlan: Plan = new ScoutOn(Protoss.Pylon)
  
  override def defaultPlacementPlan: Plan = new Trigger(
    new UnitsAtLeast(3, Protoss.Pylon),
    new PylonAtNatural)
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new If(
      new EnemyDarkTemplarPossible,
      new BuildCannonsAtNatural(2)),
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToFFE
  )
  
  override val buildOrder = ProtossBuilds.OpeningTwoGate1012Expand
  override def buildPlans = Vector(
    new RequireMiningBases(2),
    new Pump(Protoss.Observer, 1),
    new Pump(Protoss.Reaver, 3),
    new FlipIf(
      new Or(
        new SafeAtHome,
        new EnemyHasShown(Protoss.Dragoon),
        new EnemyHasShown(Protoss.Assimilator),
        new EnemyHasShown(Protoss.CyberneticsCore)),
      new If(
        new UnitsAtLeast(1, Protoss.CyberneticsCore, complete = true),
        new Pump(Protoss.Dragoon),
        new If(
          new And(
            new SafeAtHome,
            new Not(new EnemyStrategy(With.fingerprints.twoGate))),
          new Pump(Protoss.Zealot, 5),
          new Parallel(
            new Build(Get(1, Protoss.ShieldBattery)),
            new Pump(Protoss.Zealot, 7)))),
      new Build(
        Get(1, Protoss.Assimilator),
        Get(1, Protoss.CyberneticsCore))),
    new BuildCannonsAtNatural(2),
    new Build(
      Get(1, Protoss.Forge),
      Get(2, Protoss.Gateway),
      Get(Protoss.DragoonRange)),
    new BuildGasPumps,
    new Build(
      Get(3, Protoss.Gateway),
      Get(1, Protoss.RoboticsFacility),
      Get(4, Protoss.Gateway),
      Get(1, Protoss.RoboticsSupportBay),
      Get(1, Protoss.Observatory),
      Get(6, Protoss.Gateway))
  )
}

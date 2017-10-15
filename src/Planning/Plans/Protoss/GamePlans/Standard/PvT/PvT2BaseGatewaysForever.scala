package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Compound.{Do, FlipIf, If, Parallel}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvT2BaseGatewayForever

class PvT2BaseGatewaysForever extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvT2BaseGatewayForever)
  override val scoutExpansionsAt  = 60
  override val aggression         = 1.3
  override val emergencyPlans     = Vector(new PvTIdeas.Require2BaseTech)
  override val priorityAttackPlan = new PvTIdeas.PriorityAttacks
  override val defaultAttackPlan  = new PvTIdeas.AttackRespectingMines
  
  override val buildPlans = Vector(
    new If(new UnitsAtLeast(1, Protoss.HighTemplar), new Build(RequestTech(Protoss.PsionicStorm))),
    new Do(() => With.blackboard.gasBankSoftLimit = 800),
    new FlipIf(
      new UnitsAtLeast(15, UnitMatchWarriors),
      new Parallel(
        new Parallel(
          new PvTIdeas.TrainObservers,
          new TrainContinuously(Protoss.HighTemplar, 6, 2),
          new PvTIdeas.TrainZealotsOrDragoons)),
      new Build(
        RequestAtLeast(1, Protoss.Gateway),
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(2, Protoss.Gateway),
        RequestAtLeast(1, Protoss.Observatory),
        RequestAtLeast(1, Protoss.CitadelOfAdun),
        RequestAtLeast(1, Protoss.TemplarArchives),
        RequestUpgrade(Protoss.ZealotSpeed))),
    new Build(RequestAtLeast(11, Protoss.Gateway)))
}


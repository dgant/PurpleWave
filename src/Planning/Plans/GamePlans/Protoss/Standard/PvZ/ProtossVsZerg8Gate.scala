package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.BuildGasPumps
import Planning.Plans.Macro.Protoss.{BuildCannonsAtExpansions, MeldArchons}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvZMidgameGatewayAttack

class ProtossVsZerg8Gate extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvZMidgameGatewayAttack)
  override def aggression: Double = 0.75
    
  class GatewayTech extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestUpgrade(Protoss.GroundDamage),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestUpgrade(Protoss.DragoonRange),
      RequestUpgrade(Protoss.ZealotSpeed),
      RequestAtLeast(6, Protoss.Gateway),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(8, Protoss.Gateway)),
    new BuildGasPumps)
      
  class LateTech extends Parallel(
    new Build(
      RequestTech(Protoss.PsionicStorm),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory)),
    new UpgradeContinuously(Protoss.GroundArmor),
    new UpgradeContinuously(Protoss.ObserverSpeed))
  
  override def defaultArchonPlan: Plan = new If(
    new TechComplete(Protoss.PsionicStorm),
    super.defaultArchonPlan,
    new MeldArchons
  )
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvZIdeas.BuildDetectionForLurkers
  )
  
  override def buildPlans: Seq[Plan] = Vector(
    new PvZIdeas.TakeSafeNatural,
    new Build(
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.Forge)),
    new PvZIdeas.AddEarlyCannons,
    new BuildCannonsAtExpansions(5),
    new If(
      new UnitsAtLeast(40, UnitMatchWarriors),
      new Parallel(
        new PvZIdeas.TakeSafeThirdBase,
        new PvZIdeas.TakeSafeFourthBase,
        new LateTech)),
    new FlipIf(
      new SafeAtHome,
      new PvZIdeas.TrainAndUpgradeArmy,
      new GatewayTech),
    new PvZIdeas.AddGateways
  )
}

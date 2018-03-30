package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones._
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvZMidgameCorsairSpeedlot

class ProtossVsZergSpeedlotTemplar extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvZMidgameCorsairSpeedlot)
  override def aggression: Double = 0.85
  
  override def defaultAttackPlan: Plan = new Parallel(
    new Attack { attackers.get.unitMatcher.set(Protoss.Corsair) },
    new Attack { attackers.get.unitMatcher.set(Protoss.DarkTemplar) },
    super.defaultAttackPlan
  )
  
  class AddPriorityTech extends Parallel(
    new If(
      new UnitsAtLeast(1, Protoss.HighTemplar),
      new Build(RequestTech(Protoss.PsionicStorm))),
      new If(
        new UnitsAtLeast(1, Protoss.Dragoon),
        new Build(RequestUpgrade(Protoss.DragoonRange))))
  
  class AddTech extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore)),
    new IfOnMiningBases(2,
      new Parallel(
      new Build(
        RequestAtLeast(1, Protoss.Forge),
        RequestAtLeast(1, Protoss.Stargate)),
      new BuildGasPumps,
      new BuildOrder(
        RequestAtLeast(1, Protoss.CitadelOfAdun),
        RequestUpgrade(Protoss.AirDamage),
        RequestUpgrade(Protoss.GroundDamage),
        RequestUpgrade(Protoss.ZealotSpeed),
        RequestAtLeast(1, Protoss.TemplarArchives),
        RequestUpgrade(Protoss.DragoonRange),
        RequestTech(Protoss.PsionicStorm),
        RequestAtLeast(4, Protoss.Gateway),
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(1, Protoss.Observatory),
        RequestAtLeast(6, Protoss.Gateway)))),
    
    new IfOnMiningBases(3,
      new Build(
        RequestAtLeast(5, Protoss.Gateway),
        RequestAtLeast(2, Protoss.Forge),
        RequestUpgrade(Protoss.HighTemplarEnergy),
        RequestAtLeast(1, Protoss.RoboticsSupportBay),
        RequestUpgrade(Protoss.ScarabDamage))))
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvZIdeas.ReactToLurkers,
    new PvZIdeas.ReactToMutalisks
  )
  
  override def buildPlans: Seq[Plan] = Vector(
    new PvZIdeas.AddEarlyCannons,
    new PvZIdeas.TakeSafeNatural,
    new Trigger(
      new UnitsAtLeast(6, Protoss.Gateway),
      new Parallel(
        new PvZIdeas.TakeSafeThirdBase,
        new PvZIdeas.TakeSafeFourthBase)),
    new AddPriorityTech,
    new PvZIdeas.TrainAndUpgradeArmy,
    new AddTech,
    new PvZIdeas.AddGateways,
    new RequireMiningBases(3)
  )
}

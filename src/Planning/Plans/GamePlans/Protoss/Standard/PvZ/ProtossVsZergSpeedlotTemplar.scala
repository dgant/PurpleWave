package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvZMidgameCorsairSpeedlot

class ProtossVsZergSpeedlotTemplar extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvZMidgameCorsairSpeedlot)
  override def aggression: Double = 0.85
  
  override def defaultAttackPlan: Plan = new Parallel(
    new Attack(Protoss.Corsair),
    new Attack(Protoss.DarkTemplar),
    super.defaultAttackPlan
  )
  
  class AddPriorityTech extends Parallel(
    new If(
      new UnitsAtLeast(1, Protoss.HighTemplar),
      new Build(Get(Protoss.PsionicStorm))),
      new If(
        new UnitsAtLeast(1, Protoss.Dragoon),
        new Build(Get(Protoss.DragoonRange))))
  
  class AddTech extends Parallel(
    new Build(
      Get(1, Protoss.Gateway),
      Get(1, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore)),
    new IfOnMiningBases(2,
      new Parallel(
      new Build(
        Get(1, Protoss.Forge),
        Get(1, Protoss.Stargate)),
      new BuildGasPumps,
      new BuildOrder(
        Get(1, Protoss.CitadelOfAdun),
        Get(Protoss.AirDamage),
        Get(Protoss.GroundDamage),
        Get(Protoss.ZealotSpeed),
        Get(1, Protoss.TemplarArchives),
        Get(Protoss.DragoonRange),
        Get(Protoss.PsionicStorm),
        Get(4, Protoss.Gateway),
        Get(1, Protoss.RoboticsFacility),
        Get(1, Protoss.Observatory),
        Get(6, Protoss.Gateway)))),
    
    new IfOnMiningBases(3,
      new Build(
        Get(5, Protoss.Gateway),
        Get(2, Protoss.Forge),
        Get(Protoss.HighTemplarEnergy),
        Get(1, Protoss.RoboticsSupportBay),
        Get(Protoss.ScarabDamage))))
  
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

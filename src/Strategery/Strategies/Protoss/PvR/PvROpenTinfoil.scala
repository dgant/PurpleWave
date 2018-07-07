package Strategery.Strategies.Protoss.PvR

import Planning.Plan
import Planning.Plans.GamePlans.Protoss.Standard.PvR.PvRTinfoil
import Strategery.Strategies.Protoss.ProtossChoices
import Strategery.Strategies.Strategy
import bwapi.Race

object PvROpenTinfoil extends Strategy {

  override def gameplan: Option[Plan] = Some(new PvRTinfoil)

  override lazy val choices = Vector(
    ProtossChoices.pvtOpenersTransitioningFrom2Gate,
    ProtossChoices.pvpOpenersTransitioningFrom2Gate,
    ProtossChoices.pvzOpenersTransitioningFrom2Gate)

  // CIG: Allow for UAB or Opprimo only
  override def opponentsWhitelisted: Option[Iterable[String]] = Some(Iterable(
    "Dave Churchill",
    "UAlbertaBot",
    "OpprimoBot"
  ))

  override def ourRaces   : Iterable[Race]  = Vector(Race.Protoss)
  override def enemyRaces : Iterable[Race]  = Vector(Race.Unknown)
}

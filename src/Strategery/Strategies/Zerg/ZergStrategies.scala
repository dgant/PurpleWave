package Strategery.Strategies.Zerg

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvE.{NineHatchLings, NinePoolMuta, OneHatchLurker, ProxyHatch, TwoHatchHydra, Zerg4Pool}
import Planning.Plans.GamePlans.Zerg.ZvP.HydraBust
import Planning.Plans.GamePlans.Zerg.ZvT.{ThirteenPoolMuta, TwoHatchMuta}
import Planning.Plans.GamePlans.Zerg.ZvZ.Zerg5PoolProxySunkens
import Strategery.Strategies.Strategy
import bwapi.Race

class ZergStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
}

object TwoHatchMuta extends ZergStrategy {
  override def gameplan: Option[Plan] = { Some(new TwoHatchMuta) }
  override def minimumGamesVsOpponent: Int = 2
}

object FivePoolProxySunkens extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new Zerg5PoolProxySunkens)
  override def enemyRaces: Iterable[Race] = Vector(Race.Zerg)
  override def startLocationsMax: Int = 3
}
object TwoHatchHydra extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new TwoHatchHydra)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran, Race.Protoss)
}
object HydraBust extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new HydraBust)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran, Race.Protoss)
}
object NineHatchLings extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new NineHatchLings)
}
object NinePoolMuta extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new NinePoolMuta)
}
object ThirteenPoolMuta extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new ThirteenPoolMuta)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran, Race.Protoss)
}
object OneHatchLurker extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new OneHatchLurker)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran, Race.Protoss)
}

object ProxyHatchSunkens extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new ProxyHatch)
  override def enemyRaces: Iterable[Race] = Vector(Race.Unknown, Race.Terran)
}

object ProxyHatchHydras extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new ProxyHatch)
  override def enemyRaces: Iterable[Race] = Vector(Race.Unknown, Race.Terran)
}

object ProxyHatchZerglings extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new ProxyHatch)
  override def enemyRaces: Iterable[Race] = Vector(Race.Unknown, Race.Terran)
}

object Zerg4PoolAllIn extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new Zerg4Pool)
}
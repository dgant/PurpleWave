package Strategery.Strategies.Zerg

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvE.{NineHatchLings, NinePoolMuta, OneHatchLurker, ProxyHatch, TwoHatchHydra, Zerg4Pool, ZergSparkle}
import Planning.Plans.GamePlans.Zerg.ZvP.{ZvPHydraRush, ZvPNinePool, ZvPThirteenPoolMuta, ZvPTwoHatchMuta}
import Planning.Plans.GamePlans.Zerg.ZvT.ThirteenPoolMuta
import Planning.Plans.GamePlans.Zerg.ZvZ.Zerg5PoolProxySunkens
import Strategery.Strategies.Strategy
import bwapi.Race

class ZergStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
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
object NineHatchLings extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new NineHatchLings)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran, Race.Zerg)
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
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran)
}
object ProxyHatchHydras extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new ProxyHatch)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran)
}
object ProxyHatchZerglings extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new ProxyHatch)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran)
}
object ZvE4Pool extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new Zerg4Pool)
}
object ZvPTwoHatchMuta extends ZergStrategy {
  override def gameplan: Option[Plan] = { Some(new ZvPTwoHatchMuta) }
  override def enemyRaces: Iterable[Race] = Vector(Race.Protoss)
}
object ZvPNinePool extends ZergStrategy {
  override def gameplan: Option[Plan] = { Some(new ZvPNinePool) }
}
object ZvPHydraRush extends ZergStrategy {
  override def gameplan: Option[Plan] = { Some(new ZvPHydraRush) }
  override def enemyRaces: Iterable[Race] = Vector(Race.Protoss, Race.Terran)
}
object ZvPThirteenPoolMuta extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new ZvPThirteenPoolMuta)
  override def enemyRaces: Iterable[Race] = Vector(Race.Protoss)
}
object ZergSparkle extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new ZergSparkle)
  override def islandMaps: Boolean = true
  override def groundMaps: Boolean = false
}
package Strategery.Strategies.Zerg

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvE.{ZvZ10HatchLing, ZvZ9PoolMuta}
import Planning.Plans.GamePlans.Zerg.ZvZ.Zerg5PoolProxySunkens
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class ZvZStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  override def enemyRaces: Iterable[Race] = Vector(Race.Zerg)
}

object ZvZ5PoolSunkenRush extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new Zerg5PoolProxySunkens)
  override def enemyRaces: Iterable[Race] = Vector(Race.Zerg)
  override def startLocationsMax: Int = 3

  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.twelveHatch,
    With.fingerprints.twelvePool
  )
}

object ZvZ9PoolMuta extends ZvZStrategy {
  override def gameplan: Option[Plan] = Some(new ZvZ9PoolMuta)
}

object ZvZ10HatchLing extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new ZvZ10HatchLing)
}
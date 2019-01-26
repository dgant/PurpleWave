package Strategery.Strategies.Zerg

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvZ.{ZvZ5PoolSunkens, ZvZ10HatchLing, ZvZ9PoolSpeed, ZvZ9Gas9Pool}
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class ZvZStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  override def enemyRaces: Iterable[Race] = Vector(Race.Zerg)
}

object ZvZ5PoolSunkens extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new ZvZ5PoolSunkens)
  override def enemyRaces: Iterable[Race] = Vector(Race.Zerg)
  override def startLocationsMax: Int = 3

  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.twelveHatch,
    With.fingerprints.twelvePool
  )
}

object ZvZ9PoolSpeed extends ZvZStrategy {
  override def gameplan: Option[Plan] = Some(new ZvZ9PoolSpeed)
}

object ZvZ9Gas9Pool extends ZvZStrategy {
  override def gameplan: Option[Plan] = Some(new ZvZ9Gas9Pool)
}

object ZvZ10HatchLing extends ZvZStrategy {
  override def gameplan: Option[Plan] = Some(new ZvZ10HatchLing)
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.fourPool)
}
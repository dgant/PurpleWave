package Planning.UnitMatchers
import ProxyBwapi.UnitInfo.UnitInfo

object MatchIdle extends Matcher{
  override def apply(unit: UnitInfo): Boolean = {
    Seq(
      unit.remainingCompletionFrames,
      unit.friendly.map(_.remainingTrainFrames).getOrElse(0),
      unit.remainingTechFrames,
      unit.remainingUpgradeFrames)
    .max == 0
  }
}

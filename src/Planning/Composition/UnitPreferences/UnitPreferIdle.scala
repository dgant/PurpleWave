package Planning.Composition.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferIdle extends UnitPreference {
  
  override def preference(unit: FriendlyUnitInfo): Double = {
    Seq(
      unit.remainingCompletionFrames,
      unit.remainingTrainFrames,
      unit.remainingTechFrames,
      unit.remainingUpgradeFrames)
    .max
  }
}
